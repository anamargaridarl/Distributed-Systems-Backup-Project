package base.messages;

import base.Peer;

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static base.Clauses.BACKUPTABLES;

public class BackupTablesMessage extends BaseMessage {
    private final ConcurrentHashMap<String, InetSocketAddress> bckup_stored_senders;
    private final ConcurrentHashMap<String, Set<InetSocketAddress>> bckup_succ_senders;
    private final ConcurrentHashMap<String, InetSocketAddress> bckup_initiators;

    public BackupTablesMessage() {
        super(BACKUPTABLES, Peer.getID());
        bckup_stored_senders = Peer.getStorageManager().getStoredSenders();
        bckup_succ_senders = Peer.getStorageManager().getSuccessorsStoredSenders();
        bckup_initiators = Peer.getStorageManager().getInitiators();
    }

    public ConcurrentHashMap<String, InetSocketAddress> getBckupStoredSenders() {
        return bckup_stored_senders;
    }

    public ConcurrentHashMap<String, Set<InetSocketAddress>> getBckupSuccSenders() {
        return bckup_succ_senders;
    }

    public ConcurrentHashMap<String, InetSocketAddress> getBckupInitiators() {
        return bckup_initiators;
    }
}
