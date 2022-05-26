package kademlia;

import config.Constraints;
import grpcClient.DistributedClient;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
@Getter
@Setter
public class Bucket {
    public static Constraints constraints = new Constraints();
    private ArrayList<TripleNode> kBucket;
    public Bucket() {
        this.kBucket = new ArrayList<>();
    }
    private TripleNode node;
    private DistributedClient client;

    public boolean containsTripleNode(TripleNode tripleNode) {
        return kBucket.contains(tripleNode);
    }
    public void addTripleNode(TripleNode currentTripleNode, TripleNode newTripleNode){
        if(kBucketIsFull()) {
            client.sendPing(kBucket.get(0));
            return;
        }
        kBucket.add(newTripleNode);
    }
    public boolean kBucketIsFull(){
        return this.kBucket.size()<constraints.K;
    }
    public void transferToLastPosition(TripleNode tripleNode){
        kBucket.remove(tripleNode);
        kBucket.add(tripleNode);
    }
}
