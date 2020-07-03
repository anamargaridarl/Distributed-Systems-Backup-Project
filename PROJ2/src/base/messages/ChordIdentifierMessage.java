package base.messages;

import base.Clauses;
import base.chord.ChordIdentifier;

import java.io.*;

public class ChordIdentifierMessage extends BaseMessage {

    protected ChordIdentifier identifier;
    private ChordIdentifier senderID;

    public ChordIdentifierMessage(String ty, int sid, ChordIdentifier id, ChordIdentifier senderID) {
        super(ty, sid);
        identifier = id;
        this.senderID = senderID;
    }

    public ChordIdentifier getPeerIdentifier() {
        return identifier;
    }

    public ChordIdentifier getSenderID() {
        return senderID;
    }
}
