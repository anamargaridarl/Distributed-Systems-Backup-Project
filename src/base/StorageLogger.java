package base;

public class StorageLogger extends Logger {

    public static void storeChunkFail() {
        error("Failed to store chunk in file");
    }

    public static void removeChunkFail() {
        error("Failed to delete chunk file");
    }

    public static void removeChunkOk() {
        success("Chunk file deleted successfully");
    }

    public static void restoreFileOk(String filename) {
        success("File restored. Name: " + filename);
    }

    public static void restoreDuplicateChunk() {
        warning("Duplicated chunk, has been collected already");
    }

    public static void restoreChunkOk() {
        success("Added chunk to storage, almost ready to restore");
    }

    public static void restoreStartChunkOk() {
        success("Added file-chunk to storage, almost ready to restore");
    }

    public static void loadManagerFail() {
        error("Failed to load storage manager. Will initiate a new one. ");
    }

    public static void loadManagerOk() {
        success("Loaded storage manager");
    }

    public static void saveManagerFail() {
        error("Failed to save storage manager");
    }

    public static void saveManagerOk() {
        success("Saved storage state");
    }

}
