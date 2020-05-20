package base.Tasks;

import base.Peer;
import base.messages.MessageChunkNo;

import java.io.IOException;
import java.net.Socket;

import static base.Clauses.*;

public class HandleDeleteFile implements Runnable {

  private final Socket clientSocket;
  private final MessageChunkNo msg_delete;


  public HandleDeleteFile(MessageChunkNo msg, Socket socket) {
    msg_delete = msg;
    this.clientSocket = socket;
  }

  public HandleDeleteFile(String file_id, int number) {
    this(new MessageChunkNo(VANILLA_VERSION,DELETE, Peer.getID(),file_id,number),null);
  }

  @Override
  public void run() {
    if (msg_delete.getSenderId() != NOT_INITIATOR) {
      try {
        Peer.getStorageManager().handleDeleteSuccessors(msg_delete.getFileId(), msg_delete.getNumber());
      } catch (IOException e) {
        e.printStackTrace();
      }

      if (msg_delete.getNumber() == 0 && msg_delete.getSenderId() != Peer.getID()) {
        int numChunks = Peer.getStorageManager().getNumChunk(msg_delete.getFileId(), msg_delete.getNumber());
        Peer.getTaskManager().execute(new ManageNumDeleteReply(msg_delete.getVersion(), msg_delete.getSenderId(), numChunks, msg_delete.getFileId(), clientSocket));
      }
    }
    Peer.getStorageManager().deleteChunks(msg_delete.getFileId(), msg_delete.getNumber());
  }
}
