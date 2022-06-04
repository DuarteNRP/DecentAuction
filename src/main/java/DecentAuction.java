import ServiceGRPC.DataType;
import com.google.protobuf.ByteString;
import config.Constraints;
import config.Utils;
import crypto.Crypto;
import grpcClient.DistributedClient;
import grpcClient.ServerService;
import kademlia.BinaryTreeNode;
import kademlia.Bucket;
import kademlia.Node;
import kademlia.TripleNode;
import myBlockchain.Transaction;
import myBlockchain.Wallet;

import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Random;

public class DecentAuction {
    private static final Utils utils = new Utils();
    private static final Crypto cripto = new Crypto();
    private static final Constraints constraints = new Constraints();
    public static ArrayList<ServerService> bootstrapNodes = new ArrayList<>();
    static final ServerService serverService1 = new ServerService("localhost",50005);
    static final ServerService serverService2 = new ServerService("localhost",50006);
    public static void main(String[] args) throws IOException, InterruptedException, NoSuchAlgorithmException {
        //test kademlia buckets
        DecentAuction test = new DecentAuction();
        test.initializeBootstrapNodes();
        serverService1.start();
        serverService2.start();
        Node node = serverService1.getServiceNode();
        test.join(node);
        Node node1 = serverService2.getServiceNode();
        TripleNode testTripleNode = bootstrapNodes.get(0).getServiceTripleNode();
        test.join(node1);
        node.tryToAddNode(testTripleNode);
        test.testSendTransaction(node,testTripleNode,bootstrapNodes.get(0).getServiceNode());
        //test.testTryAddNode(node);
        //test.testTryAddNode(node1);
        //test.testFindKClosest(node,serverService2.getServiceTripleNode());
        //test.testPing(node,testTripleNode);
        //test.testFindNodes(node,testTripleNode);
        //test.testStore(node,testTripleNode,bootstrapNodes.get(0).getServiceNode());
        //test.testFindValue(node,testTripleNode);
        serverService1.blockUntilShutdown();
        serverService2.blockUntilShutdown();
        finalizeBootstrapNodes();
    }
    public static void initializeBootstrapNodes() throws IOException {
        //5 nodes to mitigate eclipse attack
        for(int i=0;i<5;i++){
            bootstrapNodes.add(new ServerService("localhost",5000+i));
            bootstrapNodes.get(i).start();
        }
        for(int i=0;i<5;i++){
            for(int j=0;j<5;j++){
                bootstrapNodes.get(i).getServiceNode().tryToAddNode(bootstrapNodes.get(j).getServiceTripleNode());
            }
        }
        for(int i=0;i<5;i++){
            bootstrapNodes.get(i).getServiceNode().printRouteTable();
        }
    }
    public static void finalizeBootstrapNodes() throws InterruptedException {
        //5 nodes to mitigate eclipse attack
        for(int i=0;i<5;i++){
            bootstrapNodes.get(i).blockUntilShutdown();
        }
    }
    public static void join(Node node) throws InterruptedException {
        Random generator = new Random();
        int random = generator.nextInt(5);
        System.out.println("Escolheu nó " + random + " como bootstrap: ");
        node.tryToAddNode(bootstrapNodes.get(random).getServiceTripleNode());
        node.printRouteTable();
        node.findNode(node.getNode());
        node.printRouteTable();
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
        System.out.println("Routing Table do nó: "+node.getNodeId());
        node.printRouteTable();
    }
    public ArrayList<TripleNode> testFindKClosest(Node node, TripleNode triple){
        utils.printTriples(node.findKClosestNodes(triple));
        return node.findKClosestNodes(triple);
    }
    public void testPing(Node node,TripleNode tripleNode) throws InterruptedException {
        node.pingNode(tripleNode);
        int port=tripleNode.getPort();
        tripleNode.setPort(2);
        node.pingNode(tripleNode);
        tripleNode.setPort(port);
        node.pingNode(tripleNode);
        serverService2.getServiceNode().printRouteTable();
    }
    public void testFindNodes(Node node,TripleNode tripleNode) throws InterruptedException {
        System.out.println("Find nodes");
        ArrayList<TripleNode> foundNodes = node.findNode(tripleNode);
        System.out.println("pritn kclosestnodes");
        System.out.println(foundNodes);
    }
    public void testStore(Node node,TripleNode target,Node targetNode) throws InterruptedException {
        byte[] value = "block data".getBytes();
        node.store(target,target.getNodeId(),value);
        //wait to insert value
        Thread.sleep(2000);
        System.out.println("Value inserted an it is: "+ new String(targetNode.getData().get(target.getNodeId())));
    }
    public void testFindValue(Node node,TripleNode target) throws InterruptedException {
        byte[] value = node.findValue(target.getNodeId());
        if(value!=null){
            System.out.println("Value founded and it is: "+new String(value));
        }
        else{
            System.out.println("Didn't find value");
        }
    }
    public void testSendTransaction(Node n,TripleNode target,Node targetNode) throws NoSuchAlgorithmException, InterruptedException {
        Wallet coinbase = new Wallet();
        Transaction transaction = new Transaction(coinbase.publicKey, coinbase.publicKey,100,null);
        System.out.println(n.getTransaction());
        System.out.println(targetNode.getTransaction());
        n.sendData(target,transaction, DataType.TRANSACTION);
        Thread.sleep(1000);
        if(targetNode.getTransaction()!=null)
            System.out.println(targetNode.getTransaction().amount);
    }
}
