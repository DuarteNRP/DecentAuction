package pubsubAuction;

import kademlia.Node;

import java.io.*;
import java.util.*;

public class Publisher{
    private Node node;

    public Publisher(Node node){this.node = node;}

    public void publish(Message message, Service service){service.addMessage(message);}
}
