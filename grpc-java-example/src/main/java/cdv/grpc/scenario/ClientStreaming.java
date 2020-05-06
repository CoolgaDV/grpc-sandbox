package cdv.grpc.scenario;

import cdv.grpc.CollectingStreamObserver;
import cdv.grpc.Utils;
import cdv.grpc.model.EchoRequest;
import cdv.grpc.model.EchoResponse;
import cdv.grpc.model.EchoServiceGrpc;
import io.grpc.Channel;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static cdv.grpc.Utils.*;

public class ClientStreaming {

    public static void main(String[] args) {

        String serverName = InProcessServerBuilder.generateName();
        prepareServer(serverName, new Service());
        Channel channel = prepareChannel(serverName);

        var responseObserver = new CollectingStreamObserver();
        var stub = EchoServiceGrpc.newStub(channel);
        StreamObserver<EchoRequest> requestObserver =
                stub.echoStreamClient(responseObserver);

        Stream.of("first", "second", "third")
                .map(Utils::makeRequest)
                .forEach(requestObserver::onNext);
        requestObserver.onCompleted();

        printResult(responseObserver.waitForResponses());
    }

    private static class Service extends EchoServiceGrpc.EchoServiceImplBase {

        @Override
        public StreamObserver<EchoRequest> echoStreamClient(
                StreamObserver<EchoResponse> responseObserver) {
            List<String> messages = new ArrayList<>();
            return new StreamObserver<>() {

                @Override
                public void onNext(EchoRequest value) {
                    messages.add(value.getMessage());
                }

                @Override
                public void onError(Throwable throwable) {
                    throwable.printStackTrace();
                }

                @Override
                public void onCompleted() {
                    String message = String.join("-", messages);
                    responseObserver.onNext(makeResponse(message));
                    responseObserver.onCompleted();
                }
            };
        }

    }

}
