package com.fullsecurity.server;

import android.os.Environment;

import java.io.File;

public class ServerFullDirectoryPathName {
    private String directoryPathName;
    private transient static final String AndroidPath = "/Android/data";

    public ServerFullDirectoryPathName() {
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
        directoryPathName = extStorageDirectory + AndroidPath + "/";
    }

    public String getFullDirectoryPathName(String simpleDirectoryName) {
        String fullDirectoryPathName = directoryPathName + simpleDirectoryName;
        File file = new File(fullDirectoryPathName);
        file.mkdirs();
        return fullDirectoryPathName;
    }
}
