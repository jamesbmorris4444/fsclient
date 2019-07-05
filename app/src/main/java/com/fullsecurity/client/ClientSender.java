package com.fullsecurity.client;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@SuppressWarnings("all")
public class ClientSender {

    private BufferedReader in;
    private PrintWriter out;
    private Socket clientSocket;
    private String hostName;
    private int portNumber;

    public ClientSender(String hostName, int portNumber) {
        this.hostName = hostName;
        this.portNumber = portNumber;
    }

    public String communicate(String toServer, String type) throws Exception {
        String fromServer;
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
        //debugLogOutput(fromServer, " ========== exited with tag="+type+" and", "communicate");
        return fromServer;
    }

    public static void debugLogOutput(String output, String message, String name) {
        String first = output.substring(0,(output.length() < 10 ? output.length() : 10));
        Log.d("JIM","LOG: " + name + message + " (arg=\"" + first + "...\")");
    }
}
