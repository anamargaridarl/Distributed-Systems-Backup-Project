package base.Tasks;

import base.Peer;
import base.channel.MessageSender;
import base.messages.InfoMessage;

import javax.net.ssl.SSLSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import static base.Clauses.REPLYINFOINITIATOR;

public class ManageInfoToInitiator implements Runnable {

    private InfoMessage info_message;
    private SSLSocket client_socket;

    public ManageInfoToInitiator(String version, int sender_id, String file_id, int num, InetSocketAddress address, SSLSocket client_socket) {
        this.info_message = new InfoMessage(version,REPLYINFOINITIATOR,sender_id,file_id,num,address);
        this.client_socket = client_socket;
    }

    @Override
    public void run() {
        Peer.getTaskManager().execute(new MessageSender(client_socket,info_message));
    }
}
