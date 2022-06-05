package pubsubAuction;

import kademlia.Node;

import java.io.*;
import java.util.*;

public class Subscriber implements Serializable{

    private String nodeId;

    private Set<Message> messages = new LinkedHashSet<>();

    public Subscriber(Node node){this.nodeId = node.getNodeId();}

    public Set<Message> getMessages(){return messages;}

    public void setMessages(Set<Message> messages){
	this.messages = messages;}

    public void subscribe(String topic, Service service){
	service.addSubscriber(nodeId, topic);}

    public void unsubscribe(String topic, Service service){
	service.removeSubscriber(nodeId, topic);}

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
