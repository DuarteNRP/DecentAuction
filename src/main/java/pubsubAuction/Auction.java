package pubsubAuction;

import kademlia.Node;

import java.io.*;
import java.util.*;

public class Auction{

    private Node owner;
    private ArrayList<Item> items;
    private List<Bid> bids = new LinkedList<>();

    public Node getOwner(){return owner;}

    public ArrayList<Item> getItems(){return items;}

    public List<Bid> getBids(){return bids;}

    public Auction(Node owner, ArrayList<Item> items){
	this.owner = owner;
	this.items = items;
    }

    public void bid(Node bidder, Item item, int bid){
	//TODO check bid amount, and if everythign else exists
	bids.add(new Bid(bidder, item, bid));
    }

    public Map<String, Bid> finish(){
       Map<String, Bid> results = new HashMap<>();

       for(Bid bid : bids){
	   String key = bid.getItemID();
	   Bid curr = results.get(key) == null ?
	       new Bid(0) : results.get(key);

	   if(curr.getBid() < bid.getBid())
	       curr = bid;

	   results.put(key, curr);
       }

       //anounce results
       //for debug purposes only
       for(String key : results.keySet()){
	   Bid bid = results.get(key);
	   System.out.println("item "+bid.getItem()+
			      " won by "+bid.getBidder()+
			      " bidding "+bid.getBid());
       }

       return results;
   }

    @Override
    public String toString(){
	return "Auction by "+owner+" of items: "+items;
    }
}
