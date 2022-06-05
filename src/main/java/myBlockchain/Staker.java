package myBlockchain;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class Staker implements Serializable {
    public String address;
    public float amount;
}

