package kademlia;

import config.Utils;
import crypto.Crypto;
import grpcClient.DistributedClient;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
@Getter
@Setter
public class TripleNode implements Serializable {
    public static Crypto crypto = new Crypto();
    private static final Utils utils = new Utils();
    private String nodeId;
    private String ip;
    private int port;
    public TripleNode(String ip,int port){
        this.ip=ip;
        this.port=port;
        //defend for sybil attack
        this.nodeId=utils.getBinaryFromHash(utils.mineId(crypto.hash(ip+Integer.toString(port)+Long.toString(new Date().getTime()))));
        //this.nodeId=crypto.hash(ip+Integer.toString(port)+Long.toString(new Date().getTime()));
    }
    public TripleNode(String nodeId,String ip,int port){
        this.ip=ip;
        this.port=port;
        this.nodeId=nodeId;
    }
    @Override
    public String toString(){
        return nodeId;
    }
}
