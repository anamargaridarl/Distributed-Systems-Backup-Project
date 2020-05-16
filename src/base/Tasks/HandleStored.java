package base.Tasks;

import base.Peer;
import base.messages.MessageChunkNo;

import java.net.InetSocketAddress;

/*
    Class that processes stored messages
 */
public class HandleStored implements Runnable {

    private final MessageChunkNo message_store;
    private final InetSocketAddress origin;

    public HandleStored(MessageChunkNo message, InetSocketAddress inetSocketAddress) {
        message_store = message;
        origin = inetSocketAddress;
    }

    @Override
    public void run() {
        //TODO: change function, if sender id is -1, store in references table (NEEDS ADDRESS AND PORT OF THE SENDER)
        //TODO: if sender id >-1, actual rep deg is sender id
        Peer.getStorageManager().handleStoredSendersOccurrence(message_store.getFileId(), message_store.getNumber(), message_store.getSenderId(),origin);
    }
}
