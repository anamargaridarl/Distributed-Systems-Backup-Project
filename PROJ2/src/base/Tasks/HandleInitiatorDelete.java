package base.Tasks;

import base.Peer;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static base.Clauses.*;

public class HandleInitiatorDelete implements Runnable {
    private int i;
    private final String file_id;

    public HandleInitiatorDelete(String file_id) {
        this.file_id = file_id;
        this.i = 0;
    }

    @Override
    public void run() {
        int deleteChunkNum = Peer.getStorageManager().getDeleteChunkNum(file_id);
        if (i < deleteChunkNum || i == 0) {
            try {
                UUID hash = hashChunk(file_id, i);
                InetSocketAddress allocatedPeer = Peer.getChordManager().lookup(hash);
                if (allocatedPeer.equals(Peer.getChordManager().getPeerID().getOwnerAddress())) {
                    Peer.getTaskManager().execute(new HandleDeleteFile(file_id, i));
                } else {
                    SSLSocket client_socket = createSocket(allocatedPeer);
                    Peer.getTaskManager().execute(new ManageDeleteFile(Peer.getID(), file_id, i, client_socket));
                }
                i++;
                Peer.getTaskManager().schedule(this, TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (i == deleteChunkNum) {
            Peer.getStorageManager().removeFileInfo(file_id);
        }
    }
}

