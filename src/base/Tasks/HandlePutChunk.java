package base.Tasks;

import base.ChunkInfo;
import base.Peer;
import base.TaskLogger;
import base.messages.BackupMessage;

import java.net.Socket;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static base.Clauses.ENHANCED_VERSION;
import static base.Clauses.MAX_DELAY_STORED;

/*
    Class that handles PutChunk subprotocol messages
 */
public class HandlePutChunk implements Runnable {

    private final String version;
    private final int sender_id;
    private final ChunkInfo chunk_info;
    private final byte[] chunk;
    private final Socket client_socket;

    public HandlePutChunk(BackupMessage message, Socket socket) {
        this.sender_id = message.getSenderId();
        this.version = message.getVersion();
        this.chunk_info = new ChunkInfo(message.getFileId(), message.getReplicationDeg(), message.getBody().length, message.getNumber());
        this.chunk = message.getBody();
        this.client_socket = socket;
    }

    @Override
    public void run() {
        //check in storage if the file belongs to the peer
        if (Peer.getStorageManager().ownsFile(chunk_info.getFileId())) {
            TaskLogger.ownsFile(chunk_info.getFileId());
            return;
        }
        //check if the peer already has the chunk
        if (Peer.getStorageManager().existsChunk(chunk_info)) {
            TaskLogger.alreadyBackedUp(Peer.getID(), chunk_info.getFileId(), chunk_info.getNumber());
            Peer.getTaskManager().schedule(new ManageStored(version, Peer.getID(), chunk_info.getFileId(), chunk_info.getNumber(),client_socket), new Random().nextInt(MAX_DELAY_STORED), TimeUnit.MILLISECONDS);
            return;
        }
        //check if there is space enough in the storage
        if (!Peer.getStorageManager().hasEnoughSpace(chunk_info.getSize())) {
            TaskLogger.insufficientSpaceFail(chunk_info.getSize());
            return;
        }

        Peer.getStorageManager().addStoredChunkRequest(chunk_info.getFileId(), chunk_info.getNumber());
        if (version.equals(ENHANCED_VERSION)) {
            Peer.getTaskManager().schedule(new Thread(() -> {
                int curr_rep_deg = Peer.getStorageManager().getStoredSendersOccurrences(chunk_info.getFileId(), chunk_info.getNumber());
                if (curr_rep_deg < chunk_info.getRepDeg()) {
                    TaskLogger.enhanceStoreChunk();
                    processStore(true);
                } else {
                    TaskLogger.enhanceStoreChunkOk();
                    Peer.getStorageManager().removeStoredOccurrenceChunk(chunk_info.getFileId(), chunk_info.getNumber());
                }
            }), new Random().nextInt(MAX_DELAY_STORED), TimeUnit.MILLISECONDS);

        } else {
            processStore(false);
        }
    }

    private void processStore(boolean isEnhanced) {
        boolean stored = Peer.getStorageManager().storeChunk(chunk_info, chunk);
        if (stored) {
            TaskLogger.storedChunkOk(chunk_info.getFileId(), chunk_info.getNumber(), chunk_info.getSize());
        } else {
            TaskLogger.storedChunkFail(chunk_info.getFileId(), chunk_info.getNumber());
            Peer.getStorageManager().removeStoredOccurrenceChunk(chunk_info.getFileId(), chunk_info.getNumber());
            return;
        }

        if (isEnhanced) {
            Peer.getStorageManager().saveStoredAsRepDegree(chunk_info.getFileId(),chunk_info.getNumber());
            Peer.getTaskManager().execute(new ManageStored(version, Peer.getID(), chunk_info.getFileId(), chunk_info.getNumber(),client_socket));
        } else {
            Peer.getTaskManager().schedule(new ManageStored(version, Peer.getID(), chunk_info.getFileId(), chunk_info.getNumber(),client_socket), new Random().nextInt(MAX_DELAY_STORED), TimeUnit.MILLISECONDS);
        }
    }

}
