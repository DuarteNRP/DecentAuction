package pubsubAuction;

import kademlia.Node;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.util.*;

//service for handling publishing/subscribing and broadcasting
@Getter
@Setter
public class Service implements Serializable{

    //all messages keyed by topic -> subscribers
    private Map<String, Set<String>> topicMap = new HashMap<>();
    //queue of new messages not yet broadcasted
    private Queue<Message> messages = new LinkedList<>();

    private Map<String, Auction> openAuctions = new HashMap<>();

    public void addMessage(Message message){messages.add(message);}

    public void addSubscriber(String nodeId, String topic){
		Set<String> topics =
				topicMap.containsKey(nodeId) ?
						topicMap.get(nodeId) :
						new HashSet<>();

		topics.add(topic);
		topicMap.put(nodeId, topics);

	}

    public void removeSubscriber(String nodeId, String topic){

	if(topicMap.containsKey(nodeId)){
	    Set<String> topics = topicMap.get(topic);
	    topics.remove(topic);
	    topicMap.put(nodeId, topics);
	}
    }

    public void setAuction(String topic, Auction auction){openAuctions.put(topic, auction);}

    public Auction getAuction(String topic){return openAuctions.get(topic);}

    public void close(String topic){openAuctions.remove(topic);}

    public void broadcast(){
	if(messages.isEmpty())
	    System.out.println("No messages from publishers to display");
	else    //for every message not yet broadcasted
	    while(!messages.isEmpty()){
		//remove the message from the queue and retrieve its topic
		Message message = messages.remove();
		/*String topic = message.getTopic();
		//get subscribers to that topic
		Set<String> topicSubscribers = topicMap.get(topic);
		//for every subscriber to that topic, add the new message
		if(topicSubscribers != null)
		    for(Subscriber subscriber : topicSubscribers){
			List<Message> subscriberMessages = subscriber.getMessages();
			subscriberMessages.add(message);
			subscriber.setMessages(subscriberMessages);
		    }*/
	    }
    }

    public void getMessagesOfTopic(String topic, Subscriber subscriber){
	/*if(messages.isEmpty())
	    System.out.println("No messages from publishers to display");
	else
	    while(!messages.isEmpty()){
		Message message = messages.remove();

		if(message.getTopic().equalsIgnoreCase(topic)){

		    Set<Subscriber> topicSubscribers = topicMap.get(topic);

		    for(Subscriber _subscriber : topicSubscribers)
			if(_subscriber.equals(subscriber)){
			    List<Message> subscriberMessages = subscriber.getMessages();
			    subscriberMessages.add(message);
			    subscriber.setMessages(subscriberMessages);
			}
		}
	    }*/
    }

	public void retrieveSubscribedMessages(Node node){
		Subscriber subscriber = node.getSub();
		Set<Message> subscribedMessages = subscriber.getMessages();
		Set<String> topics =
				topicMap.get(node.getNodeId()) == null ?
						new LinkedHashSet<>() :
						topicMap.get(node.getNodeId());

		for(Message message : messages)
			for(String topic : topics)
				if(message.getTopic().equalsIgnoreCase(topic))
					subscribedMessages.add(message);

		subscriber.setMessages(subscribedMessages);
	}
	public void printAll(){
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		for(Message message : messages)
			System.out.println(message.getPayload());

		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	}
}
