package cdv.grpc.scenario;

import cdv.grpc.model.EchoRequest;
import cdv.grpc.model.EchoResponse;
import cdv.grpc.model.EchoServiceGrpc;
import io.grpc.Channel;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static cdv.grpc.Utils.*;

public class ClientSideFlowControl {

    @SneakyThrows
    public static void main(String[] args) {

        String serverName = InProcessServerBuilder.generateName();
        prepareServer(serverName, new Service());
        Channel channel = prepareChannel(serverName);

        CountDownLatch done = new CountDownLatch(1);

        var stub = EchoServiceGrpc.newStub(channel);
        List<String> requests = List.of("first", "second", "third");
        stub.echoStream(new Client(requests.iterator(), done));

        done.await();
    }

    @RequiredArgsConstructor
    private static class Client
            implements ClientResponseObserver<EchoRequest, EchoResponse> {

        private final Iterator<String> requests;
        private final CountDownLatch done;
        private boolean firstRequest = true;

        private ClientCallStreamObserver<EchoRequest> requestStream;

        @Override
        public void beforeStart(ClientCallStreamObserver<EchoRequest> requestStream) {
            this.requestStream = requestStream;
            requestStream.disableAutoInboundFlowControl();
            requestStream.setOnReadyHandler(() -> {
                if (requestStream.isReady() && firstRequest) {
                    firstRequest = false;
                    requestStream.request(1);
                    requestStream.onNext(makeRequest(requests.next()));
                }
            });
        }

        @Override
        public void onNext(EchoResponse value) {
            try {
                // emulate response processing
                Thread.sleep(2_000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(">> response: " + value.getMessage());
            if (requests.hasNext()) {
                requestStream.request(1);
                requestStream.onNext(makeRequest(requests.next()));
            } else {
                requestStream.onCompleted();
            }
        }

        @Override
        public void onError(Throwable throwable) {
            throwable.printStackTrace();
            done.countDown();
        }

        @Override
        public void onCompleted() {
            done.countDown();
        }

    }

    private static class Service extends EchoServiceGrpc.EchoServiceImplBase {

        @Override
        public StreamObserver<EchoRequest> echoStream(
                final StreamObserver<EchoResponse> responseObserver) {
            return new StreamObserver<>() {

                @Override
                public void onNext(EchoRequest value) {
                    responseObserver.onNext(makeResponse(value.getMessage()));
                }

                @Override
                public void onError(Throwable throwable) {
                    throwable.printStackTrace();
                }

                @Override
                public void onCompleted() {
                    responseObserver.onCompleted();
                }
            };
        }

    }

}
