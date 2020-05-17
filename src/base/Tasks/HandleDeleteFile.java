package base.Tasks;

import base.ChunkInfo;
import base.Peer;
import base.messages.BackupMessage;
import base.messages.Message;
import base.messages.MessageChunkNo;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.SocketHandler;

public class HandleDeleteFile implements Runnable {

    private final Socket clientsocket;
    private MessageChunkNo msg_delete;
    private String[] msg;


    public HandleDeleteFile(MessageChunkNo msg, Socket socket) {
        msg_delete = msg;
        this.clientsocket = socket;
    }

    @Override
    public void run() {
        int numChunks = 0;
        try {
            Peer.getStorageManager().handleDeleteSucessors(msg_delete.getFileId(), msg_delete.getNumber());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (msg_delete.getNumber() == 0) {
            numChunks = Peer.getStorageManager().getNumChunk(msg_delete.getFileId(), msg_delete.getNumber());
            Peer.getTaskManager().execute(new ManageNumDeleteReply(msg_delete.getVersion(),msg_delete.getSenderId(), numChunks,msg_delete.getFileId(), clientsocket));
        }

        Peer.getStorageManager().deleteChunks(msg_delete.getFileId(), msg_delete.getNumber());
    }
}
