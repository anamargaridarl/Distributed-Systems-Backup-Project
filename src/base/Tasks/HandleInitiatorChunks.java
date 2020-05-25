package base.Tasks;

import base.Peer;
import base.TaskLogger;
import base.channel.MessageReceiver;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static base.Clauses.*;

public class HandleInitiatorChunks implements Runnable {

    private SSLSocket client_socket;
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
            byte[] body;
            int num_chunks = 0;

            try {
                body = Peer.getStorageManager().getChunkData(file_id, i);
                num_chunks = Peer.getStorageManager().getNumChunk(file_id, i);
                Peer.getStorageManager().addRestoreChunkNo(file_id, num_chunks);
                Peer.getStorageManager().addRestoredChunkRequest(file_id, i, body);
            } catch (IOException e) {
                try {
                    UUID hash = hashChunk(file_id, i);
                    Integer hashKey = getHashKey(hash);
                    Integer allocatedPeer = checkAllocated(hashKey); //TODO: dont use this version of the function
                    InetSocketAddress idealPeer = chord.get((allocatedPeer - 1) * 2); //TODO: fix value when putting together
                    client_socket = createSocket(idealPeer);
                    ManageGetChunk manage_getchunk = new ManageGetChunk(version, peer_id, file_id, i, client_socket);
                    Peer.getTaskManager().execute(manage_getchunk);
                } catch (NoSuchAlgorithmException | IOException a) {
                    a.printStackTrace();
                }
            }

            i = i + 1;
            Peer.getTaskManager().schedule(new HandleInitiatorChunks(i, version, file_id, peer_id, filename), 2000, TimeUnit.MILLISECONDS);

        }
    }
}
