package base.Tasks;

import base.Peer;
import base.channel.MessageSender;
import base.messages.MessageChunkNo;

import java.net.Socket;

import static base.Clauses.GETCHUNK;

public class ManageGetChunkSuc implements Runnable {

    private final MessageChunkNo getchunk_message;
    private final Socket client_socket;

    public ManageGetChunkSuc(String version, int sender_id, String file_id, int i, Socket client) {
        this.getchunk_message = new MessageChunkNo(version, GETCHUNK, sender_id, file_id, i);
        this.client_socket = client;
    }


    @Override
    public void run() {
        Peer.getTaskManager().execute(new MessageSender(client_socket,getchunk_message.toByteArrayFinal()));
        Peer.getTaskManager().execute(new HandleReply(client_socket));
    }
}
