package com.fullsecurity.common;


public class SHA256 {

    public static byte[] sha256(byte[] byteArray) {
        byte[] paddedByteArray;
        if (byteArray.length == 0 || byteArray.length % 64 != 0) {
            int paddingBlockStart = (byteArray.length / 64) * 64;
            int excessByteCount = byteArray.length % 64;
            byte[] paddedBlock = padding(byteArray, paddingBlockStart, excessByteCount);
            paddedByteArray = new byte[paddingBlockStart + paddedBlock.length];
            for (int i = 0; i < paddingBlockStart; i++) paddedByteArray[i] = byteArray[i];
            for (int i = 0; i < paddedBlock.length; i++) paddedByteArray[i+paddingBlockStart] = paddedBlock[i];
        } else
            paddedByteArray = byteArray;

        int numberOfMessageBlocks = (paddedByteArray.length - 1) / 64 + 1;

        int[] K = {
                0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
                0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
                0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
                0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
                0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
                0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
                0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
                0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2
        };

        int[] H = { 0x6a09e667, 0xbb67ae85, 0x3c6ef372, 0xa54ff53a, 0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19 };

        int w[][] = new int[numberOfMessageBlocks][64];
        for (int i = 0; i < numberOfMessageBlocks; i++) {
            for (int j = 0; j < 16; j++) {
                int p = paddedByteArray[i*64 + j*4];
                p = p << 24;
                int q = paddedByteArray[i*64 + j*4 + 1];
                q = q << 16;
                q &= 0x00ff0000;
                int r = paddedByteArray[i*64 + j*4 + 2];
                r = r << 8;
                r &= 0x0000ff00;
                int s = paddedByteArray[i*64 + j*4 + 3];
                s &= 0x000000ff;
                w[i][j] = p | q | r | s;
            }
        }

        for (int i = 0; i < numberOfMessageBlocks; i++) {
            for (int j = 16; j < 64; j++) {
                long t1 = sigmaOne(w[i][j-2]);
                t1 &= 0x00000000ffffffffL;
                long t2 = w[i][j-7];
                t2 &= 0x00000000ffffffffL;
                long t3 = sigmaZero(w[i][j-15]);
                t3 &= 0x00000000ffffffffL;
                long t4 = w[i][j-16];
                t4 &= 0x00000000ffffffffL;
                long u = (t1 + t2 + t3 + t4) & 0x00000000ffffffffL;
                w[i][j] = (int) u;
            }

            int a = H[0];
            int b = H[1];
            int c = H[2];
            int d = H[3];
            int e = H[4];
            int f = H[5];
            int g = H[6];
            int h = H[7];

            for (int j = 0; j < 64; j++) {
                long T1a = h;
                T1a &= 0x00000000ffffffffL;
                long T1b = bigSigmaOne(e);
                T1b &= 0x00000000ffffffffL;
                long T1c = Ch(e, f, g);
                T1c &= 0x00000000ffffffffL;
                long T1d = K[j];
                T1d &= 0x00000000ffffffffL;
                long T1e = w[i][j];
                long u = (T1a + T1b + T1c + T1d + T1e) & 0x00000000ffffffffL;
                int T1 = (int) u;

                long T2a = bigSigmaZero(a);
                T2a &= 0x00000000ffffffffL;
                long T2b = Maj(a,b,c);
                T2b &= 0x00000000ffffffffL;
                u = (T2a + T2b) & 0x00000000ffffffffL;
                int T2 = (int) u;

                h = g;
                g = f;
                f = e;
                long Td = d;
                Td &= 0x00000000ffffffffL;
                u = T1;
                u &= 0x00000000ffffffffL;
                u = (u + Td) & 0x00000000ffffffffL;
                e = (int) u;
                d = c;
                c = b;
                b = a;
                long Ta1 = T1;
                Ta1 &= 0x00000000ffffffffL;
                long Ta2 = T2;
                Ta2 &= 0x00000000ffffffffL;
                u = (Ta1 + Ta2) & 0x00000000ffffffffL;
                a = (int) u;
            }

            H[0] = updateVariables(H[0], a);
            H[1] = updateVariables(H[1], b);
            H[2] = updateVariables(H[2], c);
            H[3] = updateVariables(H[3], d);
            H[4] = updateVariables(H[4], e);
            H[5] = updateVariables(H[5], f);
            H[6] = updateVariables(H[6], g);
            H[7] = updateVariables(H[7], h);
        }

        byte hh[] = new byte[32];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 4; j++) {
                hh[i*4+j] = getByte(H[i], j);
            }
        }

        return hh;
    }

    private static byte[] padding(byte[] byteArray, int paddingBlockStart, int excessByteCount) {
        // if excessByteCount > 55 an additional 512-bit block must be added
        // byteArray: AA...AAABB...BBBCC...CCCEE......................EEE
        //            <- 64 -><- 64 -><- 64 -><- excessByteCount(< 64) ->
        // A, B, C are 3 blocks of 192 bytes total
        // paddingBlockStart = 192
        // paddingBlockCount = 64 - excessBlockCount (> 0)
        // return EE.......................EESZZ..............ZZZCCCCCCCC
        //        <- excessByteCount(< 64) ->1<- zeroByteCount -><- 8  ->
        // S = 0x80
        byte[] b;
        int zeroByteCount = 55 - excessByteCount;
        b = new byte[zeroByteCount >= 0 ? 64 : 128];
        for (int i = 0; i < excessByteCount; i++) b[i] = byteArray[i+paddingBlockStart];
        b[excessByteCount] = (byte) 0x80;
        int bitCount = byteArray.length * 8;
        int upper = bitCount / 256;
        int lower = bitCount % 256;
        if (zeroByteCount >= 0) {
            // one padding block is sufficient
            for (int i = 0; i < zeroByteCount+6; i++) b[i+excessByteCount+1] = (byte) 0x00;
            b[62] = (byte) upper;
            b[63] = (byte) lower;
        } else {
            // add an additional padding block to the output
            for (int i = excessByteCount+1; i < 64; i++) b[i] = (byte) 0x00;
            for (int i = 64; i < 126; i++) b[i] = (byte) 0x00;
            b[126] = (byte) upper;
            b[127] = (byte) lower;
        }
        return b;
    }

    private static int updateVariables(int a, int b) {
        long x = a;
        x &= 0x00000000ffffffffL;
        long y = b;
        y &= 0x00000000ffffffffL;
        long z = (x + y) & 0x00000000ffffffffL;
        return (int) z;
    }

    public static int sigmaZero(int w) {
        return rotateRight(w, 7) ^ rotateRight(w, 18) ^ shiftRight(w, 3);
    }

    public static int sigmaOne(int w) {
        return rotateRight(w, 17) ^ rotateRight(w, 19) ^ shiftRight(w, 10);
    }

    public static int bigSigmaZero(int w) {
        return rotateRight(w, 2) ^ rotateRight(w, 13) ^ rotateRight(w, 22);
    }

    public static int bigSigmaOne(int w) {
        return rotateRight(w, 6) ^ rotateRight(w, 11) ^ rotateRight(w, 25);
    }

    public static int Ch(int x, int y, int z) {
        return (x & y) ^ (~x & z);
    }

    public static int Maj(int x, int y, int z) {
        return (x & y) ^ (x & z) ^ (y & z);
    }

    public static int rotateRight(int w, int n) {
        int s = 32 - n;
        return shiftRight(w, n) | (w << s);
    }

    public static int shiftRight(int w, int n) {
        int m = 0x80000000 >> (n - 1);
        return (w >> n) & ~m;
    }

    private static byte getByte(int w, int k) {
        return (byte) ((w >> (24-k*8)) & 0xff);
    }
}
