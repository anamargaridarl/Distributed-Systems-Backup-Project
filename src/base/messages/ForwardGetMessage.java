package base.messages;

import base.Clauses;

import static base.Clauses.CRLF;

public class ForwardGetMessage {

    String file_id;
    int chunk_no;
    int flag;


    public ForwardGetMessage(int flag,String file_id,int chunk_no) {
        this.flag = flag;
        this.file_id = file_id;
        this.chunk_no = chunk_no;
    }

    public ForwardGetMessage(String[] msg) {
        this.flag = Integer.parseInt(msg[0]);
        this.file_id = msg[1];
        this.chunk_no = Integer.parseInt(msg[2]);
    }

    public int getFlag() {
        return flag;
    }

    public int getChunkNo() {
        return chunk_no;
    }

    public String getFileId() {
        return file_id;
    }

    public String createMessage() {
        return flag + " " + file_id + " " + chunk_no;
    }

    public String createMessageFinal() {
        String response;
        response = createMessage() + CRLF + CRLF;
        return response;
    }

    public byte[] toByteArray() {
        return this.createMessage().getBytes();
    }

    public byte[] toByteArrayFinal() {
        return this.createMessageFinal().getBytes();
    }
}
