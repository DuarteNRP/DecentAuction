package pubsubAuction;

import crypto.Crypto;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.util.*;
@Getter
@Setter
//wrapper containing auction data
public class Message<T> implements Serializable{
    private static Crypto crypto = new Crypto();
    private String topic;
    private T payload;
    private String identifier;
    Message(){}
    public Message(String topic, T payload){
        this.topic = topic;
        this.payload = payload;
        this.identifier = crypto.hash(String.valueOf(new Date().getTime())+topic);
    }

    public String getTopic(){return topic;}

    public T getPayload(){return payload;}

    public void setTopic(String topic){this.topic = topic;}

    public void setPayload(T payload){this.payload = payload;}
}
