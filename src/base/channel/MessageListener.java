package base.channel;

import base.Peer;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.Socket;

public class MessageListener implements Runnable {
  SSLServerSocket server_socket;
  int serverPort;

  public MessageListener(int port) throws IOException {
    serverPort = port;
    server_socket = (SSLServerSocket) SSLServerSocketFactory.getDefault().createServerSocket(serverPort);
  }

  @Override
  public void run() {
    while (true) {
      try {
        SSLSocket client = (SSLSocket) server_socket.accept();
        Peer.getTaskManager().execute(new MessageReceiver(client));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
