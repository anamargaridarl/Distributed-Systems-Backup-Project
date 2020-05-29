package base.messages;

import base.chord.ChordIdentifier;


public class ChordDisconnectMsg extends BaseMessage {

    protected ChordIdentifier identifier;
    protected ChordIdentifier senderIdentifier;

    public ChordDisconnectMsg(String ty, int sid, ChordIdentifier id, ChordIdentifier senderId) {
        super(ty, sid);
        identifier = id;
        senderIdentifier = senderId;
    }

    public ChordIdentifier getPeerIdentifier() {
        return identifier;
    }

    public ChordIdentifier getSenderIdentifier() {
        return senderIdentifier;
    }


}
