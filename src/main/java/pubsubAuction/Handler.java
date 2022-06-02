package pubsubAuction;

import java.io.*;
import java.util.*;

//TODO
//incorporate into kademlia or leave as is
class Handler{

    //Hashtable<String, String> notDHT = new Hashtable<>();
    //placeholder for Kademlia routing table
    List<Node> nodes = new LinkedList<>();
    Service auctionHouse = new Service();

    //placeholder for this test
    void add(Node node){nodes.add(node);}

    void startNewAuction(Node node, ArrayList<Item> items){
	String topic = "hashthis";
	Auction auction = new Auction(node, items);
	auctionHouse.setAuction(topic, auction);
	Message message = new Message(topic, auction);
	//Subscriber subscriber = new Subscriber(node);
	//Publisher publisher = new Publisher(node);
	node.publish(message, auctionHouse);
	node.subscribe(topic, auctionHouse);
	//TODO broadcast a new auction to the network?
	//maybe trigger an event to place bid?
	//System.out.println("this is the auction "+auction);
	//TODO
	//for testing as is
	print();
    }

    void makeBid(Node node, String topic, Item item, int value){
	Message message = new Message(topic, node+" just made a bid for item "+item);
	//Subscriber subscriber = new Subscriber(node);
	//Publisher publisher = new Publisher(node);
	node.subscribe(topic, auctionHouse);
	Auction auction = auctionHouse.getAuction(topic);
	auction.bid(node, item, value);
	auctionHouse.setAuction(topic, auction);
	node.publish(message, auctionHouse);



	auctionHouse.broadcast();
	//System.out.println("this is the auction "+auction);
	//TODO
	//for testing as is
	print();
    }

    void closeAuction(Node node, String topic){
	Auction auction = auctionHouse.getAuction(topic);

	Map<String, Bid> winners = auction.finish();
	for(String item : winners.keySet()){
	    Bid bid = winners.get(item);
	    String result = bid.getBidder()+" won item "+bid.getItem();
	    Message message = new Message(topic, result);
	    node.publish(message, auctionHouse);
	}

	auctionHouse.close(auction);

	auctionHouse.broadcast();

	print();

	//COMMIT results to blockchain

    }

    //TODO
    //placeholder for printing messages of the fake kademlia
    void print(){
	for(Node node : nodes)
	    node.printSubscribedMessages();
	System.out.println();
    }
}
