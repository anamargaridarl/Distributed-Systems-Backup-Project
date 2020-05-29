package base.Tasks;

import base.Peer;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class HandleReceivedTables implements Runnable {
    private final ConcurrentHashMap<String, InetSocketAddress> newInitiators;
    private final ConcurrentHashMap<String, Set<InetSocketAddress>> newSuccessors;

    public HandleReceivedTables(ConcurrentHashMap<String, InetSocketAddress> newInitiators, ConcurrentHashMap<String, Set<InetSocketAddress>> newSuccessors) {
        this.newInitiators = newInitiators;
        this.newSuccessors = newSuccessors;
    }

    @Override
    public void run() {
        Peer.getStorageManager().getInitiators().putAll(newInitiators);
        for (Map.Entry<String, Set<InetSocketAddress>> entry : newSuccessors.entrySet()) {
            Set<InetSocketAddress> currentSuccs = Peer.getStorageManager().getSuccessorsStoredSenders().getOrDefault(entry.getKey(), null);
            if (currentSuccs != null) {
                currentSuccs.addAll(entry.getValue());
            } else {
                Peer.getStorageManager().getSuccessorsStoredSenders().putIfAbsent(entry.getKey(), entry.getValue());
            }
        }
    }
}
