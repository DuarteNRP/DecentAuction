package pubsubAuction;

import kademlia.Node;
import lombok.Getter;
import lombok.Setter;
import myBlockchain.Wallet;

import java.io.*;
import java.security.PublicKey;
import java.util.*;
@Getter
@Setter
public class Bid implements Serializable{

    private String bidder;
    private Wallet wallet;
    private Item item;
    private float bid;


    public Item getItem(){return item;}


    public String getItemID(){return item.getItemID();}

    Bid(int bid){this.bid = bid;}

    Bid(Node bidder, Item item, float bid, Wallet wallet){
        this.bidder = bidder.getNodeId();
        this.item = item;
        this.bid = bid;
        this.wallet= wallet;
    }

    @Override
    public String toString(){
	return bidder+" bids "+bid+" on "+item;
    }
}
