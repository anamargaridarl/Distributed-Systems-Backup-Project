package base.Tasks;

import base.Peer;
import base.TaskLogger;
import base.messages.MessageChunkNo;
import base.messages.RestoreMessage;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import static base.Clauses.ENHANCED_VERSION;
import static base.Clauses.MAX_SIZE;

public class HandleChunk implements Runnable {

    private final RestoreMessage restore_message;

    public HandleChunk(RestoreMessage restore) {
        restore_message = restore;
    }

    @Override
    public void run() {
            Peer.getStorageManager().addRestoredChunkRequest(restore_message.getFileId(), restore_message.getNumber(), restore_message.getBody());
    }
}
