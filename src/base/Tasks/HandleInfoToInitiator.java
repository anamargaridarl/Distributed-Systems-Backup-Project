package base.Tasks;

import base.Peer;
import base.messages.InfoMessage;
import base.messages.MessageChunkNo;

import java.net.Socket;

public class HandleInfoToInitiator implements Runnable{


    private InfoMessage info_message;
    private Socket client_socket;

    public HandleInfoToInitiator(InfoMessage message, Socket client_socket) {
        info_message = message;
        this.client_socket = client_socket;
    }

    @Override
    public void run() {
        //create socket with info
        //send getchunk
        //Peer.getTaskManager().execute(new ManageGetChunk("1.0",1,file_id,chunk_no,client_socket));

    }
}
