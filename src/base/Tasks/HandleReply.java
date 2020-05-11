package base.Tasks;

import base.Clauses;
import base.Peer;

import java.io.*;
import java.net.Socket;
import java.util.List;

import static base.Clauses.MAX_SIZE;

public class HandleReply implements Runnable {
  private final Socket client_socket;

  public HandleReply(Socket c_socket) {
    client_socket = c_socket;
  }

  @Override
  public void run() {
    try {
      DataInputStream in = new DataInputStream(new BufferedInputStream(client_socket.getInputStream()));
      byte[] msg_buffer = new byte[MAX_SIZE + 100];
      int fullSize = in.read(msg_buffer);
      List<byte[]> request = Clauses.separateHeaderAndBody(msg_buffer, fullSize);
      Peer.getTaskManager().execute(new HandleReceivedManager(request,client_socket));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
