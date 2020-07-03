package base.messages;

public class RestoreMessage extends MessageChunkNo {

    byte[] body;
    int number_chunks;

    public RestoreMessage(String type, int sid, String fid, int number, int number_chunks, byte[] bdy) {
        super(type, sid, fid, number);
        body = bdy;
        this.number_chunks = number_chunks;
    }

    public byte[] getBody() {
        return body;
    }

    public int getNumChunks() {
        return this.number_chunks;
    }

}
