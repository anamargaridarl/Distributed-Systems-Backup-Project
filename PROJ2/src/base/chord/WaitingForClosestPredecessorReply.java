package base.chord;

import java.util.ArrayList;
import java.util.UUID;

public class WaitingForClosestPredecessorReply extends Throwable {
    private ArrayList<ChordIdentifier> visited;
    private int closestSuccessorIndex;
    private UUID senderID;

    public WaitingForClosestPredecessorReply(ArrayList<ChordIdentifier> visited, int closestSuccessorIndex, UUID senderID) {
        this.visited = visited;
        this.closestSuccessorIndex = closestSuccessorIndex;
        this.senderID = senderID;
    }

    public ArrayList<ChordIdentifier> getVisited() {
        return visited;
    }

    public int getClosestSuccessorIndex() {
        return closestSuccessorIndex;
    }

    public UUID getSenderID() {
        return senderID;
    }
}
