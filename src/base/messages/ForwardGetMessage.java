package base.messages;


public class ForwardGetMessage extends BaseMessage {

    String file_id;
    int chunk_no;
    int flag;


    public ForwardGetMessage(int flag, String file_id, int chunk_no) {
        super("FORWARDGET", 2);
        this.flag = flag;
        this.file_id = file_id;
        this.chunk_no = chunk_no;
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

}
