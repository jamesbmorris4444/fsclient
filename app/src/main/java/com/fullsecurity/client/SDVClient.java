package com.fullsecurity.client;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.fullsecurity.common.DHConstants;
import com.fullsecurity.common.Payload;
import com.fullsecurity.common.RSA;
import com.fullsecurity.common.Rijndael;
import com.fullsecurity.common.SHA256;
import com.fullsecurity.common.Utilities;
import com.fullsecurity.server.SDVFile;
import com.fullsecurity.server.SDVServer;
import com.fullsecurity.shared.MainActivity;
import com.fullsecurity.shared.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static android.util.Base64.encodeToString;
import static junit.framework.Assert.assertEquals;

@SuppressWarnings("all")
public class SDVClient {

    private final static int UNABLE_TO_FIND_MICROSERVICE = -10;
    private final static int CALLER_COULD_NOT_CONNECT_TO_MICROSERVICE = -11;
    private final static int IMPROPER_EXPECTED_ARGUMENT_IN_REQUEST = -12;
    private final static int EXCEPTION_READING_REQUEST_FOR_SERVICE = -13;
    private final static int EXCEPTION_WRITING_REQUEST_FOR_SERVICE = -14;
    private final static int MICROSERVICE_RETURNED_ERROR_CONDITION = -15;
    private final static int SERVER_INTERNAL_ERROR_ID_NOT_FOUND = -16;
    private final static int SERVER_VERIFICATIION_OF_STS_FAILED = -17;
    private final static int SERVER_INTERNAL_ERROR_NOT_ABLE_TO_ASSIGN_ID = -18;
    private final static int CLIENT_HAS_NO_KEY_USES_REMAINING = -19;
    private final static int SERVER_INTERNAL_ERROR_IMPOSSIBLE_VALUE_FOR_TYPE_EXPECTED = -20;
    private final static int EXCEPTION_IN_SERVER_DURING_AES_ENCRYPTION = -21;
    private final static int ENCRYPTION_KEYS_HAVE_EXPIRED = -22;
    private final static int CLIENT_VERIFICATIION_OF_STS_FAILED = -23;
    private final static int EXCEPTION_IN_SERVER_DURING_AES_DECRYPTION = -24;
    private final static int EXCEPTION_IN_CLIENT_DURING_AES_ENCRYPTION = -25;
    private final static int EXCEPTION_IN_CLIENT_DURING_AES_DECRYPTION = -26;
    private final static int INTERNAL_ERROR_UNKNOWN_OPERATION = -27;
    private final static int CLIENT_COULD_NOT_CONNECT_TO_SERVER = -28;
    private final static int CLIENT_TIMED_OUT_WHILE_WAITING_FOR_SERVER = -29;
    private final static int CANNOT_GRANT_ACCESS_TO_FILE = -30;
    private final static int ERROR_IN_DATABASE_OPERATION = -31;

    private final static int WRITE_EXTERNAL_STORAGE_REQUEST = 1;
    private final static int READ_EXTERNAL_STORAGE_REQUEST = 2;
    private final static int TIMEOUT = 10; // seconds
    private final static boolean NODEBUG = true;

    public String encryptionTestSend =
            "Under the wide and starry sky\n" +
                    "Dig the grave and let me lie.\n" +
                    "Glad did I live and gladly die,\n" +
                    "And I laid me down with a will.\n";

    // STEP 7
    public String encryptionTestReceive;
    public String readDirectoryReceive;
    public String directoryNameReceive;
    public String readFileNameReceive;
    public SDVFile readFileReceive;
    public String cheeseListReceive;
    public ArrayList<String> longEncryptionReceive = new ArrayList<>();
    public ArrayList<String> complexLoopReceive = new ArrayList<>();
    public String msNameReceive;

    private ArrayList<String> listOfDetailsInDirectory;
    private ArrayList<String> listOfFileNamesInDirectory;
    private ArrayList<String> listOfMSNames;
    private String errorMicroserviceName;
    public String simpleFileName = "filename";
    public String simpleDirectoryName;
    public String readFileNameSend;
    public String cheeseListSend;
    public String msNameSend;
    private String resultForCompletionOfFileRead;
    public final int portNumber = 8000;
    public final int readFileButton = 3;
    public final int showDirButton = 2;
    public final int testingReadFileButton = 100;
    private TextView clients;
    private boolean completed;
    private int returnValueFromOperation;
    private boolean testingInProgress = false;
    public String hostName;

    // STEP 1
    private final int numberOfButtons = 8;

    public TextView[] button;
    private TextView[] tv;
    private EditText et1;
    private EditText et2;
    private EditText et3;
    private EditText ipAddr;
    private Context context;
    private int[] colorToggle;
    private int operation;
    private SDVServer sdvServer;
    private View view;
    public long fileDecryptionTime;
    private ArrayList<Observable<String>> serverResultObservable;

    private final String password = "Universal password for Acme Corporation - 10/17/15";
    private byte[] universalPasswordHash;
    public final int NUMBER_OF_KEY_USES_PER_KEY = 100;

    private Random rnd = null;
    private BigInteger initialSecret = null;
    private BigInteger publicKey = null;
    private RSA rsa = null;
    private byte[] STSauthentication = null;
    private byte[] signature = null;
    private BigInteger sharedSecretKey = null;
    private byte[] key1024 = null;
    private byte[] key = null;
    private Rijndael AESalgorithm = null;
    private byte[] signatureAsRead = null;
    private byte[] publicKeyReceived = null;
    private RSA remoteRSA = null;
    public String clientName;
    public int userId;

    private MainActivity mainActivity;

    private String outbound;
    private String inbound;
    public int clientIdentifier;
    private int nextOperation;
    public int remainingKeyUses;

    public SDVClient(View v, MainActivity m, String n, int i) {
        view = v;
        mainActivity = m;
        context = view.getContext();
        button = new TextView[numberOfButtons];
        tv = new TextView[numberOfButtons];
        colorToggle = new int[numberOfButtons];
        initialization();
        rsa = new RSA();
        universalPasswordHash = SHA256.sha256(password.getBytes());
        remainingKeyUses = 0;
        clientName = n;
        userId = i;
        clientIdentifier = -(userId + 1);
        serverResultObservable = new ArrayList<>(numberOfButtons);
        for (int k = 0; k < numberOfButtons; k++) {
            if (k == numberOfButtons-1) {
                button[k].setOnClickListener((View view) -> {
                    mainActivity.loadCategoriesFragment(key, userId);
                });
            } else {
                final int kk = k;
                serverResultObservable.add(Observable.create(emitter -> {
                    emitter.onNext(buttonAction(kk));
                    emitter.onComplete();
                }));
                createServerObservable(kk);
            }
        }
    }

    private void createServerObservable(final int k) {
        button[k].setOnClickListener((View v) -> {
            disableButtons();
            intializeObservable(k, serverResultObservable);
        });
    };


    private void intializeObservable(int k, ArrayList<Observable<String>> observable) {
        //operation = k;
        switch (k) {
            case 0:
                observable.get(k)
                        .observeOn(Schedulers.io())
                        .map((String payload) -> {
                            // payload is either an error found by the client when creating the first STS request for the server or is the client's first STS request as a JSON string
                            if (payload.startsWith("ERR:")) return payload;

                            // payload is the client's public key as a JSON string
                            String returnPayload = communicate(payload, "localhost", portNumber, "first part of original reactive STS call in SDVClient"); // part one of STS;
                            if (returnPayload.startsWith("ERR:")) return returnPayload;

                            // returnPayload is the JSON response from the server to the client's first STS payload request
                            // an exception is thrown by clientSender if the client cannot connect to the server, returnPayload="java.lang.Exception: CAN'T CONNECT EXCEPTION"

                            // control always returns here when the server completes the first STS request successfully
                            Payload responsePayload = new Payload(Base64.decode(returnPayload, Base64.DEFAULT));

                            // Payload server response processed by client
                            return STSEstablishKeyPartTwo(responsePayload);
                        })
                        .map((String payload) -> {
                            // payload is either an error found by the client when creating the second STS request for the server or is the client's second STS request as a JSON string
                            if (payload.startsWith("ERR:")) return payload;

                            // payload is the second part of the STS protocol from the client
                            String returnPayload = communicate(payload, "localhost", portNumber, "second part of original reactive STS call in SDVClient"); // part two of STS
                            if (returnPayload.startsWith("ERR:")) return returnPayload;

                            // returnPayload is the JSON response from the server to the client's second STS payload request
                            // an exception is thrown by clientSender if the client cannot connect to the server, returnPayload="java.lang.Exception: CAN'T CONNECT EXCEPTION"

                            // control always returns here when the server completes the second STS request successfully
                            Payload responsePayload = new Payload(Base64.decode(returnPayload, Base64.DEFAULT));

                            // Payload server response processed by client
                            remainingKeyUses = NUMBER_OF_KEY_USES_PER_KEY;
                            return (responsePayload.getType() < 0 ? "ERR:" + (-responsePayload.getType()) : "SUC:100");
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .timeout(TIMEOUT, TimeUnit.SECONDS)
                        .subscribe((String result) -> {
                            button[0].setText("STS Protocol [" + userId + "]");
                            processReturnValue(result);
                        }, throwable -> {
                            // throwable.toString() is either
                            // "java.lang.Exception: CAN'T CONNECT EXCEPTION"
                            // or
                            // "java.util.concurrent.TimeoutException"
                            mainActivity.runOnUiThread(new Runnable() {
                                public void run() {
                                    if (throwable.toString().contains("CAN'T CONNECT EXCEPTION"))
                                        processReturnValue("ERR:" + (-CLIENT_COULD_NOT_CONNECT_TO_SERVER));
                                    else
                                        processReturnValue("ERR:" + (-CLIENT_TIMED_OUT_WHILE_WAITING_FOR_SERVER));
                                }
                            });
                            Log.d("JIM", "THROWABLE("+userId+")=" + throwable.toString());
                            throwable.printStackTrace();
                        });
                break;
            case 300: // read file
                Observable
                        .interval(0, 5, TimeUnit.SECONDS) // initial delay, delay interval, time units
                        .map(initial -> { return buttonAction(3); } ) // start file read process
                        .observeOn(Schedulers.io())
                        .takeUntil(Observable.timer(20,TimeUnit.SECONDS)) // timeout if no file access within 60 seconds
                        .map((String payload) -> {
                            // payload is either an error found by the client when processing the request for the server or is the client's request as a JSON string

                            if (payload.startsWith("ERR:")) return payload;

                            // payload is the client's request as a JSON string

                            String returnPayload = communicate(payload, "localhost", portNumber, "original reactive call in SDVClient for all other ops than STS");
                            if (returnPayload.startsWith("ERR:")) return returnPayload;

                            // returnPayload is the JSON response from the server
                            // an exception is thrown by clientSender if the client cannot connect to the server, returnPayload="java.lang.Exception: CAN'T CONNECT EXCEPTION"

                            // control always returns here when the server completes a request
                            Payload responsePayload = new Payload(Base64.decode(returnPayload, Base64.DEFAULT));

                            // TIME VALUE T5

                            // Payload server response processed by client
                            resultForCompletionOfFileRead = clientFinalProcessor(3, responsePayload);
                            return resultForCompletionOfFileRead;

                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .timeout(TIMEOUT, TimeUnit.SECONDS)
                        .takeUntil((String result) -> { return result.startsWith("SUC:"); })
                        .subscribe((String result) -> {
                        }, throwable -> {
                            // throwable.toString() is either
                            // "java.lang.Exception: CAN'T CONNECT EXCEPTION"
                            // or
                            // "java.util.concurrent.TimeoutException"
                            mainActivity.runOnUiThread(new Runnable() {
                                public void run() {
                                    if (throwable.toString().contains("CAN'T CONNECT EXCEPTION"))
                                        processReturnValue("ERR:" + (-CLIENT_COULD_NOT_CONNECT_TO_SERVER));
                                    else
                                        processReturnValue("ERR:" + (-CLIENT_TIMED_OUT_WHILE_WAITING_FOR_SERVER));
                                }
                            });
                            Log.d("JIM", "THROWABLE("+userId+")=" + throwable.toString());
                            throwable.printStackTrace();
                        }, () -> {;
                            processReturnValue(resultForCompletionOfFileRead);
                        });
                break;
            default:
                observable.get(k)
                        .observeOn(Schedulers.io())
                        .map((String payload) -> {
                            // payload is either an error found by the client when processing the request for the server or is the client's request as a JSON string
                            if (payload.startsWith("ERR:")) return payload;

                            // payload is the client's request as a JSON string
                            String returnPayload = communicate(payload, "localhost", portNumber, "original reactive call in SDVClient for all other ops than STS");
                            if (returnPayload.startsWith("ERR:")) return returnPayload;

                            // returnPayload is the JSON response from the server
                            // an exception is thrown by clientSender if the client cannot connect to the server, returnPayload="java.lang.Exception: CAN'T CONNECT EXCEPTION"

                            // control always returns here when the server completes a request
                            Payload responsePayload = new Payload(Base64.decode(returnPayload, Base64.DEFAULT));

                            // TIME VALUE T5

                            // Payload server response processed by client
                            return clientFinalProcessor(k, responsePayload);
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .timeout(TIMEOUT, TimeUnit.SECONDS)
                        .subscribe((String result) -> {
                            processReturnValue(result);
                        }, throwable -> {
                            // throwable.toString() is either
                            // "java.lang.Exception: CAN'T CONNECT EXCEPTION"
                            // or
                            // "java.util.concurrent.TimeoutException"
                            mainActivity.runOnUiThread(new Runnable() {
                                public void run() {
                                    if (throwable.toString().contains("CAN'T CONNECT EXCEPTION"))
                                        processReturnValue("ERR:" + (-CLIENT_COULD_NOT_CONNECT_TO_SERVER));
                                    else
                                        processReturnValue("ERR:" + (-CLIENT_TIMED_OUT_WHILE_WAITING_FOR_SERVER));
                                }
                            });
                            Log.d("JIM", "THROWABLE("+userId+")=" + throwable.toString());
                            throwable.printStackTrace();
                        });
                break;
        }
    }

    public String communicate(String toServer, String hostName, int portNumber, String type) {
        String fromServer;
        BufferedReader in;
        PrintWriter out;
        Socket clientSocket;
        try {
            clientSocket = new Socket(hostName, portNumber);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (Exception e) {
            Log.d("JIM", "ClientSender: EXCEPTION 200 (host= "+hostName+" port="+portNumber+"): " + e.toString());
            e.printStackTrace();
            return "ERR:" + (-CALLER_COULD_NOT_CONNECT_TO_MICROSERVICE);
        }
        try {
            out.println(toServer);
            out.println("***EOF***");
            StringBuffer sb = new StringBuffer();
            String s;
            while ((s = in.readLine()) != null) {
                if (s.equals("***EOF***")) break;
                sb.append(s);
            }
            fromServer = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("JIM", "ClientSender: EXCEPTION 201 (port="+portNumber+"): " + e.toString());
            return "ERR:" + (-EXCEPTION_READING_REQUEST_FOR_SERVICE);
        }
        try {
            clientSocket.close();
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("JIM", "ClientSender: EXCEPTION 202 (port="+portNumber+"): " + e.toString());
            return "ERR:" + (-EXCEPTION_READING_REQUEST_FOR_SERVICE);
        }
        //debugLogOutput(fromServer, " ========== exited with tag="+type+" and", "communicate");
        return fromServer;
    }

    public static void debugLogOutput(String output, String message, String name) {
        if (NODEBUG) return;
        String first = output.substring(0,(output.length() < 10 ? output.length() : 10));
        Log.d("JIM","LOG: " + name + message + " (arg=\"" + first + "...\")");
    }

    // STEP 4
    private String main(int op) {
        int err;
        switch (op) {
            case 1:
                // first request of two in station-to-station protocol request by client

                // initial random secret guess for DH
                Random rnd = new Random();
                initialSecret = new BigInteger(1024, rnd);
                publicKey = DHConstants.G.modPow(initialSecret, DHConstants.P);

                // Payload request by client:
                //   0: public key generated by client
                // type=1 is an initial station-to-station request
                Payload payload = new Payload.PayloadBuilder()
                        .setNumberOfPayloadParameters(1)
                        .setClientId(clientIdentifier)
                        .setPayloadType(1)
                        .setEmptyMicroserviceName()
                        .build();
                payload.setPayload(0, publicKey.toByteArray());
                return encodeToString(payload.serialize(), Base64.DEFAULT);
            case 2:
                // Encrypt a string and send to server for an encryption loop test

                if (remainingKeyUses == 0) return "ERR:" + (-CLIENT_HAS_NO_KEY_USES_REMAINING);
                byte[] sCipher = null;
                AESalgorithm = new Rijndael();
                try {
                    AESalgorithm.makeKey(key, 256, AESalgorithm.DIR_ENCRYPT);
                    sCipher = AESalgorithm.encryptArray(encryptionTestSend.getBytes());
                } catch (Exception ex) {
                    Log.d("JIM", "SDVClient: EXCEPTION " + (398 + op) + "=" + ex.toString());
                    ex.printStackTrace();
                    return "ERR:" + (-EXCEPTION_IN_CLIENT_DURING_AES_ENCRYPTION);
                }

                // Payload request by client:
                //   0: encryted string to echo back to client
                // send encrypted string to server
                payload = new Payload.PayloadBuilder()
                        .setStandardTypeValueforNonSTSPayload()
                        .setNumberOfPayloadParameters(1)
                        .setClientId(userId)
                        .setMicroserviceName("encryptionloop")
                        .build();
                payload.initializeArguments(sCipher);
                return encodeToString(payload.serialize(), Base64.DEFAULT);
            case 3:
                // Encrypt simple name of directory and send to server

                if (remainingKeyUses == 0) return "ERR:" + (-CLIENT_HAS_NO_KEY_USES_REMAINING);
                sCipher = null;
                AESalgorithm = new Rijndael();
                try {
                    AESalgorithm.makeKey(key, 256, AESalgorithm.DIR_ENCRYPT);
                    sCipher = AESalgorithm.encryptArray(simpleDirectoryName.getBytes());
                } catch (Exception ex) {
                    Log.d("JIM", "SDVClient: EXCEPTION " + (398 + op) + "=" + ex.toString());
                    ex.printStackTrace();
                    return "ERR:" + (-EXCEPTION_IN_CLIENT_DURING_AES_ENCRYPTION);
                }

                // Payload request by client:
                //   0: simple directory name
                // read directory from server
                payload = new Payload.PayloadBuilder()
                        .setStandardTypeValueforNonSTSPayload()
                        .setNumberOfPayloadParameters(1)
                        .setClientId(userId)
                        .setMicroserviceName("showdir")
                        .build();
                payload.initializeArguments(sCipher);
                return encodeToString(payload.serialize(), Base64.DEFAULT);
            case 4:
                // Encrypt full path name of file and send to server

                if (remainingKeyUses == 0) return "ERR:" + (-CLIENT_HAS_NO_KEY_USES_REMAINING);
                sCipher = null;
                AESalgorithm = new Rijndael();
                if (readFileNameSend.charAt(0) == '.') {
                    directoryNameReceive = readFileNameSend.substring(0,3) + directoryNameReceive;
                    readFileNameSend = readFileNameSend.substring(3);
                };
                String fullFilePathName = directoryNameReceive + "/" + readFileNameSend;
                try {
                    AESalgorithm.makeKey(key, 256, AESalgorithm.DIR_ENCRYPT);
                    sCipher = AESalgorithm.encryptArray(fullFilePathName.getBytes());
                } catch (Exception ex) {
                    Log.d("JIM", "SDVClient: EXCEPTION " + (398 + op) + "=" + ex.toString());
                    ex.printStackTrace();
                    return "ERR:" + (-EXCEPTION_IN_CLIENT_DURING_AES_ENCRYPTION);
                }

                // Payload request by client:
                //   0: full path name of file to retrieve
                // retrieve file from server
                payload = new Payload.PayloadBuilder()
                        .setStandardTypeValueforNonSTSPayload()
                        .setNumberOfPayloadParameters(1)
                        .setClientId(userId)
                        .setMicroserviceName("readfile")
                        .build();
                payload.initializeArguments(sCipher);
                return encodeToString(payload.serialize(), Base64.DEFAULT);
            case 5:
                // Cheesefinder: Works similarly to "showdir". The first request is the string "ALL" (which returns the entire list of cheeses)
                // Following requests are typed in by the user.

                if (remainingKeyUses == 0) return "ERR:" + (-CLIENT_HAS_NO_KEY_USES_REMAINING);
                sCipher = null;
                AESalgorithm = new Rijndael();
                try {
                    AESalgorithm.makeKey(key, 256, AESalgorithm.DIR_ENCRYPT);
                    sCipher = AESalgorithm.encryptArray(cheeseListSend.getBytes());
                } catch (Exception ex) {
                    Log.d("JIM", "SDVClient: EXCEPTION " + (398 + op) + "=" + ex.toString());
                    ex.printStackTrace();
                    return "ERR:" + (-EXCEPTION_IN_CLIENT_DURING_AES_ENCRYPTION);
                }

                // Payload request by client:
                //   0: search pattern for all cheeses containing the pattern or else "ALL" for all cheeses
                // retrieve list of cheeses
                payload = new Payload.PayloadBuilder()
                        .setStandardTypeValueforNonSTSPayload()
                        .setNumberOfPayloadParameters(1)
                        .setClientId(userId)
                        .setMicroserviceName("cheesefinder")
                        .build();
                payload.initializeArguments(sCipher);
                return encodeToString(payload.serialize(), Base64.DEFAULT);
            case 6:
                // Encrypt a string and send to server for an encryption loop test through four microservices
                String firstName = mainActivity.baseballDBCreator.COLUMN_NAME_FIRST;
                String lastName = mainActivity.baseballDBCreator.COLUMN_NAME_LAST;
                String bats = mainActivity.baseballDBCreator.COLUMN_BATS;
                String height = mainActivity.baseballDBCreator.COLUMN_HEIGHT;
                String birthMonth = mainActivity.baseballDBCreator.COLUMN_BIRTH_MONTH;
                String weight = mainActivity.baseballDBCreator.COLUMN_WEIGHT;
                String table = mainActivity.baseballDBCreator.TABLE_MASTER;
                String selectQuery = "SELECT "+firstName+", "+lastName+" FROM "+table+" WHERE "+birthMonth+" >= 10 AND "+birthMonth+" <= 12;|" +
                                     "SELECT "+firstName+", "+lastName+" FROM "+table+" WHERE "+bats+" = 'B';|" +
                                     "SELECT "+firstName+", "+lastName+" FROM "+table+" WHERE "+height+" >= 74 AND "+weight+" >= 220;|" +
                                     "SELECT "+firstName+", "+lastName+" FROM "+table+" WHERE "+height+" <= 66;";
                if (remainingKeyUses == 0) return "ERR:" + (-CLIENT_HAS_NO_KEY_USES_REMAINING);
                sCipher = null;
                AESalgorithm = new Rijndael();
                try {
                    AESalgorithm.makeKey(key, 256, AESalgorithm.DIR_ENCRYPT);
                    sCipher = AESalgorithm.encryptArray(selectQuery.getBytes());
                } catch (Exception ex) {
                    Log.d("JIM", "SDVClient: EXCEPTION " + (398 + op) + "=" + ex.toString());
                    ex.printStackTrace();
                    return "ERR:" + (-EXCEPTION_IN_CLIENT_DURING_AES_ENCRYPTION);
                }

                // Payload request by client:
                //   0: encryted string to echo back to client
                // send 4 enquries to server
                payload = new Payload.PayloadBuilder()
                        .setStandardTypeValueforNonSTSPayload()
                        .setNumberOfPayloadParameters(1)
                        .setClientId(userId)
                        .setMicroserviceName("longencryption")
                        .build();
                payload.initializeArguments(sCipher);
                return encodeToString(payload.serialize(), Base64.DEFAULT);
            case 7:
                // Encrypt a string and send to server for a complex loop test through five microservices
                firstName = mainActivity.baseballDBCreator.COLUMN_NAME_FIRST;
                lastName = mainActivity.baseballDBCreator.COLUMN_NAME_LAST;
                bats = mainActivity.baseballDBCreator.COLUMN_BATS;
                height = mainActivity.baseballDBCreator.COLUMN_HEIGHT;
                birthMonth = mainActivity.baseballDBCreator.COLUMN_BIRTH_MONTH;
                weight = mainActivity.baseballDBCreator.COLUMN_WEIGHT;
                table = mainActivity.baseballDBCreator.TABLE_MASTER;
                selectQuery = "SELECT "+firstName+", "+lastName+" FROM "+table+" WHERE "+birthMonth+" >= 10 AND "+birthMonth+" <= 12;|" +
                              "SELECT "+firstName+", "+lastName+" FROM "+table+" WHERE "+height+" <= 66;|" +
                              "SELECT "+firstName+", "+lastName+" FROM "+table+" WHERE "+bats+" = 'B';|" +
                              "SELECT "+firstName+", "+lastName+" FROM "+table+" WHERE "+height+" <= 66;|" +
                              "SELECT "+firstName+", "+lastName+" FROM "+table+" WHERE "+height+" >= 74 AND "+weight+" >= 220;|" +
                              "SELECT "+firstName+", "+lastName+" FROM "+table+" WHERE "+bats+" = 'B';|" +
                              "SELECT "+firstName+", "+lastName+" FROM "+table+" WHERE "+height+" <= 66;";
                if (remainingKeyUses == 0) return "ERR:" + (-CLIENT_HAS_NO_KEY_USES_REMAINING);
                sCipher = null;
                AESalgorithm = new Rijndael();
                try {
                    AESalgorithm.makeKey(key, 256, AESalgorithm.DIR_ENCRYPT);
                    sCipher = AESalgorithm.encryptArray(selectQuery.getBytes());
                } catch (Exception ex) {
                    Log.d("JIM", "SDVClient: EXCEPTION " + (398 + op) + "=" + ex.toString());
                    ex.printStackTrace();
                    return "ERR:" + (-EXCEPTION_IN_CLIENT_DURING_AES_ENCRYPTION);
                }

                // Payload request by client:
                //   0: encryted string to echo back to client
                // send 7 queries to server
                payload = new Payload.PayloadBuilder()
                        .setStandardTypeValueforNonSTSPayload()
                        .setNumberOfPayloadParameters(1)
                        .setClientId(userId)
                        .setMicroserviceName("complexloop")
                        .build();
                payload.initializeArguments(sCipher);
                return encodeToString(payload.serialize(), Base64.DEFAULT);
            default:
                return "ERR:" + (-INTERNAL_ERROR_UNKNOWN_OPERATION);
        }
    }

    // STEP 5
    public void initialization() {
        hostName = mainActivity.hostName;
        button[0] = (TextView) view.findViewById(R.id.button1);
        button[1] = (TextView) view.findViewById(R.id.button2);
        button[2] = (TextView) view.findViewById(R.id.button3);
        button[3] = (TextView) view.findViewById(R.id.button4);
        button[4] = (TextView) view.findViewById(R.id.button5);
        button[5] = (TextView) view.findViewById(R.id.button6);
        button[6] = (TextView) view.findViewById(R.id.button7);
        button[7] = (TextView) view.findViewById(R.id.button8);
        tv[0] = (TextView) view.findViewById(R.id.tv1);
        tv[1] = (TextView) view.findViewById(R.id.tv2);
        tv[2] = (TextView) view.findViewById(R.id.tv3);
        tv[3] = (TextView) view.findViewById(R.id.tv4);
        tv[4] = (TextView) view.findViewById(R.id.tv5);
        tv[5] = (TextView) view.findViewById(R.id.tv6);
        tv[6] = (TextView) view.findViewById(R.id.tv7);
        tv[7] = (TextView) view.findViewById(R.id.tv8);
        et1 = (EditText) view.findViewById(R.id.et1);
        et2 = (EditText) view.findViewById(R.id.et2);
        et3 = (EditText) view.findViewById(R.id.et3);

        final int k0 = 0;
        tv[k0].setOnClickListener((View v) -> {
            tv[k0] = (TextView) view.findViewById(R.id.tv1);
            tv[k0].setVisibility(View.GONE);
        });

        final int k1 = 1;
        tv[k1].setOnClickListener((View v) -> {
            tv[k1] = (TextView) view.findViewById(R.id.tv2);
            tv[k1].setVisibility(View.GONE);
        });

        final int k2 = 2;
        tv[k2].setOnClickListener((View v) -> {
            String name = et1.getText().toString();
            //if (checkNameInList(name)) disableButton(readFileButton);
            tv[k2] = (TextView) view.findViewById(R.id.tv3);
            tv[k2].setVisibility(View.GONE);
            et1 = (EditText) view.findViewById(R.id.et1);
            et1.setVisibility(View.GONE);
        });

        final int k3 = 3; // read file
        tv[k3].setOnClickListener((View v) -> {
            tv[k3] = (TextView) view.findViewById(R.id.tv4);
            tv[k3].setVisibility(View.GONE);
        });

        final int k4 = 4;
        tv[k4].setOnClickListener((View v) -> {
            tv[k4] = (TextView) view.findViewById(R.id.tv5);
            tv[k4].setVisibility(View.GONE);
            et2 = (EditText) view.findViewById(R.id.et2);
            et2.setVisibility(View.GONE);
        });

        final int k5 = 5;
        tv[k5].setOnClickListener((View v) -> {
            tv[k5] = (TextView) view.findViewById(R.id.tv6);
            tv[k5].setVisibility(View.GONE);
        });

        final int k6 = 6;
        tv[k6].setOnClickListener((View v) -> {
            tv[k6] = (TextView) view.findViewById(R.id.tv7);
            tv[k6].setVisibility(View.GONE);
        });

        final int k7 = 7;
        tv[k7].setOnClickListener((View v) -> {
            tv[k7] = (TextView) view.findViewById(R.id.tv8);
            tv[k7].setVisibility(View.GONE);
            et3 = (EditText) view.findViewById(R.id.et3);
            et3.setVisibility(View.GONE);
        });

        disableButtons();
    }

    public boolean checkNameInList(String name) {
        boolean error = true;
        String fieldText = name.trim();
        for (String s : listOfFileNamesInDirectory) {
            if (s.equals(fieldText)) {
                error = false;
                break;
            }
        }
        et1.setTextColor(error ? ContextCompat.getColor(context, R.color.red) : ContextCompat.getColor(context, R.color.black));
        return error;
    }

    // STEP 6
    public String buttonAction(int k) {
        switch (k) {
            case 0: // STS
                operation = 1;
                return main(operation);
            case 1: // encryption loop
                operation = 2;
                return main(operation);
            case 2: // show directory
                operation = 3;
                simpleDirectoryName = mainActivity.packageName;
                return main(operation);
            case 3: // read file

                // TIME VALUE T0

                readFileNameSend = et1.getText().toString();
                operation = 4;
                return main(operation);
            case 4: // search cheeses
                cheeseListSend = et2.getText().toString();
                if (cheeseListSend == null) cheeseListSend = "ALL";
                if (cheeseListSend.length() == 0) cheeseListSend = "ALL";
                operation = 5;
                return main(operation);
            case 5: // long encryptiion

                // TIME VALUE T0

                operation = 6;
                return main(operation);
            case 6: // complex loops

                // TIME VALUE T0

                operation = 7;
                return main(operation);
            case 7: // ms magmt
                msNameSend = et3.getText().toString();
                if (msNameSend == null) msNameSend = "START";
                if (msNameSend.length() == 0) msNameSend = "START";
                operation = 8;
                return main(operation);
            default: // case 100, read file (during testing only)
                switch (userId) {
                    case 0: case 1:
                        readFileNameSend = "persons.db";
                        break;
                    case 2: case 3:
                        readFileNameSend = ".1.persons.db";
                        break;
                }
                operation = 4;
                return main(operation);
        }
    }

    private String STSEstablishKeyPartTwo(Payload responsePayload) {
        // String returned is either a JSON representation of a Payload or
        //   is a final response error string of the form "ERR:<Integer value>" (representing -<Integer value>) or
        //   is a final response succeess string of the form "SUC:<Integer Value>" (representing <Integer value>)
        if (responsePayload.getType() < 0) return "ERR:" + (-responsePayload.getType());

        // second request of two in station-to-station protocol request by client

        // responsePayload response from server:
        //   0: universal password hash (signed and encrypted)
        //   1: server public key (signed and encrypted)
        //   2: client public key (signed and encrypted, this is an echo that the client originally sent to the server in the first request)
        //   3: server public key (plaintext)
        //   4: server RSA public key e part (plaintext)
        //   5: server RSA public key N part (plaintext)

        clientIdentifier = responsePayload.getClientId();
        assertEquals(clientIdentifier, userId);

        // compute shared secret key K = (g^y mod p)^x mod p
        BigInteger publicKeyAsBigInteger = new BigInteger(responsePayload.getPayload(3));
        sharedSecretKey = publicKeyAsBigInteger.modPow(initialSecret, DHConstants.P);
        key1024 = sharedSecretKey.toByteArray();
        key = SHA256.sha256(key1024);

        // decrypts the signature with secret key K
        byte[] pw = null;
        byte[] remotePublicKey = null;
        byte[] localPublicKey = null;
        AESalgorithm = new Rijndael();
        try {
            AESalgorithm.makeKey(key, 256, AESalgorithm.DIR_DECRYPT);
            pw = AESalgorithm.decryptArray(responsePayload.getPayload(0));
            remotePublicKey = AESalgorithm.decryptArray(responsePayload.getPayload(1));
            localPublicKey = AESalgorithm.decryptArray(responsePayload.getPayload(2));
        } catch (Exception ex) {
            Log.d("JIM", "SDVClient: EXCEPTION 450=" + ex.toString());
            ex.printStackTrace();
            return "ERR:" + (-EXCEPTION_IN_CLIENT_DURING_AES_DECRYPTION);
        }

        // read signature using asymmetric public key
        remoteRSA = new RSA();
        remoteRSA.setPublicKeyE(responsePayload.getPayload(4));
        remoteRSA.setPublicKeyN(responsePayload.getPayload(5));
        pw = remoteRSA.readSignature(pw);
        remotePublicKey = remoteRSA.readSignature(remotePublicKey);
        localPublicKey = remoteRSA.readSignature(localPublicKey);

        // verify:
        //   client side              server side
        //   ---------------------------------------
        //   universalPasswordHash == pw
        //   publicKey             == localPublicKey
        //   remotePublicKey       == responsePayload[3]
        // verify string recieved from server is (univeralPasswordHash, g^y mod p, g^x mod p)
        boolean verify1 = Utilities.compareArrays(universalPasswordHash, pw);
        boolean verify2 = Utilities.compareArrays(publicKey.toByteArray(), localPublicKey);
        boolean verify3 = Utilities.compareArrays(remotePublicKey, responsePayload.getPayload(3));
        boolean verify = verify1 && verify2 && verify3;
        if (!verify) return "ERR:" + (-CLIENT_VERIFICATIION_OF_STS_FAILED);

        // sign the universal password hash and the DH "Public Keys" (univeralPasswordHash, g^y mod p, g^x mod p) (order is important)
        byte[] pwSignature = rsa.applySignature(universalPasswordHash);
        byte[] localPublicKeySignature = rsa.applySignature(publicKey.toByteArray());
        byte[] remotePublicKeySignature = rsa.applySignature(responsePayload.getPayload(3));

        byte[] pwCipher = null;
        byte[] localPublicKeyCipher = null;
        byte[] remotePublicKeyCipher = null;
        AESalgorithm.makeKey(key, 256, AESalgorithm.DIR_ENCRYPT);
        try {
            pwCipher = AESalgorithm.encryptArray(pwSignature);
            localPublicKeyCipher = AESalgorithm.encryptArray(localPublicKeySignature);
            remotePublicKeyCipher = AESalgorithm.encryptArray(remotePublicKeySignature);
        } catch (Exception ex) {
            Log.d("JIM", "SDVClient: EXCEPTION 451=" + ex.toString());
            ex.printStackTrace();
            return "ERR:" + (-EXCEPTION_IN_CLIENT_DURING_AES_ENCRYPTION);
        }

        // responsePayload request to server:
        //   0: universal password hash (signed and encrypted)
        //   1: client public key (signed and encrypted)
        //   2: server public key (signed and encrypted, this is an echo that the server originally sent to the client as response to the first request)
        //   3: client RSA public key e part (plaintext)
        //   4: client RSA public key N part (plaintext)
        // type=2 is the second station-to-station request
        Payload payload = new Payload.PayloadBuilder()
                .setNumberOfPayloadParameters(5)
                .setClientId(userId)
                .setPayloadType(2)
                .setEmptyMicroserviceName()
                .build();

        payload.setPayload(0, pwCipher);
        payload.setPayload(1, localPublicKeyCipher);
        payload.setPayload(2, remotePublicKeyCipher);
        payload.setPayload(3, rsa.getPublicKeyE());
        payload.setPayload(4, rsa.getPublicKeyN());
        return encodeToString(payload.serialize(), Base64.DEFAULT);
    }

    private String finalEncryptionLoopProcessor(Payload responsePayload) {
        if (responsePayload.getType() < 0) return "ERR:" + (-responsePayload.getType());
        byte[] sPlain = null;
        try {
            AESalgorithm.makeKey(key, 256, AESalgorithm.DIR_DECRYPT);
            sPlain = AESalgorithm.decryptArray(responsePayload.getPayload(0));
            encryptionTestReceive = new String(sPlain);
        } catch (Exception ex) {
            Log.d("JIM", "SDVClient: EXCEPTION 452=" + ex.toString());
            ex.printStackTrace();
            return "ERR:" + (-EXCEPTION_IN_CLIENT_DURING_AES_DECRYPTION);
        }
        remainingKeyUses -= 1;
        return "SUC:101"; // normal return for main operation 2 with no errors
    }

    private String finalReadDirectoryProcessor(Payload responsePayload) {
        // Payload response
        //   0: filename|filename|...|filename
        //   1: full directory path name
        if (responsePayload.getType() < 0) return "ERR:" + (-responsePayload.getType());
        byte[] sPlain = null;
        byte[] dPlain = null;
        try {
            AESalgorithm.makeKey(key, 256, AESalgorithm.DIR_DECRYPT);
            sPlain = AESalgorithm.decryptArray(responsePayload.getPayload(0));
            dPlain = AESalgorithm.decryptArray(responsePayload.getPayload(1));
            readDirectoryReceive = new String(sPlain);
            directoryNameReceive = new String(dPlain);
        } catch (Exception ex) {
            Log.d("JIM", "SDVClient: EXCEPTION 453=" + ex.toString());
            ex.printStackTrace();
            return "ERR:" + (-EXCEPTION_IN_CLIENT_DURING_AES_DECRYPTION);
        }
        remainingKeyUses -= 1;
        return "SUC:102"; // normal return for main operation 3 with no errors
    }

    private String finalReadFileProcessor(Payload responsePayload) {
        if (responsePayload.getType() < 0) return "ERR:" + (-responsePayload.getType());
        byte[] sPlain = null;
        byte[] dPlain = null;
        try {
            AESalgorithm = new Rijndael();
            AESalgorithm.makeKey(key, 256, AESalgorithm.DIR_DECRYPT);
            sPlain = AESalgorithm.decryptArray(responsePayload.getPayload(0));
            readFileReceive = new SDVFile(sPlain.length);
            readFileReceive.setFileAsBytes(sPlain);
            dPlain = AESalgorithm.decryptArray(responsePayload.getPayload(1));
            readFileNameReceive = new String(dPlain);
        } catch (Exception ex) {
            Log.d("JIM", "SDVClient: EXCEPTION 454=" + ex.toString());
            ex.printStackTrace();
            return "ERR:" + (-EXCEPTION_IN_CLIENT_DURING_AES_DECRYPTION);
        }
        remainingKeyUses -= 1;
        return "SUC:103"; // normal return for main operation 4 with no errors
    }

    private String finalCheeseFinderProcessor(Payload responsePayload) {
        if (responsePayload.getType() < 0) return "ERR:" + (-responsePayload.getType());
        byte[] sPlain = null;
        try {
            AESalgorithm = new Rijndael();
            AESalgorithm.makeKey(key, 256, AESalgorithm.DIR_DECRYPT);
            sPlain = AESalgorithm.decryptArray(responsePayload.getPayload(0));
            cheeseListReceive = new String(sPlain);
        } catch (Exception ex) {
            Log.d("JIM", "SDVClient: EXCEPTION 455=" + ex.toString());
            ex.printStackTrace();
            return "ERR:" + (-EXCEPTION_IN_CLIENT_DURING_AES_DECRYPTION);
        }
        remainingKeyUses -= 1;
        return "SUC:104"; // normal return for main operation 5 with no errors
    }

    private String finalLongEncryptionProcessor(Payload responsePayload) {
        if (responsePayload.getType() < 0) return "ERR:" + (-responsePayload.getType());
        byte[] sPlain = null;
        longEncryptionReceive = new ArrayList<>();
        try {
            AESalgorithm = new Rijndael();
            AESalgorithm.makeKey(key, 256, AESalgorithm.DIR_DECRYPT);
            for (int k = 1; k < responsePayload.getPayloadLength(); k++) {
                sPlain = AESalgorithm.decryptArray(responsePayload.getPayload(k));
                longEncryptionReceive.add(new String(sPlain));
            }
        } catch (Exception ex) {
            Log.d("JIM", "SDVClient: EXCEPTION 456=" + ex.toString());
            ex.printStackTrace();
            return "ERR:" + (-EXCEPTION_IN_CLIENT_DURING_AES_DECRYPTION);
        }
        remainingKeyUses -= 1;
        return "SUC:105"; // normal return for main operation 5 with no errors
    }

    private String finalComplexLoopProcessor(Payload responsePayload) {
        if (responsePayload.getType() < 0) return "ERR:" + (-responsePayload.getType());
        byte[] sPlain = null;
        complexLoopReceive = new ArrayList<>();
        try {
            AESalgorithm = new Rijndael();
            AESalgorithm.makeKey(key, 256, AESalgorithm.DIR_DECRYPT);
            for (int k = 1; k < responsePayload.getPayloadLength(); k++) {
                sPlain = AESalgorithm.decryptArray(responsePayload.getPayload(k));
                complexLoopReceive.add(new String(sPlain));
            }
        } catch (Exception ex) {
            Log.d("JIM", "SDVClient: EXCEPTION 457=" + ex.toString());
            ex.printStackTrace();
            return "ERR:" + (-EXCEPTION_IN_CLIENT_DURING_AES_DECRYPTION);
        }
        remainingKeyUses -= 1;
        return "SUC:106"; // normal return for main operation 6 with no errors
    }
    
    private String finalMSRestartProcessor(Payload responsePayload) {
        if (responsePayload.getType() < 0) return "ERR:" + (-responsePayload.getType());
        byte[] sPlain = null;
        try {
            AESalgorithm = new Rijndael();
            AESalgorithm.makeKey(key, 256, AESalgorithm.DIR_DECRYPT);
            sPlain = AESalgorithm.decryptArray(responsePayload.getPayload(0));
            msNameReceive = new String(sPlain);
        } catch (Exception ex) {
            Log.d("JIM", "SDVClient: EXCEPTION 458=" + ex.toString());
            ex.printStackTrace();
            return "ERR:" + (-EXCEPTION_IN_CLIENT_DURING_AES_DECRYPTION);
        }
        remainingKeyUses -= 1;
        return "SUC:107"; // normal return for main operation 7 with no errors
    }

    // STEP 8 is inserted here

    // STEP 9
    private String clientFinalProcessor(int k, Payload responsePayload) {
        switch (k) {
            case 1:
                return finalEncryptionLoopProcessor(responsePayload);
            case 2:
                return finalReadDirectoryProcessor(responsePayload);
            case 3:
                return finalReadFileProcessor(responsePayload);
            case 4:
                return finalCheeseFinderProcessor(responsePayload);
            case 5:
                return finalLongEncryptionProcessor(responsePayload);
            case 6:
                return finalComplexLoopProcessor(responsePayload);
            case 7:
                return finalMSRestartProcessor(responsePayload);
            default:
                return "ERR:" + (-INTERNAL_ERROR_UNKNOWN_OPERATION);
        }
    }

    private void setTextColor(int n) {
        boolean error = (n < 0);
        if (n < 0) n = -n - 1;
        if (testingInProgress) {
            if (error)
                tv[n].setTextColor(ContextCompat.getColor(context, R.color.red));
            else if (colorToggle[n] == 0) {
                colorToggle[n] = 1 - colorToggle[n];
                tv[n].setTextColor(ContextCompat.getColor(context, R.color.yellow));
            } else {
                colorToggle[n] = 1 - colorToggle[n];
                tv[n].setTextColor(ContextCompat.getColor(context, R.color.green));
            }
            tv[n].setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            tv[n].setGravity(Gravity.CENTER);
        } else {
            if (error)
                tv[n].setTextColor(ContextCompat.getColor(context, R.color.red));
            else
                tv[n].setTextColor(ContextCompat.getColor(context, R.color.white));
        }
    }

    public void runTest(final MainActivity mainActivity, final int waitTime) {
        for (int k = 0; k < numberOfButtons; k++) {
            button[k].setEnabled(false);
            button[k].setClickable(false);
            et1.setEnabled(false);
            et1.setClickable(false);
            button[k].setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            button[k].setPadding(5, 5, 5, 5);
            colorToggle[k] = 0;
        }
        Thread testThread = new Thread() {
            public void run() {
                test(mainActivity, waitTime);
            }
        };
        testThread.start();
    }

    private Observable<String> createTestObservable(int k) {
        return Observable.create(emitter -> {
            emitter.onNext(buttonAction(k));
            emitter.onComplete();
        });
    }

    private void test(MainActivity mainActivity, int waitTime) {
        try { Thread.sleep(waitTime*1000); } catch (Exception e) {  }
        ArrayList<Observable<String>> testObservable = new ArrayList<>(numberOfButtons);
        final int TEST_MAX = numberOfButtons-1;
        mainActivity.runOnUiThread(new Runnable() {
            public void run() {
                button[TEST_MAX].setVisibility(View.GONE);
                tv[TEST_MAX].setVisibility(View.GONE);
                et3.setVisibility(View.GONE);
            }
        });
        for (int k = 0; k < TEST_MAX; k++) testObservable.add(null);
        testingInProgress = true;
        boolean directoryHasBeenRead = false;
        returnValueFromOperation = CLIENT_HAS_NO_KEY_USES_REMAINING;
        while (true) {
            completed = false;
            int randomButton;
            if (clientIdentifier < 0 /* keys have expired */ || returnValueFromOperation == CLIENT_HAS_NO_KEY_USES_REMAINING)
                randomButton = 0;
            else {
                randomButton = ThreadLocalRandom.current().nextInt(0, TEST_MAX); // 0 ... TEST_MAX-1
                if (randomButton == readFileButton) randomButton = testingReadFileButton;
            }

            // read directory before reading a file
            if (randomButton == testingReadFileButton && !directoryHasBeenRead)
                randomButton = showDirButton;
            final int selectedButton = (randomButton == testingReadFileButton ? readFileButton : randomButton);
            final int rb = randomButton;

            // at this point:
            //   randomButton == 0, 1, 2, ... TEST_MAX-1 or testingReadFileButton
            //   selectedButton = 0, 1, 2, ... TEST_MAX-1

            mainActivity.runOnUiThread(new Runnable() {
                public void run() {
                    disableButtons();
                    testObservable.set(selectedButton, createTestObservable(rb));
                    intializeObservable(selectedButton, testObservable);
                }
            });
            while (!completed) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
            }

            // if STS fails, stop the client
            if (randomButton == 0 && returnValueFromOperation < 0) break;

            // if this is the first read of a directory, ensure that no error occurred during the read of the directory
            if (randomButton == 2 && !directoryHasBeenRead && returnValueFromOperation > 0)
                directoryHasBeenRead = true;

            int randomWait = ThreadLocalRandom.current().nextInt(0, 4); // 0 ... 3
            try {
                Thread.sleep(1000 * randomWait);
            } catch (Exception e) {
            }
        }
    }

    private void disableButtons() {
        if (testingInProgress)
            for (int k = 0; k < numberOfButtons; k++)
                button[k].setTextColor(ContextCompat.getColor(context, R.color.white));
        else {
            for (int k = 0; k < numberOfButtons; k++) {
                button[k].setEnabled(false);
                button[k].setTextColor(ContextCompat.getColor(context, R.color.blue));
            }
        }
    }

    private void enableButtons() {
        if (testingInProgress) return;
        for (int k = 0; k < numberOfButtons; k++) {
            button[k].setEnabled(true);
            button[k].setTextColor(ContextCompat.getColor(context, R.color.white));
        }
    }

    private void disableButton(int k) {
        if (testingInProgress)
            button[k].setTextColor(ContextCompat.getColor(context, R.color.white));
        else {
            button[k].setEnabled(false);
            button[k].setTextColor(ContextCompat.getColor(context, R.color.blue));
        }
    }

    public void enableButton(int k) {
        if (testingInProgress) return;
        button[k].setEnabled(true);
        button[k].setTextColor(ContextCompat.getColor(context, R.color.white));
    }

    public String processReturnValue(String evonrv) {
        int errorValueOrNormalReturnValue;
        if (evonrv.startsWith("ERR:")) {
            errorValueOrNormalReturnValue = -Integer.parseInt(evonrv.substring(4, 6));
            if (evonrv.length() > 6) errorMicroserviceName = evonrv.substring(7);
        } else if (evonrv.startsWith("SUC:"))
            errorValueOrNormalReturnValue = Integer.parseInt(evonrv.substring(4));
        else
            errorValueOrNormalReturnValue = INTERNAL_ERROR_UNKNOWN_OPERATION;
        switch (operation) {
            case 1: // return from main operation 1, STS protocol to obtain new encryption key
                tv[0] = (TextView) view.findViewById(R.id.tv1);
                tv[0].setVisibility(View.VISIBLE);
                tv[0].setText(messageGetter(errorValueOrNormalReturnValue));
                if (errorValueOrNormalReturnValue < 0) {
                    // error
                    setTextColor(-1);
                    disableButtons();
                    enableButton(0);
                } else {
                    setTextColor(0);
                    enableButtons();
                    disableButton(readFileButton);
                }
                break;
            case 2: // return from main operation 2, encryption loop
                tv[operation-1] = (TextView) view.findViewById(R.id.tv2);
                tv[operation-1].setVisibility(View.VISIBLE);
                tv[operation-1].setText(messageGetter(errorValueOrNormalReturnValue));
                if (errorValueOrNormalReturnValue < 0)
                    setTextColor(-operation);
                else
                    setTextColor(operation-1);
                returnPostProcessor(operation);
                break;
            case 3: // return from main operation 3, read directory
                tv[operation-1] = (TextView) view.findViewById(R.id.tv3);
                tv[operation-1].setVisibility(View.VISIBLE);
                if (errorValueOrNormalReturnValue < 0) { // errors occurred
                    setTextColor(-operation);
                    tv[operation-1].setText(messageGetter(errorValueOrNormalReturnValue));
                    et1.setVisibility(View.GONE);
                } else {
                    setTextColor(operation-1);
                    setWatcherAndEditText(et1, readFileButton, new checkFileNameInList());
                    if (testingInProgress) et1.setVisibility(View.GONE);
                    String[] s = readDirectoryReceive.split("[|]");
                    listOfDetailsInDirectory = new ArrayList<>(s.length);
                    listOfFileNamesInDirectory = new ArrayList<>(s.length);
                    if (!readDirectoryReceive.equals("<empty directory>")) {
                        for (String fName : s) {
                            listOfDetailsInDirectory.add(fName);
                            int m = fName.indexOf('(');
                            String name = fName.substring(0, m);
                            listOfFileNamesInDirectory.add(name);
                        }
                    }
                    tv[operation-1].setText(messageGetter(errorValueOrNormalReturnValue));
                }
                returnPostProcessor(operation);
                break;
            case 4: // return from main operation 4, read file
                tv[operation-1] = (TextView) view.findViewById(R.id.tv4);
                tv[operation-1].setVisibility(View.VISIBLE);
                tv[operation-1].setText(messageGetter(errorValueOrNormalReturnValue));
                if (errorValueOrNormalReturnValue < 0)
                    setTextColor(-operation);
                else {
                    setTextColor(operation-1);
                }
                if (remainingKeyUses > 0)
                    enableButtons();
                else {
                    et1.setVisibility(View.GONE);
                    et2.setVisibility(View.GONE);
                    et3.setVisibility(View.GONE);
                }
                returnPostProcessor(operation);
                break;
            case 5: // return from main operation 5, cheese finder
                tv[operation-1] = (TextView) view.findViewById(R.id.tv5);
                tv[operation-1].setVisibility(View.VISIBLE);
                if (errorValueOrNormalReturnValue < 0) { // errors occurred
                    setTextColor(-operation);
                    tv[operation-1].setText(messageGetter(errorValueOrNormalReturnValue));
                    et2.setVisibility(View.GONE);
                } else {
                    setTextColor(operation-1);
                    if (remainingKeyUses > 0) {
                        et2.setVisibility(View.VISIBLE);
                        et2.setText("");
                    } else {
                        et1.setVisibility(View.GONE);
                        et2.setVisibility(View.GONE);
                        et3.setVisibility(View.GONE);
                    }
                    if (testingInProgress) et2.setVisibility(View.GONE);
                    tv[operation-1].setText(messageGetter(errorValueOrNormalReturnValue));
                }
                returnPostProcessor(operation);
                break;
            case 6: // return from main operation 6, long encryption loop
                tv[operation-1] = (TextView) view.findViewById(R.id.tv6);
                tv[operation-1].setVisibility(View.VISIBLE);
                tv[operation-1].setText(messageGetter(errorValueOrNormalReturnValue));
                if (errorValueOrNormalReturnValue < 0)
                    setTextColor(-operation);
                else
                    setTextColor(operation-1);
                returnPostProcessor(operation);
                break;
            case 7: // return from main operation 7, complex loop
                tv[operation-1] = (TextView) view.findViewById(R.id.tv7);
                tv[operation-1].setVisibility(View.VISIBLE);
                tv[operation-1].setText(messageGetter(errorValueOrNormalReturnValue));
                if (errorValueOrNormalReturnValue < 0)
                    setTextColor(-operation);
                else
                    setTextColor(operation-1);
                returnPostProcessor(operation);
                break;
            case 8: // return from main operation 8, e-commerce store
                tv[operation-1] = (TextView) view.findViewById(R.id.tv8);
                tv[operation-1].setVisibility(View.VISIBLE);
                if (errorValueOrNormalReturnValue < 0) { // errors occurred
                    setTextColor(-operation);
                    tv[operation-1].setText(messageGetter(errorValueOrNormalReturnValue));
                    et3.setVisibility(View.GONE);
                } else {
                    setTextColor(operation-1);
                    if (remainingKeyUses > 0) {
                        et3.setVisibility(View.VISIBLE);
                        et3.setText("");
                    } else {
                        et3.setVisibility(View.GONE);
                        et2.setVisibility(View.GONE);
                        et3.setVisibility(View.GONE);
                    }
                    if (testingInProgress) et3.setVisibility(View.GONE);
                    String[] s = msNameReceive.split("[|]");
                    listOfMSNames = new ArrayList<>(s.length);
                    if (!msNameReceive.equals("<empty list>"))
                        for (String cName : s) listOfMSNames.add(cName);
                    tv[operation-1].setText(messageGetter(errorValueOrNormalReturnValue));
                }
                returnPostProcessor(operation);
                break;
            // STEP 10 goes here
        }
        completed = true;
        return "PEM RETURN";
    }

    private interface checkTypedEntry {
        boolean checkEntry(String name, EditText et);
    }

    private class checkFileNameInList implements checkTypedEntry {
        public boolean checkEntry(String name, EditText et) {
            // file is either
            //    .D.<name>
            //       or
            //    <name>
            // where D is 0,1,2, or 3, but not userId
            //       <name> is an existing file name
            if (name.length() == 0) return true;
            boolean error = true;
            String fieldText = name.trim();
            if (fieldText.charAt(0) == '.') {
                if (fieldText.length() < 4) return true;
                int targetClient = fieldText.charAt(1) - '0';
                fieldText = fieldText.substring(3);
                if (targetClient < 0 || targetClient > 3 || targetClient == userId) return true;
            }
            for (String s : listOfFileNamesInDirectory) {
                if (s.equals(fieldText)) {
                    error = false;
                    break;
                }
            }
            et.setTextColor(error ? ContextCompat.getColor(context, R.color.red) : ContextCompat.getColor(context, R.color.black));
            return error;
        }
    }

    public void setWatcherAndEditText(final EditText et, int buttonNumber, checkTypedEntry cte) {
        if (remainingKeyUses > 0) {
            et.setVisibility(View.VISIBLE);
            et.setText("");
            TextWatcher watcherFileName = (new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    boolean error =  cte.checkEntry(et.getText().toString(), et);
                    if (error)
                        disableButton(buttonNumber);
                    else
                        enableButton(buttonNumber);
                }

                @Override
                public void onTextChanged(CharSequence a, int b, int c, int d) {
                }

                @Override
                public void beforeTextChanged(CharSequence a, int b, int c, int d) {
                }
            });
            et.addTextChangedListener(watcherFileName);
        } else
            et.setVisibility(View.GONE);
    }

    public void returnPostProcessor(int op) {
        if (clientIdentifier < 0) {
            button[0].setText("STS Protocol [?]");
            disableButtons();
            enableButton(0);
        } else {
            if (remainingKeyUses > 0) enableButtons();
            enableButton(0);
            if (op != 3) disableButton(readFileButton);
        }
    }

    private String messageGetter(int k) {
        returnValueFromOperation = k;
        String r = "UNKNOWN ERROR";
        switch(k) {
            case 100: // normal return from main operation 1
                if (testingInProgress)
                    r = "Key Exchange OK";
                else
                    r = "STS Successful\n2048-bit RSA\n256-bit AES\nAuthenticate: PW Hash";
                break;
            case 101: // normal return from main operation 2
                if (testingInProgress)
                    r = "Encryption Loop OK";
                else
                    r = "Encryption Loop Successful\n\nSENT:\n" + encryptionTestSend + "\n=============\n\nRECD:\n" + encryptionTestReceive;
                break;
            case 102: // normal return from main operation 3
                StringBuffer sb = new StringBuffer();
                sb.append("Read Directory\n"+ directoryNameReceive +"\n================\n");
                int n = listOfDetailsInDirectory.size();
                for (int i = 0; i < n; i++) {
                    sb.append(listOfDetailsInDirectory.get(i));
                    if (i < n-1) sb.append('\n');
                }
                if (testingInProgress)
                    r = "Show Directory OK";
                else
                    r = sb.toString();
                break;
            case 103: // normal return from main operation 4
                if (testingInProgress)
                    r = "Retrieve File OK";
                else {
                    if (readFileReceive.getLength() == 0)
                        r = "File Authorization Successful:\n" + readFileNameReceive;
                    else
                        r = "File Retrieve Successful:\n" + readFileNameReceive + " (" + readFileReceive.getLength() + " bytes)";
                }
                break;
            case 104: // normal return from main operation 5
                String[] s = cheeseListReceive.split("[|]");
                sb = new StringBuffer();
                n = s.length;
                if (s[0].equals("<empty list>"))
                    sb.append("Cheese Search\nNo Cheeses Found\n" + "================\n");
                else if (n == 1)
                    sb.append("Cheese Search\n1 Cheese Found\n" +"===============\n");
                else if (n < 10)
                    sb.append("Cheese Search\n"+ n + " Cheeses Found\n" +"===============\n");
                else
                    sb.append("Cheese Search\n"+ n + " Cheeses Found\n" +"================\n");
                for (int i = 0; i < (n > 20 ? 20 : n); i++) {
                    sb.append(s[i]);
                    if (i < n-1) sb.append('\n');
                }
                if (n > 20) sb.append("\nAND " + (n-20) + " MORE ...\n");
                if (testingInProgress)
                    r = "Cheese Search OK";
                else
                    r = sb.toString();
                break;
            case 105: // normal return from main operation 6
                if (testingInProgress)
                    r = "Long Encryption OK\n";
                else {
                    r = "Long Encryption Results\n======================\n\n";
                    for (int p = 0; p < longEncryptionReceive.size(); p++) {
                        s = longEncryptionReceive.get(p).split("[|]");
                        sb = new StringBuffer();
                        n = s.length;
                        if (p == 0)
                            sb.append("Short Player Search\n" + n + " Short Players Found\n" + "================\n");
                        else if (p == 1)
                            sb.append("Big Player Search\n" + n + " Big Players Found\n" + "================\n");                        
                        else if (p == 2)
                            sb.append("Switch-Hitter Search\n" + n + " Switch Hitters Found\n" + "================\n");
                        else
                            sb.append("Born Q3 Search\n" + n + " Born Q3 Players Found\n" + "================\n");
                        for (int i = 0; i < (n > 20 ? 20 : n); i++) {
                            sb.append(s[i]);
                            if (i < n - 1) sb.append('\n');
                        }
                        if (n > 20) {
                            if (p == longEncryptionReceive.size()-1)
                                sb.append("\nAND " + (n - 20) + " MORE ...");
                            else
                                sb.append("\nAND " + (n - 20) + " MORE ...\n\n");
                        }
                        r += sb.toString();
                    }
                }
                break;
            case 106: // normal return from main operation 7
                if (testingInProgress)
                    r = "Complex Loop OK\n";
                else {
                    r = "Complex Loop Results\n======================\n\n";
                    for (int p = 0; p < complexLoopReceive.size(); p++) {
                        s = complexLoopReceive.get(p).split("[|]");
                        sb = new StringBuffer();
                        n = s.length;
                        if (p == 0)
                            sb.append("Short Player Search\n" + n + " Short Players Found\n" + "================\n");
                        else if (p == 1)
                            sb.append("Switch-Hitter Search\n" + n + " Switch Hitters Found\n" + "================\n");
                        else if (p == 2)
                            sb.append("Big Player Search\n" + n + " Big Players Found\n" + "================\n");
                        else if (p == 3)
                            sb.append("Short Player Search\n" + n + " Short Players Found\n" + "================\n");
                        else if (p == 4)
                            sb.append("Switch-Hitter Search\n" + n + " Switch Hitters Found\n" + "================\n");
                        else if (p == 5)
                            sb.append("Short Player Search\n" + n + " Short Players Found\n" + "================\n");
                        else
                            sb.append("Born Q3 Search\n" + n + " Born Q3 Players Found\n" + "================\n");
                        for (int i = 0; i < (n > 20 ? 20 : n); i++) {
                            sb.append(s[i]);
                            if (i < n - 1) sb.append('\n');
                        }
                        if (n > 20) {
                            if (p == complexLoopReceive.size()-1)
                                sb.append("\nAND " + (n - 20) + " MORE ...");
                            else
                                sb.append("\nAND " + (n - 20) + " MORE ...\n\n");
                        }
                        r += sb.toString();
                    }
                }
                break;
            case 107: // normal return from main operation 8
                sb = new StringBuffer();
                n = listOfMSNames.size();
                sb.append("Microservice Mgmt\n"+ n + " Microservices Found\n" +"================\n");
                for (int i = 0; i < n; i++) {
                    sb.append(listOfMSNames.get(i));
                    if (i < n-1) sb.append('\n');
                }
                if (testingInProgress)
                    r = "Microservice Restart OK";
                else
                    r = sb.toString();
                break;
                // STEP 11 goes here
            case EXCEPTION_READING_REQUEST_FOR_SERVICE:
                r = "EXCEPTION READING REQUEST FOR SERVICE";
                break;
            case CALLER_COULD_NOT_CONNECT_TO_MICROSERVICE:
                r = "CALLER COULD NOT CONNECT TO MICROSERVICE";
                break;
            case SERVER_VERIFICATIION_OF_STS_FAILED:
                r = "SERVER VERIFICATIION OF STS FAILED";
                break;
            case SERVER_INTERNAL_ERROR_NOT_ABLE_TO_ASSIGN_ID:
                r = "SERVER INTERNAL ERROR NOT ABLE TO ASSIGN ID";
                break;
            case IMPROPER_EXPECTED_ARGUMENT_IN_REQUEST:
                r = "IMPROPER EXPECTED ARGUMENT IN REQUEST";
                break;
            case CLIENT_HAS_NO_KEY_USES_REMAINING:
                if (testingInProgress)
                    r = "NEED NEW KEYS";
                else
                    r = "CLIENT HAS NO KEY USES REMAINING";
                break;
            case SERVER_INTERNAL_ERROR_IMPOSSIBLE_VALUE_FOR_TYPE_EXPECTED:
                r = "SERVER INTERNAL ERROR IMPOSSIBLE VALUE FOR TYPE EXPECTED";
                break;
            case EXCEPTION_IN_SERVER_DURING_AES_ENCRYPTION:
                r = "EXCEPTION IN SERVER DURING AES ENCRYPTION";
                break;
            case SERVER_INTERNAL_ERROR_ID_NOT_FOUND:
                r = "SERVER INTERNAL ERROR ID NOT FOUND";
                break;
            case ENCRYPTION_KEYS_HAVE_EXPIRED:
                if (testingInProgress)
                    r = "KEYS EXPIRED";
                else
                    r = "ENCRYPTION KEYS HAVE EXPIRED";
                clientIdentifier = -(userId + 1);
                break;
            case CLIENT_VERIFICATIION_OF_STS_FAILED:
                r = "CLIENT VERIFICATIION OF STS FAILED";
                break;
            case EXCEPTION_IN_SERVER_DURING_AES_DECRYPTION:
                r = "EXCEPTION IN SERVER DURING AES DECRYPTION";
                break;
            case EXCEPTION_IN_CLIENT_DURING_AES_ENCRYPTION:
                r = "EXCEPTION IN CLIENT DURING AES ENCRYPTION";
                break;
            case EXCEPTION_IN_CLIENT_DURING_AES_DECRYPTION:
                r = "EXCEPTION IN CLIENT DURING AES DECRYPTION";
                break;
            case INTERNAL_ERROR_UNKNOWN_OPERATION:
                r = "INTERNAL ERROR UNKNOWN OPERATION";
                break;
            case CLIENT_COULD_NOT_CONNECT_TO_SERVER:
                r = "CLIENT COULD NOT CONNECT TO SERVER";
                break;
            case CLIENT_TIMED_OUT_WHILE_WAITING_FOR_SERVER:
                r = "CLIENT TIMED OUT WHILE WAITING FOR SERVER";
                break;
            case UNABLE_TO_FIND_MICROSERVICE:
                if (errorMicroserviceName == null)
                    r = "UNABLE TO FIND MICROSERVICE";
                else
                    r = errorMicroserviceName + " UNABLE TO FIND MICROSERVICE";
                break;
            case EXCEPTION_WRITING_REQUEST_FOR_SERVICE:
                r = "EXCEPTION WRITING REQUEST FOR SERVICE";
                break;
            case MICROSERVICE_RETURNED_ERROR_CONDITION:
                if (errorMicroserviceName == null)
                    r = "MICROSERVICE RETURNED ERROR CONDITION";
                else
                    r = errorMicroserviceName + " MICROSERVICE RETURNED ERROR CONDITION";
                break;
            case CANNOT_GRANT_ACCESS_TO_FILE:
                r = "CANNOT GRANT ACCESS TO FILE";
                break;
            case ERROR_IN_DATABASE_OPERATION:
                r = "ERROR IN DATABASE OPERATION";
                break;
        }
        return (k < 0 ? "ERROR: "+r : r);
    }

}
