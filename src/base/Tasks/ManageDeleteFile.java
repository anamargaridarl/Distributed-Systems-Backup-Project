package base.Tasks;

import base.Peer;
import base.TaskLogger;
import base.messages.Message;

import java.net.UnknownHostException;

import static base.Clauses.DELETE;
import static base.Clauses.ENHANCED_VERSION;

public class ManageDeleteFile implements Runnable {

    Message msg_delete;

    public ManageDeleteFile(String version, int peer_id, String file_id) {
        msg_delete = new Message(version, DELETE, peer_id, file_id);
    }

    public void processMessage() throws UnknownHostException {
        ChannelManager.getCntrChannel().sendMessage(msg_delete.createMessageFinal().getBytes());
    }

    public void run() {
        try {
            if (msg_delete.getVersion().equals(ENHANCED_VERSION))
                Peer.getStorageManager().addDeleteRequest(msg_delete.getFileId());
            processMessage();
        } catch (UnknownHostException e) {
            TaskLogger.sendMessageFail();
        }
    }


}
