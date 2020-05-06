package cdv.grpc.scenario;

import cdv.grpc.model.EchoRequest;
import cdv.grpc.model.EchoResponse;
import cdv.grpc.model.EchoServiceGrpc;
import io.grpc.Channel;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;

import static cdv.grpc.Utils.*;

public class ServerStreaming {

    public static void main(String[] args) {

        String serverName = InProcessServerBuilder.generateName();
        prepareServer(serverName, new Service());
        Channel channel = prepareChannel(serverName);

        var stub = EchoServiceGrpc.newBlockingStub(channel);
        List<String> responses = new ArrayList<>();
        stub.echoStreamServer(makeRequest("first"))
                .forEachRemaining(response ->
                        responses.add(response.getMessage()));

        printResult(responses);
    }

    private static class Service extends EchoServiceGrpc.EchoServiceImplBase {

        @Override
        public void echoStreamServer(EchoRequest request,
                                     StreamObserver<EchoResponse> responseObserver) {
            responseObserver.onNext(makeResponse(request.getMessage()));
            responseObserver.onNext(makeResponse(request.getMessage()));
            responseObserver.onCompleted();
        }

    }

}
