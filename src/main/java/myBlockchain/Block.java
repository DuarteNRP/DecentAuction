package myBlockchain;

import java.util.ArrayList;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import crypto.Crypto;
import config.*;
@Getter
@Setter
public class Block {
    public ArrayList<Transaction> transactions = new ArrayList<Transaction>(); //our data will be a simple message.
    private static final Crypto crypto = new Crypto();
    private static final Constraints constraints = new Constraints();
    private static final Utils utils = new Utils();
    public String actualHash;
    public String previousHash;
    private long timestamp;
    private int nonce;
    public String merkleRoot;

    //we will not need this block constructor
    public Block(String previousHash){
        this.previousHash=previousHash;
        this.timestamp=new Date().getTime();
        this.actualHash=calculateHash();
    }

    public String calculateHash() {
        return crypto.hash(
                this.previousHash +
                        Long.toString(this.timestamp) +
                        Integer.toString(nonce) +
                        merkleRoot
        );
    }
    public void mineBlock() {
        merkleRoot = utils.getMerkleRoot(transactions);
        String target = new String(new char[constraints.MINING_DIFFICULTY]).replace('\0', '0'); //Create a string with difficulty * "0"
        while(!actualHash.substring( 0, constraints.MINING_DIFFICULTY).equals(target)) {
            nonce ++;
            actualHash = calculateHash();
        }
        System.out.println("Block Mined!!! : " + actualHash);
    }
    public boolean addTransaction(Transaction transaction) throws Exception {
        //process transaction and check if valid, unless block is genesis block then ignore.
        if(transaction == null) return false;
        if((previousHash != "0")) {//genesis block, we dont want trasactions in genesis block
            if((transaction.processTransaction() != true)) {
                System.out.println("Transaction failed to process. Discarded.");
                return false;
            }
        }
        transactions.add(transaction);
        System.out.println("Transaction Successfully added to Block");
        return true;
    }
    //Total amount of all Transactions
    public float getTransactionsAmount(){
        float count=0;
        for(Transaction t : transactions){
            count+=t.amount;
        }
        return count;
    }
    //Total of fees of all Transactions, for now %0.1 of amount
    public float getTransactionsFees(){
        float count=0;
        for(Transaction t : transactions){
            count+=(t.amount*0.001);
        }
        return count;
    }
}
