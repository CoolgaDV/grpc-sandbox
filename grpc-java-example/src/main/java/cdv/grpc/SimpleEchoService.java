package cdv.grpc;

import cdv.grpc.model.EchoRequest;
import cdv.grpc.model.EchoResponse;
import cdv.grpc.model.EchoServiceGrpc;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;

public class SimpleEchoService extends EchoServiceGrpc.EchoServiceImplBase {

    @Override
    public void echoUnary(EchoRequest request,
                          StreamObserver<EchoResponse> responseObserver) {
        responseObserver.onNext(makeResponse(request.getMessage()));
        responseObserver.onCompleted();
    }

    @Override
    public void echoStreamServer(EchoRequest request,
                                 StreamObserver<EchoResponse> responseObserver) {
        responseObserver.onNext(makeResponse(request.getMessage()));
        responseObserver.onNext(makeResponse(request.getMessage()));
        responseObserver.onCompleted();
    }

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

    private EchoResponse makeResponse(String source) {
        return EchoResponse.newBuilder()
                .setMessage(source + "-" + source)
                .build();
    }

}
