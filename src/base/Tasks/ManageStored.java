package base.Tasks;

import static base.Clauses.STORED;

import base.TaskLogger;
import base.messages.MessageChunkNo;

import java.net.Socket;
import java.net.UnknownHostException;

public class ManageStored implements Runnable {

    MessageChunkNo st_message;

    public ManageStored(String v, int sid, String fid, int chunkno, Socket socket) {
        st_message = new MessageChunkNo(v, STORED, sid, fid, chunkno);
    }

    @Override
    public void run() {
        try {
            processMessage();
        } catch (UnknownHostException e) {
            TaskLogger.sendMessageFail();
        }
    }

    public void processMessage() throws UnknownHostException {
        //TODO: send to client channel
        //ChannelManager.getCntrChannel().sendMessage(st_message.createMessageFinal().getBytes());
    }
}
