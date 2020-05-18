package base.Tasks;

import base.Peer;
import base.messages.MessageChunkNo;

import java.net.Socket;

public class HandleForwardGet implements Runnable {

    private final MessageChunkNo getchunk_message;
    private Socket client_socket;

    public HandleForwardGet(MessageChunkNo message, Socket client_socket) {
        getchunk_message = message;
        this.client_socket = client_socket;
    }

    @Override
    public void run() {

        System.out.println("received forward");
        if (Peer.getStorageManager().existsChunkRestore(getchunk_message.getFileId(), getchunk_message.getNumber())) {
            Peer.getTaskManager().execute(new ManageGetToIdeal(1, getchunk_message.getFileId(), getchunk_message.getNumber(), client_socket));
        } else
            Peer.getTaskManager().execute(new ManageGetToIdeal(0, getchunk_message.getFileId(), getchunk_message.getNumber(), client_socket));
    }
}
