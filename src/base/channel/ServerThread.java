package base.channel;

import base.AlreadyRegistered;
import base.NoAddress;
import base.Peer;
import base.Tasks.HandleClientRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ServerThread implements Runnable {
  Map<String, String> dns_db;
  ServerSocket server_socket;
  int serverPort;

  public ServerThread(int port) throws IOException {
    serverPort = port;
    server_socket = new ServerSocket(serverPort);
    dns_db = new HashMap<String, String>(); //TODO: replace with DHT
  }

  @Override
  public void run() {
    while (true) {
      try {
        Socket client = server_socket.accept();
        Peer.getTaskManager().execute(new HandleClientRequest(client));
      } catch (IOException e) {
        e.printStackTrace();
      }

    }
  }

  /*private void logRequest(String[] args) {
    System.out.print("Server:");
    for (String arg : args) {
      System.out.print(" " + arg);
    }
    System.out.print("\n");
  }

  private int registerAddress(String dns, String ip) throws AlreadyRegistered {
    if (dns_db.containsKey(dns))
      throw new AlreadyRegistered();
    else {
      dns_db.put(dns, ip);
      return dns_db.size();
    }
  }

  private String lookupAddress(String dns) throws NoAddress {

    String response;

    if (dns_db.containsKey(dns)) {
      response = dns + " " + dns_db.get(dns);
    } else
      throw new NoAddress();

    return response;

  }*/
}
