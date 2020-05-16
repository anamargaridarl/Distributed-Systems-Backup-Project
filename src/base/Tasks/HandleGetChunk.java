package base.Tasks;

import base.Peer;
import base.TaskLogger;
import base.messages.MessageChunkNo;

import java.io.*;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static base.Clauses.MAX_DELAY_STORED;

/*
    Class that handles get chunk subprotocol messages
 */
public class HandleGetChunk implements Runnable {

    private final MessageChunkNo getchunk_message;
    private Socket client_socket;

    public HandleGetChunk(String[] message, Socket client_socket) {
        getchunk_message = new MessageChunkNo(message);
        this.client_socket = client_socket;
    }

    @Override
    public void run() {

        byte[] body;
        int num_chunks = 0;
        try {
            body = Peer.getStorageManager().getChunkData(getchunk_message.getFileId(), getchunk_message.getNumber());
            num_chunks = Peer.getStorageManager().getNumChunk(getchunk_message.getFileId(), getchunk_message.getNumber());
        } catch (IOException e) {
            TaskLogger.getChunkRetrieveFail();
            return;
        }

        if (body == null) {
            while (Peer.getStorageManager().succ_port == 0 && Peer.getStorageManager().succ_address == "") {
                try {
                    Peer.getStorageManager().handleGetChunk(getchunk_message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Peer.getTaskManager().execute(new ManageInfo(Peer.getStorageManager().succ_address,Peer.getStorageManager().succ_port,client_socket));
        } else {
            Random random = new Random();
            int time_wait = random.nextInt(MAX_DELAY_STORED);
            Peer.getTaskManager().schedule(new ManageChunk(getchunk_message.getVersion(), Peer.getID(), getchunk_message.getFileId(), getchunk_message.getNumber(), num_chunks, body, client_socket)
                    , time_wait, TimeUnit.MILLISECONDS);
        }


    }
}


