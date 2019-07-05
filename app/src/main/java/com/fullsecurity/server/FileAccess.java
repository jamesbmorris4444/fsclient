package com.fullsecurity.server;

import java.util.ArrayList;

public class FileAccess {
    private long maximumFileSize;
    private int[] authorizersPresent;
    private int[] authorizersRequired;
    private ArrayList<Integer> authorizationSource;

    public FileAccess(long maximumFileSize, int[] authorizersRequired) {
        this.maximumFileSize = maximumFileSize;
        this.authorizersRequired = authorizersRequired;
        authorizersPresent = new int[authorizersRequired.length];
        resetAuthorizers();
    }

    public String toString() {
        return "[max=" + maximumFileSize + ", pres=" + java.util.Arrays.toString(authorizersPresent) +", reqd=" + java.util.Arrays.toString(authorizersRequired) + ", source=" + authorizationSource.toString() + "]";
    }

    public void addNewAuthorizer(int clientId, int level) {
        if (authorizationSource.contains(clientId)) return;
        authorizationSource.add(clientId);
        authorizersPresent[level] += 1;
    }

    public void resetAuthorizers() {
        for (int k = 0; k < numberOfLevels(); k++) authorizersPresent[k] = 0;
        authorizationSource = new ArrayList<>();
    }

    public long getMaximumFileSize() { return maximumFileSize; }

    public int numberOfLevels() { return authorizersRequired.length; }

    public boolean grantAccess(int clientId) {
        if (authorizationSource.contains(clientId)) return false;
        // assume N == authorizersRequired.length == number of security levels, N >= 1
        // assume authorizersPresent = [P0...P(N-1)]
        // assume authorizersRequired = [R0...R(N-1)]
        // prove [for all 0<=k<=(N-1)] authorizersPresent[k] >= authorizersRequired[k] else false
        // if N == 1 then authorizersPresent[0] == [P]
        // assume N - 1 is true, prove N is true
        for (int k = 0; k < numberOfLevels(); k++) if (authorizersPresent[k] < authorizersRequired[k]) return false;
        // if N == 1
        //    authorizersPresent[0] >= authorizersRequired[0]
        // assume N - 1 is true, thus for all 0<=k<=(N-2)] authorizersPresent[k] >= authorizersRequired[k]
        //    for k == N-1, authorizersPresent[k] >= authorizersRequired[k]
        return true;
    }
}
