package cdv.grpc.scenario;

import cdv.grpc.model.EchoRequest;
import cdv.grpc.model.EchoResponse;
import cdv.grpc.model.EchoServiceGrpc;
import io.grpc.*;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;

import static cdv.grpc.Utils.*;

public class CallContext {

    private static final Metadata.Key<String> CUSTOM_HEADER_KEY = Metadata.Key.of(
            "custom_server_header_key",
            Metadata.ASCII_STRING_MARSHALLER);
    private static final Context.Key<String> CUSTOM_CONTEXT_KEY = Context.key(
            "custom_server_context_key");

    public static void main(String[] args) {
        String serverName = InProcessServerBuilder.generateName();
        prepareServer(serverName, new Service(), new ServerSideInterceptor());
        Channel channel = prepareChannel(serverName);

        var stub = EchoServiceGrpc.newBlockingStub(channel)
                .withInterceptors(new ClientSideInterceptor());

        printResult(stub.echoUnary(makeRequest("hello")).getMessage());
    }

    private static class Service extends EchoServiceGrpc.EchoServiceImplBase {

        @Override
        public void echoUnary(EchoRequest request,
                              StreamObserver<EchoResponse> responseObserver) {
            System.out.println("custom value form context: " + CUSTOM_CONTEXT_KEY.get());
            responseObserver.onNext(makeResponse(request.getMessage()));
            responseObserver.onCompleted();
        }

    }

    private static class ServerSideInterceptor implements ServerInterceptor {

        @Override
        public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
                ServerCall<ReqT, RespT> call,
                Metadata headers,
                ServerCallHandler<ReqT, RespT> next) {

            String customHeaderValue = headers.get(CUSTOM_HEADER_KEY);
            Context context = Context.current().withValue(
                    CUSTOM_CONTEXT_KEY,
                    customHeaderValue);

            return Contexts.interceptCall(context, call, headers, next);
        }

    }

    private static class ClientSideInterceptor implements ClientInterceptor {

        @Override
        public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
                MethodDescriptor<ReqT, RespT> method,
                CallOptions callOptions,
                Channel next) {
            ClientCall<ReqT, RespT> call = next.newCall(method, callOptions);
            return new ForwardingClientCall.SimpleForwardingClientCall<>(call) {
                @Override
                public void start(Listener<RespT> responseListener, Metadata headers) {
                    headers.put(CUSTOM_HEADER_KEY, "client_custom_header");
                    super.start(responseListener, headers);
                }
            };
        }
    }

}
