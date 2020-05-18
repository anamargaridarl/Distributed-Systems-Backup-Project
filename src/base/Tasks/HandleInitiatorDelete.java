package base.Tasks;

import base.Peer;
import base.TaskLogger;

import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static base.Clauses.*;

public class HandleInitiatorDelete implements Runnable{
    private int i;
    private final String version;
    private final String file_id;
    private final Integer peer_id;

    public HandleInitiatorDelete(String version, String file_id, Integer peer_id) {
        this.version = version;
        this.file_id = file_id;
        this.peer_id = peer_id;
        this.i = 0;
    }

    @Override
    public void run() {
        if (i < Peer.getStorageManager().getDeleteChunkNum(file_id) || i == 0) {
            try {
                UUID hash = hashChunk(file_id,i);
                int hashKey = getHashKey(hash);
                int allocatedPeer = checkAllocated(hashKey);
                if(allocatedPeer == Peer.getID()) {

                } else {
                    Socket client_socket = Peer.getChunkSocket(file_id, i); //TODO: fix get ideal / use allocated / check if doesnt belong to own peer
                    Peer.getTaskManager().execute(new ManageDeleteFile(version, peer_id, file_id, i , client_socket));
                }
                i++;
                Peer.getTaskManager().schedule(this, TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (NoSuchAlgorithmException | IOException e) {
                e.printStackTrace();
            }

        }
    }
}

