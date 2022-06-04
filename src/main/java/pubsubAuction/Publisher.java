package pubsubAuction;

import kademlia.Node;

import java.io.*;
import java.util.*;

public class Publisher implements Serializable{
    private String nodeId;

    public Publisher(Node node){this.nodeId = node.getNodeId();}

    public void publish(Message message, Service service){service.addMessage(message);}
}
