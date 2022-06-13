package kademlia;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class myTriple implements Serializable {
    BinaryTreeNode binaryTreeNode;
    int cursor;
    String accumulatedBits;
    myTriple(BinaryTreeNode k, int c, String a){
        this.binaryTreeNode=k;
        this.cursor=c;
        this.accumulatedBits=a;
    }
}
