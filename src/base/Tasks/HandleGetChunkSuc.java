package base.Tasks;

import base.Peer;
import base.messages.MessageChunkNo;

import java.net.Socket;

public class HandleGetChunkSuc implements Runnable {

    private final MessageChunkNo getchunk_message;
    private Socket client_socket;

    public HandleGetChunkSuc(String[] message, Socket client_socket) {
        getchunk_message = new MessageChunkNo(message);
        this.client_socket = client_socket;
    }

    @Override
    public void run() {

        if(Peer.getStorageManager().existsChunkRestore(getchunk_message.getFileId(),getchunk_message.getNumber()))
        {
            Peer.getTaskManager().execute(new ManageInfoSuc(1,client_socket));
        }
    }
}
