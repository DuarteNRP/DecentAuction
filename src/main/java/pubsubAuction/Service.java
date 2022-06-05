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
