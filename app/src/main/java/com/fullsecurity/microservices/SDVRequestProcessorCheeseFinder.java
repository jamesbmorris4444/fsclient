package com.fullsecurity.microservices;

import android.util.Base64;

import com.fullsecurity.common.Payload;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@SuppressWarnings("all")
public class SDVRequestProcessorCheeseFinder extends MSCommunicationsWrapper {

    private static final String nameUsedForOutputToLogger = "cheeseFinderMicroservice: ";
    private static final String calledMicroServiceName = "cheesesearch";

    private byte[] incoming;
    private int clientId;
    private SDVRequestProcessorCheeseSearch ms;
    private int msIndex;

    public SDVRequestProcessorCheeseFinder(int port) { super(port, nameUsedForOutputToLogger); }

    @Override
    public void setOutputPayloads(Payload request) {
        incoming = request.getPayload(0);
        clientId = request.getClientId();
        request.setMSName(calledMicroServiceName);
        msIndex = mapFromMSNameToMSIndex(msMap, calledMicroServiceName);
        if (msIndex < 0) {
            processReturnValue("ERR:" + (-UNABLE_TO_FIND_MICROSERVICE) + "|" + calledMicroServiceName);
        } else {
            ms = (SDVRequestProcessorCheeseSearch) startMicroservice(msIndex);
            if (ms == null) {
                processReturnValue("ERR:" + (-UNABLE_TO_FIND_MICROSERVICE) + "|" + calledMicroServiceName);
            } else {
                request = performWorkNecessaryToCallDownstreamMicroService(request);
                initializeObservable(Observable.just(request));
            }
        }
    }

    private Payload performWorkNecessaryToCallDownstreamMicroService(Payload request) { return request; }

    private void initializeObservable(Observable<Payload> observable) {
        observable
                .observeOn(Schedulers.io())
                .map((Payload request) -> {
                    // block until downstream microservice is listening, no need to release semaphore, because downstream microservice will not try to acquire it again
                    try { ms.startupSemaphore.acquire(); } catch (Exception e) {}
                    return sendToMicroservice(request, clientId, msMap.get(msIndex).isReturnToMe());
                })
                // serialized output Payload is returned here from the downstream microservice
                // The returned Payload may have other outputs added to its internal payloads
                // CALL ANOTHER DOWNSTREAM MICROSERVICE
                // .map((Payload request) -> { })
                // RETURN FROM ANOTHER DOWNSTREAM MICROSERVICE
                // etc.
                .timeout(5, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleNormalResponse, throwable -> { handleException(throwable, incoming); });
    }

    private void handleNormalResponse(String result) {
        if (result.startsWith("ERR:")) {
            debugLogOutput(result, "exited with ERR");
            processReturnValue(result);
        } else {
            debugLogOutput(incoming, "exited normally");
            Payload inflated = new Payload(Base64.decode(result, Base64.DEFAULT));
            processReturnValue(inflated);
        }
    }

}
