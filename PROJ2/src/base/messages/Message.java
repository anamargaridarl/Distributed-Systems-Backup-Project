package base.messages;

import base.Clauses;


public class Message extends BaseMessage {

    protected String FileId;

    public Message(String ty, int sid, String fid) {
        super(ty, sid);
        FileId = fid;
    }

    public Message(String[] msg) {
        super(msg);
        FileId = msg[3];
    }

    public String getFileId() {
        return FileId;
    }


}
