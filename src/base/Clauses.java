package base;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    public static final Integer m = 1000; //change to real value

    public static String makeChunkRef(String file_id, int number) {
        return file_id + ":" + number;
    }

    public static String makeRestoreRef(String file_id, Integer peer_id) {
        return file_id + ":" + peer_id;
    }

    public static String hashChunk(String file_id, int peer_id) throws NoSuchAlgorithmException {
        String chunkid = makeChunkRef(file_id,peer_id);
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] digest = md.digest(chunkid.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(digest);
    }
    public static String bytesToHex(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
    public static double alocatePeer(String hash) {
        return Integer.parseInt(hash) % Math.pow(2,m);
    }

  public static List<byte[]> separateHeaderAndBody(byte[] message) {
      int i = 0;
      for (; i < message.length - 4; i++) { //check the message where the <CRLF><CRLF> are located
          if (message[i] == CR && message[i + 1] == LF && message[i + 2] == CR && message[i + 3] == LF)
              break;
      }
      if (i == message.length - 4) {
          return null;
      }
      List<byte[]> newList = new ArrayList<>();
      byte[] header = Arrays.copyOfRange(message, 0, i);
      byte[] body = Arrays.copyOfRange(message, i + 4, message.length);
      newList.add(header);
      newList.add(body);
      return newList;
  }

  public static String[] parseHeader(byte[] header) {
      return new String(header, 0, header.length).split("\\s+");
  }
}
