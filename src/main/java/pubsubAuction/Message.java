package pubsubAuction;

import java.io.*;
import java.util.*;

//wrapper containing auction data
public class Message<T>{

    private String topic;
    private T payload;

    Message(){}
    public Message(String topic, T payload){
        this.topic = topic;
        this.payload = payload;
    }

    public String getTopic(){return topic;}

    public T getPayload(){return payload;}

    public void setTopic(String topic){this.topic = topic;}

    public void setPayload(T payload){this.payload = payload;}
}
