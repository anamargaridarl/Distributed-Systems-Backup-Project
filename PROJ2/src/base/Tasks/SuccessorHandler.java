package base.Tasks;

import base.chord.ChordIdentifier;

public interface SuccessorHandler {
    void assignSuccessor(ChordIdentifier successor); //This should get a successor and do with it what is most appropriate (send to another peer or store in the desired location)
}
