package kademlia;
import java.net.InetAddress;

import config.Utils;
import crypto.Crypto;
import lombok.Getter;
import lombok.Setter;
import grpcClient.DistributedClient;

import static java.nio.charset.StandardCharsets.UTF_8;

@Getter
@Setter
public class Node {
    public RoutingTable routingtable;
    private static final Crypto crypto = new Crypto();
    private static final Utils utils = new Utils();
    //Binary number of 256 bits length
    private String nodeId;
    private InetAddress inetAddress;
    private int port;
    private TripleNode node;
    private DistributedClient client;
    public Node(TripleNode node, DistributedClient distributedClient){
        this.node=node;
        this.client=distributedClient;
        this.nodeId=node.getNodeId();
        this.routingtable = new RoutingTable(node,distributedClient);
    }
    public String getHash() {
        return crypto.hash(this.toString());
    }

    @Override
    public String toString() {
        return this.inetAddress+""+ ""+this.port;
    }
    //XOR metric for kademlia
    public long distanceXOR(String nextNodeId){
        if(this.nodeId.length()!=nextNodeId.length()){
            System.out.println("Error: different nodeId lengths");
            return -1;
        }
        String answer = "";
        for(int i = 0; i<this.nodeId.length();i++){
            answer+=Integer.toString(this.nodeId.charAt(i)^nextNodeId.charAt(i));
        }
        return Integer.parseInt(answer, 2);
    }
    public void addNode(TripleNode tripleNode){
        long distance=this.distanceXOR(tripleNode.getNodeId());
        int kBucket=(int)utils.log2(distance);
        if(routingtable.buckets[kBucket].containsTripleNode(tripleNode)){
            routingtable.buckets[kBucket].transferToLastPosition(tripleNode);
            return;
        }
        routingtable.buckets[kBucket].addTripleNode(this.node,tripleNode);
    }
}
