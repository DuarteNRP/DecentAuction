import config.Constraints;
import config.Utils;
import crypto.Crypto;
import grpcClient.DistributedClient;
import kademlia.BinaryTreeNode;
import kademlia.Bucket;
import kademlia.Node;
import kademlia.TripleNode;

import java.math.BigInteger;
import java.util.ArrayList;

public class DecentAuction {
    private static final Utils utils = new Utils();
    private static final Crypto cripto = new Crypto();
    private static final Constraints constraints = new Constraints();
    public static void main(String[] args){
        //test kademlia buckets
        DecentAuction test = new DecentAuction();
        TripleNode tripleNode =new TripleNode("localhost",50000);
        DistributedClient d = new DistributedClient("localhost",50001);
        Node node = new Node(tripleNode);
        d.setNode(node);
        node.setDistributedClientClient(d);
        test.testTryAddNode(node);
        TripleNode tripleNode11 =new TripleNode("localhost",50001);
        System.out.print(test.testFindKClosest(node,tripleNode11));


    }
    public void testTryAddNode(Node node){
        TripleNode tripleNode1 =new TripleNode("localhost",50001);
        TripleNode tripleNode2 =new TripleNode("localhost",50002);
        TripleNode tripleNode3 =new TripleNode("localhost",50003);
        TripleNode tripleNode4 =new TripleNode("localhost",50004);
        TripleNode tripleNode5 =new TripleNode("localhost",50005);
        TripleNode tripleNode6 =new TripleNode("localhost",50006);
        TripleNode tripleNode7 =new TripleNode("localhost",50007);
        TripleNode tripleNode8 =new TripleNode("localhost",50008);
        TripleNode tripleNode9 =new TripleNode("localhost",50009);
        TripleNode tripleNode10 =new TripleNode("localhost",50010);
        node.tryToAddNode(tripleNode1);
        node.tryToAddNode(tripleNode2);
        node.tryToAddNode(tripleNode3);
        node.tryToAddNode(tripleNode4);
        node.tryToAddNode(tripleNode5);
        node.tryToAddNode(tripleNode6);
        node.tryToAddNode(tripleNode7);
        node.tryToAddNode(tripleNode8);
        node.tryToAddNode(tripleNode9);
        node.tryToAddNode(tripleNode10);
        node.printRouteTable();
    }
    public ArrayList<TripleNode> testFindKClosest(Node node, TripleNode triple){
        return node.findKClosestNodes(triple);
    }
}
