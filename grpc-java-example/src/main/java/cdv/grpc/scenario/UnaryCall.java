package cdv.grpc.scenario;

import cdv.grpc.SimpleEchoClient;

public class UnaryCall {

    public static void main(String[] args) throws Exception {
        SimpleEchoClient client = CallContext.prepare().getClient();
        System.out.println("response: " + client.makeUnaryCall("hello"));
    }

}
