package base.Tasks;

import base.Peer;
import base.TaskLogger;

import static base.Clauses.*;

/*
    Class that manages incoming messages sent through multicast channels
*/
public class HandleReceivedManager implements Runnable {
    private final String[] msg;

    public HandleReceivedManager(String[] message) {
        this.msg = message;
    }

    @Override
    public void run() {
        try {
            if (Integer.parseInt(msg[2]) == Peer.getID()) {
                TaskLogger.receivedOwnMessage(msg[1]);
                return;
            }
        } catch (NumberFormatException e) {
            TaskLogger.invalidSenderID(msg[2]);
            return;
        }

        switch (msg[1]) {
            case ASKDELETE:
                handleAskDelete();
                break;
            case STORED:
                handleStored();
                break;
            case GETCHUNK:
                handlegetchunk();
                break;
            case DELETE:
                handleDeleteFile();
                break;
            case REMOVED:
                handleRemovedChunk();
                break;
            default:
                TaskLogger.invalidMessage(msg[1]);
                break;
        }
    }

    private void handleAskDelete() {
        Peer.getTaskManager().execute(new HandleDeleteOffline(msg));
    }

    private void handlegetchunk() {
        Peer.getTaskManager().execute(new HandleGetChunk(msg));
    }

    private void handleDeleteFile() {
        Peer.getTaskManager().execute(new HandleDeleteFile(msg));
    }


    private void handleRemovedChunk() {
        Peer.getTaskManager().execute(new HandleRemovedChunk(msg));
    }


    private void handleStored() {
        Peer.getTaskManager().execute(new HandleStored(msg));
    }

}
