package base.Tasks;

import base.ChunkInfo;
import base.Peer;
import base.channel.MessageSender;
import base.messages.MessageChunkNo;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import static base.Clauses.*;

public class ManageRemoveChunk implements Runnable {

  private final ChunkInfo removedChunk;

  public ManageRemoveChunk(ChunkInfo removed) {
    removedChunk = removed;
  }

  //TODO: get info about who to send removed message
  @Override
  public void run() {
    try {
      UUID chunkHash = hashChunk(removedChunk.getFileId(), removedChunk.getNumber());
      Integer hashKey = getHashKey(chunkHash);
      int allocated = checkAllocated(hashKey);
      if (allocated == Peer.getID()) {
        Peer.getTaskManager().execute(new ManageBackupAuxiliar(removedChunk, removedChunk.getChunk()));
      } else {
        MessageChunkNo removedMsg = new MessageChunkNo(VANILLA_VERSION, REMOVED, Peer.getID(), removedChunk.getFileId(), removedChunk.getNumber());
        InetSocketAddress idealPeer = chord.get((allocated-1)*2); //TODO: replace with CHORD methods to obtain peer address
        SSLSocket peerSocket = createSocket(idealPeer);
        Peer.getTaskManager().execute(new MessageSender(peerSocket, removedMsg));
      }
    } catch (NoSuchAlgorithmException | IOException e) {
      e.printStackTrace();
    }
  }
}
