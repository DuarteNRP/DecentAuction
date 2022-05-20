package ssd.assignment.kademlia;

public class bucket {
    private final int k;
    private transient node[] nodes;
    public bucket(int k) {
        this.k = k;
        this.nodes = new node[k];
    }
}
