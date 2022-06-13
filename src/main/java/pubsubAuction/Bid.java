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
    private String Ip;
    private int port;
    private Item item;
    private float bid;


    public Item getItem(){return item;}


    public String getItemID(){return item.getItemID();}

    Bid(int bid){this.bid = bid;}

    Bid(Node bidder, Item item, float bid){
        this.bidder = bidder.getNodeId();
        this.item = item;
        this.bid = bid;
        this.Ip=bidder.getNode().getIp();
        this.port=bidder.getNode().getPort();
    }

    @Override
    public String toString(){
	return bidder+" bids "+bid+" on "+item;
    }
}
