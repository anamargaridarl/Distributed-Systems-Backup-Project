package base.Tasks;

import static base.Clauses.STORED;

import base.Peer;
import base.channel.MessageSender;
import base.messages.MessageChunkNo;

import java.net.Socket;

public class ManageStored implements Runnable {

  private final MessageChunkNo st_message;
  private final Socket client_socket;

  public ManageStored(String v, int sid, String fid, int chunkno, Socket socket) {
    st_message = new MessageChunkNo(v, STORED, sid, fid, chunkno);
    client_socket = socket;
  }

  @Override
  public void run() {
    Peer.getTaskManager().execute(new MessageSender(client_socket, st_message));
  }
}
