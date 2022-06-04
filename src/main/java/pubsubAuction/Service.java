package pubsubAuction;

import java.io.*;
import java.util.*;

//service for handling publishing/subscribing and broadcasting
class Service{

    //all messages keyed by topic -> subscribers
    private Map<String, Set<Subscriber>> topicMap = new HashMap<>();
    //queue of new messages not yet broadcasted
    private Queue<Message> messages = new LinkedList<>();

    private Map<String, Auction> openAuctions = new HashMap<>();

    public void addMessage(Message message){messages.add(message);}

    public void addSubscriber(String topic, Subscriber subscriber){

	Set<Subscriber> subscribers =
	    topicMap.containsKey(topic) ?
	    topicMap.get(topic) :
	    new HashSet<>();

	subscribers.add(subscriber);
	topicMap.put(topic, subscribers);
    }

    public void removeSubscriber(String topic, Subscriber subscriber){

	if(topicMap.containsKey(topic)){
	    Set<Subscriber> subscribers = topicMap.get(topic);
	    subscribers.remove(subscriber);
	    topicMap.put(topic, subscribers);
	}
    }

    public void setAuction(String topic, Auction auction){openAuctions.put(topic, auction);}

    public Auction getAuction(String topic){return openAuctions.get(topic);}

    public void close(Auction auction){openAuctions.remove(auction);}

    public void broadcast(){
	if(messages.isEmpty())
	    System.out.println("No messages from publishers to display");
	else    //for every message not yet broadcasted
	    while(!messages.isEmpty()){
		//remove the message from the queue and retrieve its topic
		Message message = messages.remove();
		String topic = message.getTopic();
		//get subscribers to that topic
		Set<Subscriber> topicSubscribers = topicMap.get(topic);
		//for every subscriber to that topic, add the new message
		if(topicSubscribers != null)
		    for(Subscriber subscriber : topicSubscribers){
			List<Message> subscriberMessages = subscriber.getMessages();
			subscriberMessages.add(message);
			subscriber.setMessages(subscriberMessages);
		    }
	    }
    }

    public void getMessagesOfTopic(String topic, Subscriber subscriber){
	if(messages.isEmpty())
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
	    }
    }
}
