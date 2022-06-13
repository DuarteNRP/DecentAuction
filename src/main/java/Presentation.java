import ServiceGRPC.DataType;
import config.Constraints;
import config.Utils;
import crypto.Crypto;
import grpcClient.ServerService;
import kademlia.Node;
import kademlia.TripleNode;
import myBlockchain.Chain;
import myBlockchain.Transaction;
import myBlockchain.TransactionOutput;
import myBlockchain.Wallet;
import pubsubAuction.Auction;
import pubsubAuction.Item;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Scanner;

public class Presentation{
    private static final Crypto crypto = new Crypto();
    private static final Utils utils = new Utils();
    private static final Constraints constraints = new Constraints();
    static ServerService service=null;
    static Node node=null;
    static Chain initialChain =null;
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        Presentation presentation = new Presentation();
        if(args[0].equals("0")){
            service = new ServerService("localhost",50000);
            service.start();
            node = service.getServiceNode();
            System.out.println("Welcome bootstrap node with nodeId: "+service.getServiceNode().getNodeId() + " ip: "+service.getIp()+" port: "+service.getPort());
            System.out.println("Create initial Blockchain");
            presentation.setInitialBlockChain();
            node.wallet.setBlockchain(node.chain);
        }
        else{
            service = new ServerService("localhost",50000+Integer.parseInt(args[0]));
            service.start();
            node = service.getServiceNode();
            System.out.println("Welcome node " + args[0] + " with nodeId: "+service.getServiceNode().getNodeId() + " ip: "+service.getIp()+" port: "+service.getPort());
            service.getServiceNode().printRouteTable();
            Thread.sleep(1000);
            System.out.println("Join the network");
            node.pingNode(new TripleNode("00000","localhost",50000));
            Thread.sleep(1000);
            node.findNode(node.getNode());
            Thread.sleep(1000);
            service.getServiceNode().printRouteTable();
            ArrayList<TripleNode> firstTripleNode = node.allNodes(node.routingtable.getRootBinaryTreeNode());
            node.askMessages(firstTripleNode.get(0));
            Thread.sleep(1000);
        }
        boolean flag=false;
        System.out.println();
        while(true){
            System.out.println("Choose one option:");
            System.out.println("(1) Print actual blockchain");
            System.out.println("(2) Wallet get balance");
            System.out.println("(3) Start new Auction");
            System.out.println("(4) View open Auctions");
            System.out.println("(5) Make bid");
            System.out.println("(6) Close auction");
            System.out.println("(7) Check Routing Table");
            System.out.println("(8) Print auctions messages");
            System.out.println("(9) Transactions Pool size");
            System.out.println("(10) Exit");
            System.out.println();
            int answer = scanner.nextInt();
            switch (answer){
                case 1:
                    if(node.getChain()==null)
                        System.out.println("No Blockchain yet!");
                    else {
                        node.getChain().printBlockChain();
                    }
                    break;
                case 2:
                    System.out.println(node.wallet.getBalance());
                    //System.out.println(Utils.bytesToPublicKey(node.wallet.getPublicKey()));
                    //System.out.println(node.wallet.getPublicKey());
                    break;
                case 3:
                    System.out.println();
                    System.out.println("Choose item to sell");
                    scanner.nextLine();
                    String item1 = scanner.nextLine();
                    Item item = new Item(item1);
                    ArrayList<Item> list = new ArrayList<>();
                    list.add(item);
                    node.startNewAuction(list);
                    break;
                case 4:
                    System.out.println(node.getAuctionHouse().getOpenAuctions());
                    break;
                case 5:
                    System.out.println("Choose auction!");
                    ArrayList<Auction> auctionList = new ArrayList<>();
                    ArrayList<String> stringList = new ArrayList<>();
                    Map<String, Auction> auctions = node.getAuctionHouse().getOpenAuctions();
                    for(String topic : auctions.keySet()) {
                        auctionList.add(auctions.get(topic));
                        stringList.add(topic);
                    }
                    int cnt=1;
                    for(Auction auction : auctionList){
                        System.out.println("["+(cnt++)+"] " + auction);
                    }
                    int auction1 = scanner.nextInt();
                    Auction auctionPick = auctionList.get(auction1-1);
                    String topicPick = stringList.get(auction1-1);
                    scanner.nextLine();
                    System.out.println("Choose item to make bid!");
                    System.out.println(auctionPick.getItems());
                    String itemToMakeBid = scanner.nextLine();
                    Item itemPick=null;
                    for(Item _item: auctionPick.getItems()){
                        if(_item.getItemID().equals(itemToMakeBid)) {
                            itemPick = _item;
                            break;
                        }
                    }
                    System.out.println("Choose value!");
                    float value=scanner.nextFloat();
                    node.makeBid(topicPick,itemPick,value);
                    break;
                case 6:
                    System.out.println("Choose auction to close!");
                    auctionList = new ArrayList<>();
                    stringList = new ArrayList<>();
                    auctions = node.getAuctionHouse().getOpenAuctions();
                    for(String topic : auctions.keySet()) {
                        if(auctions.get(topic).getOwner().equals(node.getNodeId())) {
                            auctionList.add(auctions.get(topic));
                            stringList.add(topic);
                        }
                    }
                    cnt=1;
                    for(Auction auction : auctionList){
                        System.out.println("["+(cnt++)+"] " + auction);
                    }
                    auction1 = scanner.nextInt();
                    topicPick = stringList.get(auction1-1);
                    node.closeAuction(topicPick);
                    break;
                case 7:
                    node.printRouteTable();
                    break;
                case 8:
                    node.retrieveSubscribedMessages();
                    node.print();
                    break;
                case 9:
                    System.out.println(node.transactionPool);
                    break;
                case 10:
                    flag=true;
                    service.stop();
                    break;
            }
            if(flag) break;
            System.out.println();
        }
        service.blockUntilShutdown();
    }
    public static void setInitialBlockChain() throws Exception {
        Wallet coinbase =service.getServiceNode().wallet;
        System.out.println("coinbase: "+coinbase.publicKey);
        Transaction genesisTransaction = new Transaction(coinbase.publicKey, coinbase.publicKey, 100f, null);
        genesisTransaction.setSignature(utils.bytesToPrivateKey(coinbase.privateKey));
        genesisTransaction.transactionId = "0";
        genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.receiver, genesisTransaction.amount, genesisTransaction.transactionId));
        initialChain= new Chain(genesisTransaction);
        initialChain.UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));
        node.setChain(initialChain);
    }
}
