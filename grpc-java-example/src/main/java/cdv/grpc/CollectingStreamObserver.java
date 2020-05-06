package cdv.grpc;

import cdv.grpc.model.EchoResponse;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class CollectingStreamObserver
        implements StreamObserver<EchoResponse> {

    private final List<String> responses = new ArrayList<>();
    private final CountDownLatch latch = new CountDownLatch(1);

    @Override
    public void onNext(EchoResponse response) {
        System.out.println(">> getting the response: " + response.getMessage());
        responses.add(response.getMessage());
    }

    @Override
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void onCompleted() {
        latch.countDown();
    }

    public List<String> waitForResponses() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return responses;
    }

}
