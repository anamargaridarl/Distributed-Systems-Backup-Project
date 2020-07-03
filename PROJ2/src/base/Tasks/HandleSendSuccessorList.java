package base.Tasks;

import base.Peer;
import base.channel.MessageSender;
import base.chord.ChordIdentifier;
import base.messages.ChordMessage;

import javax.net.ssl.SSLSocket;

public class HandleSendSuccessorList implements Runnable {
    private SSLSocket socket;
    private boolean allSuccessors;

    public HandleSendSuccessorList(SSLSocket socket) {
        this.socket = socket;
        allSuccessors = false;
    }

    public HandleSendSuccessorList(SSLSocket socket, boolean allSuccessors) {
        this.socket = socket;
        this.allSuccessors = allSuccessors;
    }

    @Override
    public void run() {
        ChordIdentifier[] succList = Peer.getChordManager().getSuccessorList();
        if (allSuccessors)
            Peer.getTaskManager().execute(new MessageSender(socket, ChordMessage.createSendAllSuccMessage(succList), true));
        else
            Peer.getTaskManager().execute(new MessageSender(socket, ChordMessage.createSendSuccListMessage(succList), true));
    }
}
