package kademlia;
import config.Constraints;

public class RoutingTable {
    private transient Bucket[] buckets;
    public RoutingTable() {
        this.buckets = new Bucket[Constraints.ID_LENGTH];
    }
}
