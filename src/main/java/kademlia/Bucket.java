package kademlia;

public class Bucket {
    private final int k;
    private transient Node[] nodes;
    public Bucket(int k) {
        this.k = k;
        this.nodes = new Node[k];
    }
}
