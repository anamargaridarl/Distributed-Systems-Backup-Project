package base.Tasks;

import base.ChunkInfo;
import base.Peer;
import base.messages.BackupMessage;
import base.messages.Message;

public class HandleDeleteFile implements Runnable {

    Message msg_delete;

    public HandleDeleteFile(String[] msg) {
        msg_delete = new Message(msg);
    }

    @Override
    public void run() {
        Peer.getStorageManager().deleteChunks(msg_delete.getFileId());
        return;
    }
}
