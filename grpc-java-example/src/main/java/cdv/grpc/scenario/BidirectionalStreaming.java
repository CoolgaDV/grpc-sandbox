package cdv.grpc.scenario;

import cdv.grpc.CollectingStreamObserver;
import cdv.grpc.Utils;
import cdv.grpc.model.EchoRequest;
import cdv.grpc.model.EchoResponse;
import cdv.grpc.model.EchoServiceGrpc;
import io.grpc.Channel;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;

import java.util.stream.Stream;

import static cdv.grpc.Utils.*;

public class BidirectionalStreaming {

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
