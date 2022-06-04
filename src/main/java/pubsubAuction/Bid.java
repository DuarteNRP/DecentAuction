package pubsubAuction;

import kademlia.Node;

import java.io.*;
import java.util.*;

public class Bid{

    private Node bidder;
    private Item item;
    private int bid;

    public Node getBidder(){return bidder;}

    public Item getItem(){return item;}

    public int getBid(){return bid;}

    public String getItemID(){return item.getItemID();}

    Bid(int bid){this.bid = bid;}

    Bid(Node bidder, Item item, int bid){
	this.bidder = bidder;
	this.item = item;
	this.bid = bid;
    }

    @Override
    public String toString(){
	return bidder+" bids "+bid+" on "+item;
    }
}
