package grpcClient;

import kademlia.Node;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Node n = new Node();
        ServerService s1 = new ServerService("localhost", 50001);
        try {
            s1.start();
            DistributedClient d1 = new DistributedClient("localhost", 50001);
            d1.sendPing(n);
            s1.blockUntilShutdown();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
