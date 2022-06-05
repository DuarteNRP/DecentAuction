package myBlockchain;

import config.Utils;
import crypto.Crypto;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

public class Transaction implements Serializable {
    Crypto crypto = new Crypto();
    public String transactionId;
    public PublicKey sender;
    public PublicKey receiver;
    public float amount;
    //mudar para bytes
    public String signature;

    public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
    public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
    private Chain blockchain;
    //count how many transactions have been made
    private static int numberTransation=0;

    public Transaction(PublicKey from, PublicKey to, float amount,  ArrayList<TransactionInput> inputs) {
        this.sender = from;
        this.receiver = to;
        this.amount = amount;
        this.inputs = inputs;
    }
    // This Calculates the transaction hash (which will be used as its Id)
    private String calulateHash() {
        numberTransation++; //increase the sequence to avoid 2 identical transactions having the same hash
        return crypto.hash(
                Utils.getStringFromKey(sender) +
                        Utils.getStringFromKey(receiver) +
                        Float.toString(amount) + numberTransation
        );
    }
    public void setSignature(PrivateKey privateKey) throws Exception {
        String data = Utils.getStringFromKey(sender) + Utils.getStringFromKey(receiver) + Float.toString(amount);
        signature = crypto.sign(data,privateKey);
    }
    public boolean verifySignature() throws Exception {
        String data = Utils.getStringFromKey(sender) + Utils.getStringFromKey(receiver) + Float.toString(amount);
        return crypto.verify(data,signature,sender);
    }
    public boolean processTransaction() throws Exception {
        if(verifySignature() == false) {
            System.out.println("#Transaction Signature failed to verify");
            return false;
        }
        //gather transaction inputs (Make sure they are unspent):
        for(TransactionInput i : inputs) {
            i.UTXO = blockchain.UTXOs.get(i.transactionOutputId);
        }

        //check if transaction is valid:
        if(getInputsValue() < blockchain.minimumTransaction) {
            System.out.println("#Transaction Inputs to small: " + getInputsValue());
            return false;
        }

        //generate transaction outputs:
        float leftOver = getInputsValue() - amount; //get value of inputs then the left over change:
        this.transactionId = calulateHash();
        outputs.add(new TransactionOutput( this.receiver,amount,transactionId)); //send value to receiver
        outputs.add(new TransactionOutput( this.sender, leftOver,transactionId)); //send the left over 'change' back to sender

        //add outputs to Unspent list
        for(TransactionOutput o : outputs) {
            blockchain.UTXOs.put(o.id , o);
        }

        //remove transaction inputs from UTXO lists as spent:
        for(TransactionInput i : inputs) {
            if(i.UTXO == null) continue; //if Transaction can't be found skip it
            blockchain.UTXOs.remove(i.UTXO.id);
        }

        return true;
    }

    //returns sum of inputs(UTXOs) values
    public float getInputsValue() {
        float total = 0;
        for(TransactionInput i : inputs) {
            if(i.UTXO == null) continue; //if Transaction can't be found skip it
            total += i.UTXO.amount;
        }
        return total;
    }

    //returns sum of outputs:
    public float getOutputsValue() {
        float total = 0;
        for(TransactionOutput o : outputs) {
            total += o.amount;
        }
        return total;
    }
}
