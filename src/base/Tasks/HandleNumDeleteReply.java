package base.Tasks;

import base.Peer;
import base.messages.BaseMessage;
import base.messages.ChunkReplyMessage;
import base.messages.MessageChunkNo;

public class HandleNumDeleteReply implements Runnable {

    private final ChunkReplyMessage msg_dreply;

    public HandleNumDeleteReply(ChunkReplyMessage msg) {
        msg_dreply = msg;
    }


    @Override
    public void run() {
        Peer.getStorageManager().addDeleteChunkNo(msg_dreply.getFileId(),msg_dreply.getNumChunks()); //TODO: pass sender id name to a more generic value name
    }
}
