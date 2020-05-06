package cdv.grpc;

import cdv.grpc.model.EchoRequest;
import cdv.grpc.model.EchoResponse;
import cdv.grpc.model.EchoServiceGrpc;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class SimpleEchoClient {

    private final EchoServiceGrpc.EchoServiceBlockingStub blockingStub;
    private final EchoServiceGrpc.EchoServiceStub stub;

    public SimpleEchoClient(EchoServiceGrpc.EchoServiceBlockingStub blockingStub,
                            EchoServiceGrpc.EchoServiceStub stub) {
        this.blockingStub = blockingStub;
        this.stub = stub;
    }

    public String makeUnaryCall(String message) {
        EchoResponse response = blockingStub.echoUnary(makeRequest(message));
        return response.getMessage();
    }

    public List<String> makeStreamServerCall(String message) {
        List<String> responses = new ArrayList<>();
        blockingStub.echoStreamServer(makeRequest(message))
                .forEachRemaining(response ->
                        responses.add(response.getMessage()));
        return responses;
    }

    public List<String> makeStreamClientCall(String... message) {

        var responseObserver = new CollectingStreamObserver();
        StreamObserver<EchoRequest> requestObserver =
                stub.echoStreamClient(responseObserver);

        Arrays.stream(message)
                .map(this::makeRequest)
                .forEach(requestObserver::onNext);
        requestObserver.onCompleted();

        return responseObserver.waitForResponses();
    }

    public List<String> makeStreamCall(String... message) {

        var responseObserver = new CollectingStreamObserver();
        StreamObserver<EchoRequest> requestObserver =
                stub.echoStream(responseObserver);

        Arrays.stream(message)
                .map(this::makeRequest)
                .forEach(requestObserver::onNext);
        requestObserver.onCompleted();

        return responseObserver.waitForResponses();
    }

    private EchoRequest makeRequest(String message) {
        return EchoRequest.newBuilder()
                .setMessage(message)
                .build();
    }

    private static class CollectingStreamObserver
            implements StreamObserver<EchoResponse> {

        private final List<String> responses = new ArrayList<>();
        private final CountDownLatch latch = new CountDownLatch(1);

        @Override
        public void onNext(EchoResponse response) {
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

}
