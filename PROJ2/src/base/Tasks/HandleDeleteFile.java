package base.Tasks;

import base.Peer;
import base.messages.MessageChunkNo;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.lang.invoke.VarHandle;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import static base.Clauses.*;

public class HandleDeleteFile implements Runnable {

    private final SSLSocket clientSocket;
    private final MessageChunkNo msg_delete;


    public HandleDeleteFile(MessageChunkNo msg, SSLSocket socket) {
        msg_delete = msg;
        this.clientSocket = socket;
    }

    public HandleDeleteFile(String file_id, int number) {
        this(new MessageChunkNo(DELETE, Peer.getID(), file_id, number), null);
    }

    @Override
    public void run() {
        if (msg_delete.getSenderId() != NOT_INITIATOR) {
            try {
                Peer.getStorageManager().handleDeleteSuccessors(msg_delete.getFileId(), msg_delete.getNumber());
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (msg_delete.getNumber() == 0) {
                int numChunks = Peer.getStorageManager().getNumChunk(msg_delete.getFileId(), msg_delete.getNumber());
                Peer.getTaskManager().schedule(() -> {
                    int nChunks = Peer.getStorageManager().getNumChunk(msg_delete.getFileId(), msg_delete.getNumber());
                    new ManageNumDeleteReply(Peer.getID(), nChunks, msg_delete.getFileId(), clientSocket).run();
                }, numChunks == -1 ? 400 : 0, TimeUnit.MILLISECONDS);
            }
        } else {
            int numChunks = Peer.getStorageManager().getNumChunk(msg_delete.getFileId(), msg_delete.getNumber());
            Peer.getTaskManager().execute(() -> new ManageNumDeleteReply(Peer.getID(), numChunks, msg_delete.getFileId(), clientSocket).run());
        }

        Peer.getStorageManager().deleteChunks(msg_delete.getFileId(), msg_delete.getNumber());
        if (Peer.getStorageManager().ownsFile(msg_delete.getFileId()) || msg_delete.getNumber() == Peer.getStorageManager().getNumChunk(msg_delete.getFileId(), msg_delete.getNumber()) - 1) {
            InetSocketAddress initiatorAddr = Peer.getStorageManager().getInitiator(msg_delete.getFileId(), msg_delete.getNumber());
            if (initiatorAddr != null) {
                try {
                    SSLSocket initSocket = createSocket(initiatorAddr);
                    Peer.getTaskManager().execute(new ManageDeleteFile(NOT_INITIATOR, msg_delete.getFileId(), msg_delete.getNumber(), initSocket));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Peer.getStorageManager().removeFileInfo(msg_delete.getFileId());
        }
    }
}
