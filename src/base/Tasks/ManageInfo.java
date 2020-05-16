package base.Tasks;

import base.Peer;
import base.channel.MessageSender;
import base.messages.InfoMessage;
import base.messages.SucInfoMessage;

import java.net.Socket;

public class ManageInfo implements Runnable {

    private InfoMessage info_message;
    private Socket client_socket;

    public ManageInfo(String address, int port,Socket client_socket) {
        this.info_message = new InfoMessage(address,port);
        this.client_socket = client_socket;
    }

    @Override
    public void run() {
        Peer.getTaskManager().execute(new MessageSender(client_socket,info_message.toByteArrayFinal()));
        Peer.getTaskManager().execute(new HandleReply(client_socket));
    }
}
