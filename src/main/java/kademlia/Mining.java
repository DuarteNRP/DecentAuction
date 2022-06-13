package kademlia;

import ServiceGRPC.DataType;
import config.Constraints;
import config.Utils;
import crypto.Crypto;
import myBlockchain.Block;
import myBlockchain.Chain;
import myBlockchain.Transaction;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.CopyOnWriteArrayList;

public class Mining extends Thread implements Serializable {
    private static final Constraints constraints = new Constraints();
    public Chain blockchain;
    public Node n;
    public CopyOnWriteArrayList<Transaction> transactions;
    public Node node;
    public boolean check=false;
    public Mining(String name, Chain blockchain,CopyOnWriteArrayList<Transaction> transactions,Node n){
        this.blockchain=blockchain;
        this.transactions=transactions;
        this.node=n;
        System.out.println("nodeee:"+n);
        this.check=true;
    }

    public void run() {
        Block newBlock = new Block(this.blockchain.getLastBlock().getActualHash());
        for(int i=0;i<constraints.MAX_TRANSACTIONS_PER_BLOCK;i++) {
            try {
                newBlock.addTransaction(this.transactions.get(i));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            newBlock.mineBlock();
            if (check) {
                System.out.println("node: " + this.n);
                try {
                    this.n.broadcast(Utils.serialize(newBlock), newBlock.actualHash, DataType.BLOCK);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("chegou");
                for (Transaction t : newBlock.getTransactions()) {
                    transactions.remove(t);
                }
            }
        }
    }
}

