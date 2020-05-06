package cdv.grpc;

import cdv.grpc.model.EchoRequest;
import cdv.grpc.model.EchoResponse;
import io.grpc.BindableService;
import io.grpc.Channel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Utils {

    public static EchoResponse makeResponse(String message) {
        return EchoResponse.newBuilder()
                .setMessage(message + "-" + message)
                .build();
    }

    public static EchoRequest makeRequest(String message) {
        return EchoRequest.newBuilder()
                .setMessage(message)
                .build();
    }

    public static void printResult(Object result) {
        System.out.println("response: " + result);
    }

    @SneakyThrows
    public static void prepareServer(String serverName,
                                     BindableService service) {
        InProcessServerBuilder
                .forName(serverName)
                .directExecutor()
                .addService(service)
                .build()
                .start();
    }

    public static Channel prepareChannel(String serverName) {
        return InProcessChannelBuilder
                .forName(serverName)
                .directExecutor()
                .build();
    }

}
