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
            if(sender_id != NOT_INITIATOR) {
                //TODO: create task to start backup for the successors until rep deg is matched(*) and store its references and reply with stored
            }
        }
        //check if the peer already has the chunk TODO: or has references of the same
        else if (Peer.getStorageManager().existsChunk(chunk_info)) {
            TaskLogger.alreadyBackedUp(Peer.getID(), chunk_info.getFileId(), chunk_info.getNumber());
            if(sender_id != NOT_INITIATOR) {
                //TODO: create task to check rep deg and reply with stored if is achived or start backup for the succesors until new rep deg is matched(*) and then replied with stored
            }
        }
        //check if there is space enough in the storage
        else if (!Peer.getStorageManager().hasEnoughSpace(chunk_info.getSize())) {
            TaskLogger.insufficientSpaceFail(chunk_info.getSize());
            if(sender_id != NOT_INITIATOR) {
                //TODO: create task to start backup for the successros until rep deg is matched(*) and store its references and reply with stored
            }
        } else {
            Peer.getStorageManager().addStoredChunkRequest(chunk_info.getFileId(), chunk_info.getNumber());
            processStore();
            return;
        }

        if(sender_id == NOT_INITIATOR) {
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

        if(sender_id == NOT_INITIATOR) {
            Peer.getTaskManager().execute(new ManageStored(version, NOT_INITIATOR, chunk_info.getFileId(), chunk_info.getNumber(), client_socket));
        } else {
            //TODO: if sender id >-1 rep deg > 1, send messages to successors to achieve desired rep deg(*) and then reply with stored with actual rep deg in the sender id
        }
    }

    //TODO: (*) - same procedure in each case, send putchunk for successors and store the references

}
