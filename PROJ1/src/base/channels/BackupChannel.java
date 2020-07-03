package base.channels;

import base.Peer;
import base.TaskLogger;
import base.Tasks.HandlePutChunk;
import base.messages.BackupMessage;
import base.messages.Message;
import base.messages.MessageChunkNo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import static base.Clauses.PUTCHUNK;
import static base.Clauses.makeChunkRef;

public class BackupChannel extends Channel {

    private final ConcurrentHashMap<String, Future> requestedPutchunkMessages = new ConcurrentHashMap<>();

    public BackupChannel(String mdb_addr, int mdr_port) throws IOException {
        super(mdb_addr, mdr_port);
    }

    @Override
    public void parseMessage(DatagramPacket packet) throws IOException {
        byte[] buf = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
        List<byte[]> message_parts = MessageChunkNo.separateHeaderAndBody(buf);
        String[] header_args = MessageChunkNo.parseHeader(message_parts.get(0));
        BackupMessage bck_msg = new BackupMessage(header_args, message_parts.get(1));
        if (bck_msg.getSenderId() == Peer.getID()) {
            TaskLogger.receivedOwnMessage(PUTCHUNK);
            return;
        }
        cancelPendingBackupProtocol(bck_msg);
        Peer.getTaskManager().execute(new HandlePutChunk(bck_msg)); //new task to receive message and handle it
    }

    private void cancelPendingBackupProtocol(BackupMessage bck_msg) {
        String chunk_ref = makeChunkRef(bck_msg.getFileId(), bck_msg.getNumber());
        Future pendingBackup = requestedPutchunkMessages.get(chunk_ref);
        if (pendingBackup == null)
            return;
        TaskLogger.enhancedDuplicateDetected();
        pendingBackup.cancel(true);
        requestedPutchunkMessages.remove(chunk_ref);
    }

    public void registerPutChunkMessage(String file_id, int number, Future f) {
        String chunk_ref = makeChunkRef(file_id, number);
        requestedPutchunkMessages.put(chunk_ref, f);
    }
}
