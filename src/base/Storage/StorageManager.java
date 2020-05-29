package base.Storage;

import base.ChunkInfo;
import base.FileInformation;
import base.Peer;
import base.StorageLogger;
import base.Tasks.ManageDeleteFile;
import base.*;
import base.Tasks.ManageNumDeleteReply;
import base.messages.MessageChunkNo;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static base.Clauses.*;

public class StorageManager implements java.io.Serializable {

    private final HashSet<FileInformation> files_info;
    private final ArrayList<ChunkInfo> chunks_info;
    private final ConcurrentHashMap<String, Integer> rep_degrees;

    //store "STORED MESSAGES" occurrences from distinct senders (by their id)
    private final ConcurrentHashMap<String, InetSocketAddress> stored_senders = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<InetSocketAddress>> successors_stored_senders = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, InetSocketAddress> initiators = new ConcurrentHashMap<>();

    //backup stored senders from the successor (FAULT TOLERANCE)
    private ConcurrentHashMap<String, InetSocketAddress> bckup_stored_senders = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Set<InetSocketAddress>> bckup_successors_stored_senders = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, InetSocketAddress> bckup_initiators = new ConcurrentHashMap<>();

    //stores restored chunks - <fileid , <chunkno, body>>
    private final ConcurrentHashMap<String, Map<Integer, byte[]>> restored_files = new ConcurrentHashMap<>();


    //sucessors that were already contacted in the restore process
    private final ConcurrentHashMap<String, Set<InetSocketAddress>> restore_senders = new ConcurrentHashMap<>();

    public ConcurrentHashMap<String, Integer> delete_chunk_num = new ConcurrentHashMap<>();

    //chunkref + number chunks
    public ConcurrentHashMap<String, Integer> restore_chunk_num = new ConcurrentHashMap<>();

    //chunkref + (port+address of sucessor)
    public ConcurrentHashMap<String, InetSocketAddress> succ_info = new ConcurrentHashMap<>();


    private final ArrayList<String> restore_request = new ArrayList<>(); //old??

    private int total_space = DEFAULT_STORAGE;
    private int occupied_space = 0;

    public StorageManager() {
        files_info = new HashSet<>();
        chunks_info = new ArrayList<>();
        rep_degrees = new ConcurrentHashMap<>();
    }


    public byte[] getChunkData(String file_id, int number) throws IOException {
        String chunk_filename = Peer.getID() + "_STORAGE/" + file_id + ":" + number;
        Path path = Paths.get(chunk_filename);
        AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ);

        ByteBuffer buffer = ByteBuffer.allocate(Clauses.MAX_SIZE);
        long position = 0;

        Future<Integer> operation = fileChannel.read(buffer, position);
        while (!operation.isDone()) ;

        buffer.flip();
        byte[] data = new byte[buffer.limit()];
        buffer.get(data);
        buffer.clear();

        return data;
    }


    public int getOccupiedSpace() {
        return occupied_space;
    }

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

    private static class ChunkWriteHandler implements CompletionHandler<Integer, Object> {

        private final ChunkInfo chunk_info;

        public ChunkWriteHandler(ChunkInfo chunk_info) {
            this.chunk_info = chunk_info;
        }

        @Override
        public void completed(Integer integer, Object o) {
            Peer.getStorageManager().chunks_info.add(chunk_info);
            Peer.getStorageManager().incrementRepDegree(chunk_info.getFileId(), chunk_info.getNumber());
            Peer.getStorageManager().reduceSpaceAvailable(chunk_info.getSize());
        }

        @Override
        public void failed(Throwable throwable, Object o) {
            StorageLogger.storeChunkFail();
            throwable.printStackTrace();
        }
    }

    public synchronized boolean storeChunk(ChunkInfo chunk_info, byte[] chunk_data) throws ExecutionException, InterruptedException {

        String chunk_filename = Peer.getID() + "_STORAGE/" + chunk_info.getFileId() + ":" + chunk_info.getNumber();
        try {
            File chunk = new File(chunk_filename);
            if (!chunk.exists()) {
                chunk.getParentFile().mkdirs();
                chunk.createNewFile();
            }

            AsynchronousFileChannel outChannel = AsynchronousFileChannel.open(chunk.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE);

            ByteBuffer buf = ByteBuffer.allocate(Clauses.MAX_SIZE);
            buf.clear();
            buf.put(chunk_data);
            buf.flip();

            outChannel.write(buf, 0, null, new ChunkWriteHandler(chunk_info));

        } catch (IOException e) {
            StorageLogger.storeChunkFail();
            e.printStackTrace();
            return false;
        }

        Future<Boolean> stored = Peer.getTaskManager().schedule(() -> {
            return this.chunks_info.contains(chunk_info);
        }, 100, TimeUnit.MILLISECONDS);
        return stored.get();
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

    public synchronized void handleStoredSendersOccurrence(String file_id, int number, int sender_id, InetSocketAddress origin) {
        String chunk_ref = makeChunkRef(file_id, number);
        if (sender_id == NOT_INITIATOR) {
            if (!successors_stored_senders.containsKey(chunk_ref))
                return;
            if (successors_stored_senders.get(chunk_ref).add(origin)) {
                ChunkInfo dummyCI = new ChunkInfo(file_id, 0, 0, number, 0);
                int newValue = successors_stored_senders.get(chunk_ref).size() + (existsChunk(dummyCI) ? 1 : 0);
                rep_degrees.put(chunk_ref, newValue);
            }
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
                Peer.getTaskManager().execute(new ManageDeleteFile(NOT_INITIATOR, file_id, number, socket));
            }
            successors_stored_senders.remove(chunk_ref);
        }

        //when the initiator is the one supposed to store the chunk, but another peer stores for him
        //fetch the reference stored in stored_senders and send the delete
        InetSocketAddress idealPeer = stored_senders.get(chunk_ref);
        if (idealPeer != null) {
            SSLSocket socket = createSocket(idealPeer);
            Peer.getTaskManager().execute(new ManageDeleteFile(Peer.getID(), file_id, number, socket));
            stored_senders.remove(chunk_ref);
        }

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

    public boolean existsChunk(String chunk_ref) {
        for (ChunkInfo chunk : chunks_info) {
            String storedChunkRed = makeChunkRef(chunk.getFileId(), chunk.getNumber());
            if (storedChunkRed.equals(chunk_ref)) return true;
        }
        return false;
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
        return delete_chunk_num.getOrDefault(file_id, restore_chunk_num.getOrDefault(file_id, -1));

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

    public synchronized void decrementRepDegree(String file_id, int number) {
        String chunk_ref = makeChunkRef(file_id, number);
        if (rep_degrees.containsKey(chunk_ref)) {
            int rep_degree = rep_degrees.get(chunk_ref) - 1;
            rep_degrees.replace(chunk_ref, Math.max(rep_degree, 0));
        }
    }

    public synchronized void removeRepDegree(String file_id, int number) {
        String chunk_ref = makeChunkRef(file_id, number);
        rep_degrees.remove(chunk_ref);
    }

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

    public void removeRestoreRequest(String file_id, Integer peer_id) {
        restore_request.remove(makeRestoreRef(file_id, peer_id));
    }

    public boolean existsChunkRestore(String file_id, int chunk_no) {

        for (ChunkInfo info : chunks_info) {
            if (info.validateChunk(file_id, chunk_no))
                return true;
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

        AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        long position = 0;

        for (int i = 0; i < number_chunks; i++) {
            ByteBuffer buffer = ByteBuffer.allocate(chunks.get(i).length);
            buffer.clear();
            buffer.put(chunks.get(i));
            buffer.flip();

            Future<Integer> operation = fileChannel.write(buffer, position);
            buffer.clear();
            while (!operation.isDone()) ;
            position += Clauses.MAX_SIZE;
        }

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

            AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.READ);

            ByteBuffer buffer = ByteBuffer.allocate(Clauses.MAX_SIZE); //TODO que valor ponho aqui?
            long position = 0;

            Future<Integer> operation = fileChannel.read(buffer, position);

            while (!operation.isDone()) ;

            buffer.flip();
            byte[] bytes = new byte[buffer.limit()];
            buffer.get(bytes);
            buffer.clear();

            //convert byte[] to serializable StorageManager
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInput in = null;
            StorageManager storage_manager;
            try {
                in = new ObjectInputStream(bais);
                storage_manager = (StorageManager) in.readObject();
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ex) {
                    // ignore close exception
                }
            }

            StorageLogger.loadManagerOk();
            return storage_manager;
        } catch (IOException | ClassNotFoundException e) {
            StorageLogger.loadManagerFail();
            return new StorageManager();
        }
    }

    public static void saveStorageManager() {
        String filename = Peer.getID() + "_STATE.ser";

        //convert serializable StorageManager to byte[]
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        byte[] bytes = (new String("")).getBytes();
        try {
            out = new ObjectOutputStream(baos);
            out.writeObject(Peer.getStorageManager());
            out.flush();
            bytes = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                baos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }

        //send bytes using NIO FileChannel
        try {
            Path path = Paths.get(filename);
            AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE);

            ByteBuffer buffer = ByteBuffer.allocate(Clauses.MAX_SIZE);
            long position = 0;

            buffer.put(bytes);
            buffer.flip();

            Future<Integer> operation = fileChannel.write(buffer, position);
            buffer.clear();

            while (!operation.isDone()) ;

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

    public void addInitiator(String file_id, int num, InetSocketAddress initiator) {
        String chunk_ref = makeChunkRef(file_id, num);
        initiators.put(chunk_ref, initiator);
    }

    public InetSocketAddress getInitiator(String file_id, int num) {
        String chunk_ref = makeChunkRef(file_id, num);
        return initiators.getOrDefault(chunk_ref, null);
    }

    public ConcurrentHashMap<String, InetSocketAddress> getStoredSenders() {
        return stored_senders;
    }

    public ConcurrentHashMap<String, Set<InetSocketAddress>> getSuccessorsStoredSenders() {
        return successors_stored_senders;
    }

    public ConcurrentHashMap<String, InetSocketAddress> getInitiators() {
        return initiators;
    }

    public ConcurrentHashMap<String, InetSocketAddress> getBckupStoredSenders() {
        return bckup_stored_senders;
    }

    public ConcurrentHashMap<String, Set<InetSocketAddress>> getBckupSuccessorsStoredSenders() {
        return bckup_successors_stored_senders;
    }

    public ConcurrentHashMap<String, InetSocketAddress> getBckupInitiators() {
        return bckup_initiators;
    }

    public void setBckupStoredSenders(ConcurrentHashMap<String, InetSocketAddress> st_senders) {
        this.bckup_stored_senders = st_senders;
    }

    public void setBckupSuccessorsStoredSenders(ConcurrentHashMap<String, Set<InetSocketAddress>> bckup_successors_stored_senders) {
        this.bckup_successors_stored_senders = bckup_successors_stored_senders;
    }

    public void setBckupInitiators(ConcurrentHashMap<String, InetSocketAddress> bckup_initiators) {
        this.bckup_initiators = bckup_initiators;
    }
}

