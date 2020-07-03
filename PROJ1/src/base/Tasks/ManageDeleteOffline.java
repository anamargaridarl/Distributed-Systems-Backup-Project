package base.Tasks;

import base.TaskLogger;
import base.channels.ChannelManager;
import base.messages.BaseMessage;
import base.messages.MessageChunkNo;

import java.net.UnknownHostException;

import static base.Clauses.ASKDELETE;
import static base.Clauses.STORED;

public class ManageDeleteOffline implements Runnable {

    BaseMessage delete_message;

    public ManageDeleteOffline(String v, int sid) {
        delete_message = new BaseMessage(v, ASKDELETE, sid);
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
        ChannelManager.getCntrChannel().sendMessage(delete_message.createMessageFinal().getBytes());
    }
}
