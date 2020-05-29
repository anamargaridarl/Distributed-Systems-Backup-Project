package base.Tasks;

import base.Peer;
import base.channel.MessageSender;
import base.chord.ChordIdentifier;
import base.chord.AskClosestPredecessor;
import base.chord.WaitingForClosestPredecessorReply;
import base.messages.ChordMessage;

import javax.net.ssl.SSLSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static base.Clauses.WAIT_FOR_REPLY;

public class HandleFindSuccessor implements Runnable {
    private final ChordMessage message;
    private final SSLSocket client_socket;

    public HandleFindSuccessor(ChordMessage message, SSLSocket client_socket) {
        this.message = message;
        this.client_socket = client_socket;
    }

    @Override
    public void run() {
        UUID key = message.getPeerIdentifier();
        ChordIdentifier successorOfKey;
        ChordIdentifier identifier = new ChordIdentifier(key);

        try {
            successorOfKey = Peer.getChordManager().findSuccessor(identifier); //Check if it is our successor
        } catch (AskClosestPredecessor searchForPredecessor) { //In case it wasn't out successor and we need to ask our fingerTable entries to look for it
            try {
                successorOfKey = Peer.getChordManager().getClosestPredecessor(identifier, -1, new ArrayList<>()); //if there's no predecessor online we might get the closest successor from the successorList right away
            } catch (WaitingForClosestPredecessorReply waitingForClosestPredecessorReply) { //In case we're asking one of our entries to send us a reply with the successor we are looking for
                Peer.getTaskManager().schedule(new FetchSuccessorReply(key, waitingForClosestPredecessorReply, client_socket), WAIT_FOR_REPLY, TimeUnit.MILLISECONDS);
                return;
            }
        }

        Peer.getTaskManager().execute(new MessageSender(client_socket, ChordMessage.createSendSuccessorMessage(key, successorOfKey), true)); //In case we got the successor right away just send the reply
    }
}
