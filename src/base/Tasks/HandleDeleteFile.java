package base.Tasks;

import base.Peer;
import base.messages.MessageChunkNo;

import java.net.Socket;

public class HandleDeleteFile implements Runnable {

    private final Socket clientsocket;
    private MessageChunkNo msg_delete;

    public HandleDeleteFile(MessageChunkNo msg, Socket socket) {
        msg_delete = msg;
        this.clientsocket = socket;
    }

    @Override
    public void run() {
        if(msg_delete.getNumber() == 0) {
            Peer.getTaskManager().execute(new ManageNumDeleteReply(msg_delete.getVersion(), 1, Peer.getStorageManager().getNumChunk(msg_delete.getFileId(), msg_delete.getNumber()), msg_delete.getFileId(), clientsocket));
        }
        Peer.getStorageManager().deleteChunks(msg_delete.getFileId(), msg_delete.getNumber());
    }
}
