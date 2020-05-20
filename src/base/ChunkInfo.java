package base;

/*
    Class the contains the chunk as well as relevant info such as:
        * file id
        * replication degree (both desired and current)
        * size
        * number
 */
public class ChunkInfo implements java.io.Serializable {
  private final String file_id;
  private final int rep_deg;
  private final int size;
  private final int number;
  private final int number_chunks;
  private byte[] chunk;


  public ChunkInfo(String file_id, int rep_deg, int size, int number, int number_chunks) {
    this.file_id = file_id;
    this.rep_deg = rep_deg;
    this.size = size;
    this.number = number;
    this.number_chunks = number_chunks;
  }

  public String getFileId() {
    return file_id;
  }

  public int getRepDeg() {
    return rep_deg;
  }

  public int getSize() {
    return size;
  }

  public int getNumber() {
    return number;
  }

  public boolean validateChunk(String file_id, int chunk_no) {
    return (this.file_id.equals(file_id) && number == chunk_no);
  }

  @Override
  public boolean equals(Object obj) {

    if (obj instanceof ChunkInfo) {
      ChunkInfo chk = (ChunkInfo) obj;
      return this.file_id.equals(chk.getFileId()) && this.number == chk.getNumber();
    }
    return false;
  }

  @Override
  public int hashCode() {
    return file_id.hashCode() * number;
  }

  public int getNumber_chunks() {
    return number_chunks;
  }

  public byte[] getChunk() {
    return chunk;
  }

  public void setChunk(byte[] chunk) {
    this.chunk = chunk;
  }
}
