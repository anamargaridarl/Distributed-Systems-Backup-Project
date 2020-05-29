package base.Tasks;

import base.Peer;
import base.chord.ChordIdentifier;

import java.util.UUID;

public class RemoveEntryFromAllSuccTable implements Runnable {
    private ChordIdentifier[] successorList;
    private UUID sender;

    public RemoveEntryFromAllSuccTable(UUID sender, ChordIdentifier[] successorList) {
        this.successorList = successorList;
        this.sender = sender;
    }

    @Override
    public void run() {
        Peer.getChordManager().removeAllSuccessorsFromReplyTable(sender, successorList);
    }
}
