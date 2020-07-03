package base.messages;

import base.Peer;
import base.chord.ChordIdentifier;

import java.util.UUID;

public class ChordMessage extends BaseMessage {

    protected UUID id;

    public ChordMessage(String ty, int sid, UUID id) {
        super(ty, sid);
        this.id = id;
    }

    public UUID getPeerIdentifier() {
        return id;
    }


    public static BaseMessage createGetSuccessorListMessage() {
        return new BaseMessage( "GET_SUCC_LIST", 0);
    }

    public static BaseMessage createGetAllSuccessorsMessage() {
        return new BaseMessage("GET_ALL_SUCCESSORS", 0);
    }

    public static BaseMessage dummyMessage() {
        return new BaseMessage( "DUMMY", 0);
    }

    public static ChordIdentifierMessage createGetPredecessorMessage() {
        return new ChordIdentifierMessage("GET_PREDECESSOR", 0, null, Peer.getChordManager().getPeerID());
    }

    public static ChordMessage createFindSuccessorMessage(UUID key) {
        return new ChordMessage("FIND_SUCCESSOR", 0, key);
    }

    public static ChordIdentifierMessage createSendSuccessorMessage(UUID key, ChordIdentifier successor) {
        return new ChordIdentifierMessage("SUCCESSOR", 0, successor, new ChordIdentifier(key));
    }

    public static ChordDisconnectMsg createSendDisconnectToPredecessorMessage(ChordIdentifier newSuccessor) {
        return new ChordDisconnectMsg("SUCCESSOR_DISCONNECT", 0, newSuccessor, Peer.getChordManager().getPeerID());
    }

    public static ChordDisconnectMsg createSendDisconnectToSuccessorMessage(ChordIdentifier newPredecessor) {
        return new ChordDisconnectMsg("PREDECESSOR_DISCONNECT", 0, newPredecessor, Peer.getChordManager().getPeerID());
    }

    public static ChordReplyArray createSendSuccListMessage(ChordIdentifier[] succList) {
        return new ChordReplyArray("SUCC_LIST", 0, succList);
    }

    public static ChordReplyArray createSendAllSuccMessage(ChordIdentifier[] succList) {
        return new ChordReplyArray("ALL_SUCC", 0, succList);
    }

    public static ChordIdentifierMessage createSendPredecessorMessage(ChordIdentifier predecessor) {
        return new ChordIdentifierMessage("PREDECESSOR", 0, predecessor, Peer.getChordManager().getPeerID());
    }
}
