package kademlia;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BinaryTreeNode {
    private BinaryTreeNode left;// bit 0
    private BinaryTreeNode right;//bit 1
    private Bucket kBucket;
    public String bitsPrefix;
    public BinaryTreeNode(Bucket bucket,String s){
        this.kBucket=bucket;
        this.bitsPrefix=s;
    }
    public BinaryTreeNode(BinaryTreeNode l, BinaryTreeNode r){
        this.left=l;
        this.right=r;
    }
    public void createNewLevel(Bucket left,Bucket right){
        this.left = new BinaryTreeNode(left,this.bitsPrefix+"0");
        this.right = new BinaryTreeNode(right,this.bitsPrefix+"1");
        this.kBucket=null;
    }
}
