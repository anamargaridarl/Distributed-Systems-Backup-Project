package base.chord;

import base.ChordLogger;
import base.Clauses;
import base.Peer;
import base.Tasks.*;

import javax.net.ssl.SSLSocket;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceArray;

import static base.Clauses.CHORD_STABILIZE_PERIOD;
import static base.Clauses.WAIT_FOR_REPLY;
import static java.lang.System.exit;

public class ChordManager {
    private static ChordStabilizer chordStabilizer;

    private final ChordIdentifier peerID;
    private AtomicReferenceArray<ChordIdentifier> atomicFingerTable;
    private AtomicReferenceArray<ChordIdentifier> atomicSuccessorList;
    private volatile ChordIdentifier predecessorID;
    private final ChordMessageHandler messageHandler;
    private final Hashtable<UUID, ArrayList<ChordIdentifier>> successorReplyTable = new Hashtable<>();

    private final Hashtable<UUID, ArrayList<ChordIdentifier[]>> allSuccessorsReplyTable = new Hashtable<>();

    private boolean conected = false;

    public ChordManager(InetSocketAddress selfAddress) { //Constructor for the first node in a ring  (Will probably also need the taskManager)
        this.peerID = new ChordIdentifier(selfAddress);
        atomicFingerTable = new AtomicReferenceArray<>(ChordIdentifier.bitSize);
        atomicSuccessorList = new AtomicReferenceArray<>(ChordIdentifier.bitSize);
        predecessorID = null;

        messageHandler = new TCPMessageHandler();

        atomicFingerTable.set(0, peerID);
        for (int i = 0; i < atomicSuccessorList.length(); i++) atomicSuccessorList.set(i, peerID);

        ChordLogger.startChord();

        chordStabilizer = new ChordStabilizer(this);
        Peer.getTaskManager().scheduleAtFixedRate(chordStabilizer, CHORD_STABILIZE_PERIOD, CHORD_STABILIZE_PERIOD, TimeUnit.MILLISECONDS);
    }

    public ChordManager(InetSocketAddress selfAddress, InetSocketAddress knownPeerAddress) { //Constructor for a node joining a ring
        this.peerID = new ChordIdentifier(selfAddress);
        atomicFingerTable = new AtomicReferenceArray<>(ChordIdentifier.bitSize);
        atomicSuccessorList = new AtomicReferenceArray<>(ChordIdentifier.bitSize);

        messageHandler = new TCPMessageHandler();

        joinFingerTable(knownPeerAddress);
    }

    public void addSuccessorListToReplyTable(UUID senderID, ChordIdentifier[] successorList) {
        ArrayList<ChordIdentifier[]> listOfReplies = allSuccessorsReplyTable.get(senderID);

        if (listOfReplies == null) {
            listOfReplies = new ArrayList<>();
            listOfReplies.add(successorList);
            allSuccessorsReplyTable.put(senderID, listOfReplies);
        } else {
            listOfReplies.add(successorList);
        }
    }

    public void addSuccessorToReplyTable(UUID senderID, ChordIdentifier successor) {
        ArrayList<ChordIdentifier> listOfReplies = successorReplyTable.get(senderID);

        if (listOfReplies == null) {
            listOfReplies = new ArrayList<>();
            listOfReplies.add(successor);
            successorReplyTable.put(senderID, listOfReplies);
        } else {
            listOfReplies.add(successor);
        }
    }

    public ChordIdentifier getSuccessorReply(UUID senderID) {
        ArrayList<ChordIdentifier> listOfReplies = successorReplyTable.get(senderID);
        if (listOfReplies == null || listOfReplies.isEmpty()) return null;
        else {
            ChordIdentifier reply = listOfReplies.get(listOfReplies.size() - 1);
            listOfReplies.remove(reply);
            return reply;
        }
    }

    public ChordIdentifier[] getAllSuccessorsReply(UUID senderID) {
        ArrayList<ChordIdentifier[]> listOfReplies = allSuccessorsReplyTable.get(senderID);
        if (listOfReplies == null || listOfReplies.isEmpty()) return null;
        else {
            ChordIdentifier[] reply = listOfReplies.get(listOfReplies.size() - 1);
            listOfReplies.remove(reply);
            return reply;
        }
    }

    public void removeAllSuccessorsFromReplyTable(UUID senderID, ChordIdentifier[] successors) {
        ArrayList<ChordIdentifier[]> listOfReplies = allSuccessorsReplyTable.get(senderID);
        if (listOfReplies != null) listOfReplies.remove(successors);
    }

    public void removeSuccessorFromReplyTable(UUID senderID, ChordIdentifier successor) {
        ArrayList<ChordIdentifier> listOfReplies = successorReplyTable.get(senderID);
        if (listOfReplies != null) listOfReplies.remove(successor);
    }

    private void joinFingerTable(InetSocketAddress knownPeerAddress) {
        SSLSocket socket = null;
        for (int i = 0; i < Clauses.MAX_RETRIES; i++) {
            try {
                socket = messageHandler.sendFindSuccessorRequest(peerID, knownPeerAddress);
            } catch (NoResponse noResponse) {
                if (i == Clauses.MAX_RETRIES - 1) {
                    ChordLogger.joinFingerTableFail();
                    exit(1);
                    return;
                } else {
                    try {
                        Thread.sleep(Clauses.TIMEOUT);
                    } catch (InterruptedException e) {
                        e.printStackTrace(); //log error
                    }
                    ChordLogger.tryingConnection(knownPeerAddress.getHostString(), knownPeerAddress.getPort());
                    continue;
                }
            }
            break;
        }

        messageHandler.listenToReply(socket);
        Peer.getTaskManager().schedule(new FetchSuccessorReply(peerID.getIdentifier()), Clauses.WAIT_FOR_REPLY * 2, TimeUnit.MILLISECONDS);
    }

    public void joinFingerTable(ChordIdentifier firstEntry) {
        atomicFingerTable.set(0, firstEntry);
        fixFingerTable();
        fixSuccessorList();

        chordStabilizer = new ChordStabilizer(this);
        Peer.getTaskManager().scheduleAtFixedRate(chordStabilizer, CHORD_STABILIZE_PERIOD, CHORD_STABILIZE_PERIOD, TimeUnit.MILLISECONDS);
    }

    public synchronized void fixSuccessorList() {
        if (atomicFingerTable.get(0) == peerID) return;

        SSLSocket socket;
        try {
            socket = messageHandler.sendGetSuccessorList(atomicFingerTable.get(0).getOwnerAddress(), false); //Send the message
        } catch (NoResponse noResponse) {
            return; //Couldn't connect to our successor :(
        }
        messageHandler.listenToReply(socket); //Wait for reply
    }

    public void fixSuccessorList(ChordIdentifier[] list) { //when the reply arrives
        if (list == null) return;

        atomicSuccessorList.set(0, atomicFingerTable.get(0));
        for (int i = 0; i < atomicSuccessorList.length() - 1; i++) {
            atomicSuccessorList.set(i + 1, list[i]);
        }

        if (!conected) {
            ChordLogger.successConnection();
            conected = true;
        }
    }

    public ChordIdentifier findSuccessor(ChordIdentifier id) throws AskClosestPredecessor {
        if (atomicFingerTable.get(0) != null && (id.isBetween(peerID, atomicFingerTable.get(0)) || id.isEqual(atomicFingerTable.get(0)))) {

            if (!messageHandler.pingPeer(atomicFingerTable.get(0).getOwnerAddress())) { //If successor is not online
                for (int i = 1; i < atomicSuccessorList.length(); i++) { //go through successorList
                    if (messageHandler.pingPeer(atomicSuccessorList.get(i).getOwnerAddress()))
                        return atomicSuccessorList.get(i);
                }
                return null;
            } else return atomicFingerTable.get(0);
        } else throw new AskClosestPredecessor();
    }

    public ChordIdentifier getClosestPredecessor(ChordIdentifier id, int closestSuccessorIndex, ArrayList<ChordIdentifier> visited) throws WaitingForClosestPredecessorReply {
        ChordIdentifier[] searchList = joinLists(atomicSuccessorList, atomicFingerTable);

        for (int i = searchList.length - 1; i >= 0; i--) {
            if (searchList[i] == null || searchList[i].isEqual(peerID)) continue;
            if (searchList[i] == null || searchList[i] == peerID || visited.contains(searchList[i])) continue;
            visited.add(searchList[i]);

            if (searchList[i].isBetween(peerID, id)) { //if it is a predecessor
                SSLSocket socket;
                try {
                    socket = messageHandler.sendFindSuccessorRequest(id, searchList[i].getOwnerAddress());
                } catch (NoResponse noResponse) {
                    continue; //Closest Predecessor didn't answer, continue search
                }
                messageHandler.listenToReply(socket);
                throw new WaitingForClosestPredecessorReply(visited, closestSuccessorIndex, searchList[i].getIdentifier());
            } else closestSuccessorIndex = i;
        }

        while (closestSuccessorIndex < searchList.length && closestSuccessorIndex > -1) {
            if (searchList[closestSuccessorIndex] != null && messageHandler.pingPeer(searchList[closestSuccessorIndex].getOwnerAddress()))
                return searchList[closestSuccessorIndex];
            closestSuccessorIndex++;
        }
        return peerID;
    }

    private ChordIdentifier[] joinLists(AtomicReferenceArray<ChordIdentifier> list1, AtomicReferenceArray<ChordIdentifier> list2) {
        ChordIdentifier[] tmpList = new ChordIdentifier[list1.length() + list2.length()];

        int k = 0;
        for (int i = 0, j = 0; i < list1.length() || j < list2.length(); k++) {
            ChordIdentifier node, n1 = null, n2 = null;
            if (i < list1.length()) n1 = list1.get(i);
            if (j < list2.length()) n2 = list2.get(j);

            if (n1 == null && n2 == null) { //if both are null
                i++;
                j++;
                continue;
            } else if (n1 != null && n1.isEqual(n2)) { //if n1 == n2
                node = n1;
                i++;
                j++;
            } else if (n2 == null || (n1 != null && n1.isLesserThan(n2) && (k <= 0 || n2.isLesserThan(tmpList[k - 1])))) { //if n2 is invalid or n1 < n2
                node = n1;
                i++;
            } else { //if n2 < n1 or n1 is invalid
                node = n2;
                j++;
            }

            if (k > 0 && node.isEqual(tmpList[k - 1])) {
                k--;
            } else tmpList[k] = node;
        }

        ChordIdentifier[] finalList = new ChordIdentifier[k];
        System.arraycopy(tmpList, 0, finalList, 0, finalList.length);

        return finalList;
    }

    public synchronized void fixFingerTable() { //called periodically to make sure everything stays updated
        for(int i = 1; i < atomicFingerTable.length(); i++) {
            UUID successor = new UUID(peerID.getIdentifier().getMostSignificantBits(), peerID.getIdentifier().getLeastSignificantBits() + (int)Math.pow(2,i));
            ChordIdentifier id = new ChordIdentifier(successor);

            ChordIdentifier newEntry;
            try {
                newEntry = findSuccessor(id);
            } catch (AskClosestPredecessor askClosestPredecessor) {
                try {
                    newEntry = getClosestPredecessor(id, -1, new ArrayList<>());
                } catch (WaitingForClosestPredecessorReply waitingForClosestPredecessorReply) {
                    Peer.getTaskManager().schedule(new FetchSuccessorReply(successor, waitingForClosestPredecessorReply, atomicFingerTable, i), WAIT_FOR_REPLY, TimeUnit.MILLISECONDS);
                    continue;
                }
            }
            atomicFingerTable.set(i, newEntry);
        }
    }

    public void tryNewPredecessor(ChordIdentifier Id) {
        if ((predecessorID == null && !Id.isEqual(peerID)) || (predecessorID != null && Id.isBetween(predecessorID, peerID))) {
            predecessorID = Id;
            Peer.getTaskManager().execute(new ManageSendTablesToPredecessor(predecessorID));
        }
    }

    public synchronized void checkPredecessor() {
        if (predecessorID != null && !messageHandler.pingPeer(predecessorID.getOwnerAddress())) {
            predecessorID = null;
            Peer.getTaskManager().execute(new HandlePredecessorCrash());
        }
    }

    public synchronized void stabilize() {
        if (atomicFingerTable.get(0).isEqual(peerID)) {
            stabilize(predecessorID);
            return;
        }

        SSLSocket socket;
        try {
            socket = messageHandler.sendGetPredecessorRequest(atomicFingerTable.get(0).getOwnerAddress());
        } catch (NoResponse noResponse) { //If we were unable to connect to our successor
            stabilizeNoResponse();
            return;
        }
        messageHandler.listenToReply(socket);
    }

    public void stabilize(ChordIdentifier predecessor) { //To be called by MessageReceiver
        if (predecessor != null && predecessor.isBetween(peerID, atomicFingerTable.get(0))) {
            atomicFingerTable.set(0, predecessor);
        }
    }

    public void stabilizeNoResponse() { //To be called by MessageReceiver in case of timeOut from successor
        for (int i = 1; i < atomicSuccessorList.length(); i++) {
            if (!atomicSuccessorList.get(i).isEqual(atomicFingerTable.get(0))) {
                atomicFingerTable.set(0, atomicSuccessorList.get(i)); //The other possibly failed nodes will be fixed in fixFingerTable
                return;
            }
        }
        atomicFingerTable.set(0, peerID); //this will pretty much never happen but it's better to keep it here just in case
    }

    public ChordIdentifier getPredecessor() {
        return predecessorID;
    }

    public ChordIdentifier getSuccessor() {
        return atomicSuccessorList.get(0);
    }

    public void changePredecessorOnDisconnect(ChordIdentifier oldPredecessor, ChordIdentifier newPredecessor) {
        if (!oldPredecessor.isEqual(predecessorID)) return;

        if (newPredecessor.isEqual(peerID)) predecessorID = null;
        else predecessorID = newPredecessor;
    }

    public void changeSuccessorOnDisconnect(ChordIdentifier oldSuccessor, ChordIdentifier newSuccessor) {
        if (!oldSuccessor.isEqual(atomicFingerTable.get(0))) return;


        int j = 1;
        while (j < atomicFingerTable.length() && atomicFingerTable.get(j).isEqual(oldSuccessor)) j++;
        for (int i = 0; i < j; i++) {
            atomicFingerTable.set(i, newSuccessor);
        }
    }

    public void disconnect() {
        if (atomicFingerTable.get(0) == null || predecessorID == null) return;
        messageHandler.sendDisconnectToSuccessor(atomicFingerTable.get(0).getOwnerAddress(), predecessorID);
        messageHandler.sendDisconnectToPredecessor(predecessorID.getOwnerAddress(), atomicFingerTable.get(0));
    }

    //This method returns the Address of the peer responsible for the key
    public InetSocketAddress lookup(UUID key) {
        ChordIdentifier id = new ChordIdentifier(key);
        ChordIdentifier lookupResult = null;

        try {
            lookupResult = findSuccessor(id);
        } catch (AskClosestPredecessor askClosestPredecessor) {
            try {
                lookupResult = getClosestPredecessor(id, -1, new ArrayList<>());
            } catch (WaitingForClosestPredecessorReply waitingForClosestPredecessorReply) {
                Future<InetSocketAddress> future = Peer.getTaskManager().schedule(new GetLookupResult(key, waitingForClosestPredecessorReply), WAIT_FOR_REPLY, TimeUnit.MILLISECONDS);
                try {
                    return future.get();
                } catch (InterruptedException | ExecutionException e) {
                    ChordLogger.getSuccessorFail();
                    return null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return lookupResult == null ? null : lookupResult.getOwnerAddress();
    }

    public ChordIdentifier getPeerID() {
        return peerID;
    }

    public ChordIdentifier[] getSuccessorList() {
        ChordIdentifier[] succList = new ChordIdentifier[ChordIdentifier.bitSize];
        for (int i = 0; i < succList.length; i++) succList[i] = atomicSuccessorList.get(i);
        return succList;
    }

    public ChordIdentifier[] getAllSuccessors(int nSuccessors, int offset) {
        nSuccessors += offset;
        List<ChordIdentifier> list = new ArrayList<>();
        ArrayList<ChordIdentifier> newSuccessors = new ArrayList<>();
        for (int i = 0; i < atomicSuccessorList.length(); i++) newSuccessors.add(atomicSuccessorList.get(i));

        while (!hasDoneFullCircle(newSuccessors, list) && list.size() <= nSuccessors) { //Fill list with more successors than asked for
            int lastIndex = newSuccessors.size() - 1;
            list.addAll(newSuccessors);
            if (list.size() >= nSuccessors) break;

            SSLSocket socket;
            ArrayList<ChordIdentifier> receivedSuccessors = null;
            while (lastIndex >= 0) {
                try {
                    socket = messageHandler.sendGetSuccessorList(newSuccessors.get(lastIndex).getOwnerAddress(), true); //Ask our lastSuccessor (lastIndex) to give us his successorList
                } catch (NoResponse noResponse) {
                    lastIndex--; //if lastSuccessor Fails then try the one before him, until we reach 0
                    continue;
                }

                messageHandler.listenToReply(socket); //Crete reply listener

                Future<ChordIdentifier[]> future = Peer.getTaskManager().schedule(new GetAllSuccessorsResult(newSuccessors.get(lastIndex).getIdentifier()), WAIT_FOR_REPLY, TimeUnit.MILLISECONDS); //crete getter for result of Reply
                try {
                    ChordIdentifier[] receivedList = future.get();
                    if (receivedList == null) { //reply didn't arrive before Timeout
                        lastIndex--;
                        continue;
                    } else receivedSuccessors = new ArrayList<>(Arrays.asList(future.get()));
                } catch (InterruptedException | ExecutionException e) {
                    ChordLogger.getListSuccessors(newSuccessors.get(lastIndex).getIdentifier());
                    lastIndex--; //if lastSuccessor fails then try the one before him, until we reach 0
                    continue;
                }

                break;
            }

            if (receivedSuccessors == null)
                break; //If receivedSuccessors is still null then no peer was able to reply to us, as such stop here
            else newSuccessors = receivedSuccessors;
        }

        if (offset > list.size()) offset = list.size();
        list = list.subList(offset, list.size());
        return list.toArray(new ChordIdentifier[0]);
    }

    private boolean hasDoneFullCircle(ArrayList<ChordIdentifier> successors, List<ChordIdentifier> list) { //Check if we've already gone around a full circle
        for (int i = 0; i < successors.size(); i++) {
            if (successors.get(i) == null || successors.get(i).isEqual(peerID) || (i != 0 && successors.get(i).isEqual(successors.get(0))) || (!list.isEmpty() && successors.get(i).isEqual(list.get(0)))) {
                successors = i == 0 ? new ArrayList<>() : new ArrayList<>(successors.subList(0, i));
                list.addAll(successors);
                return true;
            }
        }
        return false;
    }

}
