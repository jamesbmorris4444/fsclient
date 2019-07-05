package com.fullsecurity.microservices;

import com.fullsecurity.common.Payload;
import com.fullsecurity.server.MSSettings;
import com.fullsecurity.server.SDVServer;
import com.fullsecurity.server.ServerState;
import com.fullsecurity.shared.MainActivity;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.concurrent.Semaphore;

@SuppressWarnings("all")
public class SDVRequestProcessorMSMgmt extends MSCommunicationsWrapper {

    private static final String nameUsedForOutputToLogger = "MSMgmtMicroservice: ";

    private BitSet portAssignments;
    private Semaphore portSemaphore;
    private ArrayList<ServerState> serverStateList;
    private Semaphore sslSemaphore;
    private ArrayList<MSSettings> msMap;
    private MainActivity mainActivity;
    private int port;

    private byte[] incoming;
    private String dbResult;
    private  int clientId;
    private SDVServer sdvServer;

    public SDVRequestProcessorMSMgmt(BitSet portAssignments, ArrayList<ServerState> serverStateList, ArrayList<MSSettings> msMap,
                                     Semaphore sslSemaphore, Semaphore portSemaphore, int port, MainActivity mainActivity, SDVServer sdvServer) {
        super(portAssignments, serverStateList, msMap, sslSemaphore, portSemaphore, port, mainActivity, nameUsedForOutputToLogger);

        this.portAssignments = portAssignments;
        this.portSemaphore = portSemaphore;
        this.serverStateList = serverStateList;
        this.sslSemaphore = sslSemaphore;
        this.msMap = msMap;
        this.mainActivity = mainActivity;
        this.port = port;
    }

    @Override
    protected void setOutputPayloads(Payload request) {
        // request is the input Payload
        incoming = request.getPayload(0);
        clientId = request.getId();
        debugLogOutput(incoming, "entered");
        // START MY WORK
        String returnValue;
        String searchPattern = new String(incoming);
        if (searchPattern.equals("START")) {
            StringBuffer sb = new StringBuffer();
            ArrayList<String> eligibles = new ArrayList<String>();
            for (int i = 0; i < msMap.size(); i++) if (msMap.get(i).getParent() == null) eligibles.add(msMap.get(i).getName());
            for (int i = 0; i < eligibles.size(); i++) {
                 sb.append(eligibles.get(i));
                 if (i < eligibles.size() - 1) sb.append("|");
            }
            returnValue = sb.toString();
            if (returnValue.length() == 0) returnValue = "<empty list>";
            request.setPayload(0, returnValue.getBytes());
            processReturnValue(request);
        } else {
            boolean error = searchPattern.length() < 2;
            if (error)
                processReturnValue("ERR:"+(-UNABLE_TO_FIND_MICROSERVICE)+"|"+searchPattern);
            else {
                char command = searchPattern.charAt(0);
                error = command != '+' && command != '-';
                String searchName = searchPattern.substring(1);
                if (error)
                    processReturnValue("ERR:" + (-UNABLE_TO_FIND_MICROSERVICE) + "|" + searchName);
                else {
                    try { portSemaphore.acquire(); } catch (Exception e) { };
                    int msIndex = mapFromMSNameToMSIndex(msMap, searchName);
                    try { portSemaphore.release(); } catch (Exception e) { };
                    if (msIndex < 0 || error)
                        processReturnValue("ERR:" + (-UNABLE_TO_FIND_MICROSERVICE) + "|" + searchName);
                    else {
                        switch (command) {
                            case '-':
                                if (!msMap.get(msIndex).isActive()) {
                                    processReturnValue("ERR:" + (-UNABLE_TO_FIND_MICROSERVICE) + "|" + searchName);
                                    break;
                                }
                                try { portSemaphore.acquire(); } catch (Exception e) { };
                                msMap.get(msIndex).setActive(!msMap.get(msIndex).isActive());
//                                msMap.get(msIndex).getThread().interrupt();
//                                try {
//                                    if (msMap.get(msIndex).getServerSocket() != null)
//                                        msMap.get(msIndex).getServerSocket().close();
//                                    if (msMap.get(msIndex).getSocket() != null)
//                                        msMap.get(msIndex).getSocket().close();
//                                } catch (Exception f) {
//                                    Log.d("JIM", nameUsedForOutputToLogger + "EXCEPTION 3000=" + f.toString());
//                                };
                                try { portSemaphore.release(); } catch (Exception e) { };
                                request.setPayload(0, ("-"+searchName).getBytes());
                                // FINISHED MY WORK
                                processReturnValue(request);
                                break;
                            default: // '+'
                                if (msMap.get(msIndex).isActive()) {
                                    processReturnValue("ERR:" + (-UNABLE_TO_FIND_MICROSERVICE) + "|" + searchName+"5");
                                    break;
                                }
                                try { portSemaphore.acquire(); } catch (Exception e) { };
                                msMap.get(msIndex).setActive(!msMap.get(msIndex).isActive());
                                //sdvServer.startMicroservice(msIndex);
                                try { portSemaphore.release(); } catch (Exception e) { };
                                request.setPayload(0, ("+"+searchName).getBytes());
                                // FINISHED MY WORK
                                processReturnValue(request);
                                break;
                        }
                    }
                }
            }
        }
    }

}
