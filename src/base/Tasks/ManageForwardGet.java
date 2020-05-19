package base.Tasks;

import base.Peer;
import base.channel.MessageReceiver;
import base.channel.MessageSender;
import base.messages.MessageChunkNo;

import java.net.Socket;

import static base.Clauses.*;

public class ManageForwardGet implements Runnable {

    private final MessageChunkNo getchunk_message;
    private final Socket client_socket;

    public ManageForwardGet(String version, int sender_id, String file_id, int i, Socket client) {
        this.getchunk_message = new MessageChunkNo(version, FORWARDGET, sender_id, file_id, i);
        this.client_socket = client;
    }


    @Override
    public void run() {
        Peer.getTaskManager().execute(new MessageSender(client_socket,getchunk_message));
        Peer.getTaskManager().execute(new MessageReceiver(client_socket));
    }
}
