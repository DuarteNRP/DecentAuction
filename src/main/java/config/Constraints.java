package config;

public class Constraints {

    /*
    Sets the amount of zeros at the start of the hash when mining a block
     */
    public static final int MINING_DIFFICULTY = 2;

    public static final transient  int ID_LENGTH = 256;
    public static final float COIN_REWARD = 0.01f;
    public static final float TRANSACTION_FEE = 0.01f;

    /*
    Sets the max amount of transactions per block
     */
    public static final int MAX_TRANSACTIONS_PER_BLOCK = 10;

}
