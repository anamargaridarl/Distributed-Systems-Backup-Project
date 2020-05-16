package base.Tasks;

import base.Peer;
import base.messages.InfoMessage;
import base.messages.MessageChunkNo;

import java.net.Socket;

public class HandleInfo implements Runnable{


    private InfoMessage info_message;
    private Socket client_socket;

    public HandleInfo(String[] message, Socket client_socket) {
        info_message = new InfoMessage(message);
        this.client_socket = client_socket;
    }

    @Override
    public void run() {
        System.out.println("bananas");
        //Peer.getTaskManager().execute(new ManageGetChunk("1.0",1,file_id,chunk_no,client_socket));

    }
}
