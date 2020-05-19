package base.Tasks;

import base.ChunkInfo;
import base.Peer;
import base.TaskLogger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import static base.Clauses.*;

public class ManageBackupAuxiliar implements Runnable {

  private Socket initiatorSocket;
  private final ChunkInfo chunkInfo;
  private final byte[] chunk;
  private int nSuccsTried;

  public ManageBackupAuxiliar(ChunkInfo chunk_info, byte[] chunk, Socket initiatorSocket) {
    this.chunk = chunk;
    this.chunkInfo = chunk_info;
    this.initiatorSocket = initiatorSocket;
    nSuccsTried = 0;
  }

  public ManageBackupAuxiliar(ChunkInfo chunkInfo, byte[] chunk) {
    this(chunkInfo,chunk,null);
  }

  @Override
  public void run() {
    int currRep =  Peer.getStorageManager().getChunkRepDegree(chunkInfo.getFileId(), chunkInfo.getNumber());
    int succNeeded = chunkInfo.getRepDeg() - currRep;
    int nSuccessors = succNeeded < chord.size() ? succNeeded : 0;

    if (nSuccessors > 0 && nSuccsTried < chord.size()) { //TODO: use a better way to capsize tries
      for (int i = 1; i <= nSuccessors; i++) {
        if (nSuccsTried > chord.size()) {
          return;
        }
        try {
          //TODO: substitute with chord get Next successor
          InetSocketAddress succ = chord.get(((Peer.getID() + nSuccsTried + i - 1) % 3 * 3));
          Socket sock = createSocket(succ);
          Peer.getTaskManager().execute(new ManagePutChunk(VANILLA_VERSION, NOT_INITIATOR, chunkInfo.getFileId(), chunkInfo.getNumber(), chunkInfo.getRepDeg(), chunkInfo.getNumber_chunks(), chunk, sock));
          nSuccsTried++;
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      Peer.getTaskManager().schedule(this, 2*TIMEOUT, TimeUnit.MILLISECONDS);
    } else if(initiatorSocket != null) {
      Peer.getTaskManager().execute(new ManageStored(Peer.getVersion(),currRep,chunkInfo.getFileId(),chunkInfo.getNumber(),initiatorSocket));
    } else if(nSuccsTried> 0 && succNeeded == 0){
      TaskLogger.putChunkOk();
    }
  }
}
