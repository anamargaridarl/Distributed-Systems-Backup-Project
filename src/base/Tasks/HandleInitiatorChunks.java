package base.Tasks;

import base.Peer;
import base.TaskLogger;

import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import static base.Clauses.MAX_DELAY_STORED;

public class HandleInitiatorChunks implements Runnable {

    private Socket client_socket;
    private int i;
    private String version;
    private String file_id;
    private Integer peer_id;
    private String filename;

    public HandleInitiatorChunks(int i, String version, String file_id, Integer peer_id, String filename) {
        this.version = version;
        this.file_id = file_id;
        this.peer_id = peer_id;
        this.i = i;
        this.filename = filename;
    }

    /*
    private void noMoreChunks() {
        TaskLogger.noChunkReceivedFail();
        Peer.getStorageManager().removeRestoredChunkData(file_id);
        Peer.getStorageManager().removeRestoreRequest(file_id, Peer.getID());
        return;
    }*/

    @Override
    public void run() {
        if (i == 0 || i < Peer.getStorageManager().getRestoreChunkNum(file_id)) {
        /*if (!Peer.getStorageManager().checkReceiveChunk(file_id, i)) {
            noMoreChunks();
            return;
        }*/

            if (i == Peer.getStorageManager().getRestoreChunkNum(file_id)) {
                try {
                    Peer.getStorageManager().restoreFile(filename, file_id, Peer.getStorageManager().getRestoreChunkNum(file_id));
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }


            //TODO: use CHORD to get peer holding the chunk and create socket
            i = i + 1;
            try {
                client_socket = Peer.getChunkSocket(file_id, i);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            ManageGetChunk manage_getchunk = new ManageGetChunk(version, peer_id, file_id, i, client_socket);
            Peer.getTaskManager().execute(manage_getchunk);
            Peer.getTaskManager().schedule(new HandleInitiatorChunks(i, version, file_id, peer_id, filename), MAX_DELAY_STORED, TimeUnit.MILLISECONDS);

        }
    }
}
