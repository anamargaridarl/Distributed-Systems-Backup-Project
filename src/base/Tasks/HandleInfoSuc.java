package base.Tasks;

import base.Peer;
import base.messages.SucInfoMessage;

import java.net.InetSocketAddress;
import java.net.Socket;

public class HandleInfoSuc implements Runnable {

    private final SucInfoMessage info_message;
    private Socket client_socket;

    public HandleInfoSuc(String[] message, Socket client_socket) {
        info_message = new SucInfoMessage(message);
        this.client_socket = client_socket;
    }


    @Override
    public void run() {
        if (info_message.getFlag() == 1) {
            InetSocketAddress inetaddress = Peer.getStorageManager().getLastSucInfo();
            Peer.getStorageManager().addSucInfo(String.valueOf(inetaddress.getAddress()), inetaddress.getPort());
        }
    }
}
