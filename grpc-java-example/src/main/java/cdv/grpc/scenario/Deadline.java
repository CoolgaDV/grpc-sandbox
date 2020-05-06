package cdv.grpc.scenario;

import cdv.grpc.model.EchoRequest;
import cdv.grpc.model.EchoResponse;
import cdv.grpc.model.EchoServiceGrpc;
import io.grpc.Channel;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.TimeUnit;

import static cdv.grpc.Utils.*;

public class Deadline {

    public static void main(String[] args) {

        String serverName = InProcessServerBuilder.generateName();
        prepareServer(serverName, new Service());
        Channel channel = prepareChannel(serverName);

        // This call should be failed due to deadline
        EchoResponse response = EchoServiceGrpc.newBlockingStub(channel)
                .withDeadlineAfter(1, TimeUnit.SECONDS)
                .echoUnary(makeRequest("hello"));

        printResult(response.getMessage());
    }

    private static class Service extends EchoServiceGrpc.EchoServiceImplBase {

        @Override
        public void echoUnary(EchoRequest request,
                              StreamObserver<EchoResponse> responseObserver) {
            try {
                Thread.sleep(2_000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            responseObserver.onNext(makeResponse(request.getMessage()));
            responseObserver.onCompleted();
        }

    }

}
