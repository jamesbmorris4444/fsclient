package com.fullsecurity.microservices;

import com.fullsecurity.common.Payload;
import com.fullsecurity.server.ServerFullDirectoryPathName;

import java.io.File;

@SuppressWarnings("all")
public class SDVRequestProcessorShowDirectory extends MSCommunicationsWrapper {

    private static final String nameUsedForOutputToLogger = "showDirectoryMicroservice: ";

    private byte[] incoming;
    private String dbResult;
    private int clientId;

    public SDVRequestProcessorShowDirectory(int port) { super(port, nameUsedForOutputToLogger); }

    @Override
    public void setOutputPayloads(Payload request) {
        // request is the input Payload
        incoming = request.getPayload(0);
        clientId = request.getClientId();
        debugLogOutput(incoming, "entered");
        // START MY WORK
        ServerFullDirectoryPathName fdpn = new ServerFullDirectoryPathName();
        String dirName = fdpn.getFullDirectoryPathName(new String(incoming));
        File directory = new File(dirName);
        File[] files = directory.listFiles();
        String directoryList;
        if (files.length == 0)
            directoryList = "<empty directory>";
        else {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    if (files[i].length() < 2000000) {
                        sb.append(files[i].getName() + "(" + files[i].length() + ")");
                        if (i < files.length - 1) sb.append("|");
                    }
                }
            }
            directoryList = sb.toString();
        }
        request.setPayload(0, directoryList.getBytes());
        request.addPayload(dirName.getBytes());
        // FINISHED MY WORK
        processReturnValue(request);
    }
}