package base.Tasks;

import base.Peer;
import base.channel.MessageSender;
import base.chord.ChordIdentifier;
import base.messages.ChordMessage;

import javax.net.ssl.SSLSocket;

public class HandleGetPredecessor implements Runnable {
    private SSLSocket client_socket;
    private ChordIdentifier sender;

    public HandleGetPredecessor(SSLSocket client_socket, ChordIdentifier sender) {
        this.client_socket = client_socket;
        this.sender = sender;
    }

    @Override
    public void run() {
        Peer.getChordManager().tryNewPredecessor(sender);
        ChordIdentifier predecessor = Peer.getChordManager().getPredecessor();
        Peer.getTaskManager().execute(new MessageSender(client_socket, ChordMessage.createSendPredecessorMessage(predecessor), true));
    }
}
