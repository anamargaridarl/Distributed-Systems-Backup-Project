package base.Tasks;

import base.Peer;
import base.channel.MessageReceiver;
import base.channel.MessageSender;
import base.messages.MessageChunkNo;

import javax.net.ssl.SSLSocket;

import static base.Clauses.GETCHUNK;

/*
    Class that manages restore requests as initiator peer
 */
public class ManageGetChunk implements Runnable {

    private final MessageChunkNo getchunk_message;
    private final SSLSocket client_socket;

    public ManageGetChunk(int sender_id, String file_id, int i, SSLSocket client) {
        getchunk_message = new MessageChunkNo(GETCHUNK, sender_id, file_id, i);
        client_socket = client;
    }

    @Override
    public void run() {
        Peer.getTaskManager().execute(new MessageSender(client_socket, getchunk_message));
        Peer.getTaskManager().execute(new MessageReceiver(client_socket));
    }
}
