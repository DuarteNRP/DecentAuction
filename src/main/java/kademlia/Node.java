package kademlia;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;

import ServiceGRPC.Data;
import ServiceGRPC.DataType;
import config.Constraints;
import config.Utils;
import crypto.Crypto;
import lombok.Getter;
import lombok.Setter;
import grpcClient.DistributedClient;
import myBlockchain.*;
import pubsubAuction.*;

import static java.nio.charset.StandardCharsets.UTF_8;

@Getter
@Setter
public class Node implements Serializable {
    public Wallet wallet;
    public ConcurrentHashMap<String,byte[]> data;
    public ArrayList<Transaction> transactionPool = null;
    public Block block = null;
    public Chain chain = null;
    public RoutingTable routingtable;
    private static final Crypto crypto = new Crypto();
    private static final Utils utils = new Utils();
    private static final Constraints constraints = new Constraints();
    //Binary number of 256 bits length
    private String nodeId;
    private InetAddress inetAddress;
    private int port;
    private TripleNode node;
    private DistributedClient distributedClient;
    private Publisher pub;
    private Subscriber sub;
    public CopyOnWriteArrayList<String> broadcastId = new CopyOnWriteArrayList<>();

    //TODO tem de ser o mesmo na rede, aka DHT
    public Service auctionHouse = new Service();
    public Node(TripleNode node) throws NoSuchAlgorithmException {
        this.node=node;
        this.nodeId=node.getNodeId();
        this.routingtable = new RoutingTable(node);
        this.data = new ConcurrentHashMap<>();
        this.wallet = new Wallet();
        BlockChainThread thread = new BlockChainThread("Refresh blockchain and Id list",broadcastId);
        sub = new Subscriber(this);
        pub = new Publisher(this);
    }
    public String getHash() {
        return crypto.hash(this.toString());
    }

    @Override
    public String toString() {
        return this.inetAddress+""+ ""+this.port;
    }
    //XOR metric for kademlia
    public long distanceXOR(String nextNodeId){
        if(this.nodeId.length()!=nextNodeId.length()){
            System.out.println("Error: different nodeId lengths");
            return -1;
        }
        String answer = "";
        for(int i = 0; i<this.nodeId.length();i++){
            answer+=Integer.toString(this.nodeId.charAt(i)^nextNodeId.charAt(i));
        }
        return Integer.parseInt(answer, 2);
    }
    public void tryToAddNode(TripleNode tripleNode){
        //System.out.println("Try to add node: "+tripleNode.getNodeId());
        //String binaryNodeId=utils.getBinaryFromHash(tripleNode.getNodeId());
        if(tripleNode.getNodeId().equals(this.getNodeId()))
            return;
        String binaryNodeId = tripleNode.getNodeId();
        BinaryTreeNode currentTreeNode = routingtable.getRootBinaryTreeNode();
        int bitCursor = 0;
        String accumulatedBits="";
        //while we don't find a leaf
        myTriple triple = this.lookUp(currentTreeNode,binaryNodeId,bitCursor,accumulatedBits);
        currentTreeNode = triple.getBinaryTreeNode();
        Bucket kBucket = currentTreeNode.getKBucket();
        bitCursor=triple.getCursor();
        accumulatedBits=triple.getAccumulatedBits();
        if(kBucket.containsTripleNode(tripleNode)) {
            //System.out.println("Already contains node!");
            return;
        }
        //if kBucket not empty, just add
        if(kBucket.isNotFull()) {
            kBucket.addTripleNode(tripleNode);
            //System.out.println("Added to kbucket: "+kBucket.toString());
            return;
        }
        while(this.isInRange(bitCursor,accumulatedBits)){
            ArrayList<TripleNode> left = new ArrayList<>();
            ArrayList<TripleNode> right = new ArrayList<>();
            this.createTemporaryLeafs(left,right,bitCursor,currentTreeNode);
            currentTreeNode.createNewLevel(new Bucket(left),new Bucket(right));
            if(binaryNodeId.charAt(bitCursor)=='0') {
                accumulatedBits+="0";
                currentTreeNode = currentTreeNode.getLeft();
            }
            else {
                accumulatedBits+="1";
                currentTreeNode=currentTreeNode.getRight();
            }
            kBucket=currentTreeNode.getKBucket();
            bitCursor++;
            if(kBucket.isNotFull()) {
                kBucket.addTripleNode(tripleNode);
                //System.out.println("Added to kbucket: "+kBucket.toString());
                return;
            }
        }
        //System.out.println("Not in range, so not added!");
    }
    public myTriple lookUp(BinaryTreeNode currentTreeNode, String binaryNodeId, int bitCursor,String accumulatedBits){
        while(currentTreeNode.getKBucket()==null){
            if(binaryNodeId.charAt(bitCursor)=='0') {
                accumulatedBits+="0";
                currentTreeNode = currentTreeNode.getLeft();
            }
            else {
                accumulatedBits+="1";
                currentTreeNode = currentTreeNode.getRight();
            }
            bitCursor++;
        }
        return new myTriple(currentTreeNode,bitCursor,accumulatedBits);
    }
    public void createTemporaryLeafs(ArrayList<TripleNode> left,ArrayList<TripleNode> right,int cursor,BinaryTreeNode currentTreeNode){
        for(TripleNode t: currentTreeNode.getKBucket().getKBucket()){
            //String currentBitNodeId = utils.getBinaryFromHash(t.getNodeId());
            String currentBitNodeId = t.getNodeId();
            if(currentBitNodeId.charAt(cursor)=='0'){
                left.add(t);
            }
            else right.add(t);
        }
    }
    public boolean isInRange(int cursor,String accumulatedBits){
        if(cursor==0) return true;
        //String binaryNodeId=utils.getBinaryFromHash(this.nodeId);
        String binaryNodeId=this.nodeId;
        for(int i=0;i<cursor;i++){
            if((accumulatedBits.charAt(i)^binaryNodeId.charAt(i))!=0){
                return false;
            }
        }
        return true;
    }
    public ArrayList<TripleNode> findKClosestNodes(TripleNode tripleNode){
        //System.out.println("Try to find kclosest from: " + tripleNode.getNodeId());
        ArrayList<Bucket> visited= new ArrayList<>();
        int kClosest=constraints.K;
        ArrayList<TripleNode> closestNodes;
        //String binaryNodeId=utils.getBinaryFromHash(tripleNode.getNodeId());
        String binaryNodeId=tripleNode.getNodeId();
        BinaryTreeNode currentTreeNode = routingtable.getRootBinaryTreeNode();
        int bitCursor = 0;
        String accumulatedBits="";
        //while we don't find a leaf
        myTriple triple = this.lookUp(currentTreeNode,binaryNodeId,bitCursor,accumulatedBits);
        currentTreeNode = triple.getBinaryTreeNode();
        Bucket kBucket = currentTreeNode.getKBucket();
        accumulatedBits=triple.getAccumulatedBits();
        closestNodes=(ArrayList<TripleNode>) kBucket.getKBucket().clone();
        visited.add(kBucket);
        kClosest-=closestNodes.size();
        int changeBit;
        //flag to change to first prefix bits
        while(kClosest>0){
            String newPrefix;
            String fakeNodeId;
            changeBit=accumulatedBits.length()-1;
            if(changeBit==-1){
                //System.out.println("Contains less than k nodes in route table");
                break;
            }
            fakeNodeId=binaryNodeId.substring(changeBit+1);
            if(accumulatedBits.charAt(changeBit)=='0'){
                newPrefix=accumulatedBits.substring(0,changeBit)+"1";
            }
            else
                newPrefix=accumulatedBits.substring(0,changeBit)+"0";
            fakeNodeId=newPrefix + ""+ fakeNodeId;
            triple=this.lookUp(routingtable.getRootBinaryTreeNode(),fakeNodeId,0,"");
            kBucket = triple.getBinaryTreeNode().getKBucket();
            if(visited.contains(kBucket)){
                accumulatedBits=accumulatedBits.substring(0,changeBit);
                continue;
            }
            accumulatedBits=triple.getAccumulatedBits();
            closestNodes.addAll(kBucket.getKBucket());
            visited.add(kBucket);
            kClosest-=closestNodes.size();
            }
        utils.sortArrayList(closestNodes,binaryNodeId);
        if(closestNodes.size()>constraints.K){
            return new ArrayList<TripleNode>( closestNodes.subList(0,constraints.K));
        }
        return closestNodes;
    }
    public void printRouteTable() {
        System.out.println("Routing Table of Node: "+this.getNodeId());
        this.routingtable.printRouteTable();

    }
    public void pingNode(TripleNode tripleNode) throws InterruptedException {
        this.distributedClient.sendPing(tripleNode);
    }
    public ArrayList<TripleNode> findNode(TripleNode tripleNode) throws InterruptedException {
        CopyOnWriteArrayList<TripleNode> closestNodes = new CopyOnWriteArrayList<>(this.findKClosestNodes(tripleNode));
        if(closestNodes.size()>constraints.K)
            closestNodes= (CopyOnWriteArrayList) closestNodes.subList(0,constraints.ALPHA);
        CopyOnWriteArrayList<TripleNode> kClosestNodes = new CopyOnWriteArrayList<>();
        CopyOnWriteArrayList<String> visited = new CopyOnWriteArrayList<>();
        while(true) {
            //System.out.println("Ver o array closest");
            //System.out.println(closestNodes);
            //System.out.println(kClosestNodes);
            parallelFindNode(kClosestNodes,closestNodes,visited,tripleNode);
            Thread.sleep(2000);
            if(tripleNode.getIp().equals("") && this.getData().containsKey(tripleNode.getNodeId())){
                break;
            }
            if (utils.removeVisited(closestNodes, visited).size() == 0) {
                break;
            }
        }
        ArrayList<TripleNode> finalKClosestNodes = new ArrayList<>(kClosestNodes);
        utils.sortArrayList(finalKClosestNodes,tripleNode.getNodeId());
        if(finalKClosestNodes.size()>constraints.K){
            return new ArrayList<>(finalKClosestNodes.subList(0, constraints.K));
        }
        return new ArrayList<>(finalKClosestNodes);
    }
    public void parallelFindNode(CopyOnWriteArrayList<TripleNode> kClosestNodes,CopyOnWriteArrayList<TripleNode> closestNodes,CopyOnWriteArrayList<String> visited,TripleNode target) throws InterruptedException {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(constraints.K);
        for(int i=0;i<closestNodes.size();i++){
            int index=i;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    TripleNode tripleNode = closestNodes.get(index);
                    if(!target.getIp().equals("")) {
                        try {
                            distributedClient.findNode(closestNodes,tripleNode,target);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    else {
                        try {
                            distributedClient.findValue(closestNodes,tripleNode,target.getNodeId());
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if(!visited.contains(tripleNode.getNodeId())) {
                        visited.add(tripleNode.getNodeId());
                        kClosestNodes.add(tripleNode);
                    }
                }
            });

            if(i==constraints.ALPHA-1) break;
        }
        executor.shutdown();
        while(!executor.isTerminated()){}
    }
    //find node sem ser paralelo, acho que o outro já está a funcionar
    public void store(TripleNode target,String key,byte[] value) throws InterruptedException {
        distributedClient.storeValue(target,key,value,this.node);
    }
    public byte[] findValue(String key) throws InterruptedException {
        ArrayList<TripleNode> tripleNodes=this.findNode(new TripleNode(key,"",0));
        if(this.getData().containsKey(key)) {
            for (TripleNode tripleNode : tripleNodes) {
                this.store(tripleNode, key, this.getData().get(key));
            }
            return this.getData().get(key);
        }
        return null;
    }

    public void startNewAuction(ArrayList<Item> items/*, TripleNode target*/) throws IOException {
        //TODO fix name
        String topic = "someAuction";
        Auction auction = new Auction(this, items);
        System.out.println("node: "+auctionHouse.getOpenAuctions().size());
        auctionHouse.setAuction(topic, auction);
        System.out.println("node: "+auctionHouse.getOpenAuctions().get(0));
        Message message = new Message(topic, auction);

        pub.publish(message, auctionHouse);
        sub.subscribe(topic, auctionHouse);

        //this.distributedClient.sendData(utils.serialize(this.auctionHouse),target,DataType.AUCTION, "p00p");
        this.broadcast(utils.serialize(this.auctionHouse),topic,DataType.AUCTION);
    }

    public void makeBid(String topic, Item item, int value/*, TripleNode target*/) throws IOException {
        Message message = new Message(topic, node+" just made a bid for item "+item);

        sub.subscribe(topic, auctionHouse);
        Auction auction = auctionHouse.getAuction(topic);
        auction.bid(this, item, value);
        auctionHouse.setAuction(topic, auction);
        pub.publish(message, auctionHouse);

        //distributedClient.sendData(utils.serialize(this.auctionHouse),target,DataType.AUCTION, "p00p");
        this.broadcast(utils.serialize(this.auctionHouse),topic,DataType.AUCTION);
    }

    public void closeAuction(String topic/*, TripleNode target*/) throws IOException {
        Auction auction = auctionHouse.getAuction(topic);

        Map<String, Bid> winners = auction.finish();
        for(String item : winners.keySet()){
            Bid bid = winners.get(item);
            String result = bid.getBidder()+" won item "+bid.getItem();
            Message message = new Message(topic, result);
            pub.publish(message, auctionHouse);
        }
        auctionHouse.close(topic);

        //distributedClient.sendData(utils.serialize(this.auctionHouse),target,DataType.AUCTION, "p00p");
        this.broadcast(utils.serialize(this.auctionHouse),topic,DataType.AUCTION);
    }
    public void retrieveSubscribedMessages(){auctionHouse.retrieveSubscribedMessages(this);}

    ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(constraints.K);
    public void broadcast(byte[] arr, String identifier, DataType dataType){
        ArrayList<TripleNode> allNodes= allNodes(this.routingtable.getRootBinaryTreeNode());
        for(int i=0;i<allNodes.size();i++) {
            distributedClient.sendData(arr, allNodes.get(i), dataType, identifier);
        }
    }
    public ArrayList<TripleNode> allNodes(BinaryTreeNode root){
        if(root==null)
            return new ArrayList<>();
        if(root.getRight()==null && root.getLeft()==null){
            return root.getKBucket().getKBucket();
        }
        ArrayList<TripleNode> left = allNodes(root.getLeft());
        ArrayList<TripleNode> right = allNodes(root.getRight());
        left.addAll(right);
        return left;
    }

    public void print() {
        System.out.println("==========================================");
        sub.printMessages();
        System.out.println("==========================================");
    }
}
