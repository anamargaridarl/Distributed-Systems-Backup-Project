package base;

public class PeerLogger extends Logger {
    public static void processBackupFail(String pathname) {
        error("Failed to process the backup of file: " + pathname);
    }

    public static void restoreFileMissing(String filename) {
        error("File doesn't exist, aborting restore");
    }

    public static void createFileIDFail() {
        error("Failed to create file id");
    }

    public static void removedChunk(String file_id, int number) {
        warning("Removing chunk with file id: " + file_id + " Number: " + number);
    }

    public static void reclaimComplete(int total, int occupied) {
        success("Total space after reclaiming: " + total + "KB \tOccupied space: " + occupied);
    }

    public static void missingFile(String pathname) {
        error("There is no such file: " + pathname);
    }

    public static void channelsDisrupt() {
        warning("Sudden disruption of the communication");
    }

    public static void disconnectPeer() {
        warning("Peer disconnected while sending message");
    }

}
