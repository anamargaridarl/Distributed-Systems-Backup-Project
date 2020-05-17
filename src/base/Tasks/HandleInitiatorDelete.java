package base.Tasks;

import base.Peer;
import base.TaskLogger;

import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import static base.Clauses.MAX_DELAY_STORED;

public class HandleInitiatorDelete implements Runnable{
    private Socket client_socket;
    private int i;
    private String version;
    private String file_id;
    private Integer peer_id;

    public HandleInitiatorDelete(int i, String version, String file_id, Integer peer_id) {
        this.version = version;
        this.file_id = file_id;
        this.peer_id = peer_id;
        this.i = i;
    }

    @Override
    public void run() {
        if (i < Peer.getStorageManager().getDeleteChunkNum(file_id) || i == 0) {
            try {
                client_socket = Peer.getChunkSocket(file_id, i);
                ManageDeleteFile manage_delete = new ManageDeleteFile(version, peer_id, file_id, i ,client_socket);
                Peer.getTaskManager().execute(manage_delete);
                i = i+1;
                Peer.getTaskManager().schedule(new HandleInitiatorDelete(i, version, file_id, peer_id), MAX_DELAY_STORED, TimeUnit.MILLISECONDS);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}

