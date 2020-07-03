package base.Tasks;

import base.ChunkInfo;
import base.Peer;
import base.TaskLogger;
import base.messages.RemovedMessage;

import java.io.IOException;

import static base.Clauses.NOT_INITIATOR;

/* Class that handles chunk removal task (to be concurrent)
 * check if the peer has the specific chunk and retrieve its desired replication degree
 * if the current replication degree drops from the desired start the backup subprotocol
 */
public class HandleRemovedChunk implements Runnable {

    RemovedMessage message;

    public HandleRemovedChunk(RemovedMessage msg) {
        message = msg;
    }

    @Override
    public void run() {
        String rm_file_id = message.getFileId();
        int rm_chunk_n = message.getNumber();
        //remove reference of that senders of the table
        Peer.getStorageManager().removeSuccessorStoredOccurrence(rm_file_id, rm_chunk_n, message.getOrigin());
        TaskLogger.removedReceived(Peer.getStorageManager().getChunkRepDegree(rm_file_id, rm_chunk_n));
        TaskLogger.lowRepDeg();
        ChunkInfo backupChunk;
        if ((backupChunk = Peer.getStorageManager().getChunkInfo(message.getFileId(), message.getNumber())) != null) {
            byte[] chunk_data;
            try {
                chunk_data = Peer.getStorageManager().getChunkData(rm_file_id, rm_chunk_n);
                Peer.getTaskManager().execute(new ManageBackupAuxiliar(backupChunk, chunk_data));
            } catch (IOException e) {
                TaskLogger.getChunkRetrieveFail(rm_file_id, rm_chunk_n);
            }
        } else {
            ChunkInfo restoredChunk = message.getChunkInfo();
            Peer.getStorageManager().addStoredChunkRequest(restoredChunk.getFileId(), restoredChunk.getNumber(), NOT_INITIATOR);
            Peer.getTaskManager().execute(new ManageBackupAuxiliar(restoredChunk, restoredChunk.getChunk()));
        }
    }
}