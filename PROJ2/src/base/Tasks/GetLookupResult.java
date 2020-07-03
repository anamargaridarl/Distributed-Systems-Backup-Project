package base.Tasks;

import base.Clauses;
import base.Peer;
import base.chord.ChordIdentifier;
import base.chord.WaitingForClosestPredecessorReply;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static base.Clauses.WAIT_FOR_REPLY;

public class GetLookupResult implements Callable<InetSocketAddress> {
    private final UUID key;
    private WaitingForClosestPredecessorReply waitingForClosestPredecessorReply;
    private int numberOfTries = 0;

    public GetLookupResult(UUID key, WaitingForClosestPredecessorReply waitingForClosestPredecessorReply) {
        this.key = key;
        this.waitingForClosestPredecessorReply = waitingForClosestPredecessorReply;
    }

    @Override
    public InetSocketAddress call() throws Exception {
        ChordIdentifier successor = Peer.getChordManager().getSuccessorReply(key);
        if(successor == null) { //If the reply hasn't arrived yet
            numberOfTries++;
            if(numberOfTries < Clauses.MAX_RETRIES) {
                ScheduledFuture<InetSocketAddress> future = Peer.getTaskManager().schedule(this, WAIT_FOR_REPLY, TimeUnit.MILLISECONDS);
                return future.get();
            }
            else { //if we have timedOut
                try { //resume the closestPredecessor lookup
                    successor = Peer.getChordManager().getClosestPredecessor(new ChordIdentifier(key), waitingForClosestPredecessorReply.getClosestSuccessorIndex(), waitingForClosestPredecessorReply.getVisited());
                } catch (WaitingForClosestPredecessorReply forClosestPredecessorReply) { //schedule another lookup for the new Peer reply
                    ScheduledFuture<InetSocketAddress> future = Peer.getTaskManager().schedule(new GetLookupResult(key, waitingForClosestPredecessorReply), WAIT_FOR_REPLY, TimeUnit.MILLISECONDS);
                    return future.get();
                }
            }
        }

        return successor.getOwnerAddress();
    }
}
