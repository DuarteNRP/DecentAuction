package kademlia;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class myTriple {
    BinaryTreeNode binaryTreeNode;
    int cursor;
    String accumulatedBits;
    myTriple(BinaryTreeNode k, int c, String a){
        this.binaryTreeNode=k;
        this.cursor=c;
        this.accumulatedBits=a;
    }
}
