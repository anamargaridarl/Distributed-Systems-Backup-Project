package base.Tasks;

import base.Peer;
import base.channel.MessageSender;
import base.messages.InfoMessage;

import java.net.Socket;

public class ManageInfoToInitiator implements Runnable {

    private InfoMessage info_message;
    private Socket client_socket;

    public ManageInfoToInitiator(String address, int port, Socket client_socket) {
        this.info_message = new InfoMessage(address,port);
        this.client_socket = client_socket;
    }

    @Override
    public void run() {
        Peer.getTaskManager().execute(new MessageSender(client_socket,info_message));
    }
}
