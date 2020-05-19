package base.Tasks;

import base.Peer;
import base.TaskLogger;
import base.channel.MessageReceiver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static base.Clauses.*;

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

    @Override
    public void run() {
        if (i == 0 || i <= Peer.getStorageManager().getRestoreChunkNum(file_id)) {

            if (i == Peer.getStorageManager().getRestoreChunkNum(file_id)  && i != 0) {
                try {
                    Peer.getStorageManager().restoreFile(filename, file_id, Peer.getStorageManager().getRestoreChunkNum(file_id));
                    return;
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }

            //TODO: use CHORD to get peer holding the chunk and create socket
            try {
                UUID hash = hashChunk(file_id,i);
                Integer hashKey = getHashKey(hash);
                Integer allocatedPeer = checkAllocated(hashKey); //TODO: dont use this version of the function
                InetSocketAddress idealPeer = chord.get((allocatedPeer-1)*40); //TODO: fix value when putting together
                client_socket = createSocket(idealPeer);
            } catch (NoSuchAlgorithmException | IOException e) {
                e.printStackTrace();
            }

            ManageGetChunk manage_getchunk = new ManageGetChunk(version, peer_id, file_id, i, client_socket);
            Peer.getTaskManager().execute(manage_getchunk);
            i = i + 1;
            Peer.getTaskManager().schedule(new HandleInitiatorChunks(i, version, file_id, peer_id, filename), 1500, TimeUnit.MILLISECONDS);

        }
    }
}
