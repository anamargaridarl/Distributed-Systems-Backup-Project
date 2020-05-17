package base.Tasks;

import base.Peer;
import base.channel.MessageSender;
import base.messages.BaseMessage;
import base.messages.ChunkReplyMessage;

import java.net.Socket;

import static base.Clauses.NUMREPLY;

public class ManageNumDeleteReply implements Runnable {

    private ChunkReplyMessage msg_dreply;
    private Socket client_socket;

    public ManageNumDeleteReply(String version, int sid, int numchunks, String file_id, Socket c_socket) {
        msg_dreply = new ChunkReplyMessage(version, NUMREPLY, sid,file_id,numchunks);
        client_socket = c_socket;
    }

    public void run() {
        Peer.getTaskManager().execute(new MessageSender(client_socket, msg_dreply));
    }
}
