package base;

public class TaskLogger extends Logger {

    public static void sendMessageFail() {
        error("Failed to send message to channel");
    }

    public static void receivedOwnMessage(String type) {
        warning("Received own " + type + " message, ignoring.");
    }

    public static void invalidSenderID(int id) {
        error("Sender ID on received msg is not valid. Received: " + id);
    }

    public static void invalidMessage(String type) {
        error("Invalid message received, ignoring. Received: " + type);
    }

    public static void startShutdown(int timeout) {
        warning("Saving state before the shutdown.\n\tWaiting for pending tasks to finish. Timeout: " + timeout);
    }

    public static void shutdownOk() {
        success("Shutdown occurred correctly");
    }

    public static void forcedShutdown() {
        warning("A forced shutdown detected. Data may be lost.");
    }

    //backup
    public static void putChunkFail(String file_id, int number) {
        error("Failed to obtain desired replication degree File id: " + file_id + "\tNr: " + number);
    }

    public static void putChunkOk() {
        success("Replication degree was obtained successfully");
    }

    public static void ownsFile(String file_id) {
        warning("Peer owns file with id: " + file_id + ". Ignoring request");
    }

    public static void alreadyBackedUp(int peer_id, String file_id, int number) {
        warning("Peer: " + peer_id + " already backed up this chunk.\n\tFile id: " + file_id + "\tNr: " + number + "\tReplying with STORED message");
    }

    public static void insufficientSpaceFail(int size) {
        error("Peer doesn't have enough memory to store chunk with size: " + size + ". Ignoring request");
    }

    public static void enhanceStoreChunk() {
        warning("Replication degree still hasn't been achieved. Proceeding to store chunk.");
    }

    public static void enhanceStoreChunkOk() {
        success("Replication degree already has been achieved. Will not store this chunk");
    }

    public static void storedChunkOk(String file_id, int number, int size) {
        success("Stored the chunk.\n\tFile id: " + file_id + "\tNr: " + number + "\tSize: " + size);
    }

    public static void storedChunkFail(String file_id, int number) {
        error("Failed to store chunk.\t\nFile id: " + file_id + "\tNr: " + number);
    }

    public static void enhancedDuplicateDetected() {
        warning("Detected duplicate Backup Initiator. Cancelling the pending one");
    }


    //restore
    public static void chunkTCPWriteFail() {
        error("Failed to write chunk in TCP socket");
    }

    public static void chunkTCPReadFail() {
        error("Failed to read chunk in TCP socket");
    }

    public static void chunkTCPSocketFail() {
        error("Failed to create socket to setup TCP connection, aborting");
    }

    public static void chunkTCPAcceptSocketFail() {
        error("Failed to accept client socket and establish TCP connection");
    }

    public static void getChunkRetrieveFail() {
        error("Failed to retrieve chunk to send, aborting");
    }

    public static void noChunkReceivedFail() {
        error("No chunk received, aborting");
    }

    public static void restoreFileFail() {
        error("Failed to restore file");
    }

    //reclaim
    public static void removedReceived(int rep_deg) {
        warning("Received REMOVED of chunk. Its Rep Degree: " + rep_deg);
    }

    public static void lowRepDeg() {
        warning("Chunk Replication Degree is low, initiating Backup");
    }

}
