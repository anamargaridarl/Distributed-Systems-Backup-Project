package base.Tasks;

import base.ChunkInfo;
import base.Peer;
import base.TaskLogger;
import base.messages.BackupMessage;

import java.net.Socket;

import static base.Clauses.NOT_INITIATOR;

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
        this.chunk_info = new ChunkInfo(message.getFileId(), message.getReplicationDeg(), message.getBody().length, message.getNumber(), message.getNumberChunks());
        this.chunk = message.getBody();
        this.client_socket = socket;
    }

    @Override
    public void run() {
        //check in storage if the file belongs to the peer
        if (Peer.getStorageManager().ownsFile(chunk_info.getFileId())) {
            TaskLogger.ownsFile(chunk_info.getFileId());
        }
        //check if the peer already has the chunk TODO: or has references of the same
        else if (Peer.getStorageManager().existsChunk(chunk_info)) {
            TaskLogger.alreadyBackedUp(Peer.getID(), chunk_info.getFileId(), chunk_info.getNumber());
        }
        //check if there is space enough in the storage
        else if (!Peer.getStorageManager().hasEnoughSpace(chunk_info.getSize())) {
            TaskLogger.insufficientSpaceFail(chunk_info.getSize());
        } else {
            processStore();
            return;
        }

        if (sender_id != NOT_INITIATOR) {
            Peer.getTaskManager().execute(new ManageBackupAuxiliar(chunk_info,chunk, client_socket));
        } else {
            Peer.getTaskManager().execute(new ManageDeclined(version, NOT_INITIATOR,chunk_info.getFileId(), chunk_info.getNumber(),client_socket));
        }

    }

    private void processStore() {
        boolean stored = Peer.getStorageManager().storeChunk(chunk_info, chunk);
        if (stored) {
            TaskLogger.storedChunkOk(chunk_info.getFileId(), chunk_info.getNumber(), chunk_info.getSize());
        } else {
            TaskLogger.storedChunkFail(chunk_info.getFileId(), chunk_info.getNumber());
            Peer.getStorageManager().removeStoredOccurrenceChunk(chunk_info.getFileId(), chunk_info.getNumber());
            return;
        }

        if (sender_id != NOT_INITIATOR) {
            Peer.getTaskManager().execute(new ManageBackupAuxiliar(chunk_info,chunk, client_socket));
        } else {
            Peer.getTaskManager().execute(new ManageStored(version, NOT_INITIATOR, chunk_info.getFileId(), chunk_info.getNumber(), client_socket));
        }
    }
}
