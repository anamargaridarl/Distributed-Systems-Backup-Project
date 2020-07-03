package base.Tasks;

import base.TaskLogger;
import base.channels.ChannelManager;
import base.messages.BackupMessage;
import base.messages.MessageChunkNo;

import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;

import static base.Clauses.GETCHUNK;
import static base.Clauses.PUTCHUNK;

/*
    Class that manages restore requests as initiator peer
 */
public class ManageGetChunk implements Runnable {

    MessageChunkNo getchunk_message;

    public ManageGetChunk(String version, int sender_id, String file_id, int i) {
        getchunk_message = new MessageChunkNo(version, GETCHUNK, sender_id, file_id, i);
    }

    public void processMessage() throws UnknownHostException {
        ChannelManager.getCntrChannel().sendMessage(getchunk_message.createMessageFinal().getBytes());
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
