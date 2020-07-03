package base;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Clauses {
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
  public static final String FORWARDGET = "FORWARDGET";
  public static final String GETTOIDEAL = "GETTOIDEAL";
  public static final String REPLYINFOINITIATOR = "REPLYINFOINITIATOR";
  public static final String GET_SUCCESSOR_LIST = "GET_SUCC_LIST";
  public static final String GET_ALL_SUCCESSORS = "GET_ALL_SUCCESSORS";
  public static final String ALL_SUCC = "ALL_SUCC";
  public static final String SUCC_LIST = "SUCC_LIST";
  public static final String GET_PREDECESSOR = "GET_PREDECESSOR";
  public static final String PREDECESSOR = "PREDECESSOR";
  public static final String FIND_SUCCESSOR = "FIND_SUCCESSOR";
  public static final String SUCCESSOR = "SUCCESSOR";
  public static final String SUCCESSOR_DISCONNECT = "SUCCESSOR_DISCONNECT";
  public static final String PREDECESSOR_DISCONNECT = "PREDECESSOR_DISCONNECT";
  public static final String DUMMY = "DUMMY";
  public static final String BACKUPTABLES = "BACKUPTABLES";
  public static final String NEW_TABLES = "NEW_TABLES";
  public static final int WAIT_FOR_REPLY = 500; // in milliseconds
  public static final int MAX_DELAY_STORED = 1500; // in milliseconds
  public static final int MAX_RETRIES = 5;
  public static final int BCKUP_PERIOD = 10000; // in milliseconds
  public static final int TIMEOUT = 1000; // in milliseconds
  public static final Integer m = 8;
  public static int NOT_INITIATOR = -1; // sender id will be -1 if a message is relayed from a predecessor

  public static final int CHORD_STABILIZE_PERIOD = 1500; // in milliseconds

  public static String makeChunkRef(String file_id, int number) {
    return file_id + ":" + number;
  }

  public static String makeRestoreRef(String file_id, Integer peer_id) {
    return file_id + ":" + peer_id;
  }

  public static UUID hashChunk(String file_id, int chunk_no) {
      String chunkID = makeChunkRef(file_id, chunk_no);
      return hashChunk(chunkID);
  }

  public static UUID hashChunk(String chunkID) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] digest = md.digest(chunkID.getBytes(StandardCharsets.UTF_8));
      return UUID.nameUUIDFromBytes(digest);
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      return null;
    }
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

  public static SSLSocket createSocket(InetAddress host, int port) throws IOException {
    SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(host, port);
    socket.setEnabledCipherSuites(new String[]{
        "TLS_DHE_RSA_WITH_AES_128_CBC_SHA", "TLS_RSA_WITH_AES_256_CBC_SHA"
    });
    socket.startHandshake();
    return socket;
  }

  public static SSLSocket createSocket(InetSocketAddress host) throws IOException {
    SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(host.getAddress(), host.getPort());
    socket.setEnabledCipherSuites(new String[]{
        "TLS_DHE_RSA_WITH_AES_128_CBC_SHA", "TLS_RSA_WITH_AES_256_CBC_SHA"
    });
    socket.startHandshake();
    return socket;
  }
}
