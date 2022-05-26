package kademlia;
import config.Constraints;
import grpcClient.DistributedClient;

public class RoutingTable {
    public transient Bucket[] buckets;
    private TripleNode node;
    private DistributedClient client;
    public RoutingTable(TripleNode node,DistributedClient client) {
        this.node=node;
        this.client=client;
        this.buckets = new Bucket[Constraints.ID_LENGTH];
        for(Bucket bucket : buckets){
            bucket.setNode(node);
            bucket.setClient(client);
        }
    }
}
