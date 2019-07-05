package com.fullsecurity.microservices;

import com.fullsecurity.common.Payload;
import com.fullsecurity.server.FileAccess;
import com.fullsecurity.server.SDVFile;
import com.fullsecurity.server.SDVServer;
import com.fullsecurity.server.User;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("all")
public class SDVRequestProcessorReadFile extends MSCommunicationsWrapper {

    private static final String nameUsedForOutputToLogger = "readFileMicroservice: ";

    private byte[] incoming;
    private String dbResult;
    private int clientId;
    private SDVServer sdvServer;

    public SDVRequestProcessorReadFile(int port, SDVServer sdvServer) {
        super(port, nameUsedForOutputToLogger);
        this.sdvServer = sdvServer;
    }

    @Override
    public void setOutputPayloads(@NotNull Payload request) {
        // request is the input Payload
        incoming = request.getPayload(0);
        clientId = request.getClientId();
        if (incoming == null)
            request.setType(CANNOT_GRANT_ACCESS_TO_FILE);
        else {
            String fullFileName = new String(incoming);
            if (fullFileName.length() == 0)
                request.setType(CANNOT_GRANT_ACCESS_TO_FILE);
            else {
                boolean isAuthorization = fullFileName.charAt(0) == '.';
                if (isAuthorization) {
                    // this is an authorization
                    if (fullFileName.length() == 1)
                        request.setType(CANNOT_GRANT_ACCESS_TO_FILE);
                    else {
                        boolean inputIsOkay = fullFileName.length() >= 4;
                        int targetClient = 0;
                        if (inputIsOkay) {
                            targetClient = fullFileName.charAt(1) - '0';
                            inputIsOkay = (targetClient >= 0 && targetClient <= 3 && targetClient != clientId);
                        }
                        if (!inputIsOkay)
                            request.setType(CANNOT_GRANT_ACCESS_TO_FILE);
                        else {
                            fullFileName = fullFileName.substring(3);
                            SDVFile sdvFile = sdvServer.findFileAndLock(fullFileName, targetClient);
                            if (sdvFile == null)
                                request.setType(CANNOT_GRANT_ACCESS_TO_FILE);
                            else {
                                int levelForThisFile = getFileLevel(sdvFile);
                                User user = sdvServer.users.get(clientId);
                                // request is an authorization for others to access the file
                                sdvFile.getFileAccessList().get(levelForThisFile).addNewAuthorizer(clientId, user.getLevel());
                                request.setPayload(0, user.getName().getBytes());
                                request.addPayload("AUTHORIZED".getBytes());
                            }
                        }
                    }
                } else {
                    // this is a request to access the file
                    SDVFile sdvFile = sdvServer.findFileAndLock(fullFileName, clientId);
                    if (sdvFile == null)
                        request.setType(CANNOT_GRANT_ACCESS_TO_FILE);
                    else {
                        int levelForThisFile = getFileLevel(sdvFile);
                        User user = sdvServer.users.get(clientId);
                        if (sdvFile.getFileAccessList().get(levelForThisFile).grantAccess(clientId)) {
                            sdvFile.read();
                            request.setPayload(0, sdvFile.getFileAsBytes());
                            request.addPayload(fullFileName.getBytes());
                        } else
                            request.setType(CANNOT_GRANT_ACCESS_TO_FILE);
                        sdvFile.getFileAccessList().get(levelForThisFile).resetAuthorizers();
                        sdvFile.releaseLock();
                    }
                }
            }
        }
        processReturnValue(request);
    }

    private int getFileLevel(SDVFile file) {
        FileAccess fileAccess;
        // assume fileAccess has N entries, where N is the number of security levels, N >= 1
        // assume sdvServer.fileAccess.get(0).numberOfLevels() is present and is equal to N
        // assume [0<=k<=(N-1)] [M0...M(N-1)] are the maximum file sizes that can be accessed at security level k
        // assume values are in increasing order, i.e. M(k) > M(k-1)
        // prove file.length() > [0<=k<=levelForThisFile] sdvServer.fileAccess.get(k).getMaximumFileSize()
        //   and file.length() <= [(levelForThisFile+1)<=k<N] sdvServer.fileAccess.get(k).getMaximumFileSize()
        int levelForThisFile = 0;
        int N = file.getNumberOfLevels();
        for (int i = 1; i < N; i++) {
            if (file.getLength() > file.getFileAccessList().get(i).getMaximumFileSize())
                levelForThisFile = i;
            else
                break;
        }
        // if N == 1
        //     return 0, the only security level present
        // assume getFileLevel(file) returns the correct level for k <= N-2, N >= 2
        // thus file.length() > sdvServer.fileAccess.get(k).getMaximumFileSize(), for k <= N-2
        // prove levelForThisFile for k == N-1,
        //     where if file.length() >  sdvServer.fileAccess.get(N-1).getMaximumFileSize(), levelForThisFile = N-1
        //           if file.length() <= sdvServer.fileAccess.get(N-1).getMaximumFileSize(), levelForThisFile = N-2
        // at the beginning of the last execution of the loop, N >= 2
        //     levelForThisFile == N - 2
        //     i == N - 1
        //     case 1: file.length() >  sdvServer.fileAccess.get(N-1).getMaximumFileSize(), levelForThisFile == N-1, loop continues
        //     case 1: file.length() <= sdvServer.fileAccess.get(N-1).getMaximumFileSize(), levelForThisFile == N-2. loop stops
        return levelForThisFile;

    }
}
