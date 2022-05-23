package kademlia;
import java.net.InetAddress;
import crypto.Crypto;

public class Node {
    private static final Crypto crypto = new Crypto();
    private byte[] nodeId;
    private InetAddress inetAddress;
    private int port;
    public Node(){}
    public String getHash() {
        return crypto.hash(this.toString());
    }

    @Override
    public String toString() {
        return this.inetAddress+""+ ""+this.port;
    }
}
