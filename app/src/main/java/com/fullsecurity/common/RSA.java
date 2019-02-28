package com.fullsecurity.common;

import java.math.BigInteger;
import java.util.Random;

@SuppressWarnings("all")
public class RSA {
    private BigInteger p;
    private BigInteger q;
    private BigInteger N;
    private BigInteger phi;
    private BigInteger e;
    private BigInteger d;
    private int bitlength = 2048;
    private Random r;

    public final BigInteger g1 = new BigInteger(
        "2457940098629354814176047001302332863160298428623576000830098088544891673172944282547464711780061577237934544303048273840819561749739122369856771427629147582808516652816" +
        "3628851365724803920025741049840870970229710655052180597322829874425324536722698195460211137120421424389229010983134467537138528810500978869879589598648948344324561575885" +
        "9276322080973461608204836547226227151854102689732041562168158776636591698092742932150608410090762662125330661562843123909460990107484348410958655399884039789903818199208" +
        "55612966343676359362059533814274726687528685177521063417670118485087056165192771579044976717848539681398967353"
    );
    public final BigInteger g2 = new BigInteger(
        "2458807652835675935659071172074979659278773665061103569731535150751495733773428814942578400329849956570041285607688798648557467896980723308064958821505119329542104412994" +
        "0460063541181531798539785054270630828592326172268086044059561292094956845304949780326215447963184017703931408032906543020721168953271017848180520271165528685432278037403" +
        "9370867879729505680326750305276912459801168011524471002920914091036170855183420124241280719203822905508226213268255219396673366216987522708503293085180459065017345648559" +
        "31782827633350895590063397672408749227547142442336283856247022856232450603571295918118290492747093936247456469"
    );
    public final BigInteger g3 = new BigInteger(
        "1406117754534335974721503295512557315000966888412304102130714748319366502583813549277071207461896137140048567973294714419574238022989329160131181219545511365" +
        "45310691825239656306853041336643025250637008181565813951879803643773997138214211790254733309508855818868893860840894872446950943849797251963882988712299"
    );

    public RSA() {
        r = new Random();
        //p = BigInteger.probablePrime(bitlength, r);
        //q = BigInteger.probablePrime(bitlength, r);
        p = g1;
        q = g2;
        N = p.multiply(q);
        phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
        //e = BigInteger.probablePrime(bitlength / 2, r);
        e = g3;
        while (phi.gcd(e).compareTo(BigInteger.ONE) > 0 && e.compareTo(phi) < 0) e.add(BigInteger.ONE);
        d = e.modInverse(phi);
    }

    public byte[] encrypt(byte[] message) {
        return (new BigInteger(message)).modPow(e, N).toByteArray();
    }

    public byte[] readSignature(byte[] message) {
        return (new BigInteger(message)).modPow(e, N).toByteArray();
    }

    public byte[] decrypt(byte[] message) {
        return (new BigInteger(message)).modPow(d, N).toByteArray();
    }

    public byte[] applySignature(byte[] message) {
        return (new BigInteger(message)).modPow(d, N).toByteArray();
    }

    public byte[] getPublicKeyE() {
        return e.toByteArray();
    }

    public byte[] getPublicKeyN() {
        return N.toByteArray();
    }

    public void setPublicKeyE(byte[] k) { e = new BigInteger(k); }

    public void setPublicKeyN(byte[] k) { N = new BigInteger(k); }

    public boolean isMessageTooLarge(BigInteger message) {
        return message.compareTo(N) >= 0;
    }
}
