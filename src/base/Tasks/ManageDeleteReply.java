package base.Tasks;

import base.Peer;
import base.channel.MessageSender;
import base.messages.BaseMessage;

import java.net.Socket;

import static base.Clauses.ENHANCED_VERSION;

public class ManageDeleteReply implements Runnable {

    private BaseMessage msg_dreply;
    private Socket client_socket;

    public ManageDeleteReply(String version, int chunksNo, Socket c_socket) {
        msg_dreply = new BaseMessage(version, "DELETEREPLY", chunksNo);
        client_socket = c_socket;
    }

    public void run() {
        Peer.getTaskManager().execute(new MessageSender(client_socket, msg_dreply));
    }
}
