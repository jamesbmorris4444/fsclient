package com.fullsecurity.demoapplication;

import android.util.Base64;
import android.util.Log;

import com.fullsecurity.common.Payload;
import com.fullsecurity.common.Rijndael;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public abstract class ClientCommunicationsWrapper extends android.support.v4.app.Fragment implements ClientCommunicationsInterface {

    protected final int UNABLE_TO_FIND_MICROSERVICE = -10;
    protected final int CALLER_COULD_NOT_CONNECT_TO_MICROSERVICE = -11;
    protected final int IMPROPER_EXPECTED_ARGUMENT_IN_REQUEST = -12;
    protected final int EXCEPTION_READING_REQUEST_FOR_SERVICE = -13;
    protected final int EXCEPTION_WRITING_REQUEST_FOR_SERVICE = -14;
    protected final int MICROSERVICE_RETURNED_ERROR_CONDITION = -15;
    protected final int SERVER_INTERNAL_ERROR_ID_NOT_FOUND = -16;
    protected final int SERVER_VERIFICATIION_OF_STS_FAILED = -17;
    protected final int SERVER_INTERNAL_ERROR_NOT_ABLE_TO_ASSIGN_ID = -18;
    protected final int CLIENT_HAS_NO_KEY_USES_REMAINING = -19;
    protected final int SERVER_INTERNAL_ERROR_IMPOSSIBLE_VALUE_FOR_TYPE_EXPECTED = -20;
    protected final int EXCEPTION_IN_SERVER_DURING_AES_ENCRYPTION = -21;
    protected final int ENCRYPTION_KEYS_HAVE_EXPIRED = -22;
    protected final int CLIENT_VERIFICATIION_OF_STS_FAILED = -23;
    protected final int EXCEPTION_IN_SERVER_DURING_AES_DECRYPTION = -24;
    protected final int EXCEPTION_IN_CLIENT_DURING_AES_ENCRYPTION = -25;
    protected final int EXCEPTION_IN_CLIENT_DURING_AES_DECRYPTION = -26;
    protected final int INTERNAL_ERROR_UNKNOWN_OPERATION = -27;
    protected final int CLIENT_COULD_NOT_CONNECT_TO_SERVER = -28;
    protected final int CLIENT_TIMED_OUT_WHILE_WAITING_FOR_SERVER = -29;
    protected final int CANNOT_GRANT_ACCESS_TO_FILE = -30;
    protected final int ERROR_IN_DATABASE_OPERATION = -31;

    public final int portNumber = 8000;
    private byte[] key;

    public ClientCommunicationsWrapper(byte[] key) {
        this.key = key;
    }

    public ClientCommunicationsWrapper() {}

    protected String messageGetter(int k, String errorMicroserviceName) {
        String r;
        switch(k) {
            case EXCEPTION_READING_REQUEST_FOR_SERVICE:
                r = "EXCEPTION READING REQUEST FOR SERVICE";
                break;
            case CALLER_COULD_NOT_CONNECT_TO_MICROSERVICE:
                r = "CALLER COULD NOT CONNECT TO MICROSERVICE";
                break;
            case SERVER_VERIFICATIION_OF_STS_FAILED:
                r = "SERVER VERIFICATIION OF STS FAILED";
                break;
            case SERVER_INTERNAL_ERROR_NOT_ABLE_TO_ASSIGN_ID:
                r = "SERVER INTERNAL ERROR NOT ABLE TO ASSIGN ID";
                break;
            case IMPROPER_EXPECTED_ARGUMENT_IN_REQUEST:
                r = "IMPROPER EXPECTED ARGUMENT IN REQUEST";
                break;
            case CLIENT_HAS_NO_KEY_USES_REMAINING:
                r = "CLIENT HAS NO KEY USES REMAINING";
                break;
            case SERVER_INTERNAL_ERROR_IMPOSSIBLE_VALUE_FOR_TYPE_EXPECTED:
                r = "SERVER INTERNAL ERROR IMPOSSIBLE VALUE FOR TYPE EXPECTED";
                break;
            case EXCEPTION_IN_SERVER_DURING_AES_ENCRYPTION:
                r = "EXCEPTION IN SERVER DURING AES ENCRYPTION";
                break;
            case SERVER_INTERNAL_ERROR_ID_NOT_FOUND:
                r = "SERVER INTERNAL ERROR ID NOT FOUND";
                break;
            case ENCRYPTION_KEYS_HAVE_EXPIRED:
                r = "ENCRYPTION KEYS HAVE EXPIRED";
                break;
            case CLIENT_VERIFICATIION_OF_STS_FAILED:
                r = "CLIENT VERIFICATIION OF STS FAILED";
                break;
            case EXCEPTION_IN_SERVER_DURING_AES_DECRYPTION:
                r = "EXCEPTION IN SERVER DURING AES DECRYPTION";
                break;
            case EXCEPTION_IN_CLIENT_DURING_AES_ENCRYPTION:
                r = "EXCEPTION IN CLIENT DURING AES ENCRYPTION";
                break;
            case EXCEPTION_IN_CLIENT_DURING_AES_DECRYPTION:
                r = "EXCEPTION IN CLIENT DURING AES DECRYPTION";
                break;
            case INTERNAL_ERROR_UNKNOWN_OPERATION:
                r = "INTERNAL ERROR UNKNOWN OPERATION";
                break;
            case CLIENT_COULD_NOT_CONNECT_TO_SERVER:
                r = "CLIENT COULD NOT CONNECT TO SERVER";
                break;
            case CLIENT_TIMED_OUT_WHILE_WAITING_FOR_SERVER:
                r = "CLIENT TIMED OUT WHILE WAITING FOR SERVER";
                break;
            case UNABLE_TO_FIND_MICROSERVICE:
                if (errorMicroserviceName == null)
                    r = "UNABLE TO FIND MICROSERVICE";
                else
                    r = errorMicroserviceName + " UNABLE TO FIND MICROSERVICE";
                break;
            case EXCEPTION_WRITING_REQUEST_FOR_SERVICE:
                r = "EXCEPTION WRITING REQUEST FOR SERVICE";
                break;
            case MICROSERVICE_RETURNED_ERROR_CONDITION:
                if (errorMicroserviceName == null)
                    r = "MICROSERVICE RETURNED ERROR CONDITION";
                else
                    r = errorMicroserviceName + " MICROSERVICE RETURNED ERROR CONDITION";
                break;
            case CANNOT_GRANT_ACCESS_TO_FILE:
                r = "CANNOT GRANT ACCESS TO FILE";
                break;
            case ERROR_IN_DATABASE_OPERATION:
                r = "ERROR IN DATABASE OPERATION";
                break;
            default:
                r = "UNKNOWN ERROR";
                break;
        }
        return r;
    }

    public String communicate(String toServer, String hostName, int portNumber) {
        String fromServer;
        BufferedReader in;
        PrintWriter out;
        Socket clientSocket;
        try {
            clientSocket = new Socket(hostName, portNumber);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (Exception e) {
            Log.d("JIM", "ClientSender: EXCEPTION 200 (host= "+hostName+" port="+portNumber+"): " + e.toString());
            e.printStackTrace();
            return "ERR:" + (-CALLER_COULD_NOT_CONNECT_TO_MICROSERVICE);
        }
        try {
            out.println(toServer);
            out.println("***EOF***");
            StringBuffer sb = new StringBuffer();
            String s;
            while ((s = in.readLine()) != null) {
                if (s.equals("***EOF***")) break;
                sb.append(s);
            }
            fromServer = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("JIM", "ClientSender: EXCEPTION 201 (port="+portNumber+"): " + e.toString());
            return "ERR:" + (-EXCEPTION_READING_REQUEST_FOR_SERVICE);
        }
        try {
            clientSocket.close();
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("JIM", "ClientSender: EXCEPTION 202 (port="+portNumber+"): " + e.toString());
            return "ERR:" + (-EXCEPTION_READING_REQUEST_FOR_SERVICE);
        }
        return fromServer;
    }

    protected void getDataFromMicroservice(String query, String nameOfRequestor, int userId, String requestedMSNameForServer) {
        byte[] sCipher;
        Rijndael AESalgorithm = new Rijndael();
        try {
            AESalgorithm.makeKey(key, 256, AESalgorithm.DIR_ENCRYPT);
            sCipher = AESalgorithm.encryptArray(query.getBytes());
        } catch (Exception ex) {
            Log.d("JIM", nameOfRequestor + ": EXCEPTION 2000: "+ ex.toString());
            ex.printStackTrace();
            processErrorResponseFromMicroservice("ERR:"+(-EXCEPTION_IN_CLIENT_DURING_AES_ENCRYPTION));
            return;
        }
        Payload p = new Payload.PayloadBuilder()
                .setStandardTypeValueforNonSTSPayload()
                .setNumberOfPayloadParameters(1)
                .setClientId(userId)
                .setMicroserviceName(requestedMSNameForServer)
                .setPayload(1,sCipher)
                .build();
        String request = Base64.encodeToString(p.serialize(), Base64.DEFAULT);
        Observable<String> observable = Observable.create(emitter -> {
            emitter.onNext(request);
            emitter.onComplete();
        });
        observable
                .observeOn(Schedulers.io())
                .map((String payload) -> {
                    String returnPayload = communicate(payload, "localhost", portNumber);
                    if (returnPayload.startsWith("ERR:")) return returnPayload;
                    Payload responsePayload = new Payload(Base64.decode(returnPayload, Base64.DEFAULT));
                    return finalProcessor(responsePayload);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS)
                .subscribe((String result) -> {
                    if (result.startsWith("ERR:"))
                        processErrorResponseFromMicroservice(result);
                    else
                        processNormalResponseFromMicroservice(result);
                }, throwable -> {
                    Log.d("JIM", "THROWABLE=" + throwable.toString());
                    throwable.printStackTrace();
                    processErrorResponseFromMicroservice("ERR:"+(-MICROSERVICE_RETURNED_ERROR_CONDITION));
                });
    }

    private String finalProcessor(Payload responsePayload) {
        if (responsePayload.getType() < 0) return "ERR:" + (-responsePayload.getType());
        byte[] sPlain;
        String listReceive;
        try {
            Rijndael AESalgorithm = new Rijndael();
            AESalgorithm.makeKey(key, 256, AESalgorithm.DIR_DECRYPT);
            sPlain = AESalgorithm.decryptArray(responsePayload.getPayload(0));
            listReceive = new String(sPlain);
        } catch (Exception ex) {
            Log.d("JIM", "SDVClient: EXCEPTION 455=" + ex.toString());
            ex.printStackTrace();
            return "ERR:" + (-EXCEPTION_IN_CLIENT_DURING_AES_DECRYPTION);
        }
        return listReceive;
    }
}
