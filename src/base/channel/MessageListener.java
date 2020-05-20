package base.channel;

import base.Peer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MessageListener implements Runnable {
  ServerSocket server_socket;
  int serverPort;

  public MessageListener(int port) throws IOException {
    serverPort = port;
    server_socket = new ServerSocket(serverPort);
  }

  @Override
  public void run() {
    while (true) {
      try {
        Socket client = server_socket.accept();
        Peer.getTaskManager().execute(new MessageReceiver(client));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
