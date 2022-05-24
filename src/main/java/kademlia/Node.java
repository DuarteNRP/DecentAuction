package kademlia;
import java.net.InetAddress;

import config.Utils;
import crypto.Crypto;
import lombok.Getter;
import lombok.Setter;

import static java.nio.charset.StandardCharsets.UTF_8;

@Getter
@Setter
public class Node {
    public RoutingTable routingtable;
    private static final Crypto crypto = new Crypto();
    private static final Utils utils = new Utils();
    //Binary number of 256 bits length
    private byte[] nodeId;
    private byte[] key;
    private byte[] value;
    private InetAddress inetAddress;
    private int port;
    public Node(byte[] nodeId){
        this.nodeId=nodeId;
        this.key=utils.getBytesFromString(crypto.hash(utils.getStringFromBytes(nodeId)));
        this.routingtable = new RoutingTable();
    }
    public String getHash() {
        return crypto.hash(this.toString());
    }

    @Override
    public String toString() {
        return this.inetAddress+""+ ""+this.port;
    }
    //XOR metric for kademlia
    public byte[] distanceXOR(byte[] nextNodeId){
        if(this.nodeId.length!=nextNodeId.length){
            System.out.println("Error: different nodeId lengths");
            return null;
        }
        String answer = "";
        for(int i = 0; i<this.nodeId.length;i++){
            answer+=Integer.toString(this.nodeId[i]^nextNodeId[i]);
        }
        return utils.getBytesFromString(answer);
    }
}
