package com.fullsecurity.microservices;

import android.util.Base64;

import com.fullsecurity.common.Payload;
import com.fullsecurity.server.MSSettings;
import com.fullsecurity.server.ServerState;
import com.fullsecurity.shared.MainActivity;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@SuppressWarnings("all")
public class SDVRequestProcessorRemoteStore extends MSCommunicationsWrapper {

    private static final String nameUsedForOutputToLogger = "remoteStoreMicroservice: ";

    private BitSet portAssignments;
    private Semaphore portSemaphore;
    private ArrayList<ServerState> serverStateList;
    private Semaphore sslSemaphore;
    private ArrayList<MSSettings> msMap;
    private MainActivity mainActivity;
    private int port;

    private byte[] incoming;
    private int clientId;
    SDVRequestProcessorCategories ms;
    int msIndex;

    public SDVRequestProcessorRemoteStore(BitSet portAssignments, ArrayList<ServerState> serverStateList, ArrayList<MSSettings> msMap,
                                           Semaphore sslSemaphore, Semaphore portSemaphore, int port, MainActivity mainActivity) {
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
        request.setMSName("remotesearch");
        msIndex = mapFromMSNameToMSIndex(msMap, request.getMSName());
        if (msIndex < 0) {
            debugLogOutput(incoming, "exit with ERR msIndex < 0");
            processReturnValue("ERR:" + (-UNABLE_TO_FIND_MICROSERVICE) + "|" + nameUsedForOutputToLogger);
        } else {
            ms = (SDVRequestProcessorCategories) startMicroservice(msIndex);
            if (ms == null) {
                debugLogOutput(incoming, "exit with ERR startMicroservice returned null");
                processReturnValue("ERR:" + (-UNABLE_TO_FIND_MICROSERVICE) + "|" + nameUsedForOutputToLogger);
            } else {
                // START MY WORK
                // FINISHED MY WORK
                Observable<Payload> msObservable = Observable.create(emitter -> {
                    emitter.onNext(request);
                    emitter.onComplete();
                });
                initializeObservable(msObservable);
            }
        }
    }

    private void initializeObservable(Observable<Payload> observable) {
        observable
                .observeOn(Schedulers.io())
                .map((Payload request) -> {
                    // block until downstream microservice is listening, no need to release semaphore, because downstream microservice will not try to acquire it again
                    try { ms.startupSemaphore.acquire(); } catch (Exception e) {}
                    return sendToMicroservice(request, clientId, msMap.get(msIndex).isReturnToMe(), mainActivity);
                })
                // serialized output Payload is returned here from the downstream microservice
                // The returned Payload may have other outputs added in Payload[1]...Payload[n]
                // START MY OTHER WORK
                // FINISHED MY OTHER WORK
                .observeOn(AndroidSchedulers.mainThread())
                // calls to other downstream microservices are finished ...
                .timeout(5, TimeUnit.SECONDS)
                .subscribe((String result) -> {
                    if (result.startsWith("ERR:")) {
                        debugLogOutput(result, "exited with ERR");
                        processReturnValue(result);
                    } else {
                        debugLogOutput(incoming, "exited normally");
                        Payload inflated = new Payload(Base64.decode(result, Base64.DEFAULT));
                        processReturnValue(inflated);
                    }
                }, throwable -> {
                    handleException(throwable, incoming);
                });
    }

}
