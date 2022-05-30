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
    public Bucket(ArrayList<TripleNode> arraylist){
        this.kBucket=arraylist;
    }
    private TripleNode node;

    public boolean containsTripleNode(TripleNode tripleNode) {
        boolean contains=false;
        TripleNode t=null;
        for(TripleNode current : kBucket){
            if(current.getNodeId().equals(tripleNode.getNodeId())) {
                t=current;
                contains = true;
            }
        }
        if(contains){
            kBucket.remove(t);
            kBucket.add(t);
            return true;
        }
        return false;
    }
    public boolean isNotFull(){
        return this.kBucket.size()<constraints.K;
    }
    public void addTripleNode(TripleNode node){
        kBucket.add(node);
    }
    @Override
    public String toString(){
        String text = "[";
        for(TripleNode t : kBucket){
            text+=t.toString()+",";
        }
        return text+="]";
    }
}
