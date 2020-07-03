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

public class GetAllSuccessorsResult implements Callable<ChordIdentifier[]> {
    private final UUID sender;
    private int numberOfTries = 0;

    public GetAllSuccessorsResult(UUID sender) {
        this.sender = sender;
    }

    @Override
    public ChordIdentifier[] call() throws Exception {
        ChordIdentifier[] allSuccessors = Peer.getChordManager().getAllSuccessorsReply(sender);
        if(allSuccessors == null) { //If the reply hasn't arrived yet
            numberOfTries++;
            if(numberOfTries < Clauses.MAX_RETRIES) {
                ScheduledFuture<ChordIdentifier[]> future = Peer.getTaskManager().schedule(this, WAIT_FOR_REPLY, TimeUnit.MILLISECONDS);
                return future.get();
            }
            else return null;
        }

        return allSuccessors;
    }
}
