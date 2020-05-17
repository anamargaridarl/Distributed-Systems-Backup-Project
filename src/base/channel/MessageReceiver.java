package base.channel;

import base.Peer;
import base.Tasks.HandleReceivedManager;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;


public class MessageReceiver implements Runnable {
  private static ConcurrentHashMap<Socket,ObjectInputStream> inStreams;
  private final Socket client_socket;

  public MessageReceiver(Socket c_socket) {
    client_socket = c_socket;
    if(inStreams == null)
      inStreams = new ConcurrentHashMap<>();
  }

  @Override
  public void run() {
    try {
      ObjectInputStream in;
      if(inStreams.containsKey(client_socket)) {
        in = inStreams.get(client_socket);
      } else {
        in = new ObjectInputStream(client_socket.getInputStream());
        inStreams.put(client_socket,in);
      }
      Object msg = in.readObject();
      Peer.getTaskManager().execute(new HandleReceivedManager(msg,client_socket));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
