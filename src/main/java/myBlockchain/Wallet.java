package myBlockchain;

import crypto.Crypto;
import config.*;
import lombok.Getter;
import lombok.Setter;

import javax.sound.midi.SysexMessage;
import java.io.Serializable;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
@Getter
@Setter
public class Wallet implements Serializable {
    private final Utils utils = new Utils();
    public HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>(); //only UTXOs owned by this wallet.
    //meter a private?
    public byte[] privateKey;
    public byte[] publicKey;
    private Chain blockchain;

    public Wallet() throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyPair pair = Crypto.createKeyPair();
        privateKey=pair.getPrivate().getEncoded();
        publicKey=pair.getPublic().getEncoded();
    }

    public float getBalance() throws NoSuchAlgorithmException, InvalidKeySpecException {
        float total = 0;
        for (String key: blockchain.UTXOs.keySet()){
            TransactionOutput UTXO = blockchain.UTXOs.get(key);
            if(UTXO==null) continue;
            if(UTXO.isMine(publicKey)) { //if output belongs to me ( if coins belong to me )
                System.out.println("entrou");
                UTXOs.put(UTXO.id,UTXO); //add it to our list of unspent transactions.
                total += UTXO.amount;
            }
        }
        return total;
    }
    public Transaction sendFunds(byte[] receiver, float value ) throws Exception {
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

        Transaction newTransaction = new Transaction(publicKey, receiver, value, inputs);
        newTransaction.setBlockchain(blockchain);
        newTransaction.setSignature(utils.bytesToPrivateKey(privateKey));

        for(TransactionInput input: inputs){
            UTXOs.remove(input.transactionOutputId);
        }
        System.out.println("Transaction created successfully");
        return newTransaction;
    }
}
