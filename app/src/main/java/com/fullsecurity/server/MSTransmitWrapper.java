package com.fullsecurity.server;

import android.util.Base64;
import android.util.Log;

import com.fullsecurity.common.Payload;
import com.fullsecurity.shared.MainActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@SuppressWarnings("all")
public abstract class MSTransmitWrapper {

    private final int UNABLE_TO_FIND_MICROSERVICE = -10;
    private final int CALLER_COULD_NOT_CONNECT_TO_MICROSERVICE = -11;
    private final int IMPROPER_EXPECTED_ARGUMENT_IN_REQUEST = -12;
    private final int EXCEPTION_READING_REQUEST_FOR_SERVICE = -13;
    private final int EXCEPTION_WRITING_REQUEST_FOR_SERVICE = -14;
    private final int MICROSERVICE_RETURNED_ERROR_CONDITION = -15;
    private final int SERVER_INTERNAL_ERROR_ID_NOT_FOUND = -16;
    private final int SERVER_VERIFICATIION_OF_STS_FAILED = -17;
    private final int SERVER_INTERNAL_ERROR_NOT_ABLE_TO_ASSIGN_ID = -18;
    private final int CLIENT_HAS_NO_KEY_USES_REMAINING = -19;
    private final int SERVER_INTERNAL_ERROR_IMPOSSIBLE_VALUE_FOR_TYPE_EXPECTED = -20;
    private final int EXCEPTION_IN_SERVER_DURING_AES_ENCRYPTION = -21;
    private final int ENCRYPTION_KEYS_HAVE_EXPIRED = -22;
    private final int CLIENT_VERIFICATIION_OF_STS_FAILED = -23;
    private final int EXCEPTION_IN_SERVER_DURING_AES_DECRYPTION = -24;
    private final int EXCEPTION_IN_CLIENT_DURING_AES_ENCRYPTION = -25;
    private final int EXCEPTION_IN_CLIENT_DURING_AES_DECRYPTION = -26;
    private final int INTERNAL_ERROR_UNKNOWN_OPERATION = -27;
    private final int CLIENT_COULD_NOT_CONNECT_TO_SERVER = -28;
    private final int CLIENT_TIMED_OUT_WHILE_WAITING_FOR_SERVER = -29;
    private final int CANNOT_GRANT_ACCESS_TO_FILE = -30;

    public MSTransmitWrapper() {}

    public void writeToOutputSocket(PrintWriter pws, BufferedReader brs, String responseIntendedForClient) {
        Thread thread = new Thread() {
            public void run() {
                pws.println(responseIntendedForClient);
                pws.println("***EOF***");
                try {
                    pws.close();
                    brs.close();
                } catch (Exception e) {
                    Log.d("JIM","SDVServer: EXCEPTION 110="+e.toString());
                }
            }
        };
        thread.start();
    }

    public void sendToMicroservice(byte[] incomingRequest, int cid, int typeOfRequest, PrintWriter pws, BufferedReader brs, int port, boolean returnToMe, MainActivity mainActivity) {
        // incomingRequest is the plaintext byte[] representing the JSON request from the caller (might be the client or another microservice)
        // incomingRequest is not an error string, errors have already been handled and returned to the caller
        // cid is the client id
        // typeOfRequest is the request type from the caller, this code determines which microservice will be called
        // pws is the writer that will write back to the client
        // brs is the reader for the socket that provided the request from the client
        // brs is present because it cannnot be closed until the response is sent back to the client, at which time both pws and brs should be closed
        // returnToMe is true if return comes back to the microservice caller, otherwise return goes back to the original client caller
        // port is the port number to use when a microservice is started
        // encryptOutput=true if the result sent to the caller needs to be encrypted

        // Payload to send to the microservice has the following payloads:
        //   0: incomingRequest is the plaintext byte[] representing the original JSON request from the client
        //      incomingRequest is not an error string, errors have already been handled and returned to the client
        Payload r = new Payload(typeOfRequest, 1, cid, returnToMe);
        String first = new String(incomingRequest);
        int n = (first.length() < 10 ? first.length() : 10);
        first = first.substring(0,n);
        Log.d("JIM","LOG("+cid+"): sendToMicroservice entered (arg=\""+first+"...\") from the server: type:"+typeOfRequest+" | rtome:"+returnToMe+" | target port:"+port);
        r.setPayload(0, incomingRequest);
        runMicroservice(port, r, pws, brs, mainActivity);
        Log.d("JIM","LOG("+cid+"): sendToMicroservice exited after starting thread for microservice");
    }

    private void runMicroservice(int portNumber, Payload incomingRequest, PrintWriter pws, BufferedReader brs, MainActivity mainActivity) {
        // This "runMIcroService" method is called only by the server
        // The "runMIcroService" that calls other microServices is in the MSCommunicationsWrapper class
        Thread thread = new Thread() {
            public void run() {
                Log.d("JIM","LOG: runMicroservice entered from the server, new thread");
                String returnPayload;
                try {
                    String s = Base64.encodeToString(incomingRequest.serialize(), Base64.DEFAULT);
                    int clientId = incomingRequest.getId();

                     // TIME VALUE T1

                    String communicateReturn = communicate(s, "localhost", portNumber, "server has called top-level microservice");
                    Log.d("JIM","LOG: runMicroservice terminated with value = \"" + communicateReturn + "\", thread finishes");
                } catch (Exception e) {
                    // The communicate method may terminate by throwing an exception
                    // There are two conditions under which this exception will be thrown
                    //   1. There is a problem in communicate itself
                    //   2. There is a problem in the microservice with which communicate is communicating)
                    Log.d("JIM", "SDVServer: EXCEPTION 700=" + e.toString());
                    e.printStackTrace();
                    writeToOutputSocket(pws, brs, "ERR:"+(-CALLER_COULD_NOT_CONNECT_TO_MICROSERVICE));
                }
            }};
        thread.start();
    }

    public String communicate(String toServer, String hostName, int portNumber, String type) throws Exception {
        String fromServer;
        BufferedReader in;
        PrintWriter out;
        Socket clientSocket;
        Log.d("JIM","LOG: communicate ========== entered with tag="+type);
        try {
            clientSocket = new Socket(hostName, portNumber);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (Exception e) {
            Log.d("JIM", "ClientSender: EXCEPTION 200 (host= "+hostName+" port="+portNumber+"): " + e.toString());
            e.printStackTrace();
            throw new Exception("CAN'T CONNECT EXCEPTION");
        }
        try {
            out.println(toServer);
            out.println("***EOF***");
            StringBuffer sb = new StringBuffer();
            String s;
            while ((s = in.readLine()) != null) {
                if (s.equals("THROWEXCEPTION***EOF***")) throw new Exception("CAN'T CONNECT EXCEPTION");
                if (s.equals("***EOF***")) break;
                sb.append(s);
            }
            fromServer = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("JIM", "ClientSender: EXCEPTION 201 (port="+portNumber+"): " + e.toString());
            throw new Exception("CAN'T CONNECT EXCEPTION");
        }
        try {
            clientSocket.close();
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("JIM", "ClientSender: EXCEPTION 202 (port="+portNumber+"): " + e.toString());
            throw new Exception("CAN'T CONNECT EXCEPTION");
        }
        debugLogOutput(fromServer, " ========== exited with tag="+type+" and", "communicate");
        return fromServer;
    }

    public static void debugLogOutput(String output, String message, String name) {
        String first = output.substring(0,(output.length() < 10 ? output.length() : 10));
        Log.d("JIM","LOG: " + name + message + " (arg=\"" + first + "...\")");
    }
}
