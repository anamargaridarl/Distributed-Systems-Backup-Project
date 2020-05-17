package base.channel;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MessageSender implements Runnable {
    private final static ConcurrentHashMap<Socket,ObjectOutputStream> outStreams = new ConcurrentHashMap<>();
    private final Socket socket;
    private final Object msg;

    public MessageSender(Socket socket, Object msg) {
        this.socket = socket;
        this.msg = msg;
    }

    @Override
    public void run() {
        try {
            outStreams.putIfAbsent(socket,new ObjectOutputStream(socket.getOutputStream()));
            ObjectOutputStream out = outStreams.get(socket);
            out.reset();
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
