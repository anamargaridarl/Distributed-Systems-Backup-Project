package base.channel;

import base.Peer;
import base.PeerLogger;
import base.Tasks.HandleReceivedManager;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;


public class MessageReceiver implements Runnable {
    private static final ConcurrentHashMap<Socket, ObjectInputStream> inStreams = new ConcurrentHashMap<>();
    private final SSLSocket client_socket;

    public MessageReceiver(SSLSocket c_socket) {
        client_socket = c_socket;
    }

    @Override
    public void run() {
        try {
            ObjectInputStream in;
            if (inStreams.containsKey(client_socket)) {
                in = inStreams.get(client_socket);
            } else {
                in = new ObjectInputStream(client_socket.getInputStream());
                inStreams.put(client_socket, in);
            }
            Object msg = in.readObject();
            Peer.getTaskManager().execute(new HandleReceivedManager(msg, client_socket));
        } catch (Exception e) {

            try {
                client_socket.close();
            } catch (IOException ex) {
                PeerLogger.disconnectPeer();
            }
            PeerLogger.channelsDisrupt();
        }
    }
}
