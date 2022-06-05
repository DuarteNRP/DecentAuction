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
import myBlockchain.Chain;
import myBlockchain.Transaction;
import myBlockchain.Wallet;
import pubsubAuction.Item;

import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DecentAuction {
    private static final Utils utils = new Utils();
    private static final Crypto cripto = new Crypto();
    private static final Constraints constraints = new Constraints();
    public static ArrayList<ServerService> bootstrapNodes = new ArrayList<>();
    public static Chain initialBlockChain = new Chain();
    static final ServerService serverService1;

    static {
        try {
            serverService1 = new ServerService("localhost",50005);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    static final ServerService serverService2;

    static {
        try {
            serverService2 = new ServerService("localhost",50006);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException, NoSuchAlgorithmException {
        //test kademlia buckets
        DecentAuction test = new DecentAuction();
        test.initializeBootstrapNodes();
        serverService1.start();
        serverService2.start();
        test.broadcastInitialBlockChain();
        Node node = serverService1.getServiceNode();
        //st.join(node);
        Node node1 = serverService2.getServiceNode();
        TripleNode testTripleNode = node1.getNode();
        //test.join(node1);
        node.tryToAddNode(testTripleNode);
        //test.testBroadcast(node);
        //test.testSendTransaction(node,testTripleNode,bootstrapNodes.get(0).getServiceNode());
        //test.testSimpleAuction(node,testTripleNode,node1);
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
    public static void initializeBootstrapNodes() throws IOException, NoSuchAlgorithmException {
        //5 nodes to mitigate eclipse attack
        for(int i=0;i<4;i++){
            bootstrapNodes.add(new ServerService("localhost",5000+i));
            System.out.println("Created bootstrap with id: "+bootstrapNodes.get(i).getServiceNode().getNodeId());
            bootstrapNodes.get(i).start();
        }
        for(int i=0;i<4;i++){
            for(int j=0;j<4;j++){
                bootstrapNodes.get(i).getServiceNode().tryToAddNode(bootstrapNodes.get(j).getServiceTripleNode());
            }
        }
        for(int i=0;i<4;i++){
            bootstrapNodes.get(i).getServiceNode().printRouteTable();
        }
    }
    public static void finalizeBootstrapNodes() throws InterruptedException {
        //5 nodes to mitigate eclipse attack
        for(int i=0;i<4;i++){
            bootstrapNodes.get(i).blockUntilShutdown();
        }
    }
    public static void broadcastInitialBlockChain() throws IOException, InterruptedException {
        for(ServerService s :bootstrapNodes){
            System.out.println(s.getServiceNode().getNodeId()+","+s.getServiceNode().chain);
        }
        bootstrapNodes.get(0).getServiceNode().broadcast(utils.serialize(initialBlockChain), initialBlockChain.getBlockChainHash(),DataType.BLOCKCHAIN);
        Thread.sleep(1000);
        for(ServerService s :bootstrapNodes){
            System.out.println(s.getServiceNode().getNodeId()+","+s.getServiceNode().chain.getBlockChainHash());
        }
    }
    public static void join(Node node) throws InterruptedException {
        Random generator = new Random();
        int random = generator.nextInt(4);
        System.out.println("Escolheu nó " + random + " como bootstrap: ");
        node.tryToAddNode(bootstrapNodes.get(random).getServiceTripleNode());
        node.printRouteTable();
        node.findNode(node.getNode());
        node.printRouteTable();
        System.out.println(node.allNodes(node.routingtable.getRootBinaryTreeNode()));
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
    public void testBroadcast(Node n) throws NoSuchAlgorithmException, InterruptedException, IOException {
        System.out.println("Começou o broadcast");
        Wallet coinbase = new Wallet();
        Transaction transaction = new Transaction(coinbase.publicKey, coinbase.publicKey,100,null);
        for(ServerService s :bootstrapNodes){
            System.out.println(s.getServiceNode().broadcastId.contains("test"));
        }
        n.broadcast(utils.serialize(transaction),"test",DataType.TRANSACTION);
        Thread.sleep(5000);
        System.out.println("Chegou");
        for(ServerService s :bootstrapNodes){
            System.out.println("node:"+s.getServiceNode().getNodeId());
            System.out.println(s.getServiceNode().broadcastId.contains("test"));
        }
    }
    public void testSimpleAuction(Node n,TripleNode target,Node targetNode) throws IOException, InterruptedException {
        Item item = new Item("first Item");
        ArrayList<Item> list = new ArrayList<>();
        list.add(item);
        list.add(new Item("Second Item"));
        n.startNewAuction(list);
        Thread.sleep(1000);
        System.out.println(targetNode.getAuctionHouse().getOpenAuctions().get("hashthis"));
        targetNode.makeBid("hashthis",targetNode.getAuctionHouse().getOpenAuctions().get("hashthis").getItems().get(0),100);
        Thread.sleep(1000);
        n.closeAuction("hashthis");
        Thread.sleep(1000);
        n.getAuctionHouse().getMessagesOfTopic("hashthis",n.getSub());
        targetNode.getAuctionHouse().getMessagesOfTopic("hashthis",targetNode.getSub());
        System.out.println(n.getAuctionHouse().getOpenAuctions());
        System.out.println(targetNode.getAuctionHouse().getOpenAuctions());
    }
}
