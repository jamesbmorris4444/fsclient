package com.fullsecurity.fsclient;

import com.fullsecurity.common.DHConstants;
import com.fullsecurity.common.Payload;
import com.fullsecurity.common.RSA;
import com.fullsecurity.common.Rijndael;
import com.fullsecurity.common.SHA256;
import com.fullsecurity.common.Utilities;
import com.fullsecurity.server.FileAccess;
import com.fullsecurity.server.User;

import org.junit.Test;

import java.math.BigInteger;
import java.util.Random;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("all")
public class FSClientUnitTest {
    @Test
    public void rotateRight_isCorrect() throws Exception {
        assertEquals(0x67a12345, SHA256.rotateRight(0xa1234567, 8));
        assertEquals(0x4567a123, SHA256.rotateRight(0xa1234567, 16));
        assertEquals(0xcf42468a, SHA256.rotateRight(0xa1234567, 7));
        assertEquals(0x142468ac, SHA256.shiftRight(0xa1234567, 3));

    }
    @Test
    public void sigma_isCorrect() throws Exception {
        assertEquals(0x88888888, SHA256.bigSigmaZero(0x11111111));
        assertEquals(0xeeeeeeee, SHA256.bigSigmaOne(0x11111111));
        assertEquals(0x64444444, SHA256.sigmaZero(0x11111111));
        assertEquals(0xaaaeeeee, SHA256.sigmaOne(0x11111111));
    }
    @Test
    public void other_isCorrect() throws Exception {
        assertEquals(0x66666666, SHA256.Ch(0x33333333, 0x66666666, 0x55555555));
        assertEquals(0x77777777, SHA256.Maj(0x33333333, 0x66666666, 0x55555555));
    }
    @Test
    public void computation_isCorrect() throws Exception {
        long T2a = SHA256.bigSigmaZero(0x33333333);
        T2a &= 0x00000000ffffffffL;
        long T2b = SHA256.Maj(0x33333333, 0x66666666, 0x55555555);
        T2b &= 0x00000000ffffffffL;
        long u = (T2a + T2b) & 0x00000000ffffffffL;
        int T2 = (int) u;
        assertEquals(0x11111110, T2);
    }
    @Test
    public void sha256_isCorrect() throws Exception {
        byte z[] = new byte[3];
        z[0] = (byte) 0x61;
        z[1] = (byte) 0x62;
        z[2] = (byte) 0x63;
        byte[] hh = SHA256.sha256(z);
        String s = Utilities.toHexString(hh);
        assertEquals(s,"ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
        z = new byte[0];
        hh = SHA256.sha256(z);
        s = Utilities.toHexString(hh);
        assertEquals(s,"e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
        String ss = "abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq";
        z = ss.getBytes();
        hh = SHA256.sha256(z);
        s = Utilities.toHexString(hh);
        assertEquals(s,"248d6a61d20638b8e5c026930c3e6039a33ce45964ff2167f6ecedd419db06c1");
        ss = "abcdefghbcdefghicdefghijdefghijkefghijklfghijklmghijklmnhijklmnoijklmnopjklmnopqklmnopqrlmnopqrsmnopqrstnopqrstu";
        z = ss.getBytes();
        hh = SHA256.sha256(z);
        s = Utilities.toHexString(hh);
        assertEquals(s,"cf5b16a778af8380036ce59e7b0492370b249b11e8f07a51afac45037afee9d1");
    }
    @Test
    public void RSA_isCorrect() throws Exception {
        RSA rsa = new RSA();
        String testString = "abcdefg";
        String testStringInBytes = Utilities.toHexStringWithSeparator(testString.getBytes());
        byte[] encrypted = rsa.encrypt(testString.getBytes());
        byte[] decrypted = rsa.decrypt(encrypted);
        String decryptedStringInBytes = Utilities.toHexStringWithSeparator(decrypted);
        assertEquals(testStringInBytes,decryptedStringInBytes);
        testString = "abcdefghijkl";
        testStringInBytes = Utilities.toHexStringWithSeparator(testString.getBytes());
        encrypted = rsa.encrypt(testString.getBytes());
        decrypted = rsa.decrypt(encrypted);
        decryptedStringInBytes = Utilities.toHexStringWithSeparator(decrypted);
        assertEquals(testStringInBytes,decryptedStringInBytes);
        testString = "The quick brown fox jumped over the lazy dog";
        testStringInBytes = Utilities.toHexStringWithSeparator(testString.getBytes());
        encrypted = rsa.encrypt(testString.getBytes());
        decrypted = rsa.decrypt(encrypted);
        decryptedStringInBytes = Utilities.toHexStringWithSeparator(decrypted);
        assertEquals(testStringInBytes,decryptedStringInBytes);
        testString = "The quick brown fox jumped over the lazy dog ... The quick brown fox jumped over the lazy dog";
        testStringInBytes = Utilities.toHexStringWithSeparator(testString.getBytes());
        encrypted = rsa.encrypt(testString.getBytes());
        decrypted = rsa.decrypt(encrypted);
        decryptedStringInBytes = Utilities.toHexStringWithSeparator(decrypted);
        assertEquals(testStringInBytes,decryptedStringInBytes);

        rsa = new RSA();
        String string = "The quick brown fox jumped over the lazy dog ... The quick brown fox jumped over the lazy dog";
        byte[] z = string.getBytes();
        byte[] hash = SHA256.sha256(z);
        byte[] signature = rsa.applySignature(hash);
        encrypted = rsa.encrypt(string.getBytes());
        decrypted = rsa.decrypt(encrypted);
        hash = SHA256.sha256(decrypted);
        String decryptedHash = Utilities.toHexStringWithSeparator((hash));
        byte[] signatureAsRead = rsa.readSignature(signature);
        String readSignature = Utilities.toHexStringWithSeparator((signatureAsRead));
        assertEquals(decryptedHash, readSignature);
    }
    @Test
    public void DH_isCorrect() throws Exception {
        Random rnd = new Random();
        BigInteger aliceInitialSecret = new BigInteger(1024, rnd);
        BigInteger bobInitialSecret = new BigInteger(1024, rnd);
        BigInteger alicePublicKey = DHConstants.G.modPow(aliceInitialSecret, DHConstants.P);
        BigInteger bobPublicKey = DHConstants.G.modPow(bobInitialSecret, DHConstants.P);
        BigInteger aliceSecret = bobPublicKey.modPow(aliceInitialSecret, DHConstants.P);
        BigInteger bobSecret = alicePublicKey.modPow(bobInitialSecret, DHConstants.P);
        String aliceSecretString = Utilities.toHexString(aliceSecret.toByteArray());
        String bobSecretString = Utilities.toHexString(bobSecret.toByteArray());
        assertEquals(aliceSecretString, bobSecretString);
    }
    @Test
    public void AES_DH_isCorrect() throws Exception {
        String inText = "This is a test of JRijndael. This text will be encrypted, then decrypted using ECB mode.";
        byte[] cipherText = null;
        byte[] decText = null;
        Random rnd = new Random();
        BigInteger aliceInitialSecret = new BigInteger(1024, rnd);
        BigInteger bobInitialSecret = new BigInteger(1024, rnd);
        BigInteger alicePublicKey = DHConstants.G.modPow(aliceInitialSecret, DHConstants.P);
        BigInteger bobPublicKey = DHConstants.G.modPow(bobInitialSecret, DHConstants.P);
        BigInteger aliceSecret = bobPublicKey.modPow(aliceInitialSecret, DHConstants.P);
        BigInteger bobSecret = alicePublicKey.modPow(bobInitialSecret, DHConstants.P);
        byte[] aliceKey1024 = aliceSecret.toByteArray();
        byte[] bobKey1024 = bobSecret.toByteArray();
        byte[] aliceKey = SHA256.sha256(aliceKey1024); // aliceKey is 256-bit encryption key
        byte[] bobKey = SHA256.sha256(bobKey1024); // bobKey is 256-bit encryption key
        Rijndael AESalgorithm = new Rijndael();
        AESalgorithm.makeKey(aliceKey, 256, AESalgorithm.DIR_ENCRYPT);
        try {
            cipherText = AESalgorithm.encryptArray(inText.getBytes());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        AESalgorithm.makeKey(bobKey, 256, AESalgorithm.DIR_DECRYPT);
        try {
            decText = AESalgorithm.decryptArray(cipherText);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        assertEquals(Utilities.toHexStringWithSeparator(inText.getBytes()), Utilities.toHexStringWithSeparator(decText));
        String aText = "abcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefgh";
        try {
            cipherText = AESalgorithm.encryptArray(aText.getBytes());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        AESalgorithm.makeKey(bobKey, 256, AESalgorithm.DIR_DECRYPT);
        try {
            decText = AESalgorithm.decryptArray(cipherText);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        assertEquals(Utilities.toHexStringWithSeparator(aText.getBytes()), Utilities.toHexStringWithSeparator(decText));
    }
    @Test
    public void STS_isCorrect() throws Exception {
        byte[] aliceCipherText = null;
        byte[] aliceDecText = null;
        byte[] bobCipherText = null;
        byte[] bobDecText = null;

        // Generate hashed universal password
        String string = "Universal password for Acme Corporation - 10/17/15";
        byte[] z = string.getBytes();
        byte[] universalPasswordHash = SHA256.sha256(z);

        // initial random secret guesses x and y by Alice and Bob
        Random rnd = new Random();
        BigInteger aliceInitialSecret = new BigInteger(1024, rnd);
        BigInteger bobInitialSecret = new BigInteger(1024, rnd);
        //BigInteger aliceInitialSecret = g1;
        //BigInteger bobInitialSecret = g2;

        // Diffie-Hellman "Public Key": g^x mod p for Alice and g^y mod p for Bob
        BigInteger alicePublicKey = DHConstants.G.modPow(aliceInitialSecret, DHConstants.P);
        BigInteger bobPublicKey = DHConstants.G.modPow(bobInitialSecret, DHConstants.P);

        // create a public key/private key pair for Bob to be used for STS signature
        RSA bobRSA = new RSA();

        // Bob concatenates the universal password hash and the DH "Public Keys" (univeralPasswordHash, g^y mod p, g^x mod p) (order is important)
        byte[] STSauthenticationFromBob = Utilities.concatenateByteArrays(universalPasswordHash, bobPublicKey.toByteArray());
        STSauthenticationFromBob = Utilities.concatenateByteArrays(STSauthenticationFromBob, alicePublicKey.toByteArray());

        // Bob signs the concatenation using his asymmetric (private) key. This is his signature.
        byte[] bobSignature = bobRSA.applySignature(STSauthenticationFromBob);
        byte[] b1 = bobRSA.readSignature(bobSignature);

        // create a public key/private key pair for Alice to be used for STS signature
        RSA aliceRSA = new RSA();

        // Alice coconcatenates the universal password hash and the DH "Public Keys" (univeralPasswordHash, g^x mod p, g^y mod p) (order is important)
        byte[] STSauthenticationFromAlice = Utilities.concatenateByteArrays(universalPasswordHash, alicePublicKey.toByteArray());
        STSauthenticationFromAlice = Utilities.concatenateByteArrays(STSauthenticationFromAlice, bobPublicKey.toByteArray());

        // Alice signs the concatenation using her asymmetric (private) key. This is her signature.
        byte[] aliceSignature = aliceRSA.applySignature(STSauthenticationFromAlice);
        byte[] b2 = aliceRSA.readSignature(aliceSignature);

        // Alice computes her shared secret key K = (g^y mod p)^x mod p
        BigInteger aliceSecret = bobPublicKey.modPow(aliceInitialSecret, DHConstants.P);

        // Bob computes his shared secret key K = (g^x mod p)^y mod p
        BigInteger bobSecret = alicePublicKey.modPow(bobInitialSecret, DHConstants.P);

        // At this point, aliceSecret == bobSecret

        byte[] aliceKey1024 = aliceSecret.toByteArray();
        byte[] bobKey1024 = bobSecret.toByteArray();
        byte[] aliceKey = SHA256.sha256(aliceKey1024); // aliceKey is 256-bit symmetric AES encryption key
        byte[] bobKey = SHA256.sha256(bobKey1024); // bobKey is 256-bit symmetric AES encryption key

        // At this point, aliceKey == bobKey

        // Bob encrypts his signature with his secret key K and sends the encrypted signature to Alice
        Rijndael bobAESalgorithm = new Rijndael();
        bobAESalgorithm.makeKey(bobKey, 256, bobAESalgorithm.DIR_ENCRYPT);
        try {
            bobCipherText = bobAESalgorithm.encryptArray(bobSignature);
        } catch (Exception ex) {
        }

        byte[] b3 = null;
        bobAESalgorithm.makeKey(bobKey, 256, bobAESalgorithm.DIR_DECRYPT);
        try {
            b3 = bobAESalgorithm.decryptArray(bobCipherText);
        } catch (Exception ex) {
        }

        // Alice encrypts her signature with her secret key K and sends the encrypted signature to Bob
        Rijndael aliceAESalgorithm = new Rijndael();
        aliceAESalgorithm.makeKey(aliceKey, 256, aliceAESalgorithm.DIR_ENCRYPT);
        try {
            aliceCipherText = aliceAESalgorithm.encryptArray(aliceSignature);
        } catch (Exception ex) {
        }

        byte[] b4 = null;
        aliceAESalgorithm.makeKey(aliceKey, 256, aliceAESalgorithm.DIR_DECRYPT);
        try {
            b4 = aliceAESalgorithm.decryptArray(aliceCipherText);
        } catch (Exception ex) {
        }

        // Alice decrypts the signature she got from Bob with her secret key K
        aliceAESalgorithm.makeKey(aliceKey, 256, aliceAESalgorithm.DIR_DECRYPT);
        try {
            aliceDecText = aliceAESalgorithm.decryptArray(bobCipherText);
        } catch (Exception ex) {
        }

        // Bob decrypts the signature he got from Alice with his secret key K
        bobAESalgorithm.makeKey(bobKey, 256, bobAESalgorithm.DIR_DECRYPT);
        try {
            bobDecText = bobAESalgorithm.decryptArray(aliceCipherText);
        } catch (Exception ex) {
        }

        // Bob verifies Alice's signature using her asymmetric public key
        byte[] signatureAsReadByBob = aliceRSA.readSignature(bobDecText);

        // Alice verifies Bob's signature using his asymmetric public key
        byte[] signatureAsReadByAlice = bobRSA.readSignature(aliceDecText);

        // Alice received signature (univeralPasswordHash, g^y mod p, g^x mod p) from Bob and she breaks it down into three arrays
        // bobPublicKeyReceivedByAlice == g^y mod p and alicePublicKeyReceivedByAlice == g^x mod p
        byte[] universalPasswordHashReceivedByAlice = Utilities.splitArray(signatureAsReadByAlice, 0);
        byte[] bobPublicKeyReceivedByAlice = Utilities.splitArray(signatureAsReadByAlice, 1);
        byte[] alicePublicKeyReceivedByAlice = Utilities.splitArray(signatureAsReadByAlice, 2);

        // Bob received signature (univeralPasswordHash, g^x mod p, g^y mod p) from Alice and he breaks it down into three arrays
        // alicePublicKeyReceivedByBob == g^x mod p and bobPublicKeyReceivedByBob == g^y mod p
        byte[] universalPasswordHashReceivedByBob = Utilities.splitArray(signatureAsReadByBob, 0);
        byte[] alicePublicKeyReceivedByBob = Utilities.splitArray(signatureAsReadByBob, 1);
        byte[] bobPublicKeyReceivedByBob = Utilities.splitArray(signatureAsReadByBob, 2);

        assertEquals(Utilities.toHexStringWithSeparator(bobPublicKeyReceivedByAlice), Utilities.toHexStringWithSeparator(bobPublicKey.toByteArray()));
        assertEquals(Utilities.toHexStringWithSeparator(alicePublicKeyReceivedByAlice), Utilities.toHexStringWithSeparator(alicePublicKey.toByteArray()));
        assertEquals(Utilities.toHexStringWithSeparator(alicePublicKeyReceivedByBob), Utilities.toHexStringWithSeparator(alicePublicKey.toByteArray()));
        assertEquals(Utilities.toHexStringWithSeparator(bobPublicKeyReceivedByBob), Utilities.toHexStringWithSeparator(bobPublicKey.toByteArray()));
        assertEquals(Utilities.toHexStringWithSeparator(universalPasswordHashReceivedByAlice), Utilities.toHexStringWithSeparator(universalPasswordHash));
        assertEquals(Utilities.toHexStringWithSeparator(universalPasswordHashReceivedByBob), Utilities.toHexStringWithSeparator(universalPasswordHash));
    }
    @Test
    public void serialization_isCorrect() throws Exception {
        String encryptionTestSend =
                "Under the wide and starry sky\n" +
                "Dig the grave and let me lie.\n" +
                "Glad did I live and gladly die,\n" +
                "And I laid me down with a will.\n";

        String longEncryptionTest =
                "Half a league, half a league, Half a league onward, All in the valley of Death Rode the six hundred." +
                "Forward, the Light Brigade! Charge for the guns! he said: Into the valley of Death Rode the six hundred." +
                "Forward, the Light Brigade! Was there a man dismay'd? Not tho' the soldier knew Someone had blunder'd:" +
                "Theirs not to make reply, Theirs not to reason why, Theirs but to do and die: Into the valley of Death Rode the six hundred." +
                "Cannon to right of them, Cannon to left of them, Cannon in front of them Volley'd and thunder'd;" +
                "Storm'd at with shot and shell, Boldly they rode and well, Into the jaws of Death, Into the mouth of Hell Rode the six hundred." +
                "Flash'd all their sabres bare, Flash'd as they turn'd in air, Sabring the gunners there, Charging an army, while All the world wonder'd:" +
                "Plunged in the battery-smoke Right thro' the line they broke; Cossack and Russian Reel'd from the sabre stroke Shatter'd and sunder'd." +
                "Then they rode back, but not Not the six hundred. Cannon to right of them, Cannon to left of them, Cannon behind them Volley'd and thunder'd;" +
                "Storm'd at with shot and shell, While horse and hero fell, They that had fought so well Came thro' the jaws of Death Back from the mouth of Hell," +
                "All that was left of them, Left of six hundred. When can their glory fade? O the wild charge they made! All the world wondered. Honor the charge they made," +
                "Honor the Light Brigade, Noble six hundred.";
        Payload p = new Payload.PayloadBuilder()
                .setStandardTypeValueforNonSTSPayload()
                .setNumberOfPayloadParameters(3)
                .setMicroserviceName("darkstar")
                .build();
        p.setPayload(0, "the quick brown fox jumped over the lazy dog".getBytes()); // Q
        p.setPayload(1, encryptionTestSend.getBytes());                             // R
        p.setPayload(2, longEncryptionTest.getBytes());                             // C
        // p has three payloads [Q,R,C]
        byte[] serialized = p.serialize();
        Payload q = new Payload(serialized);
        // q should have three payloads [Q,R,C]
        assertEquals(p.getMSName(), q.getMSName());
        assertEquals(p.getClientId(), q.getClientId());
        assertEquals(p.getType(), q.getType());
        assertEquals(p.getRemainingKeyUses(), q.getRemainingKeyUses());
        assertEquals(p.isReturnToCaller(), q.isReturnToCaller());
        assertEquals(p.getPayloadLength(), q.getPayloadLength());
        assertEquals(new String(p.getPayload(0)), new String(q.getPayload(0)));
        assertEquals(new String(p.getPayload(1)), new String(q.getPayload(1)));
        assertEquals(new String(p.getPayload(2)), new String(q.getPayload(2)));
        Payload r = new Payload(p);
        // r should have one payload equal to Q, the original p zero payload
        assertEquals(r.getPayloadLength(),1);
        assertEquals(new String(p.getPayload(0)), new String(r.getPayload(0)));
        r.addPayload(q.getPayload(1)); // r should now be [Q,R]
        r.addPayload(q.getPayload(2)); // r should now be [Q,R,C]
        assertEquals(new String(p.getPayload(0)), new String(r.getPayload(0)));
        assertEquals(new String(p.getPayload(1)), new String(r.getPayload(1)));
        assertEquals(new String(p.getPayload(2)), new String(r.getPayload(2)));
        r = new Payload(p);
        // r should have one payload equal to Q, the original p zero payload
        r.addPayload(q.getPayload(1));
        // r should now be [Q,R]
        // p has three payloads [Q,R,C]
        // q has three payloads [Q,R,C]
        p.join(r);
        // p should now have payloads [Q,R,C,R]
        p.join(q);
        // p should now have payloads [Q,R,C,R,R,C]
        assertEquals(p.getPayloadLength(),6);
        assertEquals(new String(p.getPayload(0)), new String(q.getPayload(0)));
        assertEquals(new String(p.getPayload(1)), new String(q.getPayload(1)));
        assertEquals(new String(p.getPayload(2)), new String(q.getPayload(2)));
        assertEquals(new String(p.getPayload(3)), new String(q.getPayload(1)));
        assertEquals(new String(p.getPayload(4)), new String(q.getPayload(1)));
        assertEquals(new String(p.getPayload(5)), new String(q.getPayload(2)));
    }
    @Test
    public void fileAuhorization_isCorrect() throws Exception {
        int[] a = { 1, 2 };
        FileAccess f = new FileAccess("doc.pdf", 300000, a);
        User sam = new User("sam", 0);
        User bill = new User("bill", 1);
        User jane = new User("jane", 0);
        User mary = new User("mary", 1);
        f.addNewAuthorizer(sam);
        f.addNewAuthorizer(bill);
        boolean g1 = f.grantAccess(jane);
        assertEquals(g1, false);
        boolean g2 = f.grantAccess(mary);
        assertEquals(g2, true);
    }
}