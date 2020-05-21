package base.Tasks;

import base.Peer;
import base.TaskLogger;
import base.messages.MessageChunkNo;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static base.Clauses.*;

/*
    Class that handles get chunk subprotocol messages
 */
public class HandleGetChunk implements Runnable {

    private final MessageChunkNo getchunk_message;
    private SSLSocket client_socket;

    public HandleGetChunk(MessageChunkNo message, SSLSocket client_socket) {
        getchunk_message = message;
        this.client_socket = client_socket;
    }

    @Override
    public void run() {

        byte[] body;
        int num_chunks = 0;

        try {
            body = Peer.getStorageManager().getChunkData(getchunk_message.getFileId(), getchunk_message.getNumber());
            num_chunks = Peer.getStorageManager().getNumChunk(getchunk_message.getFileId(), getchunk_message.getNumber());
            Peer.getTaskManager().schedule(new ManageChunk(getchunk_message.getVersion(), Peer.getID(), getchunk_message.getFileId(), getchunk_message.getNumber(), num_chunks, body, client_socket)
                    , 500, TimeUnit.MILLISECONDS);
        } catch (IOException e) {
            InetSocketAddress inetSocketAddress = null;
            InetSocketAddress info = null;
            info = Peer.getStorageManager().getSuccInfo(getchunk_message.getFileId(), getchunk_message.getNumber());
            if (info == null) {
                try {
                    inetSocketAddress = Peer.getStorageManager().handleGetChunk(getchunk_message);
                    if (inetSocketAddress == null) {
                        TaskLogger.restoreFileFail();
                        return;
                    }
                    SSLSocket socket = createSocket(inetSocketAddress);
                    Peer.getTaskManager().execute(new ManageForwardGet(getchunk_message.getVersion(), Peer.getID(), getchunk_message.getFileId(), getchunk_message.getNumber(), socket));
                    Peer.getTaskManager().schedule(this, 500, TimeUnit.MILLISECONDS);
                } catch (IOException a) {
                    a.printStackTrace();

                }
            } else {
                Peer.getTaskManager().execute(new ManageInfoToInitiator(getchunk_message.getVersion(), getchunk_message.getSenderId(), getchunk_message.getFileId(), getchunk_message.getNumber(), info, client_socket));
            }
        }

    }
}


