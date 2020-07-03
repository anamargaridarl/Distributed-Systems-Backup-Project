package base.Tasks;

import base.Peer;
import base.messages.MessageChunkNo;


/*
    Class that processes stored messages
 */
public class HandleStored implements Runnable {

    private final MessageChunkNo message_store;

    public HandleStored(MessageChunkNo message) {
        message_store = message;
    }

    @Override
    public void run() {
        Peer.getStorageManager().handleStoredSendersOccurrence(message_store.getFileId(), message_store.getNumber(), message_store.getSenderId(),message_store.getOrigin());
    }
}
