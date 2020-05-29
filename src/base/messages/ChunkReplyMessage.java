package base.messages;

public class ChunkReplyMessage extends BaseMessage {

    String file_id;
    int num_chunks;

    public ChunkReplyMessage(String type, int sid, String file_id, int num_chunks) {
        super(type, sid);
        this.file_id = file_id;
        this.num_chunks = num_chunks;
    }

    public int getNumChunks() {
        return num_chunks;
    }

    public String getFileId() {
        return file_id;
    }

}
