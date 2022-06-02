package grpcClient;

import ServiceGRPC.*;
import crypto.Crypto;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import kademlia.Node;
import kademlia.TripleNode;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
@Setter
@Getter
public class ServerService{
    private static final Logger logger = Logger.getLogger(ServerService.class.getName());
    private Server server;
    public String ip;
    public int port;
    private Node serviceNode;
    private TripleNode serviceTripleNode;
    private DistributedClient distributedClient;
    public ServerService(String ip, int port){
        this.ip=ip;
        this.port=port;
        this.distributedClient=new DistributedClient(this.ip,this.port);
        this.serviceTripleNode = new TripleNode(this.ip,this.port);
        this.serviceNode = new Node(this.serviceTripleNode);
        serviceNode.setDistributedClient(this.distributedClient);
        distributedClient.setNode(this.serviceNode);
    }
    public void start() throws IOException {
        server = ServerBuilder.forPort(this.port)
                .addService(new ServerServiceImpl(this.ip,this.port,this.serviceNode))
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
        //public final Crypto crypto = new
        String ip;
        int port;
        Node node;
        ServerServiceImpl(String ip,int port,Node node){
            this.ip=ip;this.port=port;this.node=node;
        }
        @Override
        public void ping(Ping request, StreamObserver<Ping> responseObserver) {
            responseObserver.onNext(request);
            TripleNode tripleNode = new TripleNode(request.getIp(),request.getPort());
            tripleNode.setNodeId(request.getNodeId());
            this.node.tryToAddNode(tripleNode);
            responseObserver.onCompleted();
        }

        @Override
        public void findNode(Ping request, StreamObserver<KBucket> responseObserver) {
            System.out.println("find node do nó: "+request.getNodeId()+" em: "+this.node.getNodeId());
            TripleNode tripleNode = new TripleNode(request.getNodeId(), request.getIp(), request.getPort());
            ArrayList<TripleNode> kClosestNodes = this.node.findKClosestNodes(tripleNode);
            for(TripleNode t : kClosestNodes){
                responseObserver.onNext(
                        KBucket.newBuilder()
                                .setNodeId(t.getNodeId())
                                .setIp(t.getIp())
                                .setPort(t.getPort())
                                .build()
                );
            }
            responseObserver.onCompleted();
        }

        @Override
        public void findValue(Ping request, StreamObserver<KBucket> responseObserver) {
            TripleNode tripleNode = new TripleNode(request.getNodeId(), request.getIp(), request.getPort());
            //var result = this.node.data.get(crypto)
            ArrayList<TripleNode> kClosestNodes = this.node.findKClosestNodes(tripleNode);
            for(TripleNode t : kClosestNodes){
                responseObserver.onNext(
                        KBucket.newBuilder()
                                .setNodeId(t.getNodeId())
                                .setIp(t.getIp())
                                .setPort(t.getPort())
                                .build()
                );
            }
            responseObserver.onCompleted();
        }

        @Override
        public void store(Data request, StreamObserver<Empty> responseObserver) {

        }
    }
}