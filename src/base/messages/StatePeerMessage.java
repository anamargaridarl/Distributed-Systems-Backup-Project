package base.messages;

import base.Clauses;

import static base.Clauses.*;

public class StatePeerMessage extends MessageChunkNo {

    int flag;


    public StatePeerMessage(int sender_id,String type, int flag, String file_id, int chunk_no) {
        super(VANILLA_VERSION,type,sender_id,file_id,chunk_no);
        this.flag = flag;
    }

    public int getFlag() {
        return flag;
    }


}
