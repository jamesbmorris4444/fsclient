package com.fullsecurity.server;

import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;

import com.fullsecurity.common.DHConstants;
import com.fullsecurity.common.Payload;
import com.fullsecurity.common.RSA;
import com.fullsecurity.common.Rijndael;
import com.fullsecurity.common.SHA256;
import com.fullsecurity.common.Utilities;
import com.fullsecurity.microservices.SDVRequestProcessorCategories;
import com.fullsecurity.microservices.SDVRequestProcessorCheeseFinder;
import com.fullsecurity.microservices.SDVRequestProcessorComplexLoop;
import com.fullsecurity.microservices.SDVRequestProcessorEncryptionLoop;
import com.fullsecurity.microservices.SDVRequestProcessorLongEncryption;
import com.fullsecurity.microservices.SDVRequestProcessorReadFile;
import com.fullsecurity.microservices.SDVRequestProcessorShoppingCart;
import com.fullsecurity.microservices.SDVRequestProcessorShowDirectory;
import com.fullsecurity.microservices.SDVRequestProcessorStoreItems;
import com.fullsecurity.shared.MainActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Random;
import java.util.concurrent.Semaphore;

@SuppressWarnings("all")
public class SDVServer {

    public static final String password = "Universal password for Acme Corporation - 10/17/15";
    public static byte[] universalPasswordHash;
    public static final int NUMBER_OF_KEY_USES_PER_KEY = 100;
    public static final long LIFETIME_OF_CLIENT_STATE_IN_SECS = 86400L; // 24 hours
    public static final int portNumber = 8000;
    public static final int msPortStart = 9000;
    private static final boolean NODEBUG = true;

    public static final int UNABLE_TO_FIND_MICROSERVICE = -10;
    public static final int CALLER_COULD_NOT_CONNECT_TO_MICROSERVICE = -11;
    public static final int IMPROPER_EXPECTED_ARGUMENT_IN_REQUEST = -12;
    public static final int EXCEPTION_READING_REQUEST_FOR_SERVICE = -13;
    public static final int EXCEPTION_WRITING_REQUEST_FOR_SERVICE = -14;
    public static final int MICROSERVICE_RETURNED_ERROR_CONDITION = -15;
    public static final int SERVER_INTERNAL_ERROR_ID_NOT_FOUND = -16;
    public static final int SERVER_VERIFICATIION_OF_STS_FAILED = -17;
    public static final int SERVER_INTERNAL_ERROR_NOT_ABLE_TO_ASSIGN_ID = -18;
    public static final int CLIENT_HAS_NO_KEY_USES_REMAINING = -19;
    public static final int SERVER_INTERNAL_ERROR_IMPOSSIBLE_VALUE_FOR_TYPE_EXPECTED = -20;
    public static final int EXCEPTION_IN_SERVER_DURING_AES_ENCRYPTION = -21;
    public static final int ENCRYPTION_KEYS_HAVE_EXPIRED = -22;
    public static final int CLIENT_VERIFICATIION_OF_STS_FAILED = -23;
    public static final int EXCEPTION_IN_SERVER_DURING_AES_DECRYPTION = -24;
    public static final int EXCEPTION_IN_CLIENT_DURING_AES_ENCRYPTION = -25;
    public static final int EXCEPTION_IN_CLIENT_DURING_AES_DECRYPTION = -26;
    public static final int INTERNAL_ERROR_UNKNOWN_OPERATION = -27;
    public static final int CLIENT_COULD_NOT_CONNECT_TO_SERVER = -28;
    public static final int CLIENT_TIMED_OUT_WHILE_WAITING_FOR_SERVER = -29;
    public static final int CANNOT_GRANT_ACCESS_TO_FILE = -30;
    public static final int ERROR_IN_DATABASE_OPERATION = -31;

    // data provided by the server to microservices
    public static ArrayList<ServerState> serverStateList = new ArrayList<>(); // keeps the state of known clients (must be synchronized by a semaphore)
    public static Semaphore sslSemaphore = new Semaphore(1);          // semaphore for serverStateList

    public static BitSet portAssignments = new BitSet();                      // microservice port assignments (must be synchronized by a semaphore)
    public static Semaphore portSemaphore = new Semaphore(1);         // semaphore for portAssignments

    public static ArrayList<MSSettings> msMap = new ArrayList<>();            // data about microservices (does not change and does not have to be synchronized)
    public static Semaphore fileSemaphore = new Semaphore(1);         // semaphore for file access

    public static ArrayList<User> users = new ArrayList<>();                  // list of users able to log in to the system

    public static ArrayList<SDVFile> accessibleFiles = new ArrayList<>();     // list of accessible files plus security
    public static MainActivity mainActivity;                                  // used only for demo purposes, will be removed in a real server

    public SDVServer () {}

    public SDVServer(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void startServer() {

        universalPasswordHash = SHA256.sha256(password.getBytes());

        ServerFullDirectoryPathName fdpn = new ServerFullDirectoryPathName();
        String dirName = fdpn.getFullDirectoryPathName("com.fullsecurity.shared");
        File directory = new File(dirName);
        File[] files = directory.listFiles();
        ArrayList<FileAccess> fileAccessList;
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                fileAccessList = new ArrayList<>();
                int a[] = {0, 1};
                fileAccessList.add(new FileAccess(1000000, a));
                int b[] = {0, 1};
                fileAccessList.add(new FileAccess(5000000, b));
                accessibleFiles.add(new SDVFile(dirName + "/" + files[i].getName(), fileAccessList, -1, files[i].length()));
            }
        }

        // DO NOT change the order of the four lines below
        users.add(new User("tlfrg",0));
        users.add(new User("trfrg",0));
        users.add(new User("blfrg",1));
        users.add(new User("brfrg",1));

        // if parent is null, returnToCaller is a "don't care" value
        // one and only one returnToCaller value in a group must be false, but the one need not be the parent
        // allowable return to caller values for four microservices are:
        //   false, true, true, true (the first three microservices see a response from downstream microservices, first microservice responds to client)
        //   false, false, true, true (the second and third microservices see a response from downstream microservices, second microservice responds to client)
        //   false, false, false, true (only the third microservice sees a response from downstream microservices, third microservice responds to client)
        //   false, false, false, true (no microservices see a response from downstream microservices, last microservice responds to client)
        msMap.add(new MSSettings("categories", false, null));
        msMap.add(new MSSettings("showdir", false, null));
        msMap.add(new MSSettings("readfile", false, null));
        msMap.add(new MSSettings("cheesefinder", false, null));
        msMap.add(new MSSettings("cheesesearch", true, "cheesefinder"));
        msMap.add(new MSSettings("longencryption", false, null));
        msMap.add(new MSSettings("longencryption1", true, "longencryption"));
        msMap.add(new MSSettings("longencryption2", true, "longencryption"));
        msMap.add(new MSSettings("longencryption3", true, "longencryption"));
        msMap.add(new MSSettings("complexloop", false, null));
        msMap.add(new MSSettings("complexloop1", true, "complexloop"));
        msMap.add(new MSSettings("complexloop2", true, "complexloop"));
        msMap.add(new MSSettings("complexloop3", true, "complexloop"));
        msMap.add(new MSSettings("complexloop4", true, "complexloop"));
        msMap.add(new MSSettings("complexloop5", true, "complexloop"));
        msMap.add(new MSSettings("complexloop6", true, "complexloop"));
        msMap.add(new MSSettings("encryptionloop", false, null));
        msMap.add(new MSSettings("items", false, null));
        msMap.add(new MSSettings("shoppingcart", false, null));

        Thread serverThread = new Thread() {
            public void run() {
                serverListener();
            }
        };
        serverThread.start();
    }

    private void serverListener() {
        Socket clientSocket = null;
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(portNumber);
            while (true) {
                // server waits at this line for client to connect to socket and make a request
                clientSocket = serverSocket.accept();
                // client has made a request, handle this client request
                handleClient(clientSocket);
            }
        } catch (Exception e) {
            Log.d("JIM","SDVServer: SERVER SHUTDOWN ************* EXCEPTION 100="+e.toString());
            e.printStackTrace();
            try {
                if (serverSocket != null) serverSocket.close();
                if (clientSocket != null) clientSocket.close();
            } catch (Exception f) {};
        }
    }

    public SDVFile findFileAndLock(String fullFilePathName, int clientId) {
        // null: could not find file
        // null: file is already locked
        // not null: file successfully locked
        try { fileSemaphore.acquire(); } catch (Exception e) {}
        SDVFile returnValue = null;
        for (int i = 0; i < accessibleFiles.size(); i++) {
            if (accessibleFiles.get(i).getName().equals(fullFilePathName)) {
                returnValue = accessibleFiles.get(i);
                boolean lockNewlySetOrLockWasAlreadySet = returnValue.setLock(clientId);
                if (!lockNewlySetOrLockWasAlreadySet) returnValue = null;
                break;
            }
        }
        try { fileSemaphore.release(); } catch (Exception e) {}
        return returnValue;
    }

    public void findFileAndClearLock(String fullFilePathName) {
        try { fileSemaphore.acquire(); } catch (Exception e) {}
        for (int i = 0; i < accessibleFiles.size(); i++) {
            if (accessibleFiles.get(i).getName().equals(fullFilePathName)) {
                accessibleFiles.get(i).releaseLock();
                break;
            }
        }
        try { fileSemaphore.release(); } catch (Exception e) {}
    }

    private int processIncomingRequest(Payload request) {
        // request type > 3 is the same as 3

        ServerState cs;
        int nextAvailableServerStateIndex;
        int ident = request.getClientId();

        // id is the userId (or the negative of it), a unique integer 0-3 assigned to each of the fragments, this integer identifiers the fragment
        // it is not necessarily true that the userId (ident) is the same as the serverState index assigned to userId
        // 1. ident < 0 : create a new ServerState
        // 2. ident >= 0 AND ident is not found in ServerState : return internal error indicating ident not found
        // 3. ident >= 0 AND ident is found in ServerState AND clientState(ident) has not expired : Use existing ServerState
        // 4. ident >= 0 AND ident was found in ServerState AND clientState(ident) has expired : send error indicating keys have expired AND remove entry from CLientSate

        // handle cases 2 and 4 above
        int idFoundInList = -1;
        if (ident >= 0) {
            idFoundInList = indexOfClientWithId(ident);
            if (idFoundInList < 0) {
                // clientIdentifier was not found in the serverStateList
                return SERVER_INTERNAL_ERROR_ID_NOT_FOUND;
            }
            // clientIdentifier was found in the clientState
            //if (serverStateList.get(idFoundInList).hasExpired(LIFETIME_OF_CLIENT_STATE_IN_SECS) && request.getType() >= 3) {
            if (serverStateList.get(idFoundInList).hasExpired(mainActivity.expireTime()) && request.getType() >= 3) {
                try { sslSemaphore.acquire(); } catch (Exception e) {};
                serverStateList.remove(idFoundInList);
                try { sslSemaphore.release(); } catch (Exception e) {};
                return ENCRYPTION_KEYS_HAVE_EXPIRED; // error: encryption keys have expired
            }
        }

        // serverStateList(clientIdentifier) has not expired

        if (ident < 0) {
            // new client, ident < 0, but -ident is the userId of the client
            cs = new ServerState(-ident-1);
        } else {
            // existing client, ident >= 0
            cs = serverStateList.get(idFoundInList);
            if (cs.getRemainingKeyUses() <= 0 && request.getType() > 2) {
                return CLIENT_HAS_NO_KEY_USES_REMAINING; // error bufferedReaderStream request, no key uses remaining
            }
        }

        // cs and newId have been set to good values

        if (cs.getTypeExpected() < 1 || cs.getTypeExpected() > 3) {;
            return SERVER_INTERNAL_ERROR_IMPOSSIBLE_VALUE_FOR_TYPE_EXPECTED; // error, internal error bufferedReaderStream typeExpected
        }
        if (checkForImproperClientStateExpectedType(request.getType(), cs.getTypeExpected()) < 0) {
            return IMPROPER_EXPECTED_ARGUMENT_IN_REQUEST; // error bufferedReaderStream request, not expected type
        }

        // there are no errors bufferedReaderStream the incoming data, the operation should succeed unless there is a catastrophic error

        if (ident < 0) {
            // new client
            serverStateList.add(cs);
        } else {
            // existing client
            if (request.getType() >= 3) cs.setRemainingKeyUses(cs.getRemainingKeyUses()-1);
        }
        cs.setTimeStamp(System.currentTimeMillis());
        cs.setTypeExpected(getNextClientStateExpectedType(request.getType(), cs.getTypeExpected()));
        if (request.getType() == 2) cs.setRemainingKeyUses(NUMBER_OF_KEY_USES_PER_KEY);

        mainActivity.runOnUiThread(new Runnable() { public void run() { mainActivity.updateClientState(); }});

        return (ident < 0 ? -ident-1 : ident); // no errors
    }

    private int checkForImproperClientStateExpectedType(int requestType, int csExpectedType)  {
        // request type > 3 is the same as three

        // Allowed
        // CS=1 RQ=1
        // CS=2 RQ=2
        // CS=3 RQ=3
        // CS=3 RQ=1
        // All others are errors
        if (requestType > 3) requestType = 3;
        if (requestType == csExpectedType) return 0;
        if (requestType == 1 && csExpectedType == 3) return 0;
        return IMPROPER_EXPECTED_ARGUMENT_IN_REQUEST;
    }

    private int getNextClientStateExpectedType(int requestType, int csExpectedType)  {
        // request type > 3 is the same as three

        // ServerState next expected type
        // rows = current ServerState expected type
        // cols = current request type
        //    1    2    3
        //
        //1   2    1    1
        //2   1    3    1
        //3   2    1    3
        if (requestType > 3) requestType = 3;
        int[][] nextType = {{ 2,1,1 },{ 1,3,1 },{ 2,1,3 }};
        return nextType[csExpectedType-1][requestType-1];
    }

    private int indexOfClientWithId(int id) {
        for (int k = 0; k < serverStateList.size(); k++) if (serverStateList.get(k).getId() == id) return k;
        return -1;
    }

    private Payload mapFromMSNameToMSIndex(Payload request) {
        int pType = request.getType();
        if (pType == 1 || pType == 2) return request;
        int msIndex = -1;
        for (int i = 0; i < msMap.size(); i++)
            if (msMap.get(i).getName().equals(request.getMSName()))
                msIndex = i;
        if (msIndex < 0)
            return null;
        else {
            Payload p = new Payload(msIndex+3, request);
            return p;
        }
    }

    private void handleClient(Socket clientSocket) {
        PrintWriter pws = null;
        BufferedReader brs = null;
        try {
            pws = new PrintWriter(clientSocket.getOutputStream(), true);
            brs = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (Exception e) {
            // Cannot read server request; client will have to wait for timeout
            // If the problem is with the printWriter, it does no good to continue, because response cannot be sent to the client
            Log.d("JIM","SDVServer: ************* EXCEPTION 101="+e.toString());
            e.printStackTrace();
            closeAll(clientSocket, pws, brs);
            return;
        }
        // clientSocket, pws, and brs are all good values
        // start reading the incoming String from the socket
        StringBuffer sb = new StringBuffer();
        String s = null;
        while (true) {
            try{
                s = brs.readLine();
            } catch (Exception e) {
                Log.d("JIM","SDVServer: ************* EXCEPTION 102="+e.toString());
                e.printStackTrace();
                String outbound = "ERR:" + (-EXCEPTION_READING_REQUEST_FOR_SERVICE);
                writeToOutputSocket(clientSocket, pws, brs, outbound);
                return;
            }
            if (s == null) break;
            if (s.equals("***EOF***")) break;
            sb.append(s);
        }
        // end reading the incoming String from the socket
        Payload payload = new Payload(Base64.decode(sb.toString(), Base64.DEFAULT));
        Payload request = mapFromMSNameToMSIndex(payload);
        if (request == null)
            writeToOutputSocket(clientSocket, pws, brs, "ERR:"+(-UNABLE_TO_FIND_MICROSERVICE)+"|"+payload.getMSName());
        else {
            int clientId = processIncomingRequest(request);
            if (clientId < 0)
                writeToOutputSocket(clientSocket, pws, brs, "ERR:"+(-clientId));
            else {
                int stateIndex = indexOfClientWithId(clientId);
                if (stateIndex < 0) {
                    // clientIdentifier was not found in the clientState
                    writeToOutputSocket(clientSocket, pws, brs, "ERR:" + (-SERVER_INTERNAL_ERROR_ID_NOT_FOUND));
                } else {
                    switch (request.getType()) {
                        case 1: // first of two STS requests
                            writeToOutputSocket(clientSocket, pws, brs, processType1Request(stateIndex, clientId, request));
                            break;
                        case 2: // second of two STS requests
                            writeToOutputSocket(clientSocket, pws, brs, processType2Request(stateIndex, clientId, request));
                            break;
                        default:
                            byte[] responseFromPreprocessor = preprocessType3Request(stateIndex, request);
                            String checkForErrors = new String(responseFromPreprocessor);
                            if (checkForErrors.startsWith("ERR:"))
                                writeToOutputSocket(clientSocket, pws, brs, checkForErrors);
                            else {
                                // leave the world of the server and enter the world of the microservice
                                // call the root microservice and expect a response here
                                serverStateList.get(stateIndex).setPrintWriter(pws);
                                int msIndex = request.getType() - 3; // STS part one=1, STS part two=2, first command after STS=3, so need to subtract 3 to get the correct index
                                int port = portAssignment();
                                // the following is pending: closeAll(clientSocket, pws, brs); and recyclePortNumber(port);
                                // These tasks must be completed when the new microservice thread exits
                                // in what follows, there is no need to release the semaphore, because a downstream microservice will not try to acquire it again
                                boolean msFound = true;
                                switch(msIndex) {
                                    case 0:
                                        SDVRequestProcessorCategories ms00 = new  SDVRequestProcessorCategories(port);
                                        try { ms00.startupSemaphore.acquire(); } catch (Exception e) {} // block until downstream microservice is listening at its port
                                        break;
                                    case 1:
                                        SDVRequestProcessorShowDirectory ms01 = new SDVRequestProcessorShowDirectory(port);
                                        try { ms01.startupSemaphore.acquire(); } catch (Exception e) {} // block until downstream microservice is listening at its port
                                        break;
                                    case 2:
                                        SDVRequestProcessorReadFile ms02 = new SDVRequestProcessorReadFile(port, this);
                                        try { ms02.startupSemaphore.acquire(); } catch (Exception e) {} // block until downstream microservice is listening at its port
                                        break;
                                    case 3:
                                        SDVRequestProcessorCheeseFinder ms03 = new SDVRequestProcessorCheeseFinder(port);
                                        try { ms03.startupSemaphore.acquire(); } catch (Exception e) {} // block until downstream microservice is listening at its port
                                        break;
                                    case 5:
                                        SDVRequestProcessorLongEncryption ms05 = new SDVRequestProcessorLongEncryption(port);
                                        try { ms05.startupSemaphore.acquire(); } catch (Exception e) {} // block until downstream microservice is listening at its port
                                        break;
                                    case 9:
                                        SDVRequestProcessorComplexLoop ms09 = new SDVRequestProcessorComplexLoop(port);
                                        try { ms09.startupSemaphore.acquire(); } catch (Exception e) {} // block until downstream microservice is listening at its port
                                        break;
                                    case 16:
                                        SDVRequestProcessorEncryptionLoop ms16 = new SDVRequestProcessorEncryptionLoop(port);
                                        try { ms16.startupSemaphore.acquire(); } catch (Exception e) {} // block until downstream microservice is listening at its port
                                        break;
                                    case 17:
                                        SDVRequestProcessorStoreItems ms17 = new SDVRequestProcessorStoreItems(port);
                                        try { ms17.startupSemaphore.acquire(); } catch (Exception e) {} // block until downstream microservice is listening at its port
                                        break;
                                    case 18:
                                        SDVRequestProcessorShoppingCart ms18 = new SDVRequestProcessorShoppingCart(port);
                                        try { ms18.startupSemaphore.acquire(); } catch (Exception e) {} // block until downstream microservice is listening at its port
                                        break;
                                    // STEP 11 goes here
                                    default:
                                        msFound = false;
                                        break;
                                }
                                if (msFound)
                                    sendToMicroservice(msMap.get(msIndex).getName(), clientSocket, responseFromPreprocessor, clientId, request.getType(), pws, brs, port, msMap.get(msIndex).isReturnToMe(), mainActivity);
                                else
                                    writeToOutputSocket(clientSocket, pws, brs, "ERR:"+(-UNABLE_TO_FIND_MICROSERVICE)+"|"+msMap.get(msIndex).getName());
                            }
                            break;
                    }
                }
            }
        }
    }

    private String processType1Request(int stateIndex, int clientId, Payload request) {
        // first request of two bufferedReaderStream station-to-station protocol request by client
        // Payload request from client:
        //   0: public key generated by client

        // create a public key/private key pair to be used for STS signature
        RSA rsa = new RSA();

        // initial random secret guesse for DH
        Random rnd = new Random();
        BigInteger initialSecret = new BigInteger(1024, rnd);
        serverStateList.get(stateIndex).setServerPublicKey(DHConstants.G.modPow(initialSecret, DHConstants.P).toByteArray());
        
        // sign the universal password hash and the DH "Public Keys" (univeralPasswordHash, g^y mod port, g^x mod port) (order is important)
        byte[] pwSignature = rsa.applySignature(universalPasswordHash);
        byte[] localPublicKeySignature = rsa.applySignature(serverStateList.get(stateIndex).getServerPublicKey());
        serverStateList.get(stateIndex).setClientPublicKey(request.getPayload(0));
        byte[] remotePublicKeySignature = rsa.applySignature(serverStateList.get(stateIndex).getClientPublicKey());

        // compute shared secret key K = (g^y mod port)^x mod port
        BigInteger publicKeyFromClientAsBigInteger = new BigInteger(request.getPayload(0));
        BigInteger sharedSecretKey = publicKeyFromClientAsBigInteger.modPow(initialSecret, DHConstants.P);
        byte[] key1024 = sharedSecretKey.toByteArray();
        serverStateList.get(stateIndex).setKey(SHA256.sha256(key1024));

        // encrypt the signature with secret key K and send the encrypted signature to client as response to request
        Rijndael AESalgorithm = new Rijndael();
        byte[] pwCipher = null;
        byte[] localPublicKeyCipher = null;
        byte[] remotePublicKeyCipher = null;
        try {
            AESalgorithm.makeKey(serverStateList.get(stateIndex).getKey(), 256, AESalgorithm.DIR_ENCRYPT);
            pwCipher = AESalgorithm.encryptArray(pwSignature);
            localPublicKeyCipher = AESalgorithm.encryptArray(localPublicKeySignature);
            remotePublicKeyCipher = AESalgorithm.encryptArray(remotePublicKeySignature);
        } catch (Exception e) {
            Log.d("JIM","SDVServer: ************* EXCEPTION 103="+e.toString());
            e.printStackTrace();
            return "ERR:" + (-EXCEPTION_IN_SERVER_DURING_AES_ENCRYPTION);
        }

        // payload response to client:
        //   0: universal password hash (signed and encrypted)
        //   1: server public key (signed and encrypted)
        //   2: client public key (signed and encrypted, this is an echo that the client originally sent to the server bufferedReaderStream the first request)
        //   3: server public key (plaintext)
        //   4: server RSA public key e part (plaintext)
        //   5: server RSA public key N part (plaintext)
        Payload response = new Payload.PayloadBuilder()
                .setZeroTypeValueforResponsePayload()
                .setNumberOfPayloadParameters(6)
                .setClientId(clientId)
                .setEmptyMicroserviceName()
                .build();
        response.setPayload(0, pwCipher);
        response.setPayload(1, localPublicKeyCipher);
        response.setPayload(2, remotePublicKeyCipher);
        response.setPayload(3, serverStateList.get(stateIndex).getServerPublicKey());
        response.setPayload(4, rsa.getPublicKeyE());
        response.setPayload(5, rsa.getPublicKeyN());
        response.setRemainingKeyUses(serverStateList.get(stateIndex).getRemainingKeyUses());
        return Base64.encodeToString(response.serialize(), Base64.DEFAULT);
    }

    private String processType2Request(int stateIndex, int clientId, Payload request) {
        // second request of two bufferedReaderStream station-to-station protocol request by client

        // Payload request from client:
        //   0: universal password hash (signed and encrypted)
        //   1: client public key (signed and encrypted)
        //   2: server public key (signed and encrypted, this is an echo that the server responded to the client from the first STS request)
        //   4: server RSA public key e part (plaintext)
        //   5: server RSA public key N part (plaintext)

        // decrypt the signature with secret key K
        byte[] pw = null;
        byte[] remotePublicKey = null;
        byte[] localPublicKey = null;
        Rijndael AESalgorithm = new Rijndael();
        try {
            AESalgorithm.makeKey(serverStateList.get(stateIndex).getKey(), 256, AESalgorithm.DIR_DECRYPT);
            pw = AESalgorithm.decryptArray(request.getPayload(0));
            remotePublicKey = AESalgorithm.decryptArray(request.getPayload(1));
            localPublicKey = AESalgorithm.decryptArray(request.getPayload(2));
        } catch (Exception e) {
            Log.d("JIM","SDVServer: ************* EXCEPTION 104="+e.toString());
            e.printStackTrace();
            return "ERR:"+(-EXCEPTION_IN_SERVER_DURING_AES_DECRYPTION);
        }

        // read signature using asymmetric public key
        RSA remoteRSA = new RSA();
        remoteRSA.setPublicKeyE(request.getPayload(3));
        remoteRSA.setPublicKeyN(request.getPayload(4));
        pw = remoteRSA.readSignature(pw);
        remotePublicKey = remoteRSA.readSignature(remotePublicKey);
        localPublicKey = remoteRSA.readSignature(localPublicKey);

        // verify:
        //   server side              client side
        //   ---------------------------------------
        //   universalPasswordHash == pw
        //   publicKey             == localPublicKey
        //   remotePublicKey       ==  remotePublicKeyFromClient
        // verify string recieved from server is (univeralPasswordHash, g^y mod port, g^x mod port)
        boolean verify1 = Utilities.compareArrays(universalPasswordHash, pw);
        boolean verify2 = Utilities.compareArrays(serverStateList.get(stateIndex).getServerPublicKey(), localPublicKey);
        boolean verify3 = Utilities.compareArrays(remotePublicKey, serverStateList.get(stateIndex).getClientPublicKey());
        boolean verify = verify1 && verify2 && verify3;

        // payload response to client:
        // type == 0, verify failed
        // type == 1, verified ok
        Payload sr = new Payload.PayloadBuilder()
                .setZeroTypeValueforResponsePayload()
                .setNumberOfPayloadParameters(0)
                .setClientId(clientId)
                .setEmptyMicroserviceName()
                .build();
        if (!verify) sr.setType(SERVER_VERIFICATIION_OF_STS_FAILED); // verify failed
        return Base64.encodeToString(sr.serialize(), Base64.DEFAULT);
    }

    private byte[] preprocessType3Request(int stateIndex, Payload request) {
        // used to preprocess various requests of type > 2
        // Payload request from client:
        //   0: encrypted Payload argument
        // returns plaintext byte array or error string as byte array
        byte[] sPlain;
        Rijndael AESalgorithm = new Rijndael();
        try {
            AESalgorithm.makeKey(serverStateList.get(stateIndex).getKey(), 256, AESalgorithm.DIR_DECRYPT);
            sPlain = AESalgorithm.decryptArray(request.getPayload(0));
        } catch (Exception e) {
            Log.d("JIM","SDVServer: ************* EXCEPTION 105="+e.toString());
            e.printStackTrace();
            return ("ERR:"+(-EXCEPTION_IN_SERVER_DURING_AES_DECRYPTION)).getBytes();
        }
        return sPlain;
    }

    private void writeToOutputSocket(Socket clientSocket, PrintWriter pws, BufferedReader brs, String responseIntendedForClient) {
        Thread thread = new Thread() {
            public void run() {
                pws.println(responseIntendedForClient);
                pws.println("***EOF***");
                closeAll(clientSocket, pws, brs);
            }
        };
        thread.start();
    }
    
    private void closeAll(Socket clientSocket, PrintWriter pws, BufferedReader brs) {
        try {
            if (pws != null) pws.close();
            if (brs != null) brs.close();
            if (clientSocket != null) clientSocket.close();
        } catch (Exception e) {
            Log.d("JIM","SDVServer: ************* EXCEPTION 106="+e.toString());
            e.printStackTrace();
        }
    }
    
    private int portAssignment() {
        try { portSemaphore.acquire(); } catch (Exception e) {};
        int port = portAssignments.nextClearBit(0);
        portAssignments.set(port);
        try { portSemaphore.release(); } catch (Exception e) {};
        return port + msPortStart;
    }

    private void sendToMicroservice(String msName, Socket clientSocket, byte[] incomingRequest, int cid, int typeOfRequest, PrintWriter pws, BufferedReader brs, int port, boolean returnToCaller, MainActivity mainActivity) {
        // msName is the name of the microservice
        // incomingRequest is the plaintext byte[] representing the serialized request from the client
        // incomingRequest is not an error string, errors have already been handled and returned to the caller
        // cid is the client id
        // typeOfRequest is the request type from the caller, this code determines which microservice will be called
        // pws is the writer that will write back to the client
        // brs is the reader for the socket that provided the request from the client
        // brs is present because it cannnot be closed until the response is sent back to the client, at which time both pws and brs should be closed
        // returnToCaller is true if return comes back to the microservice caller, otherwise return goes back to the original client caller
        // port is the port number to use when a microservice is started
        Payload r = new Payload.PayloadBuilder()
                .setStandardTypeValueforNonSTSPayload()
                .setNumberOfPayloadParameters(1)
                .setClientId(cid)
                .setReturnToCaller(returnToCaller)
                .setMicroserviceName(msName)
                .build();
        String first = new String(incomingRequest);
        int n = (first.length() < 10 ? first.length() : 10);
        first = first.substring(0,n);
        r.setPayload(0, incomingRequest);
        // the following is pending: closeAll(clientSocket, pws, brs); and recyclePortNumber(port);
        // These tasks must be completed when the new microservice thread exits
        runMicroservice(clientSocket, port, r, pws, brs, mainActivity);
    }

    private void runMicroservice(Socket clientSocket, int portNumber, Payload incomingRequest, PrintWriter pws, BufferedReader brs, MainActivity mainActivity) {
        // This "runMIcroService" method is called only by the server
        // The "runMIcroService" that calls other microServices is in the MSCommunicationsWrapper class
        // the following is pending: closeAll(clientSocket, pws, brs); and recyclePortNumber(port);
        // These tasks must be completed when the new microservice thread exits
        Thread thread = new Thread() {
            public void run() {
                String returnPayload;
                SystemClock.sleep(5); // give the microservice time to start listening
                String s = Base64.encodeToString(incomingRequest.serialize(), Base64.DEFAULT);
                int clientId = incomingRequest.getClientId();
                // TIME VALUE T1
                String communicateReturn = communicate(s, "localhost", portNumber, "server has called top-level microservice");
                // recycling the port is handled by the microservice when it terminates because the port can't be recycled until the microservice terminates
                pws.println(communicateReturn);
                pws.println("***EOF***");
                closeAll(clientSocket, pws, brs);
            }};
        thread.start();
    }

    private String communicate(String toMicroservice, String hostName, int portNumber, String type) {
        String fromMicroserver;
        BufferedReader in = null;
        PrintWriter out = null;
        Socket clientSocket = null;
        try {
            clientSocket = new Socket(hostName, portNumber);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (Exception e) {
            Log.d("JIM", "ClientSender: EXCEPTION 200 (host= "+hostName+" port="+portNumber+"): " + e.toString());
            e.printStackTrace();
            closeAll(clientSocket, out, in);
            return "ERR:"+(-MICROSERVICE_RETURNED_ERROR_CONDITION);
        }
        try {
            out.println(toMicroservice);
            out.println("***EOF***");
            StringBuffer sb = new StringBuffer();
            String s;
            while ((s = in.readLine()) != null) {
                if (s.equals("***EOF***")) break;
                sb.append(s);
            }
            fromMicroserver = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("JIM", "ClientSender: EXCEPTION 201 (port="+portNumber+"): " + e.toString());
            closeAll(clientSocket, out, in);
            return "ERR:"+(-MICROSERVICE_RETURNED_ERROR_CONDITION);
        }
        try {
            closeAll(clientSocket, out, in);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("JIM", "ClientSender: EXCEPTION 202 (port="+portNumber+"): " + e.toString());
            fromMicroserver = "ERR:"+(-MICROSERVICE_RETURNED_ERROR_CONDITION);
        }
        return fromMicroserver;
    }

    private void debugLogOutput(String output, String message, String name) {
        if (NODEBUG) return;
        String first = output.substring(0,(output.length() < 10 ? output.length() : 10));
        Log.d("JIM","LOG: " + name + message + " (arg=\"" + first + "...\")");
    }
}
