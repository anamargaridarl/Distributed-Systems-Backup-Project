package base.Tasks;

import base.Peer;
import base.messages.BaseMessage;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class HandleDeleteOffline implements Runnable {

    BaseMessage delete_message;

    public HandleDeleteOffline(String[] msg) {
        delete_message = new BaseMessage(msg);
    }

    @Override
    public void run() {
        ArrayList<String> delete_requests = Peer.getStorageManager().getDeleteRequests();
        delete_requests.forEach((value) -> Peer.getTaskManager().execute(new ManageDeleteFile(delete_message.getVersion(), Peer.getID(), value)));
    }
}
