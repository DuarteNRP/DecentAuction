package client;


import io.grpc.stub.StreamObserver;

public class helloWorldServiceImpl extends HelloWorldServiceGrpc.HelloWorldServiceImplBase {
    @Override
    public void hello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
        super.hello(request, responseObserver);
    }


}
