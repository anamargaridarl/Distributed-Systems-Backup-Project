package base.Tasks;

import base.Peer;
import base.TaskLogger;
import base.channel.MessageSender;
import base.messages.Message;

import java.net.Socket;
import java.net.UnknownHostException;

import static base.Clauses.DELETE;
import static base.Clauses.ENHANCED_VERSION;

public class ManageDeleteFile implements Runnable {

    private final Message msg_delete;
    private final Socket client_socket;
    public ManageDeleteFile(String version, int peer_id, String file_id, Socket c_socket) {
        msg_delete = new Message(version, DELETE, peer_id, file_id);
        client_socket = c_socket;
    }

    public void processMessage() throws UnknownHostException {
        Peer.getTaskManager().execute(new MessageSender(client_socket,msg_delete.toByteArrayFinal()));
    }

    public void run() {
        try {
            if (msg_delete.getVersion().equals(ENHANCED_VERSION))
                Peer.getStorageManager().addDeleteRequest(msg_delete.getFileId());
            processMessage();
        } catch (UnknownHostException e) {
            TaskLogger.sendMessageFail();
        }
    }
}
