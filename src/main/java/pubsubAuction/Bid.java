package pubsubAuction;

import kademlia.Node;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.util.*;
@Getter
@Setter
public class Bid implements Serializable{

    private String bidder;
    private Item item;
    private int bid;


    public Item getItem(){return item;}

    public int getBid(){return bid;}

    public String getItemID(){return item.getItemID();}

    Bid(int bid){this.bid = bid;}

    Bid(Node bidder, Item item, int bid){
	this.bidder = bidder.getNodeId();
	this.item = item;
	this.bid = bid;
    }

    @Override
    public String toString(){
	return bidder+" bids "+bid+" on "+item;
    }
}
