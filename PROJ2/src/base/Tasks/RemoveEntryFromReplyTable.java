package base.Tasks;

import base.Peer;
import base.chord.ChordIdentifier;

import java.util.UUID;

public class RemoveEntryFromReplyTable implements Runnable {
    private ChordIdentifier successor;
    private UUID sender;

    public RemoveEntryFromReplyTable(UUID sender, ChordIdentifier successor) {
        this.successor = successor;
        this.sender = sender;
    }

    @Override
    public void run() {
        Peer.getChordManager().removeSuccessorFromReplyTable(sender, successor);
    }
}
