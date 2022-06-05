package kademlia;
import com.sun.source.doctree.SerialDataTree;
import config.Constraints;
import grpcClient.DistributedClient;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
@Getter
@Setter
public class RoutingTable implements Serializable {
    public static Constraints constraints = new Constraints();
    public long numberOfContacts;
    private TripleNode node;
    private BinaryTreeNode rootBinaryTreeNode;
    public RoutingTable(TripleNode node) {
        this.node=node;
        this.numberOfContacts=0;
        this.rootBinaryTreeNode=new BinaryTreeNode(new Bucket(),"");
    }
    public void printRouteTable() {
        System.out.println("------------------");
        print(this.rootBinaryTreeNode);
        System.out.println("------------------");
    }
    public void print(BinaryTreeNode root){
        if(root==null)
            return;
        if(root.getRight()==null && root.getLeft()==null){
            System.out.println("prefix: "+root.getBitsPrefix());
            System.out.println("kbucket: "+root.getKBucket().toString());
            return;
        }
        print(root.getLeft());
        print(root.getRight());
    }
}
