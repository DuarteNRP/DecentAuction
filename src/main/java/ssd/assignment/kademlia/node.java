package ssd.assignment.kademlia;
import java.net.InetAddress;
import ssd.assignment.crypto.Crypto;

public class node {
    private static final Crypto crypto = new Crypto();
    private byte[] nodeId;
    private InetAddress inetAddress;
    private int port;

    public String getHash() {
        return crypto.hash(this.toString());
    }

    @Override
    public String toString() {
        return this.inetAddress+""+ ""+this.port;
    }
}
