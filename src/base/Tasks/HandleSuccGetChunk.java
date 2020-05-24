package base.Tasks;

import base.Peer;
import base.messages.MessageChunkNo;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.lang.invoke.VarHandle;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import static base.Clauses.VANILLA_VERSION;
import static base.Clauses.createSocket;

public class HandleSuccGetChunk implements Runnable {
  private final MessageChunkNo succGetChunk;
  private final SSLSocket client_socket;

  public HandleSuccGetChunk(MessageChunkNo succGetChunk, SSLSocket client_socket) {
    this.succGetChunk = succGetChunk;
    this.client_socket = client_socket;
  }

  @Override
  public void run() {
    byte[] body;
    int num_chunks = 0;
    try {
      body = Peer.getStorageManager().getChunkData(succGetChunk.getFileId(), succGetChunk.getNumber());
      num_chunks = Peer.getStorageManager().getNumChunk(succGetChunk.getFileId(), succGetChunk.getNumber());
      Peer.getTaskManager().schedule(new ManageChunk(succGetChunk.getVersion(), Peer.getID(), succGetChunk.getFileId(), succGetChunk.getNumber(), num_chunks, body, client_socket)
          , 500, TimeUnit.MILLISECONDS);
    } catch (IOException e) {
      System.out.println("ERROR: Chunk not found. File ID: " + succGetChunk.getFileId() + " Nr: " + succGetChunk.getNumber()); //TODO: add to task logger
      InetSocketAddress nextSucc = Peer.getStorageManager().getSuccGetChunk(succGetChunk);
      Peer.getTaskManager().execute(new ManageDeclined(VANILLA_VERSION,Peer.getID(),succGetChunk.getFileId(),succGetChunk.getNumber(),client_socket,nextSucc));
    }
  }
}
