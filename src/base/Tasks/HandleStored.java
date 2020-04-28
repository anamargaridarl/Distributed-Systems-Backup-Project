package base.Tasks;

import base.Peer;
import base.messages.MessageChunkNo;

/*
    Class that processes stored messages
 */
public class HandleStored implements Runnable {

    MessageChunkNo message_store;

    public HandleStored(String[] message) {
        message_store = new MessageChunkNo(message);
    }

    @Override
    public void run() {
        Peer.getStorageManager().handleStoredSendersOccurrence(message_store.getFileId(), message_store.getNumber(), message_store.getSenderId());
    }
}
