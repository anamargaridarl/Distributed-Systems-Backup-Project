package base.Tasks;

import base.Peer;
import base.channel.MessageReceiver;
import base.channel.MessageSender;
import base.messages.MessageChunkNo;
import base.messages.StatePeerMessage;

import javax.net.ssl.SSLSocket;
import java.net.Socket;

import static base.Clauses.GETTOIDEAL;
import static base.Clauses.VANILLA_VERSION;


public class ManageGetToIdeal implements Runnable {

    private final StatePeerMessage info_message;
    private final SSLSocket client_socket;

    public ManageGetToIdeal(int flag,String file_id, int chunk_no, SSLSocket client_socket) {
        this.info_message = new StatePeerMessage(Peer.getID(),GETTOIDEAL,flag,file_id,chunk_no);
        this.client_socket = client_socket;
    }

    @Override
    public void run() {
        Peer.getTaskManager().execute(new MessageSender(client_socket,info_message));
        Peer.getTaskManager().execute(new MessageReceiver(client_socket));
    }
}
