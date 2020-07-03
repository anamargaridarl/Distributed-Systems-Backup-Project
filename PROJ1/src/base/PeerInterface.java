package base;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public interface PeerInterface extends Remote {

    int backup(String pathname, int rep_deg) throws RemoteException;

    int restore(String pathname) throws RemoteException;

    int delete(String pathname) throws RemoteException;

    int reclaim(int max_space) throws RemoteException;

    List<String> state() throws RemoteException;

}
