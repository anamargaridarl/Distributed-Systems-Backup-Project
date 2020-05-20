package base;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import static base.Clauses.ENHANCED_VERSION;
import static base.Clauses.VANILLA_VERSION;

public class PeerInitial {


    static String protocol_vs;
    static int s_id;

    static Registry registry;
    static String remote_name;

    static int port;

    static Peer obj;

    public static void main(String[] args) throws IOException {
        if (args.length != 4) {
            System.out.println("Invalid arguments. Please use: java PeerInitial <version> <peer_id> <remote_name> <port>");
            System.exit(1);
        }

        if (!(args[0].equals(VANILLA_VERSION) || args[0].equals(ENHANCED_VERSION))) {
            System.out.println("Invalid version of the peers userd. Use one of the following:\n - Vanilla version: 1.0\n - Enhanced version: 2.0");
            System.exit(1);
        }

        protocol_vs = args[0];
        s_id = Integer.parseInt(args[1]);
        remote_name = args[2];
        port = Integer.parseInt(args[3]);

        obj = new Peer(protocol_vs, s_id, port);

        open();

    }

    public static void open() throws RemoteException {

        try {
            registry = LocateRegistry.createRegistry(1099);
        } catch (RemoteException e) {
            System.out.println("Registry already activate. Won't create new one.");
            registry = LocateRegistry.getRegistry();
        }


        try {
            registry.rebind(remote_name, obj);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        System.out.println("Server ready");
    }


    public void close() {
        try {
            registry.unbind(remote_name);
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }
}
