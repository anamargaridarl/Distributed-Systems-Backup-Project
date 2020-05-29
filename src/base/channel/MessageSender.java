package base.channel;

import javax.net.ssl.SSLSocket;

import base.PeerLogger;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class MessageSender implements Runnable {
    private final static ConcurrentHashMap<Socket, ObjectOutputStream> outStreams = new ConcurrentHashMap<>();
    private final SSLSocket socket;
    private final Object msg;
    private boolean closeAfterSending;

    public MessageSender(SSLSocket socket, Object msg) {
        this.socket = socket;
        this.msg = msg;
        closeAfterSending = false;
    }

    public MessageSender(SSLSocket socket, Object msg, boolean closeAfterSending) {
        this.socket = socket;
        this.msg = msg;
        this.closeAfterSending = closeAfterSending;
    }

    @Override
    public void run() {
        try {
            outStreams.putIfAbsent(socket, new ObjectOutputStream(socket.getOutputStream()));
            ObjectOutputStream out = outStreams.get(socket);
            out.reset();
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            PeerLogger.disconnectPeer();
        }

        if (closeAfterSending) {
            try {
                socket.close();
            } catch (IOException e) {
                PeerLogger.disconnectPeer();
            }
        }
    }
}
