package base.Tasks;

import base.TaskLogger;
import base.messages.MessageChunkNo;

import java.net.UnknownHostException;

import static base.Clauses.GETCHUNK;

/*
    Class that manages restore requests as initiator peer
 */
public class ManageGetChunk implements Runnable {

    MessageChunkNo getchunk_message;

    public ManageGetChunk(String version, int sender_id, String file_id, int i) {
        getchunk_message = new MessageChunkNo(version, GETCHUNK, sender_id, file_id, i);
    }

    public void processMessage() throws UnknownHostException {
        //TODO: send to client channel
        //ChannelManager.getCntrChannel().sendMessage(getchunk_message.createMessageFinal().getBytes());
    }

    @Override
    public void run() {
        try {
            processMessage();
        } catch (UnknownHostException e) {
            TaskLogger.sendMessageFail();
        }
    }
}
