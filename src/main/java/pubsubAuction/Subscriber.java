package pubsubAuction;

import kademlia.Node;

import java.io.*;
import java.util.*;

public class Subscriber implements Serializable{

    private String nodeId;

    private List<Message> messages = new LinkedList<>();

    public Subscriber(Node node){this.nodeId = node.getNodeId();}

    public List<Message> getMessages(){return messages;}

    public void setMessages(List<Message> messages){
	this.messages = messages;}

    public void subscribe(String topic, Service service){
	service.addSubscriber(topic, this);}

    public void unsubscribe(String topic, Service service){
	service.removeSubscriber(topic, this);}

    public void getMessagesOfTopic(String topic, Service service){
	service.getMessagesOfTopic(topic, this);}

    public void printMessages(){
	System.out.println(this.nodeId+": ");
	if(messages.size() == 0)
	    System.out.println("No messages");
	else
	    for(Message message : messages)
		System.out.println("Message Topic -> "+message.getTopic()+" : "+message.getPayload());
    }
}
