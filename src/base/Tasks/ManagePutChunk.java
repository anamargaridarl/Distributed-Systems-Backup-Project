package base.Tasks;

import base.FailedPutChunk;
import base.Peer;
import base.TaskLogger;
import base.channel.MessageSender;
import base.messages.BackupMessage;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import static base.Clauses.*;

/*
    Class that implements PutChunk has an initiator peer
 */
public class ManagePutChunk implements Runnable {

    private final Socket client_socket;
    private final BackupMessage bk_message;
    private int n_try;

    public ManagePutChunk(String v, int sid, String fid, int chunkno, int repd, int n_chunks, byte[] bdy, Socket client) throws IOException {
        bk_message = new BackupMessage(v, PUTCHUNK, sid, fid, chunkno, repd, n_chunks, bdy);
        n_try = 0;
        client_socket = client;
    }

    @Override
    public void run() {
        try {
            processMessage();
        } catch (IOException | FailedPutChunk e) {
            TaskLogger.putChunkFail(bk_message.getFileId(), bk_message.getNumber());
        }
    }

    public void processMessage() throws FailedPutChunk, IOException {
        //TODO: check if has recieved stored with actual rep deg desirable in the sender_id field
        //TODO: or if sender id is -1, check is has received /or has enough references
        int curr_rep_degree = Peer.getStorageManager().getCurrentRepDegree(bk_message.getFileId(), bk_message.getNumber()); //TODO: change
        if (curr_rep_degree < bk_message.getReplicationDeg()) {

            if (n_try < MAX_RETRIES) {
                Peer.getTaskManager().execute(new MessageSender(client_socket,bk_message.createByteMessage()));
                Peer.getStorageManager().addStoredChunkRequest(bk_message.getFileId(), bk_message.getNumber(),bk_message.getSenderId());
                Peer.getTaskManager().execute(new HandleReply(client_socket));
                if(bk_message.getSenderId() != NOT_INITIATOR) {
                    Peer.getTaskManager().schedule(this, (long) (1000 * Math.pow(2, n_try)), TimeUnit.MILLISECONDS);
                    n_try++;
                }
            } else {
                Peer.getStorageManager().removeStoredOccurrenceChunk(bk_message.getFileId(), bk_message.getNumber());
                throw new FailedPutChunk();
            }
        } else {
            TaskLogger.putChunkOk();

        }
    }
}
