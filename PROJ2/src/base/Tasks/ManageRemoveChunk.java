package base.Tasks;

import base.ChunkInfo;
import base.Peer;
import base.channel.MessageSender;
import base.messages.MessageChunkNo;
import base.messages.RemovedMessage;

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

    @Override
    public void run() {
        try {
            UUID chunkHash = hashChunk(removedChunk.getFileId(), removedChunk.getNumber());
            InetSocketAddress allocatedHost = Peer.getChordManager().lookup(chunkHash);
            if (allocatedHost.equals(Peer.getChordManager().getPeerID().getOwnerAddress())) {
                Peer.getTaskManager().execute(new ManageBackupAuxiliar(removedChunk, removedChunk.getChunk()));
            } else {
                RemovedMessage removedMsg = new RemovedMessage(REMOVED, Peer.getID(), removedChunk.getFileId(), removedChunk.getNumber(), removedChunk);
                SSLSocket peerSocket = createSocket(allocatedHost);
                Peer.getTaskManager().execute(new MessageSender(peerSocket, removedMsg));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
