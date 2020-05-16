package base.Tasks;

import base.Peer;
import base.channel.MessageSender;
import base.messages.SucInfoMessage;

import java.net.Socket;


public class ManageInfoSuc implements Runnable {

    private final SucInfoMessage info_message;
    private final Socket client_socket;

    public ManageInfoSuc(int flag,Socket client_socket) {
        this.info_message = new SucInfoMessage(flag);
        this.client_socket = client_socket;
    }

    @Override
    public void run() {
        Peer.getTaskManager().execute(new MessageSender(client_socket,info_message.toByteArrayFinal()));
        Peer.getTaskManager().execute(new HandleReply(client_socket));
    }
}
