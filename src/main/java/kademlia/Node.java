package kademlia;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import config.Constraints;
import config.Utils;
import crypto.Crypto;
import lombok.Getter;
import lombok.Setter;
import grpcClient.DistributedClient;

import static java.nio.charset.StandardCharsets.UTF_8;

@Getter
@Setter
public class Node{
    public Map<String,byte[]> data;
    public RoutingTable routingtable;
    private static final Crypto crypto = new Crypto();
    private static final Utils utils = new Utils();
    private static final Constraints constraints = new Constraints();
    //Binary number of 256 bits length
    private String nodeId;
    private InetAddress inetAddress;
    private int port;
    private TripleNode node;
    private DistributedClient distributedClientClient;
    public Node(TripleNode node){
        this.node=node;
        this.nodeId=node.getNodeId();
        this.routingtable = new RoutingTable(node);
        this.data = new HashMap<>();
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
        System.out.println("Try to add node: "+tripleNode.getNodeId());
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
            System.out.println("Already contains node!");
            return;
        }
        //if kBucket not empty, just add
        if(kBucket.isNotFull()) {
            kBucket.addTripleNode(tripleNode);
            System.out.println("Added to kbucket: "+kBucket.toString());
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
                System.out.println("Added to kbucket: "+kBucket.toString());
                return;
            }
        }
        System.out.println("Not in range, so not added!");
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
}
