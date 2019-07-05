package com.fullsecurity.microservices;

import android.util.Base64;
import android.util.Log;

import com.fullsecurity.common.Payload;
import com.fullsecurity.server.MSSettings;
import com.fullsecurity.server.ServerState;
import com.fullsecurity.shared.MainActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

@SuppressWarnings("all")
public abstract class MSReceiveWrapper {

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

    private int myIndex;
    private ArrayList<ServerState> serverStateList;
    private ArrayList<MSSettings> msMap;
    private Semaphore sslSemaphore;
    private Semaphore msmSemaphore;
    private String nameUsedForOutputToLogger;
    private MainActivity mainActivity;

    private int listeningPort;

    public MSReceiveWrapper(int myIndex, ArrayList<ServerState> serverStateList, ArrayList<MSSettings> msMap,
                            Semaphore sslSemaphore, Semaphore msmSemaphore, MainActivity mainActivity, String nameUsedForOutputToLogger) {
        this.myIndex = myIndex;
        this.serverStateList = serverStateList;
        this.msMap = msMap;
        this.sslSemaphore = sslSemaphore;
        this.msmSemaphore = msmSemaphore;
        this.mainActivity = mainActivity;

        this.listeningPort = msMap.get(myIndex).getPort();
        this.nameUsedForOutputToLogger = nameUsedForOutputToLogger;

        Thread msThread = new Thread() {
            public void run() {
                msWrapperListener();
            }
        };
        msThread.start();
        msMap.get(myIndex).setThread(msThread);
        Log.d("JIM", nameUsedForOutputToLogger + "LISTENER listening on port "+listeningPort);
    }

    public MSReceiveWrapper() {}

    public void msWrapperListener() {
        Socket clientSocket = null;
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(listeningPort);
            msMap.get(myIndex).setServerSocket(serverSocket);
            while (true) {
                clientSocket = serverSocket.accept();
                msMap.get(myIndex).setSocket(clientSocket);
                Log.d("JIM", "LOG: " + nameUsedForOutputToLogger + "accepted request on port "+listeningPort);
                handleMicroserviceIncomingRequestOnly(clientSocket);
            }
        } catch (Exception e) {
            Log.d("JIM", nameUsedForOutputToLogger + "MICROSERVICE LISTENER SHUTDOWN on port "+listeningPort+": "+e.toString());
            e.printStackTrace();
            try {
                if (serverSocket != null) serverSocket.close();
                if (clientSocket != null) clientSocket.close();
            } catch (Exception f) {};
        }
    }

    public void writeToOutputSocket(PrintWriter pws, BufferedReader brs, String responseIntendedForClient) {
        pws.println(responseIntendedForClient);
        pws.println("***EOF***");
        try {
            pws.close();
            brs.close();
        } catch (Exception e) {
            Log.d("JIM", nameUsedForOutputToLogger + "EXCEPTION 1000="+e.toString());
            e.printStackTrace();
        }
    }

    public void handleMicroserviceIncomingRequestOnly(Socket clientSocket) {
        PrintWriter pws = null;
        BufferedReader brs = null;
        try {
            pws = new PrintWriter(clientSocket.getOutputStream(), true);
            brs = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (Exception e) {
            // Cannot read request; caller will have to wait for timeout
            // If the problem is with the printWriter, it does no good to continue, because response cannot be sent to the caller
            Log.d("JIM", nameUsedForOutputToLogger + "EXCEPTION 1001="+e.toString());
            e.printStackTrace();
            closeStreams(pws, brs);
            return;
        }
        StringBuffer sb = new StringBuffer();
        String s = null;
        while (true) {
            try{
                s = brs.readLine();
            } catch (Exception e) {
                pws.println("ERR:"+(-EXCEPTION_READING_REQUEST_FOR_SERVICE));
                pws.println("***EOF***");
                Log.d("JIM", nameUsedForOutputToLogger + "EXCEPTION 1002="+e.toString());
                e.printStackTrace();
                closeStreams(pws, brs);
                return;
            }
            if (s == null) break;
            if (s.equals("***EOF***")) break;
            sb.append(s);
        }
        String sTransmitted = sb.toString();
        Payload request = new Payload(Base64.decode(sTransmitted, Base64.DEFAULT));
        int numberOfBytesTransmitted = sTransmitted.length();
        boolean returnToMe = request.isReturnToMe();
        Log.d("JIM","LOG: "+nameUsedForOutputToLogger+"(in handleMicroserviceIncomingRequestOnly) started to handle request");

        if (!returnToMe) {
            // release the "communicate" method in ClientSender so it will unblock and terminate, because no return will come back through this socket
            pws.println("RELEASE");
            pws.println("***EOF***");
            closeStreams(pws, brs);
            pws = null;
            brs = null;
        }

        int clientId = request.getId();
        boolean streamsOK = true;

        if (!returnToMe) {
            try { sslSemaphore.acquire(); } catch (Exception e) { }
            int stateIndex = indexOfClientWithId(clientId);
            if (stateIndex >= 0) {
                PrintWriter pwsSave = pws;
                BufferedReader brsSave = brs;
                try {
                    pws = new PrintWriter(serverStateList.get(stateIndex).getSocket().getOutputStream(), true);
                    brs = new BufferedReader(new InputStreamReader(serverStateList.get(stateIndex).getSocket().getInputStream()));
                } catch (Exception e) {
                    streamsOK = false;
                    pwsSave.println("ERR:" + (-EXCEPTION_WRITING_REQUEST_FOR_SERVICE));
                    pwsSave.println("***EOF***");
                    Log.d("JIM", nameUsedForOutputToLogger + "EXCEPTION 1003=" + e.toString());
                    e.printStackTrace();
                    closeStreams(pwsSave, brsSave);
                    closeStreams(pws, brs);
                    return;
                } finally {
                    try {  sslSemaphore.release(); } catch (Exception e) { }
                }
            } else {
                try { sslSemaphore.release(); } catch (Exception e) { }
                pws.println("ERR:" + (-SERVER_INTERNAL_ERROR_ID_NOT_FOUND));
                pws.println("***EOF***");
                Log.d("JIM", nameUsedForOutputToLogger + "RETURNED SOFT EXCEPTION 1005");
                closeStreams(pws, brs);
            }
        }

        if (streamsOK) {
            // this is where the microservice does its real work

            switch (clientId) {
                case 0:
                    mainActivity.tlfrg.sdvClient.addTime(numberOfBytesTransmitted);    // TIME VALUE T2
                    break;
                case 1:
                    mainActivity.trfrg.sdvClient.addTime(numberOfBytesTransmitted);
                    break;
                case 2:
                    mainActivity.blfrg.sdvClient.addTime(numberOfBytesTransmitted);
                    break;
                default:
                    mainActivity.brfrg.sdvClient.addTime(numberOfBytesTransmitted);
                    break;
            }
            Log.d("JIM","LOG: TIME VALUE T2="+numberOfBytesTransmitted);
            setOutputPayloads(request, pws, brs, returnToMe);
        }
        Log.d("JIM","LOG: handleMicroserviceIncomingRequestOnly exited microservice after calling setOutputPayloads to handle microservice functions");
    }

    public void closeStreams(PrintWriter pwsLocal, BufferedReader brsLocal) {
        try {
            if (pwsLocal != null) pwsLocal.close();
            if (brsLocal != null) brsLocal.close();
        } catch (Exception e) {
            Log.d("JIM", nameUsedForOutputToLogger + "EXCEPTION 1006="+e.toString());
            e.printStackTrace();
        }
    }

    private int indexOfClientWithId(int id) {
        for (int k = 0; k < serverStateList.size(); k++) if (serverStateList.get(k).getId() == id) return k;
        return -1;
    }

    abstract void setOutputPayloads(Payload request, PrintWriter pws, BufferedReader brs, boolean returnToM);
}
