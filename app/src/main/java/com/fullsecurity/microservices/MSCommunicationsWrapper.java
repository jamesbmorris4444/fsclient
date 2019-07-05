package com.fullsecurity.microservices;

import android.util.Base64;
import android.util.Log;

import com.fullsecurity.common.Payload;
import com.fullsecurity.common.Rijndael;
import com.fullsecurity.server.MSSettings;
import com.fullsecurity.server.SDVServer;
import com.fullsecurity.server.ServerState;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import static android.util.Base64.encodeToString;

@SuppressWarnings("all")
public abstract class MSCommunicationsWrapper extends SDVServer implements MSCommunicationsInterface {

    private static final boolean NODEBUG = true;
    
    // MSCommunicationsWrapper contains all the communications code for microservices and is common to all microservices

    private String nameUsedForOutputToLogger;         // names used for debug output by individual microservices

    // this data is associated with incoming requests from the server or upstream microservices
    private boolean returnToCaller;                   // determines if the caller needs a response by a PrintWriter (client ALWAYS need a response)
    private Socket callerSocket = null;               // caller socket (must be closed when the microservice terminates) 
    private PrintWriter callerWriter = null;          // caller PrintWriter (must be closed when the microservice terminates) 
    private BufferedReader callerReader = null;       // caller BufferedReader (must be closed when the microservice terminates)
    private int listeningPort;                        // the incoming port at which the microservice listens (must be recycled when the microservice terminates)
    private int callerServerStateListIndex;           // index of caller client in serverStateList
    private boolean callCameFromServer;               // microservice was called directly by the server
    private boolean callerShouldNotSendOutput;        // true implies that the special string "NOACTION" is sent back to the caller to indicate caller should not send output
    private PrintWriter savedLocalCallerWriter;       // this printWriter should be used to write back to the caller if callerShouldNotSendOutput is true
    
    // this data is associated with outgoing requests to downstream microservices and must be managed by the microservice
    private int calledPort;                           // the outgoing port at which the downstream microservice listens (managed by this microservice)
    private Socket calledSocket = null;               // called socket (must be closed when the microservice receives a response, created from calledPort) 
    private PrintWriter calledWriter = null;          // called PrintWriter (must be closed when the microservice receives a response, created from calledSocket) 
    private BufferedReader calledReader = null;       // called BufferedReader (must be closed when the microservice receives a response, created from calledSocket)

    public Semaphore startupSemaphore = new Semaphore(1);

    protected MSCommunicationsWrapper(int listeningPort, String nameUsedForOutputToLogger) {
        this.listeningPort = listeningPort;
        try { startupSemaphore.acquire(); } catch (Exception e) {}
        this.nameUsedForOutputToLogger = nameUsedForOutputToLogger;
        Thread msThread = new Thread() {
            public void run() {
                msWrapperListener();
            }
        };
        msThread.start();
    }

    protected MSCommunicationsWrapper() {}

    // the code below here deals with receiving requests from an upstream caller

    protected void msWrapperListener() {
        callerSocket = null;
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(listeningPort);
            try { startupSemaphore.release(); } catch (Exception e) {}
            callerSocket = serverSocket.accept();
            if (serverSocket != null) serverSocket.close();
            serverSocket = null;
            handleMicroserviceIncomingRequest();
        } catch (Exception e) {
            Log.d("JIM", nameUsedForOutputToLogger + "************* EXCEPTION 1000: MICROSERVICE LISTENER SHUTDOWN on port "+listeningPort+": "+e.toString());
            e.printStackTrace();
            try {
                if (serverSocket != null) serverSocket.close();
                if (callerSocket != null) callerSocket.close();
            } catch (Exception f) {};
        }
    }

    protected void handleMicroserviceIncomingRequest() {
        // callerSocket is a good value
        // callerWriter and callerReader have not been set and are null
        try {
            callerWriter = new PrintWriter(callerSocket.getOutputStream(), true);
            callerReader = new BufferedReader(new InputStreamReader(callerSocket.getInputStream()));
        } catch (Exception e) {
            Log.d("JIM", nameUsedForOutputToLogger + "************* EXCEPTION 1001="+e.toString());
            e.printStackTrace();
            // Cannot read incoming request; client will have to wait for timeout
            // If the problem is with the printWriter, it does no good to continue, because response cannot be sent to the client
            closeCallerStreams();
            return;
        }
        // callerSocket, callerWriter, and callerReader are all good values
        // start reading the incoming String from the socket
        StringBuffer sb = new StringBuffer();
        String s = null;
        while (true) {
            try{
                s = callerReader.readLine();
            } catch (Exception e) {
                Log.d("JIM", nameUsedForOutputToLogger + "************* EXCEPTION 1002="+e.toString());
                e.printStackTrace();
                writeOutput("ERR:" + (-EXCEPTION_READING_REQUEST_FOR_SERVICE));
                return;
            }
            if (s == null) break;
            if (s.equals("***EOF***")) break;
            sb.append(s);
        }
        String sTransmitted = sb.toString();
        // end reading the incoming String from the socket
        Payload request = new Payload(Base64.decode(sTransmitted, Base64.DEFAULT));
        int numberOfBytesTransmitted = sTransmitted.length();
        callerShouldNotSendOutput = false;
        int msIndex = mapFromMSNameToMSIndex(msMap, request.getMSName());
        if (msIndex < 0) {
            writeOutput("ERR:" + (-UNABLE_TO_FIND_MICROSERVICE) + "|" + nameUsedForOutputToLogger);
        } else {
            callCameFromServer = msMap.get(msIndex).getParent() == null;
            returnToCaller = msMap.get(msIndex).isReturnToMe();
            int clientId = request.getClientId();
            int stateIndex = -1;
            boolean streamsOK = true;
            if (!returnToCaller) {
                try { sslSemaphore.acquire(); } catch (Exception e) { }
                stateIndex = indexOfClientWithId(clientId);
                try { sslSemaphore.release(); } catch (Exception e) { }
                if (stateIndex < 0) {
                    Log.d("JIM", "LOG: " + nameUsedForOutputToLogger + "Could not find client id in serverStateList");
                    writeOutput("ERR:" + (-EXCEPTION_READING_REQUEST_FOR_SERVICE));
                    streamsOK = false;
                } else {
                    // stateIndex is a good value
                    if (!callCameFromServer) {
                        // switch printWriter for this microservice to printWriter for original client because no other response will be made to the caller
                        savedLocalCallerWriter = callerWriter;
                        callerWriter = serverStateList.get(stateIndex).getPrintWriter();
                        callerShouldNotSendOutput = true;
                    }
                }
            }
            callerServerStateListIndex = stateIndex;
            if (streamsOK) setOutputPayloads(request);
        }
    }

    protected int indexOfClientWithId(int id) {
        for (int k = 0; k < serverStateList.size(); k++) if (serverStateList.get(k).getId() == id) return k;
        return -1;
    }

    // the code below here deals with sending requests to a downstream microservice

    protected int mapFromMSNameToMSIndex(ArrayList<MSSettings> msMap, String msName) {
        int msIndex = -1;
        for (int i = 0; i < msMap.size(); i++) {
            if (msMap.get(i).getName().equals(msName) && msMap.get(i).isActive())
                msIndex = i;
        }
        return msIndex;
    }

    protected int indexOfClientWithId(int id, ArrayList<ServerState> serverStateList) {
        for (int k = 0; k < serverStateList.size(); k++) if (serverStateList.get(k).getId() == id) return k;
        return -1;
    }

    protected void debugLogOutput(byte[] incomingRequest, String message) {
        if (NODEBUG) return;
        String first = new String(incomingRequest, 0, (incomingRequest.length < 20 ? incomingRequest.length : 20));
        debugLogOutput(first, message);
    }

    protected void debugLogOutput(String output, String message) {
        if (NODEBUG) return;
        String first = output.substring(0,(output.length() < 20 ? output.length() : 20));
        Log.d("JIM","LOG: " + nameUsedForOutputToLogger + message + " (arg=\"" + first + " ...\")");
    }

    protected int portAssignment() {
        try { portSemaphore.acquire(); } catch (Exception e) {};
        int port = portAssignments.nextClearBit(0);
        portAssignments.set(port);
        try { portSemaphore.release(); } catch (Exception e) {};
        return port + msPortStart;
    }

    private void recyclePortNumber(int port) {
        try { portSemaphore.acquire(); } catch (Exception e) {};
        int p = port - msPortStart;
        portAssignments.clear(p);
        try { portSemaphore.release(); } catch (Exception e) {};
    }

    // called directly by the microservice to start a downstream microservice
    protected Object startMicroservice(int k) {
        calledPort = portAssignment();
        switch(k) {
            case 4:
                return new SDVRequestProcessorCheeseSearch(calledPort);
            case 6:
                return new SDVRequestProcessorLongEncryption1(calledPort);
            case 7:
                return new SDVRequestProcessorLongEncryption2(calledPort);
            case 8:
                return new SDVRequestProcessorLongEncryption3(calledPort);
            case 10:
                return new SDVRequestProcessorComplexLoop1(calledPort);
            case 11:
                return new SDVRequestProcessorComplexLoop2(calledPort);
            case 12:
                return new SDVRequestProcessorComplexLoop3(calledPort);
            case 13:
                return new SDVRequestProcessorComplexLoop4(calledPort);
            case 14:
                return new SDVRequestProcessorComplexLoop5(calledPort);
            case 15:
                return new SDVRequestProcessorComplexLoop6(calledPort);
            case 17:
                return new SDVRequestProcessorCategories(calledPort);
            default:
                return null;
        }
    }

    // called directly by the microservice to send request to a downstream microservice
    protected String sendToMicroservice(Payload incomingRequest, int cid, boolean returnToCaller) {
        // incomingRequest (in Payload[0]) is the plaintext byte[] representing the serialized request from the caller (might be the client or another microservice)
        // incomingRequest is not an error string, errors have already been handled and returned to the caller
        // cid is the client id
        // returnToCaller is true if return comes back to the microservice caller, otherwise return goes back to the original client caller
        String toMicroservice = encodeToString(incomingRequest.serialize(), Base64.DEFAULT);
        // TIME VALUE T3
        return communicate(toMicroservice, "activate microservice");
    }

    public String communicate(String toMicroservice, String type) {
        String fromMicroservice;
        try {
            calledSocket = new Socket("localhost", calledPort);
            calledWriter = new PrintWriter(calledSocket.getOutputStream(), true);
            calledReader = new BufferedReader(new InputStreamReader(calledSocket.getInputStream()));
        } catch (Exception e) {
            Log.d("JIM", "sendToMicroservice: ************* EXCEPTION 1004 (port="+calledPort+"): " + e.toString());
            e.printStackTrace();
            closeDownstreamMicroserviceConnection();
            recyclePortNumber(calledPort);
            return "ERR:"+(-MICROSERVICE_RETURNED_ERROR_CONDITION);
        }
        try {
            calledWriter.println(toMicroservice);
            calledWriter.println("***EOF***");
            StringBuffer sb = new StringBuffer();
            String s;
            while ((s = calledReader.readLine()) != null) {
                if (s.equals("***EOF***")) break;
                sb.append(s);
            }
            fromMicroservice = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("JIM", "sendToMicroservice: ************* EXCEPTION 1005 (port="+calledPort+"): " + e.toString());
            closeDownstreamMicroserviceConnection();
            recyclePortNumber(calledPort);
            return "ERR:"+(-MICROSERVICE_RETURNED_ERROR_CONDITION);
        }
        try {
            closeDownstreamMicroserviceConnection();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("JIM", "sendToMicroservice: ************* EXCEPTION 1006 (port="+calledPort+"): " + e.toString());
            fromMicroservice = "ERR:"+(-MICROSERVICE_RETURNED_ERROR_CONDITION);
        } finally {
            recyclePortNumber(calledPort);
        }
        //debugLogOutput(fromMicroservice, " ========== exited with tag="+type+" and", "communicate");
        return fromMicroservice;
    }

    protected void closeDownstreamMicroserviceConnection() {
        try {
            if (calledSocket != null) calledSocket.close();
            if (calledWriter != null) calledWriter.close();
            if (calledReader != null) calledReader.close();
        } catch (Exception e) {
            Log.d("JIM", nameUsedForOutputToLogger + "************* EXCEPTION 1007="+e.toString());
            e.printStackTrace();
        }
    }

    protected void processReturnValue(Payload result) {
        // The result parameter is a completed Payload, but the Payload has not yet been encrypted (or serialized)
        byte[] key;
        String retString;
        int clientId = result.getClientId();
        if (returnToCaller) {
            retString = encodeToString(result.serialize(), Base64.DEFAULT);
            writeOutput(retString);
        } else {
            key = serverStateList.get(callerServerStateListIndex).getKey();
            Payload retval = postProcessor(result, key, clientId, nameUsedForOutputToLogger);
            if (retval == null) {
                writeOutput("ERR:" + (-MICROSERVICE_RETURNED_ERROR_CONDITION));
            } else {
                retString = encodeToString(retval.serialize(), Base64.DEFAULT);
                writeOutput(retString);
            }
        }
    }

    protected void processReturnValue(String result) {
        if (result == null)
            writeOutput("ERR:" + (-MICROSERVICE_RETURNED_ERROR_CONDITION));
        else
            writeOutput(result);
    }

    protected Payload postProcessor(Payload outgoing, byte[] key, int clientId, String nameUsedForOutputToLogger) {
        Rijndael AESalgorithm = new Rijndael();
        try {
            AESalgorithm.makeKey(key, 256, AESalgorithm.DIR_ENCRYPT);
            for (int k = 0; k < outgoing.getPayloadLength(); k++)
                if (outgoing.getPayload(k).length > 0) outgoing.setPayload(k, AESalgorithm.encryptArray(outgoing.getPayload(k)));
            return outgoing;
        } catch (Exception ex) {
            Log.d("JIM", nameUsedForOutputToLogger + "************* EXCEPTION 1008="+ex.toString());
            ex.printStackTrace();
            return null;
        }
    }

    protected Payload errorPayload(String errorString) {
        // errorString is a string of the form "ERR:dd"
        // returns a Payload with type -dd
        int k = errorString.indexOf(':');
        int error = Integer.parseInt(errorString.substring(k+1,k+3));
        return new Payload.PayloadBuilder()
                .setErrorValueforErrorPayload(-error)
                .build();
    }

    protected void writeOutput(final String output) {
        final PrintWriter p = callerWriter;
        Thread thread = new Thread() {
            public void run() {
                // TIME VALUE T4
                p.println(output);
                p.println("***EOF***");
                if (callerShouldNotSendOutput) sendNoActionToCaller();
                closeCallerStreams();
            }};
        thread.start();
    }

    protected void sendNoActionToCaller() {
        if (!callCameFromServer) {
            savedLocalCallerWriter.println("NOACTION");
            savedLocalCallerWriter.println("***EOF***");
        }
        savedLocalCallerWriter.close();
    }

    protected void closeCallerStreams() {
        try {
            if (callerWriter != null) callerWriter.close();
            if (callerReader != null) callerReader.close();
            if (callerSocket != null) callerSocket.close();
            recyclePortNumber(listeningPort);
        } catch (Exception e) {
            Log.d("JIM", nameUsedForOutputToLogger + "EXCEPTION 1006="+e.toString());
            e.printStackTrace();
        }
    }

    protected void handleException(Throwable throwable, byte[] incoming) {
        // throwable.toString() is "java.util.concurrent.TimeoutException"
        Log.d("JIM", "THROWABLE="+throwable.toString());
        throwable.printStackTrace();
        debugLogOutput(incoming, "exited with exception");
        processReturnValue((String) null);
    }

}
