package com.fullsecurity.microservices;

import android.util.Base64;
import android.util.Log;

import com.fullsecurity.client.ClientSender;
import com.fullsecurity.common.Payload;
import com.fullsecurity.common.Rijndael;
import com.fullsecurity.server.MSSettings;
import com.fullsecurity.server.ServerState;
import com.fullsecurity.shared.MainActivity;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import static android.util.Base64.encodeToString;

@SuppressWarnings("all")
public class MSUtilities {

    private static final int UNABLE_TO_FIND_MICROSERVICE = -10;
    private static final int CALLER_COULD_NOT_CONNECT_TO_MICROSERVICE = -11;
    private static final int IMPROPER_EXPECTED_ARGUMENT_IN_REQUEST = -12;
    private static final int EXCEPTION_READING_REQUEST_FOR_SERVICE = -13;
    private static final int EXCEPTION_WRITING_REQUEST_FOR_SERVICE = -14;
    private static final int MICROSERVICE_RETURNED_ERROR_CONDITION = -15;
    private static final int SERVER_INTERNAL_ERROR_ID_NOT_FOUND = -16;
    private static final int SERVER_VERIFICATIION_OF_STS_FAILED = -17;
    private static final int SERVER_INTERNAL_ERROR_NOT_ABLE_TO_ASSIGN_ID = -18;
    private static final int CLIENT_HAS_NO_KEY_USES_REMAINING = -19;
    private static final int SERVER_INTERNAL_ERROR_IMPOSSIBLE_VALUE_FOR_TYPE_EXPECTED = -20;
    private static final int EXCEPTION_IN_SERVER_DURING_AES_ENCRYPTION = -21;
    private static final int ENCRYPTION_KEYS_HAVE_EXPIRED = -22;
    private static final int CLIENT_VERIFICATIION_OF_STS_FAILED = -23;
    private static final int EXCEPTION_IN_SERVER_DURING_AES_DECRYPTION = -24;
    private static final int EXCEPTION_IN_CLIENT_DURING_AES_ENCRYPTION = -25;
    private static final int EXCEPTION_IN_CLIENT_DURING_AES_DECRYPTION = -26;
    private static final int INTERNAL_ERROR_UNKNOWN_OPERATION = -27;
    private static final int CLIENT_COULD_NOT_CONNECT_TO_SERVER = -28;
    private static final int CLIENT_TIMED_OUT_WHILE_WAITING_FOR_SERVER = -29;


    public static int mapFromMSNameToMSIndex(ArrayList<MSSettings> msMap, String msName) {
        int msIndex = -1;
        for (int i = 0; i < msMap.size(); i++) {
            if (msMap.get(i).getName().equals(msName) && msMap.get(i).isActive())
                msIndex = i;
        }
        Log.d("JIM","LOG: mapFromMSNameToMSIndex in MSUtilities mapped name \""+msName+"\" to index "+msIndex);
        return msIndex;
    }

    public static int mapFromMSNameToMSIndex(ArrayList<MSSettings> msMap, String msName, char command) {
        // if commmand is '+', this method will find only the deactivated microservices
        // if command is '-', this mehtod will find only the active microservices
        int msIndex = -1;
        for (int i = 0; i < msMap.size(); i++) {
            if (msMap.get(i).getName().equals(msName) && (command  == '-' ? msMap.get(i).isActive() : !msMap.get(i).isActive()))
                msIndex = i;
        }
        Log.d("JIM","LOG: mapFromMSNameToMSIndex in MSUtilities mapped name \""+msName+"\" to index "+msIndex);
        return msIndex;
    }

    public static void debugLogOutput(byte[] incomingRequest, String message, String name, boolean returnToMe) {
        String first = new String(incomingRequest, 0, (incomingRequest.length < 10 ? incomingRequest.length : 10));
        Log.d("JIM","LOG: " + name + message + " (arg=\"" + first + "...\") r2me="+returnToMe);
    }

    public static String sendToMicroservice(Payload incomingRequest, int cid, boolean returnToMe,  int port, MainActivity mainActivity) {
        // incomingRequest (in Payload[0]) is the plaintext byte[] representing the JSON request from the caller (might be the client or another microservice)
        // incomingRequest is not an error string, errors have already been handled and returned to the caller
        // cid is the client id
        // typeOfRequest is the request type from the caller, this code determines which microservice will be called
        // pws is the writer that will write back to the client
        // brs is the reader for the socket that provided the request from the client
        // brs is present because it cannnot be closed until the response is sent back to the client, at which time both pws and brs should be closed
        // returnToMe is true if return comes back to the microservice caller, otherwise return goes back to the original client caller
        // msMap is the list of microservice names the caller is allowed to call
        // port is the starting port number to use when a microservice is started
        // encryptOutput=true if the result sent to the caller needs to be encrypted

        // Payload to send to the microservice has the following payloads:
        //   0: incomingRequest is the plaintext byte[] representing the original JSON request from the client
        //      incomingRequest is not an error string, errors have already been handled and returned to the client
        incomingRequest.setReturnToMe(returnToMe);
        Log.d("JIM","LOG: entered sendtoMicroservice (for microservices, not server)=rtome:"+returnToMe+" | port:"+port);
        try {
            Log.d("JIM","LOG: sendtoMicroservice (for microservices, not server) exited");
            return runDownstreamMicroservice(port, cid, incomingRequest, mainActivity);
        } catch (Exception e) {
            e.printStackTrace();
            return "EXCEPTION";
        }
    }

    public static int indexOfClientWithId(int id, ArrayList<ServerState> serverStateList) {
        for (int k = 0; k < serverStateList.size(); k++) if (serverStateList.get(k).getId() == id) return k;
        return -1;
    }

    private static void updateTimers(int clientId, String s, MainActivity mainActivity) {
        switch (clientId) {
            case 0:
                mainActivity.tlfrg.sdvClient.addTime(s.length());
                break;
            case 1:
                mainActivity.trfrg.sdvClient.addTime(s.length());
                break;
            case 2:
                mainActivity.blfrg.sdvClient.addTime(s.length());
                break;
            default:
                mainActivity.brfrg.sdvClient.addTime(s.length());
                break;
        }
    }

    public static String runDownstreamMicroservice(int portNumber, int clientId, Payload incomingRequest, MainActivity mainActivity) throws Exception {
        // This method is called only by other microservices
        // It always throws an exception when it returns
        // It either returns a JSON payload string of the form NORMAL:<JSON>
        //    or it returns the string "EXCEPTION", but it returns the string in the exception
        ClientSender microService = new ClientSender("localhost", portNumber);
        try {
            String s = Base64.encodeToString(incomingRequest.serialize(), Base64.DEFAULT);
            updateTimers(clientId, s, mainActivity);    // TIME VALUE T3
            Log.d("JIM","LOG: TIME VALUE T3="+s.length());
            return microService.communicate(s, "activate microservice");
        } catch (Exception e) {
            // There are two conditions under which this exception will be thrown
            //   1. There is a problem in communicate itself (s == "EXCEPTION")
            //   2. There is a problem in the microservice with which communicate is communicating  (s == "EXCEPTION")
            Log.d("JIM", "SDVServer: EXCEPTION 700=" + e.toString());
            e.printStackTrace();
            throw new RuntimeException("EXCEPTION");
        }
    }

    public static void processReturnValue(Payload result, int clientId, String nameUsedForOutputToLogger, ArrayList<ServerState> serverStateList,
                                          Semaphore sslSemaphore, Semaphore msmSemaphore, PrintWriter pws, BufferedReader brs, boolean returnToMe,
                                          MainActivity mainActivity) {
        // The result parameter is a completed Payload, but the Payload has not yet been encrypted (or serialized)
        Log.d("JIM","LOG: processReturnValue entered for normal Payload return");
        byte[] key;
        String retString;
        if (!returnToMe) {
            try { sslSemaphore.acquire(); } catch (Exception e) { };
            int stateIndex = MSUtilities.indexOfClientWithId(clientId, serverStateList);
            if (stateIndex >= 0) {
                key = serverStateList.get(stateIndex).getKey();
                try { sslSemaphore.release(); } catch (Exception e) { };
                Payload retval = postProcessor(result, key, clientId, nameUsedForOutputToLogger);
                if (retval == null)
                    writeOutput("ERR:" + (-MICROSERVICE_RETURNED_ERROR_CONDITION), returnToMe, nameUsedForOutputToLogger, pws, brs, mainActivity, clientId);
                else {
                    retString = encodeToString(retval.serialize(), Base64.DEFAULT);
                    writeOutput(retString, returnToMe, nameUsedForOutputToLogger, pws, brs, mainActivity, clientId);
                    Log.d("JIM", "LOG: processReturnValue exited normally, encrypted output, " + (returnToMe ? "local" : "remote") + " value write");
                }
            } else {
                try { sslSemaphore.release(); } catch (Exception e) { };
                writeOutput("ERR:" + (-SERVER_INTERNAL_ERROR_ID_NOT_FOUND), returnToMe, nameUsedForOutputToLogger, pws, brs, mainActivity, clientId);
                Log.d("JIM", "LOG: processReturnValue exited with error, unencrypted output, " + (returnToMe ? "local" : "remote") + " value write");
            }
        } else {
            retString = encodeToString(result.serialize(), Base64.DEFAULT);
            writeOutput(retString, returnToMe, nameUsedForOutputToLogger, pws, brs, mainActivity, clientId);
            Log.d("JIM", "LOG: processReturnValue exited normally, unencrypted output, " + (returnToMe ? "local" : "remote") + " value write");
        }
    }

    public static void processReturnValue(String result, int clientId, String nameUsedForOutputToLogger, ArrayList<ServerState> serverStateList,
                                          Semaphore sslSemaphore, Semaphore msmSemaphore, PrintWriter pws, BufferedReader brs, boolean returnToMe,
                                          MainActivity mainActivity) {
        Log.d("JIM","LOG: processReturnValue for error string entered");
        if (result == null)
            writeOutput("ERR:" + (-MICROSERVICE_RETURNED_ERROR_CONDITION), returnToMe, nameUsedForOutputToLogger, pws, brs, mainActivity, clientId);
        else
            writeOutput(result, returnToMe, nameUsedForOutputToLogger, pws, brs, mainActivity, clientId);
        Log.d("JIM","LOG: processReturnValue for string exited normally, unencrypted output, " + (returnToMe ? "local" : "remote") + " value write");
    }

    public static Payload postProcessor(Payload outgoing, byte[] key, int clientId, String nameUsedForOutputToLogger) {
        Rijndael AESalgorithm = new Rijndael();
        try {
            AESalgorithm.makeKey(key, 256, AESalgorithm.DIR_ENCRYPT);
            for (int k = 0; k < outgoing.getPayloadLength(); k++)
                if (outgoing.getPayload(k).length > 0)
                    outgoing.setPayload(k, AESalgorithm.encryptArray(outgoing.getPayload(k)));
            return outgoing;
        } catch (Exception ex) {
            Log.d("JIM", nameUsedForOutputToLogger + "EXCEPTION 1010="+ex.toString());
            ex.printStackTrace();
            return null;
        }
    }

    public static void writeOutput(String output, boolean returnToMe, String nameUsedForOutputToLogger, PrintWriter pws, BufferedReader brs,
                                   MainActivity mainActivity, int clientId) {
        Thread thread = new Thread() {
            public void run() {
                Log.d("JIM","LOG: TIME VALUE T4="+output.length());
                updateTimers(clientId, output, mainActivity);    // TIME VALUE T4
                pws.println(output);
                pws.println("***EOF***");
                closeStreams(nameUsedForOutputToLogger, pws, brs);
            }};
            thread.start();
    }

    public static void closeStreams(String nameUsedForOutputToLogger, PrintWriter pws, BufferedReader brs) {
        try {
            if (pws != null) pws.close();
            if (brs != null) brs.close();
        } catch (Exception e) {
            Log.d("JIM", nameUsedForOutputToLogger + "EXCEPTION 2000="+e.toString());
            e.printStackTrace();
        }
    }

}
