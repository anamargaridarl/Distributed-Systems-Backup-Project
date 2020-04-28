package base;

public class Clauses {

    public static final String VANILLA_VERSION = "1.0";
    public static final String ENHANCED_VERSION = "2.0";
    public static final char CR = (char) 0x0D;
    public static final char LF = (char) 0x0A;
    public static final String CRLF = "" + CR + LF;
    public static final int KB = 1000;
    public static final int MAX_SIZE = 64 * KB;
    public static final int DEFAULT_STORAGE = 100000000; //in KB
    public static final String PUTCHUNK = "PUTCHUNK";
    public static final String STORED = "STORED";
    public static final String DELETE = "DELETE";
    public static final String GETCHUNK = "GETCHUNK";
    public static final String CHUNK = "CHUNK";
    public static final String REMOVED = "REMOVED";
    public static final String ASKDELETE = "ASKDELETE";
    public static final int MAX_DELAY_STORED = 400; // in milliseconds
    public static final int MAX_RETRIES = 5;
    public static final int SAVE_PERIOD = 30000; // in milliseconds
    public static final int TIMEOUT = 1000;

    public static String makeChunkRef(String file_id, int number) {
        return file_id + ":" + number;
    }

    public static String makeRestoreRef(String file_id, Integer peer_id) {
        return file_id + ":" + peer_id;
    }
}
