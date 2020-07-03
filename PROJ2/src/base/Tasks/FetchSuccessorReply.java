package base.Tasks;

import base.Clauses;
import base.Peer;
import base.TaskLogger;
import base.chord.ChordIdentifier;
import base.chord.WaitingForClosestPredecessorReply;

import javax.net.ssl.SSLSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceArray;

import static base.Clauses.WAIT_FOR_REPLY;
import static java.lang.System.exit;

public class FetchSuccessorReply implements Runnable {
    private final UUID key;
    private final WaitingForClosestPredecessorReply waitingForClosestPredecessorReply;
    private int numberOfTries = 0;
    private final SuccessorHandler successorHandler;
    private final int maxRetries;

    public FetchSuccessorReply(UUID key, WaitingForClosestPredecessorReply waitingForClosestPredecessorReply, SSLSocket socket) { //When sending successor back to another peer
        this.key = key;
        this.waitingForClosestPredecessorReply = waitingForClosestPredecessorReply;

        successorHandler = new SendSuccessorToPeer(key, socket);
        maxRetries = Clauses.MAX_RETRIES;
    }

    public FetchSuccessorReply(UUID identifier) { //When joining fingerTable for the first time
        key = identifier;
        waitingForClosestPredecessorReply = null;
        successorHandler = new AssignSuccessorToFingerTable();
        maxRetries = Clauses.MAX_RETRIES * 2;
    }

    public FetchSuccessorReply(UUID successor, WaitingForClosestPredecessorReply waitingForClosestPredecessorReply, AtomicReferenceArray<ChordIdentifier> fingerTable, int position) { //when fixing fingerTable entries
        key = successor;
        this.waitingForClosestPredecessorReply = waitingForClosestPredecessorReply;
        successorHandler = new AssignSuccessorToFingerTable(fingerTable, position);
        maxRetries = Clauses.MAX_RETRIES;
    }

    @Override
    public void run() {
        ChordIdentifier successor = Peer.getChordManager().getSuccessorReply(key);
        if(successor == null) { //If the reply hasn't arrived yet
            numberOfTries++;
            if(numberOfTries < maxRetries) {
                Peer.getTaskManager().schedule(this, WAIT_FOR_REPLY, TimeUnit.MILLISECONDS);
                return;
            }
            else { //if we have timedOut
                if(waitingForClosestPredecessorReply == null) { //In case we haven't joined the fingerTable yet
                    TaskLogger.failedConnection();
                    exit(1);
                    return;
                }

                try { //resume the closestPredecessor lookup
                    successor = Peer.getChordManager().getClosestPredecessor(new ChordIdentifier(key), waitingForClosestPredecessorReply.getClosestSuccessorIndex(), waitingForClosestPredecessorReply.getVisited());
                } catch (WaitingForClosestPredecessorReply forClosestPredecessorReply) { //schedule another lookup for the new Peer reply
                    Peer.getTaskManager().schedule(this, WAIT_FOR_REPLY, TimeUnit.MILLISECONDS);
                    return;
                }
            }
        }

        successorHandler.assignSuccessor(successor);
    }
}
