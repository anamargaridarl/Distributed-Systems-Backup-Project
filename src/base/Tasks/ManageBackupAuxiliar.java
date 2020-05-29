package base.Tasks;

import base.ChunkInfo;
import base.Peer;
import base.TaskLogger;
import base.chord.ChordIdentifier;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import static base.Clauses.*;

public class ManageBackupAuxiliar implements Runnable {

  private int offset;
  private final SSLSocket initiatorSocket;
  private final ChunkInfo chunkInfo;
  private final byte[] chunk;
  private int nTry;

  public ManageBackupAuxiliar(ChunkInfo chunk_info, byte[] chunk, SSLSocket initiatorSocket) {
    this.chunk = chunk;
    this.chunkInfo = chunk_info;
    this.initiatorSocket = initiatorSocket;
    nTry = 0;
    offset = 0;
  }

  public ManageBackupAuxiliar(ChunkInfo chunkInfo, byte[] chunk) {
    this(chunkInfo,chunk,null);
  }

  @Override
  public void run() {
    int currRep =  Peer.getStorageManager().getChunkRepDegree(chunkInfo.getFileId(), chunkInfo.getNumber());
    int succNeeded = chunkInfo.getRepDeg() - currRep;
    ChordIdentifier[] successors = Peer.getChordManager().getAllSuccessors(succNeeded, offset);
    if(succNeeded > 0 && successors.length < succNeeded) {
      TaskLogger.repDegreeNotEnough();
    }

    if (succNeeded > 0 && nTry < 3) {
      for (ChordIdentifier succID : successors) {
        try {
          InetSocketAddress succ = Peer.getChordManager().lookup(succID.getIdentifier());
          SSLSocket sock = createSocket(succ);
          Peer.getTaskManager().execute(new ManagePutChunk(NOT_INITIATOR, chunkInfo.getFileId(), chunkInfo.getNumber(), chunkInfo.getRepDeg(), chunkInfo.getNumber_chunks(), chunk, sock));
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      nTry++;
      offset += succNeeded;
      Peer.getTaskManager().schedule(this, TIMEOUT, TimeUnit.MILLISECONDS);
    } else if(initiatorSocket != null) {
        Peer.getTaskManager().execute(new ManageStored(currRep,chunkInfo.getFileId(),chunkInfo.getNumber(),initiatorSocket));
    } else if(succNeeded == 0){
      TaskLogger.putChunkOk(chunkInfo.getFileId(),chunkInfo.getNumber());
    } else {
      TaskLogger.putChunkFail(chunkInfo.getFileId(),chunkInfo.getNumber());
    }
  }
}
