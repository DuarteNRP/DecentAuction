package ssd.assignment.myBlockchain;

import ssd.assignment.crypto.Crypto;
import ssd.assignment.config.*;

import javax.sound.midi.SysexMessage;
import java.security.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Wallet {
    public HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>(); //only UTXOs owned by this wallet.
    //meter a private?
    public PrivateKey privateKey;
    public PublicKey publicKey;
    public Wallet() throws NoSuchAlgorithmException{
        KeyPair pair = Crypto.createKeyPair();
        privateKey=pair.getPrivate();
        publicKey=pair.getPublic();
    }

    public float getBalance() {
        float total = 0;
        for (String key: Chain.UTXOs.keySet()){
            TransactionOutput UTXO = Chain.UTXOs.get(key);
            if(UTXO==null) continue;
            if(UTXO.isMine(publicKey)) { //if output belongs to me ( if coins belong to me )
                UTXOs.put(UTXO.id,UTXO); //add it to our list of unspent transactions.
                total += UTXO.amount;
            }
        }
        return total;
    }
    public Transaction sendFunds(PublicKey receiver, float value ) throws Exception {
        if(getBalance() < value) { //gather balance and check funds.
            System.out.println("#Not Enough funds to send transaction. Transaction Discarded.");
            return null;
        }
        //create array list of inputs
        ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();

        float total = 0;
        for (String key: UTXOs.keySet()){
            TransactionOutput UTXO = UTXOs.get(key);
            total += UTXO.amount;
            inputs.add(new TransactionInput(UTXO.id));
            if(total > value) break;
        }

        Transaction newTransaction = new Transaction(publicKey, receiver , value, inputs);
        newTransaction.setSignature(privateKey);

        for(TransactionInput input: inputs){
            UTXOs.remove(input.transactionOutputId);
        }
        return newTransaction;
    }
}
