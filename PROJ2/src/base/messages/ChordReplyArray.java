package base.messages;

import base.Peer;
import base.chord.ChordIdentifier;

import java.util.UUID;

public class ChordReplyArray extends BaseMessage {

    protected ChordIdentifier[] identifiers;
    protected UUID sender;

    public ChordReplyArray(String ty, int sid, ChordIdentifier[] ids) {
        super(ty, sid);
        identifiers = ids;
        sender = Peer.getChordManager().getPeerID().getIdentifier();
    }

    public ChordIdentifier[] getSuccList() {
        return identifiers;
    }

    public UUID getSender() {
        return sender;
    }

}
