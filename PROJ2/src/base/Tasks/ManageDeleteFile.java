package base.Tasks;

import base.Peer;
import base.channel.MessageReceiver;
import base.channel.MessageSender;
import base.messages.MessageChunkNo;

import javax.net.ssl.SSLSocket;

import static base.Clauses.DELETE;

public class ManageDeleteFile implements Runnable {

    private final MessageChunkNo msg_delete;
    private final SSLSocket client_socket;

    public ManageDeleteFile(int peer_id, String file_id, int chunk_no, SSLSocket c_socket) {
        msg_delete = new MessageChunkNo(DELETE, peer_id, file_id, chunk_no);
        client_socket = c_socket;
    }

    public void run() {
        Peer.getTaskManager().execute(new MessageSender(client_socket, msg_delete));
        if (msg_delete.getNumber() == 0)
            Peer.getTaskManager().execute(new MessageReceiver(client_socket));
    }
}
