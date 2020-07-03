package base.Tasks;

import base.chord.ChordManager;

public class ChordStabilizer implements Runnable {
    private ChordManager chordManager;

    public ChordStabilizer(ChordManager chordManager) {
        this.chordManager = chordManager;
    }

    @Override
    public void run() {
            chordManager.stabilize();
            chordManager.fixFingerTable();
            chordManager.fixSuccessorList();
            chordManager.checkPredecessor();
    }
}
