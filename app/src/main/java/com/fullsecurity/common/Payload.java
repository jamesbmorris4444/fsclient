package com.fullsecurity.common;

public class Payload {
    private String msName;
    private int clientId;
    private int type;
    private int remainingKeyUses;
    private boolean returnToCaller;
    private byte[][] payload;

    public Payload(int type, Payload p) {
        // request payload that has been mapped from an MS name to an MS index
        this.type = type;
        msName = p.getMSName();
        clientId = p.getClientId();
        remainingKeyUses = p.getRemainingKeyUses();
        returnToCaller = p.isReturnToCaller();
        payload = p.getPayload();
    }

    private Payload(PayloadBuilder builder) {
        this.msName = builder.msName;
        this.clientId = builder.clientId;
        this.type = builder.pType;
        this.remainingKeyUses = builder.remainingKeyUses;
        this.returnToCaller = builder.returnToCaller;
        this.payload = builder.payload;
    }

    public int getRemainingKeyUses() {
        return remainingKeyUses;
    }

    public byte[][] getPayload() {
        return payload;
    }

    public static class PayloadBuilder {
        private String msName;
        private int clientId;
        private int pType;
        private int remainingKeyUses;
        private boolean returnToCaller;
        private byte[][] payload;

        public PayloadBuilder setMicroserviceName(String msName) {
            this.msName = msName;
            return this;
        }

        public PayloadBuilder setEmptyMicroserviceName() {
            this.msName = "<NONE>";
            return this;
        }
        
        public PayloadBuilder setClientId(int clientId) {
            this.clientId = clientId;
            return this;
        }

        public PayloadBuilder setPayloadType(int pType) {
            this.pType = pType;
            return this;
        }

        public PayloadBuilder setErrorValueforErrorPayload(int pType) {
            this.pType = pType;
            return this;
        }

        public PayloadBuilder setStandardTypeValueforNonSTSPayload() {
            this.pType = 3;
            return this;
        }

        public PayloadBuilder setZeroTypeValueforResponsePayload() {
            this.pType = pType;
            return this;
        }

        public PayloadBuilder setRemainingKeyUses(int remainingKeyUses) {
            this.remainingKeyUses = remainingKeyUses;
            return this;
        }

        public PayloadBuilder setReturnToCaller(boolean returnToCaller) {
            this.returnToCaller = returnToCaller;
            return this;
        }

        public PayloadBuilder setNumberOfPayloadParameters(int n) {
            payload = new byte[n][];
            return this;
        }

        public PayloadBuilder setPayload(int n, byte[] payload) {
            this.payload = new byte[n][];
            this.payload[0] = payload;
            return this;
        }

        public Payload build() {
            return new Payload(this);
        }

    }

    // old stuff again

    public Payload(Payload p) {
        // create a new Payload that is exactly the same as the Payload argument, except individual payloads 1,2,... have been
        // stripped out, leaving only the single parameter payload remaining
        this.type = type;
        msName = p.getMSName();
        clientId = p.getClientId();
        remainingKeyUses = p.getRemainingKeyUses();
        returnToCaller = p.isReturnToCaller();
        payload = new byte[1][];
        byte[] a = p.getPayload(0);
        payload[0] = new byte[a.length];
        System.arraycopy(a, 0, payload[0], 0, a.length);
    }

    private byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte) value};
    }

    public byte[] serialize() {
        //  1.  S as a 4-byte array
        //  2.  msName as an S-byte array
        //  3.  clientId as a 4-byte array
        //  4.  type as a 4-byte array
        //  5.  remainingKeyUses as a 4-byte array
        //  6.  returnToCaller as a 4-byte array (0 or 1)
        //  7.  N (number if payloads)
        //  8.  N 4-byte integers giving the length of each of the N payloads
        //  9.  Payload 1
        // 10.  Payload 2
        //      ...
        //  ?.  Payload N
        int N = (payload == null ? 0 : payload.length);
        int totalPayloadLength = 0;
        for (int k = 0; k < N; k++) totalPayloadLength += payload[k].length;
        byte[] buffer = new byte[4+msName.length()+4+4+4+4+4+(4*N)+totalPayloadLength];
        int bIndex = 0;

        System.arraycopy(intToByteArray(msName.length()), 0, buffer, bIndex, 4);
        bIndex += 4;

        System.arraycopy(msName.getBytes(), 0, buffer, bIndex, msName.length());
        bIndex += msName.length();

        System.arraycopy(intToByteArray(clientId), 0, buffer, bIndex, 4);
        bIndex += 4;

        System.arraycopy(intToByteArray(type), 0, buffer, bIndex, 4);
        bIndex += 4;

        System.arraycopy(intToByteArray(remainingKeyUses), 0, buffer, bIndex, 4);
        bIndex += 4;

        System.arraycopy(intToByteArray(returnToCaller ? 1 : 0), 0, buffer, bIndex, 4);
        bIndex += 4;

        System.arraycopy(intToByteArray(N), 0, buffer, bIndex, 4);
        bIndex += 4;

        for (int k = 0; k < N; k++) {
            System.arraycopy(intToByteArray(payload[k].length), 0, buffer, bIndex, 4);
            bIndex += 4;
        }

        for (int k = 0; k < N; k++) {
            System.arraycopy(payload[k], 0, buffer, bIndex, payload[k].length);
            bIndex += payload[k].length;
        }

        return buffer;
    }

    private int fromByteArray(byte[] bytes) {
        return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }

    public Payload(byte[] buffer) {
        //  1.  S as a 4-byte array
        //  2.  msName as an S-byte array
        //  3.  clientId as a 4-byte array
        //  4.  type as a 4-byte array
        //  5.  remainingKeyUses as a 4-byte array
        //  6.  returnToCaller as a 4-byte array (0 or 1)
        //  7.  N (number if payloads)
        //  8.  N 4-byte integers giving the length of each of the N payloads
        //  9.  Payload 1
        // 10.  Payload 2
        //      ...
        //  ?.  Payload N
        byte[] iBuffer = new byte[4];
        int bIndex = 0;

        System.arraycopy(buffer, bIndex, iBuffer, 0, 4);
        bIndex += 4;
        int S = fromByteArray(iBuffer);

        byte[] sBuffer = new byte[S];
        System.arraycopy(buffer, bIndex, sBuffer, 0, S);
        msName = new String(sBuffer);
        bIndex += S;

        System.arraycopy(buffer, bIndex, iBuffer, 0, 4);
        bIndex += 4;
        clientId = fromByteArray(iBuffer);

        System.arraycopy(buffer, bIndex, iBuffer, 0, 4);
        bIndex += 4;
        type = fromByteArray(iBuffer);

        System.arraycopy(buffer, bIndex, iBuffer, 0, 4);
        bIndex += 4;
        remainingKeyUses = fromByteArray(iBuffer);

        System.arraycopy(buffer, bIndex, iBuffer, 0, 4);
        bIndex += 4;
        returnToCaller = fromByteArray(iBuffer) == 1;

        System.arraycopy(buffer, bIndex, iBuffer, 0, 4);
        bIndex += 4;
        int N = fromByteArray(iBuffer);

        if (N == 0)
            payload = null;
        else {
            int[] counts = new int[N];
            for (int k = 0; k < N; k++) {
                System.arraycopy(buffer, bIndex, iBuffer, 0, 4);
                bIndex += 4;
                counts[k] = fromByteArray(iBuffer);
            }

            payload = new byte[N][];
            for (int k = 0; k < N; k++) {
                payload[k] = new byte[counts[k]];
                System.arraycopy(buffer, bIndex, payload[k], 0, counts[k]);
                bIndex += counts[k];
            }
        }
    }

    public void appendArgument(String a, String separator) {
        String arg = new String(payload[0]);
        payload[0] = (arg + separator + a).getBytes();
    }

    public void initializeArguments(byte[] arg) {
        payload[0] = arg;
    }

    public String getAndRemoveArgument(String separator) {
        if (payload[0].length == 0) return null;
        int lensep = separator.length();
        String args = new String(payload[0]);
        int k = args.indexOf(separator);
        String arg;
        if (k < 0) {
            arg = args;
            args = "";
        } else {
            arg = args.substring(0, k);
            args = args.substring(k+lensep);
        }
        payload[0] = args.getBytes();
        return arg;
    }

    public void join(Payload p) {
        byte[][] newPayload = new byte[payload.length+p.getPayloadLength()-1][];
        byte[][] pBytes = p.getPayload();
        System.arraycopy(payload, 0, newPayload, 0, payload.length);
        System.arraycopy(pBytes, 1, newPayload, payload.length, pBytes.length-1);
        payload = newPayload;

    }

    public void setRemainingKeyUses(int remainingKeyUses) {
        this.remainingKeyUses = remainingKeyUses;
    }

    public boolean isReturnToCaller() {
        return returnToCaller;
    }

    public int getType() { return type; }

    public void setType(int type) {
        this.type = type;
    }

    public int getClientId() {
        return clientId;
    }

    public void setPayload(int ndx, byte[] b) { payload[ndx] = b; }

    public void addPayload(byte[] b) {
        byte[][] newPayload = new byte[payload.length+1][];
        System.arraycopy(payload, 0, newPayload, 0, payload.length);
        newPayload[payload.length] = b;
        payload = newPayload;
    }

    public byte[] getPayload(int ndx) { return payload[ndx]; }

    public int getPayloadLength() { return payload.length; }

    public String getMSName() { return msName; }

    public void setMSName(String name) { msName = name; }

    public String toString() {
        String s;
        if (msName == null)
            s = "PAYLOAD:[id="+clientId+" rem="+remainingKeyUses+" pays="+payload.length;
        else
            s = "PAYLOAD:[name="+msName+" id="+clientId+" rem="+remainingKeyUses+" pays="+payload.length;
        for (int k = 0; k < payload.length; k++) {
            if (payload[k].length < 16)
                s += " pay["+k+"]="+(new String(payload[k]));
            else {
                byte[] first = new byte[8];
                byte[] last = new byte[8];
                System.arraycopy(payload[k], 0, first, 0, 8);
                System.arraycopy(payload[k], payload[k].length-8, last, 0, 8);
                s += " pay[" + k + "]LONG(" + payload[k].length + "):"+Utilities.toHexStringWithSeparator(first)+"|"+Utilities.toHexStringWithSeparator(last);
            }
        }
        return s;
    }
}
