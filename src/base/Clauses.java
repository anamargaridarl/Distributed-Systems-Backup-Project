package base;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

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
  public static final String DECLINED = "DECLINED";
  public static final String DELETE = "DELETE";
  public static final String GETCHUNK = "GETCHUNK";
  public static final String CHUNK = "CHUNK";
  public static final String REMOVED = "REMOVED";
  public static final String SUCCGETCHUNK = "SUCCGETCHUNK";
  public static final String ASKDELETE = "ASKDELETE";
  public static final String NUMREPLY = "NUMREPLY";
  public static final String DELETEREPLY = "DELETEREPLY";
  public static final String FORWARDGET = "FORWARDGET";
  public static final String GETTOIDEAL = "GETTOIDEAL";
  public static final String REPLYINFOINITIATOR = "REPLYINFOINITIATOR";
  public static final int MAX_DELAY_STORED = 1500; // in milliseconds
  public static final int MAX_RETRIES = 5;
  public static final int SAVE_PERIOD = 30000; // in milliseconds
  public static final int TIMEOUT = 1000;
  public static final Integer m = 3; //TODO: change to m:8
  public static int NOT_INITIATOR = -1; // sender id will be -1 if a message is relayed from a predecessor

  /**FOR TEST PURPOSES ONLY*/
  public static final Hashtable<Integer, InetSocketAddress> chord = new Hashtable<>();

  public static void addElements() {
    InetSocketAddress obj0 = new InetSocketAddress("localhost", 5000);
    InetSocketAddress obj1 = new InetSocketAddress("localhost", 5001);
    InetSocketAddress obj2 = new InetSocketAddress("localhost", 5002);
    InetSocketAddress obj3 = new InetSocketAddress("localhost", 5003);


    chord.put(0, obj0);
    chord.put(2, obj1);
    chord.put(4, obj2);
    chord.put(6, obj3);
  }

  //checks what peer id is supposed to be assigned (TESTING)
  public static Integer checkAllocated(Integer hashKey) {
    if (hashKey >= 6) {
      return 4;
    } else if (hashKey >= 4) {
      return 3;
    }
    if (hashKey >= 2) {
      return 2;
    } else
      return 1;
  }

  //simple method to find out what peer (by hash key) should store the chunk
  public static Integer allocatePeer(Integer hashKey) {
    //TODO: use chord sucessor logic to find key sucessor
    if (hashKey >= 6) {
      return Peer.getID() != 4 ? 6 : 0;
    } else if (hashKey >= 4) {
      return Peer.getID() != 3 ? 4 : 6;
    } else if (hashKey >= 2) {
      return Peer.getID() != 2 ? 2 : 4;
    }
    return Peer.getID() != 1 ? 0 : 2;
  }

  /***/

  public static String makeChunkRef(String file_id, int number) {
    return file_id + ":" + number;
  }

  public static String makeRestoreRef(String file_id, Integer peer_id) {
    return file_id + ":" + peer_id;
  }

  public static UUID hashChunk(String file_id, int chunk_no) throws NoSuchAlgorithmException {
    String chunkID = makeChunkRef(file_id, chunk_no);
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    byte[] digest = md.digest(chunkID.getBytes(StandardCharsets.UTF_8));
    return UUID.nameUUIDFromBytes(digest);
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

  public static Integer getHashKey(UUID hash) {
    long msb = hash.getMostSignificantBits();
    long lsb = hash.getLeastSignificantBits();
    byte[] allBytes = new byte[]{
        (byte) ((msb >> 56) & 0xff),
        (byte) ((msb >> 48) & 0xff),
        (byte) ((msb >> 40) & 0xff),
        (byte) ((msb >> 32) & 0xff),
        (byte) ((msb >> 24) & 0xff),
        (byte) ((msb >> 16) & 0xff),
        (byte) ((msb >> 8) & 0xff),
        (byte) (msb & 0xff),
        (byte) ((lsb >> 56) & 0xff),
        (byte) ((lsb >> 48) & 0xff),
        (byte) ((lsb >> 40) & 0xff),
        (byte) ((lsb >> 32) & 0xff),
        (byte) ((lsb >> 24) & 0xff),
        (byte) ((lsb >> 16) & 0xff),
        (byte) ((lsb >> 8) & 0xff),
        (byte) (lsb & 0xff),
    };

    BigInteger hashInt = new BigInteger(allBytes);
    BigInteger divisor = BigInteger.valueOf(1 << m);
    return hashInt.remainder(divisor).intValue();
  }


  public static List<byte[]> separateHeaderAndBody(byte[] message, int fullSize) {
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
    byte[] body = Arrays.copyOfRange(message, i + 4, fullSize);
    newList.add(header);
    newList.add(body);
    return newList;
  }

  public static String[] parseHeader(byte[] header) {
    return new String(header, 0, header.length).split("\\s+");
  }

  public static Socket createSocket(InetAddress host, int port) throws IOException {
    return new Socket(host, port);
  }

  /**
   * FOR TEST PURPOSES ONLY
   */
  public static Socket createSocket(InetSocketAddress host) throws IOException {
    return new Socket(host.getAddress(), host.getPort());
  }
}