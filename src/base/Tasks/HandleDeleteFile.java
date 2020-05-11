package base.Tasks;

import base.ChunkInfo;
import base.Peer;
import base.messages.BackupMessage;
import base.messages.Message;
import base.messages.MessageChunkNo;

public class HandleDeleteFile implements Runnable {

    MessageChunkNo msg_delete;

    public HandleDeleteFile(String[] msg) {
        msg_delete = new MessageChunkNo(msg);
    }

    @Override
    public void run() {
       Peer.getStorageManager().deleteChunks(msg_delete.getFileId(), msg_delete.getNumber());
    }
}
