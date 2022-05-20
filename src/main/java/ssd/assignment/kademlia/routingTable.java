package ssd.assignment.kademlia;
import ssd.assignment.config.Utils;
import ssd.assignment.config.Constraints;

public class routingTable {
    private transient bucket[] buckets;
    public routingTable() {
        this.buckets = new bucket[Constraints.ID_LENGTH];
    }
}
