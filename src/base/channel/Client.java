package base.channel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Client {
    private static int port;
    private static InetAddress host;
    private static String request;
    private static String reply;
    private static Socket socket;

    public static void main(String[] args) throws IOException {
        port = Integer.parseInt(args[1]);
        host = InetAddress.getByName(args[0]);

        socket = new Socket(host,port);

        List<String> operation = retrieveOperation(args);
        sendRequest(operation);
        processReply(operation);
    }

    private static List<String> retrieveOperation(String[] args) {
        List<String> op = new ArrayList<>();
        op.addAll(Arrays.asList(args).subList(2, args.length));
        return op;
    }

    private static void processRequest(List<String> operation) {

        StringBuilder aux = new StringBuilder();
        for(String op : operation) {
            aux.append(op).append(" ");
        }
        request = aux.toString();
    }

    private static void sendRequest(List<String> operation) throws IOException {

        processRequest(operation);
        PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
        out.println(request);


    }

    private static void processReply(List<String> operation) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        reply = in.readLine();
        if(reply != null) {

            String[] result = reply.split(" ");
            System.out.print("Client:");

            for(String op: operation)
            {
                System.out.print(" " +op);
            }

            System.out.print(" :" );

            for(String r: result) {
                System.out.print(" " + r);
            }

            System.out.print("\n");
        } else {
            System.out.println("Failed to receive reply from server");
        }
    }
}
