package base.Tasks;

import base.Clauses;
import base.Peer;
import base.TaskLogger;
import base.messages.MessageChunkNo;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
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
        //TODO:successor chain needs to be implemented
        try {
            body = Peer.getStorageManager().getChunkData(getchunk_message.getFileId(), getchunk_message.getNumber());
            //TODO: no need for this value with all the chunks
            num_chunks = Peer.getStorageManager().getNumChunk(getchunk_message.getFileId(), getchunk_message.getNumber());
            //TODO: check error in number chunks
        } catch (IOException e) {
            TaskLogger.getChunkRetrieveFail();
            return;
        }

        Random random = new Random();
        int time_wait = random.nextInt(MAX_DELAY_STORED);
        Peer.getTaskManager().schedule(new ManageChunk(getchunk_message.getVersion(), Peer.getID(), getchunk_message.getFileId(), getchunk_message.getNumber(), num_chunks, body, client_socket)
                , time_wait, TimeUnit.MILLISECONDS);
    }
}


