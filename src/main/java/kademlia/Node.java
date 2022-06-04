package kademlia;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import config.Constraints;
import config.Utils;
import crypto.Crypto;
import lombok.Getter;
import lombok.Setter;
import grpcClient.DistributedClient;
import pubsubAuction.*;

import static java.nio.charset.StandardCharsets.UTF_8;

@Getter
@Setter
public class Node{
    public ConcurrentHashMap<String,byte[]> data;
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

    private Publisher pub = new Publisher(this);
    private Subscriber sub = new Subscriber(this);

    //TODO tem de ser o mesmo que a blockchain
    public Service auctionHouse = new Service();

	
    public Node(TripleNode node){
        this.node=node;
        this.nodeId=node.getNodeId();
        this.routingtable = new RoutingTable(node);
        this.data = new ConcurrentHashMap<>();
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
        System.out.println("Try to find kclosest from: " + tripleNode.getNodeId());
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
                System.out.println("Contains less than k nodes in route table");
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
        this.routingtable.printRouteTable();

    }
    public void pingNode(TripleNode tripleNode){
        this.distributedClient.sendPing(tripleNode);
    }
    public ArrayList<TripleNode> findNode(TripleNode tripleNode) throws InterruptedException {
        System.out.println("Começou");
        CopyOnWriteArrayList<TripleNode> closestNodes = new CopyOnWriteArrayList<>(this.findKClosestNodes(tripleNode));
        if(closestNodes.size()>constraints.K)
            closestNodes= (CopyOnWriteArrayList) closestNodes.subList(0,constraints.ALPHA);
        CopyOnWriteArrayList<TripleNode> kClosestNodes = new CopyOnWriteArrayList<>();
        CopyOnWriteArrayList<String> visited = new CopyOnWriteArrayList<>();
        while(true) {
            int size = (closestNodes.size()<constraints.ALPHA) ? closestNodes.size() : constraints.ALPHA;
            System.out.println("Ver o array closest");
            System.out.println(closestNodes);
            System.out.println(kClosestNodes);
            parallelFindNode(kClosestNodes,closestNodes,visited,tripleNode);
            if (utils.removeVisited(closestNodes, visited).size() == 0) {
                break;
            }
        }
        System.out.println("saiu");
        ArrayList<TripleNode> finalKClosestNodes = new ArrayList<>(kClosestNodes);
        utils.sortArrayList(finalKClosestNodes,tripleNode.getNodeId());
        System.out.println(finalKClosestNodes);
        if(finalKClosestNodes.size()>constraints.K){
            return new ArrayList<TripleNode>( finalKClosestNodes.subList(0,constraints.K));
        }
        return new ArrayList<>(finalKClosestNodes);
    }
    public void parallelFindNode(CopyOnWriteArrayList<TripleNode> kClosestNodes,CopyOnWriteArrayList<TripleNode> closestNodes,CopyOnWriteArrayList<String> visited,TripleNode target) throws InterruptedException {
        System.out.println("Chegou");
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(3);
        for(int i=0;i<closestNodes.size();i++){
            int index=i;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    TripleNode tripleNode = closestNodes.get(index);
                    distributedClient.findNode(closestNodes,tripleNode,target);
                    visited.add(tripleNode.getNodeId());
                    kClosestNodes.add(tripleNode);
                }
            });

            if(i==2) break;
        }
        executor.shutdown();
        System.out.println("A fazer threads ainda");
        Thread.sleep(5000);
        while(!executor.isTerminated()){}
        System.out.println("Acabou threads");
    }
    //find node sem ser paralelo, acho que o outro já está a funcionar
    public ArrayList<TripleNode> findNode1(TripleNode tripleNode) throws InterruptedException {
        System.out.println("Começou");
        CopyOnWriteArrayList<TripleNode> closestNodes = new CopyOnWriteArrayList<>(this.findKClosestNodes(tripleNode));
        if(closestNodes.size()>constraints.K)
            closestNodes= (CopyOnWriteArrayList) closestNodes.subList(0,constraints.ALPHA);
        CopyOnWriteArrayList<TripleNode> kClosestNodes = new CopyOnWriteArrayList<>();
        List<String> visited = Collections.synchronizedList(new ArrayList<>());
        while(true) {
            System.out.println("Ver o array closest");
            System.out.println(closestNodes);
            System.out.println(kClosestNodes);
            kClosestNodes.add(closestNodes.get(0));
            visited.add(closestNodes.get(0).getNodeId());
            distributedClient.findNode(closestNodes,closestNodes.get(0),tripleNode);
            if (utils.removeVisited(closestNodes, visited).size() == 0) {
                    break;
            }
        }
        System.out.println("saiu");
        ArrayList<TripleNode> finalKClosestNodes = new ArrayList<>(kClosestNodes);
        utils.sortArrayList(finalKClosestNodes,tripleNode.getNodeId());
        if(finalKClosestNodes.size()>constraints.K){
            return new ArrayList<TripleNode>( finalKClosestNodes.subList(0,constraints.K));
        }
        return new ArrayList<>(finalKClosestNodes);
    }
    public void store(TripleNode target,String key,byte[] value){
        distributedClient.storeValue(target,key,value);
    }

    //TODO
    //auction
    public void startNewAuction(ArrayList<Item> items){
	//TODO fix name
	String topic = "hashthis";
	Auction auction = new Auction(this, items);
	auctionHouse.setAuction(topic, auction);
	Message message = new Message(topic, auction);

    pub.publish(message, auctionHouse);
    sub.subscribe(topic, auctionHouse);

	//TODO
	//broadcast to everyone
    }

    public void makeBid(String topic, Item item, int value){
	Message message = new Message(topic, node+" just made a bid for item "+item);

	sub.subscribe(topic, auctionHouse);
	Auction auction = auctionHouse.getAuction(topic);
	auction.bid(this, item, value);
	auctionHouse.setAuction(topic, auction);
	pub.publish(message, auctionHouse);

	//TODO
	//broadcast to subsribers
	auctionHouse.broadcast();
    }

    void closeAuction(String topic){
	Auction auction = auctionHouse.getAuction(topic);

	Map<String, Bid> winners = auction.finish();
	for(String item : winners.keySet()){
	    Bid bid = winners.get(item);
	    String result = bid.getBidder()+" won item "+bid.getItem();
	    Message message = new Message(topic, result);
	    pub.publish(message, auctionHouse);
	}

	auctionHouse.close(auction);

	//TODO
	//boradcast to subscribers
	//commit to blockchain
	auctionHouse.broadcast();
    }
}
