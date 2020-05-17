package base.Tasks;

import base.Peer;
import base.channel.MessageSender;
import base.messages.ForwardGetMessage;

import java.net.Socket;


public class ManageGetToIdeal implements Runnable {

    private final ForwardGetMessage info_message;
    private final Socket client_socket;

    public ManageGetToIdeal(int flag,String file_id, int chunk_no, Socket client_socket) {
        this.info_message = new ForwardGetMessage(flag,file_id,chunk_no);
        this.client_socket = client_socket;
    }

    @Override
    public void run() {
        Peer.getTaskManager().execute(new MessageSender(client_socket,info_message.toByteArrayFinal()));
    }
}
