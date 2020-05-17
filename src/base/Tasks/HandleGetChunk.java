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

    public HandleGetChunk(MessageChunkNo message, Socket client_socket) {
        getchunk_message = message;
        this.client_socket = client_socket;
    }

    @Override
    public void run() {

        /*if (Peer.getStorageManager().existsChunkRestore(getchunk_message.getFileId(), getchunk_message.getNumber())) {
            byte[] body;
            try {
                body = Peer.getStorageManager().getChunkData(getchunk_message.getFileId(), getchunk_message.getNumber());
            } catch (IOException e) {
                TaskLogger.getChunkRetrieveFail();
                return;
            }

            Random random = new Random();
            int time_wait = random.nextInt(MAX_DELAY_STORED);
            Peer.getTaskManager().schedule(new ManageChunk(getchunk_message.getVersion(), Peer.getID(), getchunk_message.getFileId(), getchunk_message.getNumber(), body, client_socket)
                    , time_wait, TimeUnit.MILLISECONDS);
        }*/
    }

}
