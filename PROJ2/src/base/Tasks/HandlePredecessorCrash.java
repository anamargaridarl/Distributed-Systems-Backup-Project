package base.Tasks;

import base.Peer;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class HandlePredecessorCrash implements Runnable {
  @Override
  public void run() {
    ConcurrentHashMap<String, InetSocketAddress> initiators = Peer.getStorageManager().getBckupInitiators();
    ConcurrentHashMap<String, Set<InetSocketAddress>> successorsStoredSenders = Peer.getStorageManager().getBckupSuccessorsStoredSenders();
    Peer.getStorageManager().getInitiators().putAll(initiators);
    for(Map.Entry<String,Set<InetSocketAddress>> entry : successorsStoredSenders.entrySet()) {
      Set<InetSocketAddress> currentSuccs = Peer.getStorageManager().getSuccessorsStoredSenders().getOrDefault(entry.getKey(),null);
      if(currentSuccs != null) {
        currentSuccs.addAll(entry.getValue());
      } else {
        Peer.getStorageManager().getSuccessorsStoredSenders().putIfAbsent(entry.getKey(),entry.getValue());
      }
    }
    initiators.clear();
    successorsStoredSenders.clear();
  }
}
