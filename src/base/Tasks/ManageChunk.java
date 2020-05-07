package base.Tasks;

import base.TaskLogger;
import base.messages.RestoreMessage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import static base.Clauses.CHUNK;
import static base.Clauses.ENHANCED_VERSION;

public class ManageChunk implements Runnable {

    private RestoreMessage restore_message;
    private static ServerSocket server_socket;
    private static Socket client_socket;
    private Integer port;

    public ManageChunk(String version, int sender_id, String file_id, int i, byte[] body) {
        restore_message = new RestoreMessage(version, CHUNK, sender_id, file_id, i, body);
    }

    public void sendModifiedChunk() {
        try {
            byte[] request = restore_message.createByteModifiedMessage(port);
            ChannelManager.getRstrChannel().sendMessage(request);
        } catch (IOException e) {
            TaskLogger.sendMessageFail();
        }
    }

    public void createServerSocket() {
        try {
            server_socket = new ServerSocket(0);
            this.port = this.server_socket.getLocalPort();
        } catch (IOException e) {
            TaskLogger.chunkTCPSocketFail();
        }
    }

    public void acceptClientSocket() {
        try {
            client_socket = server_socket.accept();
        } catch (IOException e) {
            TaskLogger.chunkTCPAcceptSocketFail();
        }
    }


    public void sendTCPChunk() {
        try {
            DataOutputStream out = new DataOutputStream(client_socket.getOutputStream());
            byte[] message = restore_message.getBody();
            out.write(message, 0, message.length);
            server_socket.close();
        } catch (IOException e) {
            TaskLogger.chunkTCPWriteFail();
        }
    }

    @Override
    public void run() {
        if (restore_message.getVersion().equals(ENHANCED_VERSION)) {
            createServerSocket();
            sendModifiedChunk();
            acceptClientSocket();
            sendTCPChunk();
        } else {
            try {
                ChannelManager.getRstrChannel().sendMessage(restore_message.createByteMessage());
            } catch (IOException e) {
                TaskLogger.sendMessageFail();
            }
        }

    }
}
