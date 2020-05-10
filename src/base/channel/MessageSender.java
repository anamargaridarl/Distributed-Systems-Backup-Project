package base.channel;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MessageSender implements Runnable {

    private final Socket socket;
    private final byte[] msg;

    public MessageSender(Socket socket, byte[] msg) {
        this.socket = socket;
        this.msg = msg;
    }

    @Override
    public void run() {
        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.write(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
