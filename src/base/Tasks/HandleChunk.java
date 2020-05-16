package base.Tasks;

import base.Peer;
import base.messages.RestoreMessage;

import java.net.Socket;

public class HandleChunk implements Runnable {

    private final RestoreMessage restore_message;

    public HandleChunk(String[] message, byte[] body) {
        restore_message = new RestoreMessage(message, body);
    }

    @Override
    public void run() {
            Peer.restorechunks = restore_message.getNumChunks();
            Peer.getStorageManager().addRestoredChunkRequest(restore_message.getFileId(), restore_message.getNumber(), restore_message.getBody());
    }

}
