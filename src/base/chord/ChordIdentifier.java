package base.chord;

import base.Clauses;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.io.Serializable;

import static base.Clauses.*;

public class ChordIdentifier implements Serializable {
    public static final int bitSize = Clauses.m;

    private final UUID identifier;
    private InetSocketAddress ownerAddress;

    public ChordIdentifier(InetSocketAddress address) {
        ownerAddress = address;
        identifier = hashChunk(address.getHostName(), address.getPort());
    }

    public ChordIdentifier(UUID identifier) {
        this.identifier = identifier;
    }

    private int compare(ChordIdentifier chordIdentifier) {
        return identifier.compareTo(chordIdentifier.identifier);
    }

    public boolean isGreaterThan(ChordIdentifier chordIdentifier) {
        return compare(chordIdentifier) > 0;
    }

    public boolean isLesserThan(ChordIdentifier chordIdentifier) {
        return compare(chordIdentifier) < 0;
    }

    public boolean isBetween(ChordIdentifier chord1, ChordIdentifier chord2) {
        if (chord1.isLesserThan(chord2)) return (isGreaterThan(chord1) && isLesserThan(chord2));
        else return (isGreaterThan(chord1) || isLesserThan(chord2));
    }

    public boolean isEqual(ChordIdentifier chordIdentifier) {
        if (chordIdentifier == null) return false;
        return compare(chordIdentifier) == 0;
    }

    public InetSocketAddress getOwnerAddress() {
        return ownerAddress;
    }

    public UUID getIdentifier() {
        return identifier;
    }

    public String toString() {
        String identifier_string = null;
        ObjectOutputStream stream;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            stream = new ObjectOutputStream(baos);
            stream.writeObject(identifier);
            stream.flush();
            byte[] identifier_bytes = baos.toByteArray();
            identifier_string = new String(identifier_bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                baos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        return identifier_string;
    }
}
