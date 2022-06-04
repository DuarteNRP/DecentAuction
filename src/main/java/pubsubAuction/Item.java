package pubsubAuction;

import java.io.*;
import java.util.*;

public class Item implements Serializable{

    private String itemID;

    public Item(String itemID){this.itemID=itemID;}

    public String getItemID(){return itemID;}

    @Override
    public String toString(){
	return "item#"+itemID;
    }
}
