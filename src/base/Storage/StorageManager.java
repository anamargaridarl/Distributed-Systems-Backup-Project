package base.Storage;

import base.ChunkInfo;
import base.FileInformation;
import base.Peer;
import base.StorageLogger;
import base.Tasks.ManageDeleteFile;
import base.channel.MessageReceiver;
import base.*;
import base.messages.MessageChunkNo;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static base.Clauses.*;

public class StorageManager implements java.io.Serializable {

    private final HashSet<FileInformation> files_info;
    private final ArrayList<ChunkInfo> chunks_info;
    private final ConcurrentHashMap<String, Integer> rep_degrees;
    private final ArrayList<String> delete_requests = new ArrayList<>();

    //store "STORED MESSAGES" occurrences from distinct senders (by their id)
    private final ConcurrentHashMap<String, InetSocketAddress> stored_senders = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<InetSocketAddress>> successors_stored_senders = new ConcurrentHashMap<>();

    //stores restored chunks - <fileid , <chunkno, body>>
    private final transient ConcurrentHashMap<String, Map<Integer, byte[]>> restored_files = new ConcurrentHashMap<>();


    //sucessors that were already contacted in the restore process
    private final ConcurrentHashMap<String, Set<InetSocketAddress>> restore_senders = new ConcurrentHashMap<>();

    public ConcurrentHashMap<String, Integer> delete_chunk_num = new ConcurrentHashMap<>();

    //chunkref + number chunks
    public ConcurrentHashMap<String, Integer> restore_chunk_num = new ConcurrentHashMap<>();

    //chunkref + (port+address of sucessor)
    public ConcurrentHashMap<String, InetSocketAddress> succ_info = new ConcurrentHashMap<>();


    private final ArrayList<String> restore_request = new ArrayList<>(); //old??
    private final transient Set<String> stored_chunk_request = new HashSet<>();

    private int total_space = DEFAULT_STORAGE;
    private int occupied_space = 0;

    public StorageManager() {
        files_info = new HashSet<>();
        chunks_info = new ArrayList<>();
        rep_degrees = new ConcurrentHashMap<>();
    }


    //shared functions
    public byte[] getChunkData(String file_id, int number) throws IOException {
        String chunk_filename = Peer.getID() + "_STORAGE/" + file_id + ":" + number;
        return Files.readAllBytes(Paths.get(chunk_filename));
    }


    public int getOccupiedSpace() {
        return occupied_space;
    }
    //end shared functions

    //backup functions
    public boolean ownsFile(String file_id) {
        for (FileInformation file_info : files_info) {
            if (file_info.getFileId().equals(file_id)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void addFileInfo(FileInformation file) {
        this.files_info.add(file);
    }

    public synchronized void removeFileInfo(String file_id) {
        for (Iterator<FileInformation> iter = this.files_info.iterator(); iter.hasNext(); ) {
            FileInformation file = iter.next();
            if (file.getFileId().equals(file_id)) {
                int num = file.getNumberChunks();
                for (int i = 0; i < num; i++) {
                    String chunkRef = makeChunkRef(file_id, i);
                    stored_senders.remove(chunkRef);
                    rep_degrees.remove(chunkRef);
                }
                iter.remove();
                return;
            }
        }
    }

    public int getCurrentRepDegree(String file_id, int number) {
        String chunk_ref = makeChunkRef(file_id, number);
        return rep_degrees.getOrDefault(chunk_ref, 0);
    }

    public void removeSuccessorStoredOccurrence(String fileId, int number, InetSocketAddress inetSocketAddress) {
        String chunk_ref = makeChunkRef(fileId, number);
        if (successors_stored_senders.containsKey(chunk_ref)) {
            successors_stored_senders.get(chunk_ref).remove(inetSocketAddress);
            decrementRepDegree(fileId, number);
        } else if (stored_senders.containsKey(chunk_ref)) {
            decrementRepDegree(fileId, number);
        }
    }

    public synchronized void addStoredChunkRequest(String file_id, int number, int sender_id) {
        String chunk_ref = makeChunkRef(file_id, number);
        if (sender_id == NOT_INITIATOR) {
            successors_stored_senders.putIfAbsent(chunk_ref, new HashSet<>());
        } else {
            stored_senders.put(chunk_ref, new InetSocketAddress(Peer.getServerPort()));
        }
    }

    public synchronized void removeStoredOccurrenceChunk(String file_id, int number) {
        String chunk_ref = makeChunkRef(file_id, number);
        stored_senders.remove(chunk_ref);
    }

    public synchronized InetSocketAddress getSuccGetChunk(MessageChunkNo msg) {
        String chunk_ref = makeChunkRef(msg.getFileId(), msg.getNumber());
        if (successors_stored_senders.containsKey(chunk_ref)) {
            Set<InetSocketAddress> succ = successors_stored_senders.get(chunk_ref);
            for (InetSocketAddress host : succ) {
                return host;
            }
            return null;
        } else {
            return stored_senders.getOrDefault(chunk_ref, null);
        }
    }

    public synchronized boolean storeChunk(ChunkInfo chunk_info, byte[] chunk_data) {

        String chunk_filename = Peer.getID() + "_STORAGE/" + chunk_info.getFileId() + ":" + chunk_info.getNumber();
        try {
            File chunk = new File(chunk_filename);
            if (!chunk.exists()) {
                chunk.getParentFile().mkdirs();
                chunk.createNewFile();
            }

            FileOutputStream outputStream = new FileOutputStream(chunk_filename);
            outputStream.write(chunk_data);

        } catch (IOException e) {
            StorageLogger.storeChunkFail();
            return false;
        }
        this.chunks_info.add(chunk_info);
        incrementRepDegree(chunk_info.getFileId(), chunk_info.getNumber());
        reduceSpaceAvailable(chunk_info.getSize());
        return true;
    }

    public synchronized void incrementRepDegree(String file_id, int number) {
        String chunk_ref = makeChunkRef(file_id, number);
        if (rep_degrees.containsKey(chunk_ref)) {
            int rep_degree = rep_degrees.get(chunk_ref) + 1;
            rep_degrees.replace(chunk_ref, rep_degree);
        } else {
            rep_degrees.put(chunk_ref, 1);
        }

    }

    private synchronized void reduceSpaceAvailable(int size) {
        occupied_space += size;
    }

    //backup-stored
    public synchronized void handleStoredSendersOccurrence(String file_id, int number, int sender_id, InetSocketAddress origin) {
        String chunk_ref = makeChunkRef(file_id, number);
        if (sender_id == NOT_INITIATOR) {
            if (!successors_stored_senders.containsKey(chunk_ref))
                return;
            successors_stored_senders.get(chunk_ref).add(origin);
            int newValue = rep_degrees.get(chunk_ref) + successors_stored_senders.get(chunk_ref).size();
            rep_degrees.put(chunk_ref, newValue);
        } else {
            if (!stored_senders.containsKey(chunk_ref))
                return;
            stored_senders.put(chunk_ref, origin);
            rep_degrees.put(chunk_ref, sender_id);
        }
    }


    public synchronized void handleDeleteSuccessors(String file_id, int number) throws IOException {
        String chunk_ref = makeChunkRef(file_id, number);
        Set<InetSocketAddress> suc = successors_stored_senders.get(chunk_ref);
        if (suc != null) {
            for (InetSocketAddress peer : suc) {
                SSLSocket socket = createSocket(peer);
                Peer.getTaskManager().execute(new ManageDeleteFile(VANILLA_VERSION, NOT_INITIATOR, file_id, number, socket));
            }
            successors_stored_senders.remove(chunk_ref);
        }

        //when the initiator is the one supposed to store the chunk, but another peer stores for him
        //fetch the reference stored in stored_senders and send the delete
        InetSocketAddress idealPeer = stored_senders.get(chunk_ref);
        if (idealPeer != null) {
            SSLSocket socket = createSocket(idealPeer);
            Peer.getTaskManager().execute(new ManageDeleteFile(VANILLA_VERSION, Peer.getID(), file_id, number, socket));
            stored_senders.remove(chunk_ref);
        }

    }

    public ArrayList<String> getDeleteRequests() {
        return delete_requests;
    }

    public synchronized void deleteChunks(String file_id, int chunk_no) {
        for (Iterator<ChunkInfo> iter = chunks_info.iterator(); iter.hasNext(); ) {
            ChunkInfo chunkInfo = iter.next();
            if (chunkInfo.validateChunk(file_id, chunk_no)) {
                removeStoredOccurrenceChunk(file_id, chunk_no);
                removeRepDegree(file_id, chunkInfo.getNumber());
                removeChunkFile(file_id, chunkInfo.getNumber());
                gainSpaceAvailable(chunkInfo.getSize());
                iter.remove();
            }
        }
    }

    public boolean existsChunk(ChunkInfo chunk) {
        return chunks_info.contains(chunk);
    }

    public synchronized InetSocketAddress handleGetChunk(MessageChunkNo msg) throws IOException {

        String chunk_ref = makeChunkRef(msg.getFileId(), msg.getNumber());
        Set<InetSocketAddress> succ = successors_stored_senders.get(chunk_ref);
        Set<InetSocketAddress> add_set = new HashSet<>();

        add_set = restore_senders.getOrDefault(chunk_ref, null);

        if (succ != null && !succ.isEmpty()) {
            for (InetSocketAddress s : succ) {
                if (add_set == null) {
                    add_set = new HashSet<>();
                    add_set.add(s);
                    restore_senders.put(chunk_ref, add_set);
                    return s;
                } else if (!add_set.contains(s)) {
                    add_set.add(s);
                    if (restore_senders != null && restore_senders.contains(chunk_ref)) {
                        restore_senders.remove(chunk_ref);
                    }
                    restore_senders.put(chunk_ref, add_set);
                    return s;
                }
            }
            return null;
        } else
            return stored_senders.getOrDefault(chunk_ref, null);
    }

    public int getNumChunk(String file_id, int chunk_no) {
        for (ChunkInfo chunkInfo : chunks_info) {
            if (chunkInfo.validateChunk(file_id, chunk_no)) {
                return chunkInfo.getNumber_chunks();
            }
        }
        return -1;
    }

    private void removeChunkFile(String file_id, int chunk_number) {
        String chunk_filename = Peer.getID() + "_STORAGE" + "/" + file_id + ":" + chunk_number;
        File chunk = new File(chunk_filename);
        if (chunk.delete()) {
            StorageLogger.removeChunkOk();
        } else {
            StorageLogger.removeChunkFail();
        }
    }

    public boolean hasEnoughSpace(int size) {
        return occupied_space + size <= total_space * KB; // 1MB of safety net
    }
    //end backup functions

    //delete functions
    public void addDeleteRequest(String fileId) {
        if (!delete_requests.contains(fileId)) {
            delete_requests.add(fileId);
        }
    }


    public boolean isRepDegreeLow(String file_id, int number) {
        String chunk_ref = makeChunkRef(file_id, number);
        if (rep_degrees.containsKey(chunk_ref)) {
            int rep_degree = rep_degrees.get(chunk_ref);
            for (ChunkInfo chunk_info : chunks_info) {
                if (chunk_info.getFileId().equals(file_id)
                        && chunk_info.getNumber() == number) {
                    if (rep_degree < chunk_info.getRepDeg()) {
                        return true;
                    }
                    break;
                }
            }
        } else {
            return true;
        }
        return false;
    }

    public synchronized void decrementRepDegree(String file_id, int number) {
        String chunk_ref = makeChunkRef(file_id, number);
        if (rep_degrees.containsKey(chunk_ref)) {
            if (rep_degrees.get(chunk_ref) == 1) {
                rep_degrees.remove(chunk_ref);
            } else {
                int rep_degree = rep_degrees.get(chunk_ref) - 1;
                rep_degrees.replace(chunk_ref, rep_degree);
            }
        }
    }

    public synchronized void removeRepDegree(String file_id, int number) {
        String chunk_ref = makeChunkRef(file_id, number);
        rep_degrees.remove(chunk_ref);
    }

    //Replication degree functions
    public Integer getChunkRepDegree(String file_id, int number) {
        String chunk_ref = makeChunkRef(file_id, number);
        return rep_degrees.getOrDefault(chunk_ref, 0);
    }


    public synchronized void setTotalSpace(int new_space) {
        total_space = new_space;
    }

    public ChunkInfo removeExpendableChunk() throws IOException {
        ChunkInfo expandable = null;
        for (ChunkInfo chunk : chunks_info) { //check if there is any disposable chunk with more than enough replicas
            if (chunk.getRepDeg() < getChunkRepDegree(chunk.getFileId(), chunk.getNumber())) {
                expandable = chunk;
                break;
            }
        }
        if (expandable == null) { // if there aren't any, remove the oldest chunk
            expandable = chunks_info.get(0);
        }
        chunks_info.remove(expandable);
        byte[] chunk_data = Peer.getStorageManager().getChunkData(expandable.getFileId(), expandable.getNumber());
        expandable.setChunk(chunk_data);
        removeChunkFile(expandable.getFileId(), expandable.getNumber());
        removeRepDegree(expandable.getFileId(), expandable.getNumber());
        removeStoredOccurrenceChunk(expandable.getFileId(), expandable.getNumber());
        gainSpaceAvailable(expandable.getSize());
        return expandable;
    }

    public ChunkInfo getChunkInfo(String file_id, int number) {
        for (ChunkInfo chunk_info : chunks_info) {
            if (chunk_info.getFileId().equals(file_id) && chunk_info.getNumber() == number) {
                return chunk_info;
            }
        }
        return null;
    }


    //reclaim functions

    //state functions
    public HashSet<FileInformation> getFilesInfo() {
        return files_info;
    }

    public ArrayList<ChunkInfo> getChunksInfo() {
        return chunks_info;
    }

    public int getTotalSpace() {
        return total_space;
    }

    private synchronized void gainSpaceAvailable(int size) {
        occupied_space -= size;
    }


    public void addRestoreRequest(String file_id, Integer peer_id) {
        restore_request.add(makeRestoreRef(file_id, peer_id));
    }

    public byte[] getRestoredChunk(String file_id, Integer chunk_no) {
        if (restored_files.containsKey(file_id)) {
            return restored_files.get(file_id).getOrDefault(chunk_no, null);
        }
        return null;
    }

    public void removeRestoreRequest(String file_id, Integer peer_id) {
        restore_request.remove(makeRestoreRef(file_id, peer_id));
    }

    public boolean checkLastChunk(String file_id) {
        Map<Integer, byte[]> chunk = restored_files.get(file_id);
        for (int i = 0; i < chunk.size(); i++) {
            if (chunk.get(i).length == 0) {
                chunk.remove(chunk.get(i));
                restored_files.replace(file_id, chunk);
            }
            if (chunk.get(i).length < MAX_SIZE)
                return true;
        }
        return false;
    }

    public boolean existsRestoreRequest(String file_id, Integer peer_id) {
        return restore_request.contains(makeRestoreRef(file_id, peer_id));
    }

    public boolean existsChunkRestore(String file_id, int chunk_no) {

        for (ChunkInfo info : chunks_info) {
            if (info.validateChunk(file_id, chunk_no))
                return true;
        }
        return false;
    }

    public boolean checkReceiveChunk(String file_id, Integer chunk_no) {
        if (restored_files.containsKey(file_id)) {
            Map<Integer, byte[]> chunk = restored_files.get(file_id);
            return chunk.get(chunk_no) != null;
        }
        return false;
    }

    public void restoreFile(String filename, String fileid, Integer number_chunks) throws IOException {

        Map<Integer, byte[]> chunks = restored_files.get(fileid);

        if (chunks.isEmpty()) {
            TaskLogger.restoreFileFail();
            return;
        }

        String chunk_filename = Peer.getID() + "_RESTORE" + "/" + filename;
        File file = new File(chunk_filename);

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        FileOutputStream file_to_write = new FileOutputStream(file);

        for (int i = 0; i < number_chunks; i++) {
            file_to_write.write(chunks.get(i));
        }

        file_to_write.close();
        StorageLogger.restoreFileOk(filename);
        removeRestoredChunkData(fileid);
        removeRestoreRequest(fileid, Peer.getID());


    }

    public void addRestoredChunkRequest(String fileId, int number, byte[] body) {

        if (restored_files.containsKey(fileId)) {
            Map<Integer, byte[]> file = restored_files.get(fileId);
            if (file.containsKey(number)) {
                StorageLogger.restoreDuplicateChunk();
            } else {
                file.put(number, body);
                restored_files.replace(fileId, file);
                StorageLogger.restoreChunkOk();
            }
        } else {
            Map<Integer, byte[]> chunk = new HashMap<>();
            chunk.put(number, body);
            restored_files.put(fileId, chunk);
            StorageLogger.restoreStartChunkOk();
        }
    }

    public synchronized void removeRestoredChunkData(String file_id) {
        restored_files.remove(file_id);
    }

    //save information when peer is off functions
    public static StorageManager loadStorageManager() {
        String filename = Peer.getID() + "_STATE.ser";
        try {
            File file = new File(filename);
            if (!file.exists()) {
                return new StorageManager();
            }
            FileInputStream file_is = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(file_is);
            StorageManager storage_manager = (StorageManager) in.readObject();
            in.close();
            file_is.close();
            StorageLogger.loadManagerOk();
            return storage_manager;
        } catch (IOException | ClassNotFoundException e) {
            StorageLogger.loadManagerFail();
            return new StorageManager();
        }
    }

    public static void saveStorageManager() {
        String filename = Peer.getID() + "_STATE.ser";
        try {
            FileOutputStream file = new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(file);
            out.writeObject(Peer.getStorageManager());
            out.close();
            file.close();
            StorageLogger.saveManagerOk();
        } catch (IOException e) {
            StorageLogger.saveManagerFail();
        }
    }

    public void emptyChunksInfo() {
        this.chunks_info.clear();
        this.rep_degrees.clear();
    }

    public int getRestoreChunkNum(String file_id) {
        return restore_chunk_num.getOrDefault(file_id, -1);
    }

    public InetSocketAddress getSuccInfo(String file_id, int chunkNo) {
        String chunk_ref = makeChunkRef(file_id, chunkNo);
        return succ_info.getOrDefault(chunk_ref, null);
    }

    public void addSuccInfo(String fileId, int chunkNo, InetSocketAddress address) {
        String chunk_ref = makeChunkRef(fileId, chunkNo);
        succ_info.putIfAbsent(chunk_ref, address);
    }

    public void addRestoreChunkNo(String file_id, int num) {
        restore_chunk_num.putIfAbsent(file_id, num);
    }

    public int getDeleteChunkNum(String file_id) {
        return delete_chunk_num.getOrDefault(file_id, -1);
    }

    public void addDeleteChunkNo(String file_id, int num) {
        delete_chunk_num.putIfAbsent(file_id, num);
    }
    //end save information when peer is off functions
}

