package kademlia;

import myBlockchain.Chain;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class BlockChainThread implements Runnable, Serializable {
    public CopyOnWriteArrayList<String> broadcastId;
    Thread t;
    public BlockChainThread(String name,CopyOnWriteArrayList<String> broadcastId){
        this.broadcastId=broadcastId;
        t=new Thread(this,name);
        t.start();
    }

    @Override
    public void run() {
        while(true) {
            broadcastId.clear();
            try {
                Thread.sleep(10000);//5 to 5 seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    /*public long max(){
        long max=0;
        for(Chain c : chains){
            if(c.numberOfBlocks>max)
                max=c.numberOfBlocks;
        }
        return max;
    }*/
}
