package kademlia;

import ServiceGRPC.DataType;
import config.Constraints;
import config.Utils;
import crypto.Crypto;
import myBlockchain.Block;
import myBlockchain.Chain;
import myBlockchain.Transaction;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

public class Mining extends Thread {
    private static final Constraints constraints = new Constraints();
    private static final Utils utils = new Utils();
    private static final Crypto crypto = new Crypto();
    public Chain blockchain;
    public Node n;
    public CopyOnWriteArrayList<Transaction> transactions;
    public Node node;
    public boolean check=false;
    public Mining(String name, Chain blockchain,CopyOnWriteArrayList<Transaction> transactions,Node n){
        this.blockchain=blockchain;
        this.transactions=transactions;
        this.node=n;
        this.check=true;
    }

    public void run() {
        Block newBlock = new Block(this.blockchain.getLastBlock().getActualHash());
        for(int i=0;i<constraints.MAX_TRANSACTIONS_PER_BLOCK;i++){
            try {
                newBlock.addTransaction(this.transactions.get(i));
                newBlock.mineBlock();
                if(check) {
                    this.n.broadcast(utils.serialize(newBlock), newBlock.actualHash, DataType.BLOCK);
                    for(Transaction t : newBlock.getTransactions()){
                        transactions.remove(t);
                    }
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
