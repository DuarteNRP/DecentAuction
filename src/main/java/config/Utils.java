package config;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.ArrayList;
import java.util.Base64;
import crypto.*;
import myBlockchain.*;

public class Utils {
    private static Crypto crypto = new Crypto();
    private static Constraints constraints= new Constraints();
    public static int log2(long N){

        // calculate log2 N indirectly
        // using log() method
        int result = (int)(Math.log(N) / Math.log(2));

        return result;
    }
    public static String getBinaryFromHash(String str) {
        String hashToBinary=new BigInteger(str,16).toString(2);
        return hashToBinary.substring(hashToBinary.length()-constraints.ID_LENGTH);
    }
    public static String getStringFromKey(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
    public static String getStringFromBytes(byte[] bytes){
        return new String(bytes, StandardCharsets.UTF_8);
    }
    public static byte[] getBytesFromString(String str){
        return str.getBytes(StandardCharsets.UTF_8);
    }
    public static String getMerkleRoot(ArrayList<myBlockchain.Transaction> transactions) {
        int count = transactions.size();
        if(count==1){
            return crypto.hash(transactions.get(0).transactionId);
        }
        ArrayList<String> previousTreeLayer = new ArrayList<String>();
        for(Transaction transaction : transactions) {
            previousTreeLayer.add(transaction.transactionId);
        }
        ArrayList<String> treeLayer = previousTreeLayer;
        while(count > 1) {
            treeLayer = new ArrayList<String>();
            for(int i=0; i < previousTreeLayer.size(); i+=2) {
                if(i==previousTreeLayer.size()-1){
                    treeLayer.add(crypto.hash(previousTreeLayer.get(i)));
                    break;
                }
                treeLayer.add(crypto.hash(previousTreeLayer.get(i) + previousTreeLayer.get(i+1)));
            }
            count = treeLayer.size();
            previousTreeLayer = treeLayer;
        }
        String merkleRoot = (treeLayer.size() == 1) ? treeLayer.get(0) : "";
        return merkleRoot;
    }
}
