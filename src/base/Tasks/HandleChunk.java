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

    private RestoreMessage restore_message;
    private static Socket socket;
    private InetAddress host;

    public HandleChunk(String[] message, byte[] body, InetAddress address) {
        restore_message = new RestoreMessage(message, body);
        host = address;
    }

    public void readTCPChunk() throws Exception {
        DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[MAX_SIZE + 100];
        baos.write(buffer, 0, in.read(buffer));
        restore_message.setBody(baos.toByteArray());
    }

    @Override
    public void run() {
        if (Peer.getStorageManager().existsRestoreRequest(restore_message.getFileId(), Peer.getID())) {
            if (restore_message.getVersion().equals(ENHANCED_VERSION)) {
                int port = ByteBuffer.wrap(restore_message.getBody()).getInt();
                try {
                    socket = new Socket(host, port);
                    readTCPChunk();
                } catch (IOException e) {
                    TaskLogger.chunkTCPSocketFail();
                } catch (Exception e) {
                    TaskLogger.chunkTCPReadFail();
                }
            }
            Peer.getStorageManager().addRestoredChunkRequest(restore_message.getFileId(), restore_message.getNumber(), restore_message.getBody());
        }
    }
}
