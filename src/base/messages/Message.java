package base.messages;

import base.Clauses;

import java.io.UnsupportedEncodingException;

public class Message extends BaseMessage {

    protected String FileId;

    public Message(String v, String ty, int sid, String fid) {
        super(v, ty, sid);
        FileId = fid;
    }

    public Message(String[] msg) {
        super(msg);
        FileId = msg[3];
    }

    public String getFileId() {
        return FileId;
    }

    public String createMessage() {
        String response;
        response = super.createMessage() + FileId;
        return response;
    }

    public String createMessageFinal() {
        String response;
        response = createMessage() + Clauses.CRLF + Clauses.CRLF;
        return response;
    }

    @Override
    public byte[] toByteArray() {
        return this.createMessage().getBytes();
    }

    @Override
    public byte[] toByteArrayFinal() {
        return this.createMessageFinal().getBytes();
    }
}
