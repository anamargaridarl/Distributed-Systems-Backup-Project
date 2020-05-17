package base.Tasks;

import base.Peer;
import base.messages.BaseMessage;
import base.messages.MessageChunkNo;

public class HandleDeleteReply implements Runnable {

    private BaseMessage msg_dreply;

    public  HandleDeleteReply(BaseMessage msg) {
        msg_dreply = msg;
    }


    @Override
    public void run() {
        Peer.deletechunks = msg_dreply.getSenderId(); //TODO: pass sender id name to a more generic value name
    }
}
