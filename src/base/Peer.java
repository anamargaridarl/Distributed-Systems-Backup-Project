package base;


import base.Storage.StorageManager;
import base.Tasks.*;
import base.channel.MessageListener;

import javax.swing.text.BadLocationException;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static base.Clauses.*;
import static java.lang.Thread.sleep;

public class Peer extends UnicastRemoteObject implements PeerInterface {

    public static int deletechunks = 0;
    public static int restorechunks = 0;
    //add methods to access storage and save/load data
    private static String version;
    private static int peer_id;
    private static int server_port;
    private static StorageManager storage_manager;
    private static ScheduledThreadPoolExecutor task_manager;

    Peer(String protocol_vs, int s_id, int port) throws IOException {
        version = protocol_vs;
        peer_id = s_id;
        server_port = port;
        storage_manager = StorageManager.loadStorageManager();
        task_manager = new ScheduledThreadPoolExecutor(10);
        task_manager.scheduleAtFixedRate(new SaveState(), SAVE_PERIOD, SAVE_PERIOD, TimeUnit.MILLISECONDS);
        task_manager.execute(new MessageListener(server_port));
        addShutdownHook();
        askforDeleteRequests();
        Clauses.addElements();
    }

    private void askforDeleteRequests() {
        if (version.equals(ENHANCED_VERSION))
            getTaskManager().execute(new ManageDeleteOffline(version, peer_id));
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new HandleShutdown());
    }

    public static int getID() {
        return peer_id;
    }

    public static String getVersion() {
        return version;
    }

    public static ScheduledThreadPoolExecutor getTaskManager() {
        return task_manager;
    }

    public static StorageManager getStorageManager() {
        return storage_manager;
    }

    public static Socket getChunkSocket(String file_id, int num) throws NoSuchAlgorithmException, IOException {
        UUID hash = hashChunk(file_id, num);
        Integer hashKey = getHashKey(hash);
        Integer peerID = allocatePeer(hashKey);
        InetSocketAddress peerHost = chord.get(peerID);
        return createSocket(peerHost);
    }

    @Override
    public int backup(String pathname, int rep_deg) throws RemoteException {

        File file = new File(pathname);
        FileInformation file_information = null;
        if (!file.exists()) {
            PeerLogger.missingFile(pathname);
            return -1;
        }

        try {
            file_information = new FileInformation(file, pathname, rep_deg);
            byte[][] chunks = file_information.splitIntoChunk();
            file_information.setNumberChunks(chunks.length);
            Peer.getStorageManager().addFileInfo(file_information);
            for (int i = 1; i <= chunks.length; i++) {
                //TODO: use CHORD to lookup peers addresses and create sockets
                Socket taskSocket = getChunkSocket(file_information.getFileId(), i);
                ManagePutChunk manage_putchunk = new ManagePutChunk(version, peer_id, file_information.getFileId(), i, rep_deg, chunks.length, chunks[i], taskSocket);
                getTaskManager().execute(manage_putchunk);
            }
        } catch (Exception e) {
            PeerLogger.processBackupFail(pathname);
            return -1;
        }
        return 0;
    }


    @Override
    public int restore(String pathname) {

        File file = null;
        file = new File(pathname);

        if (!file.exists()) {
            PeerLogger.restoreFileMissing(pathname);
            return -1;
        }

        String file_id = null;
        try {
            file_id = FileInformation.createFileid(pathname, file.lastModified());
        } catch (NoSuchAlgorithmException e) {
            PeerLogger.createFileIDFail();
            return -1;
        }

        //find the name of the file
        String[] parts = pathname.split("/");
        String filename = parts[parts.length - 1];

        Integer id = getID();
        getStorageManager().addRestoreRequest(file_id, id);
        int i = 0;
        //TODO: use CHORD to lookup peers that hold the chunk and create socket
        Socket taskSocket = null;
        /*try {
            taskSocket = getChunkSocket(file_id, i);
            ManageGetChunk manage_getchunk = new ManageGetChunk(version, peer_id, file_id, i,taskSocket);
            getTaskManager().execute(manage_getchunk);
            //TODO: use the created socket to pass it to the task
            Peer.getTaskManager().schedule(new HandleInitiatorChunks(i, version, file_id, peer_id, filename), MAX_DELAY_STORED, TimeUnit.MILLISECONDS);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        return 0;
    }


    @Override
    public int delete(String pathname) throws RemoteException {

        File file = new File(pathname);
        String file_id = null;
        try {
            file_id = FileInformation.createFileid(pathname, file.lastModified());
        } catch (NoSuchAlgorithmException e) {
            PeerLogger.createFileIDFail();
            return -1;
        }

        //TODO: use CHORD to lookup peers that have the chunk and create sockets
        try {
            Socket taskSocket = getChunkSocket(file_id, 1);
            ManageDeleteFile first_manage_delete = new ManageDeleteFile(version, peer_id, file_id, taskSocket);
            getTaskManager().execute(first_manage_delete); //TODO: verify correctness in this new implementation
            for (int i = 2; i <= deletechunks; i++) {
                ManageDeleteFile manage_delete = new ManageDeleteFile(version, peer_id, file_id, taskSocket);
                getTaskManager().execute(manage_delete); //TODO: verify correctness in this new implementation
            }
        } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
            noSuchAlgorithmException.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return 0;
    }

    @Override
    public int reclaim(int max_space) throws RemoteException {
        if (max_space < Peer.getStorageManager().getOccupiedSpace() * KB) {
            while (Peer.getStorageManager().getOccupiedSpace() * KB > max_space) {
                ChunkInfo removed = Peer.getStorageManager().removeExpendableChunk();
                PeerLogger.removedChunk(removed.getFileId(), removed.getNumber());
                Peer.getTaskManager().execute(new ManageRemoveChunk(Peer.version, REMOVED, Peer.getID(), removed.getFileId(), removed.getNumber()));
            }
            if (max_space == 0) {
                Peer.getStorageManager().emptyChunksInfo();
            }
        }

        Peer.getStorageManager().setTotalSpace(max_space);
        PeerLogger.reclaimComplete(Peer.getStorageManager().getTotalSpace(), Peer.getStorageManager().getOccupiedSpace());
        return 0;
    }

    @Override
    public List<String> state() throws RemoteException {
        List<String> state_report = new ArrayList<>();
        int i = 1;
        state_report.add("Peer id: " + Peer.getID() + " status report:");
        state_report.add("--------------------------------------------");
        state_report.add("\tInitiated by peer file backup details:");
        for (FileInformation file : Peer.getStorageManager().getFilesInfo()) {
            state_report.add("\t\tBacked up file nº " + i + ":");
            state_report.add("\t\t\tPathname: " + file.getPathname());
            state_report.add("\t\t\tDesired replication degree: " + file.getDesiredRepDegree());
            state_report.add("\t\t\tFile chunks details: ");
            for (int j = 0; j < file.getNumberChunks(); j++) {
                state_report.add("\t\t\t\tChunk id: " + makeChunkRef(file.getFileId(), j));
                state_report.add("\t\t\t\t\tPerceived replication degree: " + Peer.getStorageManager().getChunkRepDegree(file.getFileId(), j));
            }
            state_report.add("");
            i++;
        }
        i = 1;
        state_report.add("--------------------------------------------");
        state_report.add("\tStored by peer chunk details:");
        for (ChunkInfo info : Peer.getStorageManager().getChunksInfo()) {
            state_report.add("\t\tChunk stored nº " + i + ":");
            state_report.add("\t\t\tID: " + makeChunkRef(info.getFileId(), info.getNumber()));
            state_report.add("\t\t\tSize: " + info.getSize() / 1000.0 + "KB");
            state_report.add("\t\t\t\tPerceived replication degree: " + Peer.getStorageManager().getChunkRepDegree(info.getFileId(), info.getNumber()));
            state_report.add("");
            i++;
        }
        state_report.add("--------------------------------------------");
        state_report.add("\tPeer total space: " + Peer.getStorageManager().getTotalSpace() + "KB");
        state_report.add("\t\tPeer occupied space: " + Peer.getStorageManager().getOccupiedSpace() / 1000.0 + "KB");
        return state_report;
    }
}
