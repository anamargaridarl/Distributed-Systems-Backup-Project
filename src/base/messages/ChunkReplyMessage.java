package base.messages;

import static base.Clauses.CRLF;

public class ChunkReplyMessage extends BaseMessage{

    String file_id;
    int num_chunks;

    public ChunkReplyMessage(String version, String type, int sid, String file_id,int num_chunks) {
        super(version,type,sid);
        this.file_id = file_id;
        this.num_chunks = num_chunks;
    }

    public ChunkReplyMessage(String[] msg) {
        super(msg[0],msg[1], Integer.parseInt(msg[2]));
        this.file_id = msg[3];
        this.num_chunks = Integer.parseInt(msg[4]);
    }

    public int getNumChunks() {
        return num_chunks;
    }

    public String getFileId() {
        return file_id;
    }

    public String createMessage() {
        return file_id + " " + num_chunks;
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
