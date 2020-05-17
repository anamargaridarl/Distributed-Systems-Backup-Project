package base.Tasks;

import base.Peer;
import base.SucessorInfo;
import base.messages.ForwardGetMessage;

import java.net.InetSocketAddress;
import java.net.Socket;

public class HandleGetToIdeal implements Runnable {

    private final ForwardGetMessage info_message;
    private Socket client_socket;

    public HandleGetToIdeal(ForwardGetMessage message, Socket client_socket) {
        info_message =message;
        this.client_socket = client_socket;
    }


    @Override
    public void run() {
        if (info_message.getFlag() == 1) {
            InetSocketAddress address = Peer.getStorageManager().getLastSucInfo(info_message.getFileId(), info_message.getChunkNo());
            Peer.getStorageManager().addSuccInfo(info_message.getFileId(),info_message.getChunkNo(), address);
        }
    }
}
