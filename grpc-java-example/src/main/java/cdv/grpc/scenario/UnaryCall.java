package cdv.grpc.scenario;

import cdv.grpc.model.EchoRequest;
import cdv.grpc.model.EchoResponse;
import cdv.grpc.model.EchoServiceGrpc;
import io.grpc.Channel;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;

import static cdv.grpc.Utils.*;

public class UnaryCall {

    public static void main(String[] args) {

        String serverName = InProcessServerBuilder.generateName();
        prepareServer(serverName, new Service());
        Channel channel = prepareChannel(serverName);

        var stub = EchoServiceGrpc.newBlockingStub(channel);

        printResult(stub.echoUnary(makeRequest("hello")));
    }

    private static class Service extends EchoServiceGrpc.EchoServiceImplBase {

        @Override
        public void echoUnary(EchoRequest request,
                              StreamObserver<EchoResponse> responseObserver) {
            responseObserver.onNext(makeResponse(request.getMessage()));
            responseObserver.onCompleted();
        }

    }

}
