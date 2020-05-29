package base;

import base.chord.ChordIdentifier;

import java.util.UUID;

public class ChordLogger extends Logger {

    public static void startChord() {
        success("Started Chord Ring");
    }

    public static void joinFingerTableFail() {
        error("The known Peer Address inserted didn't respond, please try again later, or try connecting to another known Address");
    }

    public static void tryingConnection(String host, int port) {
        warning("...Trying to connect to " + host + ":" + port);
    }

    public static void successConnection() {
        success("Connected to the Chord Ring!");
    }

    public static void getSuccessorFail() {
        warning("Trying to get successor for the desired key");
    }

    public static void getListSuccessors(UUID identifier) {
        warning("trying to get listOfAll Successors from the peer: " + identifier);
    }

}


