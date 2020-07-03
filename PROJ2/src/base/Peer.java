package base;


import base.Storage.StorageManager;
import base.Tasks.*;
import base.channel.MessageListener;
import base.chord.ChordIdentifier;
import base.chord.ChordManager;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static base.Clauses.*;
import static java.lang.Thread.sleep;

public class Peer extends UnicastRemoteObject implements PeerInterface {

    private static int peer_id;
    private static int server_port;
    private static StorageManager storage_manager;
    private static ScheduledThreadPoolExecutor task_manager;
    private static ChordManager chordManager;

    Peer(int s_id, int port) throws IOException { //When creating the chordRing
        peer_id = s_id;
        server_port = port;
        storage_manager = StorageManager.loadStorageManager();
        task_manager = new ScheduledThreadPoolExecutor(50);
        task_manager.execute(new MessageListener(server_port));
        addShutdownHook();
        task_manager.scheduleAtFixedRate(new ManageBackupTables(), BCKUP_PERIOD, BCKUP_PERIOD, TimeUnit.MILLISECONDS);
        chordManager = new ChordManager(new InetSocketAddress("localhost", port));
    }

    Peer(int s_id, int port, String knownPeerIP, int knownPeerPort) throws IOException { //when joining a ChordRing
        peer_id = s_id;
        server_port = port;
        storage_manager = StorageManager.loadStorageManager();
        task_manager = new ScheduledThreadPoolExecutor(50);
        task_manager.execute(new MessageListener(server_port));
        addShutdownHook();
        task_manager.scheduleAtFixedRate(new ManageBackupTables(), BCKUP_PERIOD, BCKUP_PERIOD, TimeUnit.MILLISECONDS);
        chordManager = new ChordManager(new InetSocketAddress("localhost", port), new InetSocketAddress(knownPeerIP, knownPeerPort));
    }

    public static ChordManager getChordManager() {
        return chordManager;
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new HandleShutdown());
    }

    public static int getID() {
        return peer_id;
    }

    public static int getServerPort() {
        return server_port;
    }

    public static ScheduledThreadPoolExecutor getTaskManager() {
        return task_manager;
    }

    public static StorageManager getStorageManager() {
        return storage_manager;
    }

    public static SSLSocket getChunkSocket(String file_id, int num) throws NoSuchAlgorithmException, IOException {
        UUID hash = hashChunk(file_id, num);
        InetSocketAddress peerHost = getChordManager().lookup(hash);
        SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(peerHost.getAddress(), peerHost.getPort());
        socket.setEnabledCipherSuites(new String[]{
                "TLS_DHE_RSA_WITH_AES_128_CBC_SHA", "TLS_RSA_WITH_AES_256_CBC_SHA"
        });
        socket.startHandshake();
        return socket;
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

            for (int i = 0; i < chunks.length; i++) {
                SSLSocket taskSocket = getChunkSocket(file_information.getFileId(), i);
                ManagePutChunk manage_putchunk = new ManagePutChunk(peer_id, file_information.getFileId(), i, rep_deg, chunks.length, chunks[i], taskSocket);
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

        String[] parts = pathname.split("/");
        String filename = parts[parts.length - 1];

        Peer.getTaskManager().schedule(new HandleInitiatorChunks(0, file_id, peer_id, filename), MAX_DELAY_STORED, TimeUnit.MILLISECONDS);

        return 0;
    }


    @Override
    public int delete(String pathname) {

        File file = new File(pathname);
        String file_id = null;
        try {
            file_id = FileInformation.createFileid(pathname, file.lastModified());
        } catch (NoSuchAlgorithmException e) {
            PeerLogger.createFileIDFail();
            return -1;
        }


        getTaskManager().execute(new HandleInitiatorDelete(file_id));
        return 0;
    }

    @Override
    public int reclaim(int max_space) throws IOException {
        if (max_space < Peer.getStorageManager().getOccupiedSpace() * KB) {
            while (Peer.getStorageManager().getOccupiedSpace() * KB > max_space) {
                ChunkInfo removed = Peer.getStorageManager().removeExpendableChunk();
                PeerLogger.removedChunk(removed.getFileId(), removed.getNumber());
                Peer.getTaskManager().execute(new ManageRemoveChunk(removed));
            }
            if (max_space == 0) {
                Peer.getStorageManager().emptyChunksInfo();
            }
        }

        Peer.getStorageManager().setTotalSpace(max_space);
        PeerLogger.reclaimComplete(Peer.getStorageManager().getTotalSpace(), Peer.getStorageManager().getOccupiedSpace());
        return 0;
    }

    public List<String> state() {
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
