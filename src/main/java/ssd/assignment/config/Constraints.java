package ssd.assignment.config;

public class Constraints {

    /*
    Sets the amount of zeros at the start of the hash when mining a block
     */
    public static final int MINING_DIFFICULTY = 2;

    public static final transient  int ID_LENGTH = 256;

    /*
    Sets the max amount of transactions per block
     */
    public static final int MAX_TRANSACTIONS_PER_BLOCK = 10;

}
