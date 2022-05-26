package kademlia;

import crypto.Crypto;
import grpcClient.DistributedClient;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
@Getter
@Setter
public class TripleNode {
    public static Crypto crypto = new Crypto();
    private String nodeId;
    private DistributedClient distributedClient;
    private String ip;
    private int port;
    public TripleNode(String ip,int port,DistributedClient distributedClient){
        this.distributedClient=distributedClient;
        this.ip=ip;
        this.port=port;
        //defend for sybil attack
        this.nodeId=crypto.hash(ip+Integer.toString(port)+Long.toString(new Date().getTime()));
    }

}
