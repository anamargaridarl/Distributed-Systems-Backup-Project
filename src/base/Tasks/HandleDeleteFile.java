package base.Tasks;

import base.ChunkInfo;
import base.Peer;
import base.messages.BackupMessage;
import base.messages.Message;
import base.messages.MessageChunkNo;

import java.net.Socket;
import java.util.logging.SocketHandler;

public class HandleDeleteFile implements Runnable {

    private final Socket clientsocket;
    private MessageChunkNo msg_delete;

    public HandleDeleteFile(String[] msg, Socket socket) {
        msg_delete = new MessageChunkNo(msg);
        this.clientsocket = socket;
    }

    @Override
    public void run() {
        if(msg_delete.getNumber() == 0) {
            Peer.getTaskManager().execute(new ManageDeleteReply(msg_delete.getVersion(), Peer.getStorageManager().getNumChunk(msg_delete.getFileId(), msg_delete.getNumber()), clientsocket));
        }
        Peer.getStorageManager().deleteChunks(msg_delete.getFileId(), msg_delete.getNumber());
    }
}
