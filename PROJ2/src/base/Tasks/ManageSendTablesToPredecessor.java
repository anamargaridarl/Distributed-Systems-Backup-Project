package base.Tasks;

import base.Clauses;
import base.Peer;
import base.channel.MessageSender;
import base.chord.ChordIdentifier;
import base.messages.TablesToSendMessage;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ManageSendTablesToPredecessor implements Runnable {
    private ChordIdentifier predecessorID;

    public ManageSendTablesToPredecessor(ChordIdentifier predecessorID) {
        this.predecessorID = predecessorID;
    }

    @Override
    public void run() {
        ConcurrentHashMap<String, InetSocketAddress> initiators = Peer.getStorageManager().getInitiators();
        ConcurrentHashMap<String, Set<InetSocketAddress>> successorsStoredSenders = Peer.getStorageManager().getSuccessorsStoredSenders();
        ConcurrentHashMap<String, InetSocketAddress> initiatorsToSend = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, Set<InetSocketAddress>> successorsToSend = new ConcurrentHashMap<>();

        for (Iterator<Map.Entry<String, InetSocketAddress>> iterator = initiators.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, InetSocketAddress> entry = iterator.next();

            ChordIdentifier chunkID = new ChordIdentifier(Clauses.hashChunk(entry.getKey()));
            if (chunkID.isBetween(Peer.getChordManager().getPeerID(), predecessorID)) {
                initiatorsToSend.put(entry.getKey(), entry.getValue());
                iterator.remove();
            }
        }

        for (Iterator<Map.Entry<String, Set<InetSocketAddress>>> iterator = successorsStoredSenders.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, Set<InetSocketAddress>> entry = iterator.next();

            ChordIdentifier chunkID = new ChordIdentifier(Clauses.hashChunk(entry.getKey()));
            if (chunkID.isBetween(Peer.getChordManager().getPeerID(), predecessorID)) {
                Set<InetSocketAddress> successorsSet = entry.getValue();
                if (Peer.getStorageManager().existsChunk(entry.getKey())) {
                    InetSocketAddress address = Peer.getChordManager().getPeerID().getOwnerAddress();
                    successorsSet.add(address);
                }
                successorsToSend.put(entry.getKey(), successorsSet);
                iterator.remove();
            }
        }

        //Send those tables to our predecessor
        SSLSocket socket;
        try {
            socket = Clauses.createSocket(predecessorID.getOwnerAddress());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Peer.getTaskManager().execute(new MessageSender(socket, new TablesToSendMessage(initiatorsToSend, successorsToSend)));
    }
}
