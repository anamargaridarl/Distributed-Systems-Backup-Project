package base.Tasks;

import base.Peer;
import base.channel.MessageSender;
import base.chord.ChordIdentifier;
import base.messages.ChordMessage;

import javax.net.ssl.SSLSocket;
import java.util.UUID;

public class SendSuccessorToPeer implements SuccessorHandler {
    private UUID key;
    private SSLSocket socket;

    public SendSuccessorToPeer(UUID key, SSLSocket socket) {
        this.key = key;
        this.socket = socket;
    }

    @Override
    public void assignSuccessor(ChordIdentifier successor) {
        Peer.getTaskManager().execute(new MessageSender(socket, ChordMessage.createSendSuccessorMessage(key, successor), true));
    }
}
