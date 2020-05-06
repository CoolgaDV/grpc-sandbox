package cdv.grpc.scenario;

import cdv.grpc.CollectingStreamObserver;
import cdv.grpc.Utils;
import cdv.grpc.model.EchoRequest;
import cdv.grpc.model.EchoResponse;
import cdv.grpc.model.EchoServiceGrpc;
import io.grpc.Channel;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

import static cdv.grpc.Utils.*;

public class ServerSideFlowControl {

    public static void main(String[] args) {

        String serverName = InProcessServerBuilder.generateName();
        prepareServer(serverName, new Service());
        Channel channel = prepareChannel(serverName);

        var stub = EchoServiceGrpc.newStub(channel);

        var responseObserver = new CollectingStreamObserver();
        StreamObserver<EchoRequest> requestObserver =
                stub.echoStream(responseObserver);

        Stream.of("first", "second", "third")
                .map(Utils::makeRequest)
                .forEach(requestObserver::onNext);
        requestObserver.onCompleted();

        printResult(responseObserver.waitForResponses());
    }

    private static class Service extends EchoServiceGrpc.EchoServiceImplBase {

        @Override
        public StreamObserver<EchoRequest> echoStream(
                final StreamObserver<EchoResponse> responseObserver) {

            ServerCallStreamObserver<EchoResponse> serverObserver =
                    (ServerCallStreamObserver<EchoResponse>) responseObserver;
            serverObserver.disableAutoInboundFlowControl();

            var readyHandler = new ReadyHandler(serverObserver);
            serverObserver.setOnReadyHandler(readyHandler);

            return new StreamObserver<>() {

                @Override
                public void onNext(EchoRequest value) {
                    try {
                        // emulate request processing
                        Thread.sleep(2_000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    responseObserver.onNext(makeResponse(value.getMessage()));

                    if (serverObserver.isReady()) {
                        serverObserver.request(1);
                    } else {
                        readyHandler.markReady();
                    }
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

    @RequiredArgsConstructor
    private static class ReadyHandler implements Runnable {

        private final ServerCallStreamObserver<EchoResponse> serverObserver;
        private volatile boolean wasReady = false;

        @Override
        public void run() {
            if (serverObserver.isReady() && !wasReady) {
                wasReady = true;
                serverObserver.request(1);
            }
        }

        public void markReady() {
            wasReady = false;
        }

    }

}
