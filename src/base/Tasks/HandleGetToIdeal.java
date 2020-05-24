package base.Tasks;

import base.Peer;
import base.messages.StatePeerMessage;

import javax.net.ssl.SSLSocket;
import java.net.Socket;

public class HandleGetToIdeal implements Runnable {

    private final StatePeerMessage info_message;
    private Socket client_socket;

    public HandleGetToIdeal(StatePeerMessage message, SSLSocket client_socket) {
        info_message =message;
        this.client_socket = client_socket;
    }


    @Override
    public void run() {
        if (info_message.getFlag() == 1) {
            Peer.getStorageManager().addSuccInfo(info_message.getFileId(),info_message.getNumber(),info_message.getOrigin());
        }
    }
}
