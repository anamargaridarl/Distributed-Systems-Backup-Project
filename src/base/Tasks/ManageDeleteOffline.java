package base.Tasks;

import base.TaskLogger;
import base.messages.BaseMessage;

import java.net.UnknownHostException;

import static base.Clauses.ASKDELETE;

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
        //TODO: send to client channel
        //ChannelManager.getCntrChannel().sendMessage(delete_message.createMessageFinal().getBytes());
    }
}
