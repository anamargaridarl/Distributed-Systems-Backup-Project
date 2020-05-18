package base.Tasks;

import base.Peer;
import base.TaskLogger;
import base.messages.MessageChunkNo;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static base.Clauses.MAX_DELAY_STORED;
import static base.Clauses.createSocket;

/*
    Class that handles get chunk subprotocol messages
 */
public class HandleGetChunk implements Runnable {

    private final MessageChunkNo getchunk_message;
    private Socket client_socket;

    public HandleGetChunk(MessageChunkNo message, Socket client_socket) {
        getchunk_message = message;
        this.client_socket = client_socket;
    }

    @Override
    public void run() {

        byte[] body;
        int num_chunks = 0;
        Random random = new Random();
        int time_wait = random.nextInt(MAX_DELAY_STORED);

        try {
            if (Peer.getID() != 2) {
                body = Peer.getStorageManager().getChunkData(getchunk_message.getFileId(), getchunk_message.getNumber());
                num_chunks = Peer.getStorageManager().getNumChunk(getchunk_message.getFileId(), getchunk_message.getNumber());
                Peer.getTaskManager().schedule(new ManageChunk(getchunk_message.getVersion(), Peer.getID(), getchunk_message.getFileId(), getchunk_message.getNumber(), num_chunks, body, client_socket)
                        , time_wait, TimeUnit.MILLISECONDS);
            } else {
                throw new IOException();
            }
        } catch (IOException e) {
            InetSocketAddress inetSocketAddress = null;
            try {
                inetSocketAddress = Peer.getStorageManager().handleGetChunk(getchunk_message);
                if (inetSocketAddress == null) {
                    TaskLogger.restoreFileFail();
                    return;
                }
                Peer.getTaskManager().execute(new ManageInfoToInitiator(getchunk_message.getVersion(), getchunk_message.getSenderId(), getchunk_message.getFileId(), getchunk_message.getNumber(), inetSocketAddress, client_socket));
            } catch (IOException a) {
                a.printStackTrace();

            }

        }


    }
}


