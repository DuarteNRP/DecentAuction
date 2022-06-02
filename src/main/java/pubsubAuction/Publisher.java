package pubsubAuction;

import java.io.*;
import java.util.*;

class Publisher{
    private Node node;

    public Publisher(Node node){this.node = node;}

    public void publish(Message message, Service service){service.addMessage(message);}
}
