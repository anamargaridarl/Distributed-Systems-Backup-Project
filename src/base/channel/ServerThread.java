package base.channel;

import base.AlreadyRegistered;
import base.NoAddress;

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
  DatagramSocket socket;
  Map<String, String> dns_db;
  DatagramPacket packet_res;
  DatagramPacket packet_req;
  ServerSocket server_socket;
  int serverPort;

  public ServerThread(int port) throws IOException {
    serverPort = port;
    server_socket = new ServerSocket(serverPort);
    dns_db = new HashMap<String, String>();
  }

  @Override
  public void run() {
    while (true) {
      try {
        Socket client = server_socket.accept();
        PrintWriter out = new PrintWriter(client.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        String inputLine, outputLine;

        inputLine = in.readLine();
        if (inputLine != null) {
          outputLine = processPacket(inputLine);
          out.println(outputLine);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }

    }
  }

  private String processPacket(String request) {
    String[] args = request.split(" ");
    String response = "-1";
    logRequest(args);

    if (args[0].equalsIgnoreCase("REGISTER")) {
      try {
        int value = registerAddress(args[1], args[2]);
        return Integer.toString(value);
      } catch (AlreadyRegistered alreadyRegistered) {
        return response;
      }
    } else if (args[0].equalsIgnoreCase("LOOKUP")) {
      try {
        return lookupAddress(args[1]);
      } catch (NoAddress noAddress) {
        return response;
      }
    }

    return response;
  }

  private void logRequest(String[] args) {
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

  }
}
