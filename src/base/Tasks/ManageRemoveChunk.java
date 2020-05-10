package base.Tasks;

import base.messages.MessageChunkNo;

public class ManageRemoveChunk implements Runnable {

    MessageChunkNo rmv_message;

    public ManageRemoveChunk(String v, String type, int sid, String fid, int number) {
        rmv_message = new MessageChunkNo(v, type, sid, fid, number);
    }

    //TODO: get info about who to send removed message
    @Override
    public void run() {
        /*try {
            //Peer.getTaskManager().execute(new Client());
        } catch (UnknownHostException e) {
            TaskLogger.sendMessageFail();
        }*/
    }
}
