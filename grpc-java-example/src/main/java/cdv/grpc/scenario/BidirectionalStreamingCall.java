package cdv.grpc.scenario;

import cdv.grpc.SimpleEchoClient;

public class BidirectionalStreamingCall {

    public static void main(String[] args) throws Exception {
        SimpleEchoClient client = CallContext.prepare().getClient();
        System.out.println("response: " + client.makeStreamCall(
                "first",
                "second",
                "third"));
    }

}
