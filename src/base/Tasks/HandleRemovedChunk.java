package base.Tasks;

import base.ChunkInfo;
import base.Peer;
import base.TaskLogger;
import base.messages.MessageChunkNo;

import java.io.IOException;

/* Class that handles chunk removal task (to be concurrent)
 * check if the peer has the specific chunk and retrieve its desired replication degree
 * if the current replication degree drops from the desired start the backup subprotocol
 */
public class HandleRemovedChunk implements Runnable {

  MessageChunkNo message;

  public HandleRemovedChunk(MessageChunkNo msg) {
    message = msg;
  }

  @Override
  public void run() {
    String rm_file_id = message.getFileId();
    int rm_chunk_n = message.getNumber();
    //remove reference of that senders of the table
    Peer.getStorageManager().removeSuccessorStoredOccurrence(rm_file_id, rm_chunk_n, message.getOrigin());
    TaskLogger.removedReceived(Peer.getStorageManager().getChunkRepDegree(rm_file_id, rm_chunk_n));
    if (Peer.getStorageManager().isRepDegreeLow(rm_file_id, rm_chunk_n)) {
      TaskLogger.lowRepDeg();
      ChunkInfo backupChunk;
      if ((backupChunk = Peer.getStorageManager().getChunkInfo(message.getFileId(), message.getNumber())) != null) { //dummy chunkInfo to check the chunk existance
        byte[] chunk_data;
        try {
          chunk_data = Peer.getStorageManager().getChunkData(rm_file_id, rm_chunk_n);
          Peer.getTaskManager().execute(new ManageBackupAuxiliar(backupChunk, chunk_data));
        } catch (IOException e) {
          TaskLogger.getChunkRetrieveFail();
        }
      } else {
        //TODO: deal with case when the ideal peer doesnt have the chunk within
        //TODO: ask a successor in the reference table and retrieve it. then initiate backup
      }
    }
  }
}