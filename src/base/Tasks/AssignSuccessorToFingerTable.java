package base.Tasks;

import base.Peer;
import base.chord.ChordIdentifier;

import java.util.concurrent.atomic.AtomicReferenceArray;

public class AssignSuccessorToFingerTable implements SuccessorHandler {
    private AtomicReferenceArray<ChordIdentifier> fingerTable;
    private int position;
    private boolean joinFingerTable;

    public AssignSuccessorToFingerTable() {
        joinFingerTable = true;
    }

    public AssignSuccessorToFingerTable(AtomicReferenceArray<ChordIdentifier> fingerTable, int position) {
        this.fingerTable = fingerTable;
        this.position = position;
        joinFingerTable = false;
    }

    @Override
    public void assignSuccessor(ChordIdentifier successor) {
        if(joinFingerTable) Peer.getChordManager().joinFingerTable(successor);
        else fingerTable.set(position,successor);
    }
}
