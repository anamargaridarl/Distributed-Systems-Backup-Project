package base.Tasks;

import base.Peer;
import base.TaskLogger;
import base.messages.MessageChunkNo;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static base.Clauses.MAX_DELAY_STORED;

/* Class that handles chunk removal task (to be concurrent)
 * check if the peer has the specific chunk and retrieve its desired replication degree
 * if the current replication degree drops from the desired start the backup subprotocol
 */
public class HandleRemovedChunk implements Runnable {

    MessageChunkNo message;

    public HandleRemovedChunk(String[] msg) {
        message = new MessageChunkNo(msg);
    }

    @Override
    public void run() {
        String rm_file_id = message.getFileId();
        int rm_chunk_n = message.getNumber();
        Peer.getStorageManager().decrementRepDegree(rm_file_id, rm_chunk_n);
        TaskLogger.removedReceived(Peer.getStorageManager().getChunkRepDegree(rm_file_id, rm_chunk_n));
        if (Peer.getStorageManager().isRepDegreeLow(rm_file_id, rm_chunk_n)) {
            TaskLogger.lowRepDeg();
            int desired_rep_deg = Peer.getStorageManager().getChunkInfo(rm_file_id, rm_chunk_n).getRepDeg();
            byte[] chunk_data = new byte[0];
            try {
                chunk_data = Peer.getStorageManager().getChunkData(rm_file_id, rm_chunk_n);
            } catch (IOException e) {
                TaskLogger.getChunkRetrieveFail();
            }
            ScheduledFuture<?> pending = Peer.getTaskManager().schedule(
                    new ManagePutChunk(Peer.getVersion(), Peer.getID(), rm_file_id, rm_chunk_n, desired_rep_deg, chunk_data),
                    new Random().nextInt(MAX_DELAY_STORED),
                    TimeUnit.MILLISECONDS
            );
            //TODO: send to client channel
            //ChannelManager.getBckChannel().registerPutChunkMessage(rm_file_id, rm_chunk_n, pending);
        }
    }
}
