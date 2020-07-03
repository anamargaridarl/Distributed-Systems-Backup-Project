package base.Tasks;

import base.Peer;
import base.TaskLogger;
import base.messages.MessageChunkNo;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;


public class HandleSuccGetChunk implements Runnable {
    private final MessageChunkNo succGetChunk;
    private final SSLSocket client_socket;

    public HandleSuccGetChunk(MessageChunkNo succGetChunk, SSLSocket client_socket) {
        this.succGetChunk = succGetChunk;
        this.client_socket = client_socket;
    }

    @Override
    public void run() {
        byte[] body;
        int num_chunks = 0;
        try {
            body = Peer.getStorageManager().getChunkData(succGetChunk.getFileId(), succGetChunk.getNumber());
            num_chunks = Peer.getStorageManager().getNumChunk(succGetChunk.getFileId(), succGetChunk.getNumber());
            Peer.getTaskManager().schedule(new ManageChunk(Peer.getID(), succGetChunk.getFileId(), succGetChunk.getNumber(), num_chunks, body, client_socket)
                    , 500, TimeUnit.MILLISECONDS);
        } catch (IOException e) {
            TaskLogger.noChunk(succGetChunk.getFileId(), succGetChunk.getNumber());
            InetSocketAddress nextSucc = Peer.getStorageManager().getSuccGetChunk(succGetChunk);
            if (nextSucc != null) {
                Peer.getTaskManager().execute(new ManageDeclined(Peer.getID(), succGetChunk.getFileId(), succGetChunk.getNumber(), client_socket, nextSucc));
            }
        }
    }
}
