package com.fullsecurity.server;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

@SuppressWarnings("all")
public class SDVFile {
    private byte[] contents;
    private long length;
    private ArrayList<FileAccess> fileAccessList;
    private int clientId;
    private String name;

    public SDVFile (String fullInputFilePathName, ArrayList<FileAccess> fileAccessList, int clientId, long length) {
        this.name = fullInputFilePathName;
        this.fileAccessList = fileAccessList;
        this.clientId = clientId;
        this.length = length;
    }

    public String toString() {
        return "[" + name + ", " + fileAccessList.toString() +", " + length + ", " + clientId + "]";
    }

    public SDVFile (int n) { contents = new byte[n]; }

    public int getLength() { return (int) length; }

    public byte[] getFileAsBytes() { return contents; }

    public void setFileAsBytes(byte[] file) { contents = file; }

    public String getName() { return name; }

    public int getNumberOfLevels() { return fileAccessList.size(); }

    public ArrayList<FileAccess> getFileAccessList() { return fileAccessList; }

    public boolean setLock(int clientId) {
        // critical region protected by fileSemaphore
        if (this.clientId >= 0)
            return this.clientId == clientId;
        else {
            this.clientId = clientId;
            return true;
        }
    }

    public void releaseLock() { this.clientId = -1; }

    public void read(){
        File file = new File(name);
        contents = new byte[(int)file.length()];
        try {
            InputStream input = null;
            try {
                int totalBytesRead = 0;
                input = new BufferedInputStream(new FileInputStream(file));
                while(totalBytesRead < contents.length){
                    int bytesRemaining = contents.length - totalBytesRead;
                    //input.read() returns -1, 0, or more :
                    int bytesRead = input.read(contents, totalBytesRead, bytesRemaining);
                    if (bytesRead > 0) totalBytesRead = totalBytesRead + bytesRead;
                }
            } finally {
                if (input != null) input.close();
            }
        }
        catch (FileNotFoundException ex) {
            Log.d("JIM","Exception 600: File not found="+ex.toString());
        }
        catch (IOException ex) {
            Log.d("JIM","Exception 601: IO Exception="+ex.toString());
        }
    }

    public void write(String aOutputFileName){
        try {
            OutputStream output = null;
            try {
                output = new BufferedOutputStream(new FileOutputStream(aOutputFileName));
                output.write(contents);
            }
            finally {
                output.close();
            }
        }
        catch(FileNotFoundException ex){
            Log.d("JIM","Exception 602: File not found="+ex.toString());
        }

        catch(IOException ex){
            Log.d("JIM","Exception 603: IO Exception="+ex.toString());
        }
    }

}
