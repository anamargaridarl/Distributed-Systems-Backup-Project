package base.Tasks;

import base.Peer;
import base.channel.MessageSender;
import base.messages.MessageChunkNo;

import java.net.Socket;

import static base.Clauses.GETCHUNK;

/*
    Class that manages restore requests as initiator peer
 */
public class ManageGetChunk implements Runnable {

    private final MessageChunkNo getchunk_message;
    private final Socket client_socket;

    public ManageGetChunk(String version, int sender_id, String file_id, int i, Socket client) {
        getchunk_message = new MessageChunkNo(version, GETCHUNK, sender_id, file_id, i);
        client_socket = client;
    }

    @Override
    public void run() {
        Peer.getTaskManager().execute(new MessageSender(client_socket,getchunk_message.toByteArrayFinal()));
    }
}
