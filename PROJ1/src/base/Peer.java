package base;


import base.Storage.StorageManager;
import base.Tasks.*;
import base.channels.BackupChannel;
import base.channels.ChannelManager;
import base.channels.ControlChannel;
import base.channels.RestoreChannel;
import base.messages.Message;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static base.Clauses.*;
import static java.lang.Thread.sleep;

public class Peer extends UnicastRemoteObject implements PeerInterface {

    //add methods to access storage and save/load data
    private static String version;
    private static int peer_id;
    private static StorageManager storage_manager;
    private static ChannelManager channel_manager;
    private static ScheduledThreadPoolExecutor task_manager;

    Peer(String protocol_vs, int s_id, String mc_addr, String mdb_addr, String mdr_addr, int mc_port, int mdb_port, int mdr_port) throws IOException {
        version = protocol_vs;
        peer_id = s_id;
        channel_manager = new ChannelManager();
        storage_manager = StorageManager.loadStorageManager();
        channel_manager.setChannels(createControl(mc_addr, mc_port), createBackup(mdb_addr, mdb_port), createRestore(mdr_addr, mdr_port));
        task_manager = new ScheduledThreadPoolExecutor(10);
        task_manager.scheduleAtFixedRate(new SaveState(), SAVE_PERIOD, SAVE_PERIOD, TimeUnit.MILLISECONDS);
        addShutdownHook();
        askforDeleteRequests();
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

    public BackupChannel createBackup(String mdb_addr, int mdb_port) throws IOException {
        BackupChannel bck_channel = new BackupChannel(mdb_addr, mdb_port);
        new Thread(bck_channel).start();
        return bck_channel;
    }

    public ControlChannel createControl(String mc_addr, int mc_port) throws IOException {
        ControlChannel cnt_channel = new ControlChannel(mc_addr, mc_port);
        new Thread(cnt_channel).start();
        return cnt_channel;
    }

    public RestoreChannel createRestore(String mdr_addr, int mdr_port) throws IOException {
        RestoreChannel rst_channel = new RestoreChannel(mdr_addr, mdr_port);
        new Thread(rst_channel).start();
        return rst_channel;

    }

    @Override
    public int backup(String pathname, int rep_deg) throws RemoteException {

        File file = new File(pathname);
        FileInformation file_information = null;
        if(!file.exists()) {
            PeerLogger.missingFile(pathname);
            return -1;
        }

        try {
            file_information = new FileInformation(file, pathname, rep_deg);
            byte[][] chunks = file_information.splitIntoChunk();
            file_information.setNumberChunks(chunks.length);
            Peer.getStorageManager().addFileInfo(file_information);
            for (int i = 0; i < chunks.length; i++) {
                ManagePutChunk manage_putchunk = new ManagePutChunk(version, peer_id, file_information.getFileId(), i, rep_deg, chunks[i]);
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
        ManageGetChunk manage_getchunk = new ManageGetChunk(version, peer_id, file_id, i);
        getTaskManager().execute(manage_getchunk);
        Peer.getTaskManager().schedule(new HandleInitiatorChunks(i, version, file_id, peer_id, filename), MAX_DELAY_STORED, TimeUnit.MILLISECONDS);

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
        for (int i = 0; i < 5; i++) {
            ManageDeleteFile manage_delete = new ManageDeleteFile(version, peer_id, file_id);
            getTaskManager().schedule(manage_delete, (i + 1) * TIMEOUT, TimeUnit.MILLISECONDS);
        }
        getStorageManager().deleteChunks(file_id);
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
