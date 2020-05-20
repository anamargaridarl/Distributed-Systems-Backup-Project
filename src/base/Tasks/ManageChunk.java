package base.Tasks;

import base.Peer;
import base.channel.MessageSender;
import base.messages.RestoreMessage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import static base.Clauses.CHUNK;

public class ManageChunk implements Runnable {

    private final RestoreMessage restore_message;
    private final Socket client_socket;

    public ManageChunk(String version, int sender_id, String file_id, int i, int numchunks, byte[] body, Socket client_socket) {
        restore_message = new RestoreMessage(version, CHUNK, sender_id, file_id, i,numchunks, body);
        this.client_socket = client_socket;
    }

    @Override
    public void run() {
        Peer.getTaskManager().execute(new MessageSender(client_socket,restore_message));
    }
}