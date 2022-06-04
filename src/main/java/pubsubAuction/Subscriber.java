package pubsubAuction;

import java.io.*;
import java.util.*;

class Subscriber{

    private Node node;

    private List<Message> messages = new LinkedList<>();

    Subscriber(Node node){this.node = node;}

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
	System.out.println(node.getNodeId()+": ");
	if(messages.size() == 0)
	    System.out.println("No messages");
	else
	    for(Message message : messages)
		System.out.println("Message Topic -> "+message.getTopic()+" : "+message.getPayload());
    }
}
