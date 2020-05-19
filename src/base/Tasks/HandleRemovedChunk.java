package base.Tasks;

import base.ChunkInfo;
import base.Peer;
import base.TaskLogger;
import base.channel.MessageReceiver;
import base.messages.MessageChunkNo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import static base.Clauses.*;

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
    int desiredRepDeg = Peer.getStorageManager().getChunkRepDegree(rm_file_id, rm_chunk_n);
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
        TaskLogger.getChunkRetrieveFail();
      }
    } else {
      try {
        InetSocketAddress succAddr = Peer.getStorageManager().getSuccGetChunk(message);
        Socket succSocket = createSocket(succAddr);
        Peer.getTaskManager().execute(new ManageSuccGetChunk(message.getFileId(), message.getNumber(), succSocket));
        Peer.getTaskManager().schedule(() -> {
          if (Peer.getStorageManager().checkReceiveChunk(message.getFileId(), message.getNumber())) {
            byte[] chunk = Peer.getStorageManager().getRestoredChunk(message.getFileId(), message.getNumber());
            int chunks_num = Peer.getStorageManager().getRestoreChunkNum(message.getFileId());
            ChunkInfo restoredChunk = new ChunkInfo(message.getFileId(), desiredRepDeg, NOT_INITIATOR, message.getNumber(), chunks_num);
            Peer.getTaskManager().execute(new ManageBackupAuxiliar(restoredChunk, chunk));
            Peer.getStorageManager().removeRestoredChunkData(message.getFileId());
          } else {
            TaskLogger.putChunkFail(message.getFileId(), message.getNumber());
          }
        }, 2 * TIMEOUT, TimeUnit.MILLISECONDS);
      } catch (Exception e) {
        e.printStackTrace();
      }

    }
  }
}