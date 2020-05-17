package base.Tasks;

import base.Peer;
import base.messages.BaseMessage;
import base.messages.ChunkReplyMessage;
import base.messages.MessageChunkNo;

public class HandleDeleteReply implements Runnable {

    private ChunkReplyMessage msg_dreply;

    public  HandleDeleteReply(String[] msg) {
        msg_dreply = new ChunkReplyMessage(msg);
    }


    @Override
    public void run() {
        Peer.getStorageManager().addDeleteChunkNo(msg_dreply.getFileId(),msg_dreply.getNumChunks()); //TODO: pass sender id name to a more generic value name
    }
}
