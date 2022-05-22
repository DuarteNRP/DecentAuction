package kademlia;
import config.Utils;
import config.Constraints;

public class routingTable {
    private transient bucket[] buckets;
    public routingTable() {
        this.buckets = new bucket[Constraints.ID_LENGTH];
    }
}
