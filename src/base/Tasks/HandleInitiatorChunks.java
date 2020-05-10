package base.Tasks;

import base.Peer;
import base.TaskLogger;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import static base.Clauses.MAX_DELAY_STORED;

public class HandleInitiatorChunks implements Runnable {

    private final Socket client_socket;
    private int i;
    private String version;
    private String file_id;
    private Integer peer_id;
    private String filename;

    public HandleInitiatorChunks(int i, String version, String file_id, Integer peer_id, String filename, Socket client_socket) {
        this.version = version;
        this.file_id = file_id;
        this.peer_id = peer_id;
        this.i = i;
        this.filename = filename;
        this.client_socket = client_socket;
    }

    @Override
    public void run() {
        if (!Peer.getStorageManager().checkReceiveChunk(file_id, i)) {
            TaskLogger.noChunkReceivedFail();
            Peer.getStorageManager().removeRestoredChunkData(file_id);
            Peer.getStorageManager().removeRestoreRequest(file_id, Peer.getID());
            return;
        }
        i += 1;
        if (Peer.getStorageManager().checkLastChunk(file_id)) {
            try {
                Peer.getStorageManager().restoreFile(filename, file_id, i);
            } catch (IOException e) {
                TaskLogger.restoreFileFail();
            }
            return;
        }

        //TODO: use CHORD to get peer holding the chunk and create socket
        //ManageGetChunk manage_getchunk = new ManageGetChunk(version, peer_id, file_id, i);
        //Peer.getTaskManager().execute(manage_getchunk);
        //TODO: and pass the new socket to the task
        //Peer.getTaskManager().schedule(new HandleInitiatorChunks(i, version, file_id, peer_id, filename), MAX_DELAY_STORED, TimeUnit.MILLISECONDS);
    }
}
