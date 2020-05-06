package cdv.grpc.scenario;

import cdv.grpc.SimpleEchoClient;
import cdv.grpc.SimpleEchoService;
import cdv.grpc.model.EchoServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;

import java.io.IOException;

public class CallContext {

    private final SimpleEchoClient client;

    private CallContext(SimpleEchoClient client) {
        this.client = client;
    }

    public SimpleEchoClient getClient() {
        return client;
    }

    public static CallContext prepare() throws IOException {

        String serverName = InProcessServerBuilder.generateName();

        InProcessServerBuilder
                .forName(serverName)
                .directExecutor()
                .addService(new SimpleEchoService())
                .build()
                .start();
        ManagedChannel channel = InProcessChannelBuilder
                .forName(serverName)
                .directExecutor()
                .build();

        return new CallContext(new SimpleEchoClient(
                EchoServiceGrpc.newBlockingStub(channel),
                EchoServiceGrpc.newStub(channel)));
    }

}
