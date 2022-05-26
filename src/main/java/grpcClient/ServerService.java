package grpcClient;

import ServiceGRPC.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import kademlia.Node;
import kademlia.TripleNode;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ServerService{
    private static final Logger logger = Logger.getLogger(ServerService.class.getName());
    private Server server;
    public String ip;
    public int port;
    private Node serviceNode;
    private TripleNode servicetripleNode;
    private DistributedClient distributedClient;
    public ServerService(String ip, int port,DistributedClient distributedClient){
        this.ip=ip;
        this.port=port;
        this.distributedClient=distributedClient;
        this.servicetripleNode = new TripleNode(this.ip,this.port,this.distributedClient);
        this.serviceNode = new Node(this.servicetripleNode,this.distributedClient);
    }
    public void start() throws IOException {
        server = ServerBuilder.forPort(this.port)
                .addService(new ServerServiceImpl())
                .build()
                .start();
        logger.info("Server started, listening on " + ip+":"+port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {

                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    ServerService.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });
    }

    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }


    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    static class ServerServiceImpl extends P2PServiceGrpc.P2PServiceImplBase {
        ServerServiceImpl(){}
        @Override
        public void ping(Ping request, StreamObserver<Ping> responseObserver) {
            responseObserver.onNext(request);
            responseObserver.onCompleted();
        }

        @Override
        public void findNode(ID request, StreamObserver<ID> responseObserver) {
            super.findNode(request, responseObserver);
        }

        @Override
        public void findValue(ID request, StreamObserver<ID> responseObserver) {
            super.findValue(request, responseObserver);
        }

        @Override
        public void store(Data request, StreamObserver<Empty> responseObserver) {
            super.store(request, responseObserver);
        }
    }
}