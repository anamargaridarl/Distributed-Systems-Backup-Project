package base.Tasks;

import base.Peer;
import base.messages.MessageChunkNo;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import static base.Clauses.NOT_INITIATOR;
import static base.Clauses.createSocket;

public class HandleDeclined implements Runnable{
  private final MessageChunkNo declineMsg;

  public HandleDeclined(MessageChunkNo declineMsg) {
    this.declineMsg = declineMsg;
  }

  @Override
  public void run() {
    if(declineMsg.getSenderId() != NOT_INITIATOR) {
      InetSocketAddress chunkHolder = declineMsg.getOrigin();
      try {
        SSLSocket socket = createSocket(chunkHolder);
        Peer.getTaskManager().execute(new ManageSuccGetChunk(declineMsg.getFileId(),declineMsg.getNumber(),socket));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
