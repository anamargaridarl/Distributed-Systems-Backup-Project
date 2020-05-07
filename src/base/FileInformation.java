package base;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

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

    public byte[][] splitIntoChunk() throws IOException //test left chunk zero
    {
        byte[] file_content = Files.readAllBytes(file.toPath());
        int file_length = file_content.length;
        final byte[][] parts = new byte[(file_length + Clauses.MAX_SIZE) / Clauses.MAX_SIZE][];

        int part_i = 0;
        int stop_i = 0;
        for (int start_i = 0; start_i + Clauses.MAX_SIZE <= file_length; start_i += Clauses.MAX_SIZE) {
            stop_i += Clauses.MAX_SIZE;
            parts[part_i++] = Arrays.copyOfRange(file_content, start_i, stop_i);
        }

        if (stop_i < file_length)
            parts[part_i] = Arrays.copyOfRange(file_content, stop_i, file_length);

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
}
