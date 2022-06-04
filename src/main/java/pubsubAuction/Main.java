package pubsubAuction;

import java.io.*;
import java.util.*;
import kademlia.Node;

/*class Node{
    private String nodeId;
    private Publisher pub = new Publisher(this);
    private Subscriber sub = new Subscriber(this);

    Node(String id){nodeId = id;}

    public String getNodeId(){return nodeId;}
    //public Publisher getPublisher(){return pub;}
    //public Subscriber getSubscriber(){return sub;}

    public void publish(Message message, Service service){pub.publish(message, service);}
    public void subscribe(String topic, Service service){sub.subscribe(topic, service);}
    public void printSubscribedMessages(){sub.printMessages();}
}*/


public class Main{
    public static void main(String[] args){

	//test for an auction using fake kademlia placeholder
	//person 0 auctions 3 items
	//person 1 bids on 2 items, wins 1
	//person 2 bids and wins on 1
	//person 3 bids and wins on 1
	//person 4 is playing fortnite instead

	//hastable is placeholder for Kademlia HDT
	/*Handler fakedemlia = new Handler();

	//make 5 people
	Node p1 = new Node("0");
	Node p2 = new Node("1");
	Node p3 = new Node("2");
	Node p4 = new Node("3");
	Node p5 = new Node("4");

	//add to fakedemlia as if nodes
	fakedemlia.add(p1);
	fakedemlia.add(p2);
	fakedemlia.add(p3);
	fakedemlia.add(p4);
	fakedemlia.add(p5);

	//make 3 items to auction
	Item i1 = new Item("0");
	Item i2 = new Item("1");
	Item i3 = new Item("2");
	ArrayList<Item> items = new ArrayList<>();
	items.add(i1);
	items.add(i2);
	items.add(i3);

	//start Auction
	fakedemlia.startNewAuction(p1, items);

	//make bids
	fakedemlia.makeBid(p2, "hashthis", i1, 42);
	fakedemlia.makeBid(p2, "hashthis", i2, 42);
	fakedemlia.makeBid(p3, "hashthis", i2, 69);
	fakedemlia.makeBid(p4, "hashthis", i3, 42);

	System.out.println("====FINAL RESULTS====");
	fakedemlia.closeAuction(p1, "hashthis");*/
    }
}
