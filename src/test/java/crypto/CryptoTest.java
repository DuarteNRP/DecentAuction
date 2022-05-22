package crypto;
import org.junit.jupiter.api.Test;
import crypto.Crypto;
import java.security.KeyPair;

import static org.junit.jupiter.api.Assertions.*;

class CryptoTest {
    Crypto crypto = new Crypto();
    @Test
    public void testSha256() {
        String hello_world = crypto.hash("Hello World");
        assertEquals(hello_world,"a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e");
    }
    @Test
    public void testKeyPar() throws Exception {
        KeyPair pair = Crypto.createKeyPair();
        KeyPair pair1 = Crypto.createKeyPair();
        String sign = Crypto.sign("myMessage",pair.getPrivate());
        System.out.print(sign);
        assertTrue(Crypto.verify("myMessage",sign,pair.getPublic()));
        assertFalse(Crypto.verify("myMessage",sign,pair1.getPublic()));
    }
}