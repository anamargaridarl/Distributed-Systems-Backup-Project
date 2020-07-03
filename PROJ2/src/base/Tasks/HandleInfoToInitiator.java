package base.Tasks;

import base.Peer;
import base.messages.InfoMessage;
import base.messages.MessageChunkNo;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import static base.Clauses.createSocket;

public class HandleInfoToInitiator implements Runnable {


    private InfoMessage info_message;

    public HandleInfoToInitiator(InfoMessage message) {
        info_message = message;
    }

    @Override
    public void run() {
        try {
            SSLSocket socket = createSocket(info_message.getAddress());
            Peer.getTaskManager().execute(new ManageGetChunk(info_message.getSenderId(), info_message.getFileId(), info_message.getNumber(), socket));
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
