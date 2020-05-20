package base.Tasks;


import base.Peer;
import base.channel.MessageSender;
import base.messages.MessageChunkNo;

import java.net.InetSocketAddress;
import java.net.Socket;

import static base.Clauses.DECLINED;

public class ManageDeclined implements Runnable {

  private final MessageChunkNo st_message;
  private final Socket client_socket;

  public ManageDeclined(String v, int sid, String fid, int chunkno, Socket socket) {
    st_message = new MessageChunkNo(v, DECLINED, sid, fid, chunkno);
    client_socket = socket;
  }

  public ManageDeclined(String v, int sid, String fid, int chunkno, Socket socket, InetSocketAddress origin) {
    this(v, sid, fid, chunkno, socket);
    st_message.setOrigin(origin);
  }

  @Override
  public void run() {
    Peer.getTaskManager().execute(new MessageSender(client_socket, st_message));
  }
}