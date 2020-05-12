package base.Tasks;

import base.Peer;
import base.TaskLogger;
import base.channel.MessageSender;
import base.messages.Message;
import base.messages.MessageChunkNo;

import java.net.Socket;
import java.net.UnknownHostException;

import static base.Clauses.DELETE;
import static base.Clauses.ENHANCED_VERSION;

public class ManageDeleteFile implements Runnable {

    private final MessageChunkNo msg_delete;
    private final Socket client_socket;
    public ManageDeleteFile(String version, int peer_id, String file_id,int chunk_no, Socket c_socket) {
        msg_delete = new MessageChunkNo(version, DELETE, peer_id, file_id,chunk_no);
        client_socket = c_socket;
    }

    public void run() {
        if (msg_delete.getVersion().equals(ENHANCED_VERSION))
            Peer.getStorageManager().addDeleteRequest(msg_delete.getFileId());
        Peer.getTaskManager().execute(new MessageSender(client_socket,msg_delete.toByteArrayFinal()));
        Peer.getTaskManager().execute(new HandleReply(client_socket));
    }
}
