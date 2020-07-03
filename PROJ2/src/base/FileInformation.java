package base;

import java.io.File;
import java.io.*;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.channels.*;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.Future;

import static base.Clauses.bytesToHex;

public class FileInformation implements Serializable {

    private String fileId;
    private File file;
    private int numberChunks;
    private String pathname;
    private int desired_rep_degree;

    public FileInformation(File f, String pathname, int rep_degree) throws NoSuchAlgorithmException {
        file = f;
        this.fileId = createFileid(pathname, f.lastModified());
        this.pathname = pathname;
        desired_rep_degree = rep_degree;
    }

    public static String encodeFileId(File file) {
        return "";
    }

    public String getFileId() {
        return fileId;
    }

    public File getFile() {
        return file;
    }

    public int getNumberChunks() {
        return numberChunks;
    }

    public void setNumberChunks(int numberChunks) {
        this.numberChunks = numberChunks;
    }

    public String getPathname() {
        return pathname;
    }

    public int getDesiredRepDegree() {
        return desired_rep_degree;
    }

    public byte[][] splitIntoChunk() throws IOException{
        double file_length = file.length();
        int num_parts = (int)((file_length + Clauses.MAX_SIZE) / Clauses.MAX_SIZE);
        final byte[][] parts = new byte[num_parts][];

        AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.READ);
    
        ByteBuffer buffer = ByteBuffer.allocate(Clauses.MAX_SIZE);
        long position = 0;

        for (int i = 0; i < num_parts; i++) {
    
            Future<Integer> operation = fileChannel.read(buffer, position);
    
            while(!operation.isDone());
            
            buffer.flip();
            byte[] data = new byte[buffer.limit()];
            buffer.get(data);
            buffer.clear();

            parts[i] = Arrays.copyOf(data, data.length);
            position += Clauses.MAX_SIZE;
        }
 
        return parts;
    }

    public static String getSHA256(String pathname) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(pathname.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(digest);
    }

    public static String createFileid(String pathname, long last_modified) throws NoSuchAlgorithmException {
        String file_id_string = pathname + last_modified;
        return getSHA256(file_id_string);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FileInformation) {
            FileInformation chk = (FileInformation) obj;
            return this.fileId.equals(chk.getFileId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return fileId.hashCode();
    }
}
