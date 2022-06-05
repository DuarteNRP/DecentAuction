package grpcClient;

import ServiceGRPC.*;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import config.Constraints;
import config.Utils;
import crypto.Crypto;
import io.grpc.Context;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import kademlia.Node;
import kademlia.TripleNode;
import lombok.Getter;
import lombok.Setter;
import myBlockchain.Chain;
import pubsubAuction.Auction;
import pubsubAuction.Service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
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
    public ServerService(String ip, int port) throws NoSuchAlgorithmException {
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
        private static final Crypto crypto = new Crypto();
        private static final Utils utils = new Utils();
        private static final Constraints constraints = new Constraints();
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
            TripleNode tripleNode = new TripleNode(request.getNodeId(), request.getIp(), request.getPort());
            this.node.tryToAddNode(tripleNode);
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
        public void findValue(Ping request, StreamObserver<Found> responseObserver) {
            TripleNode tripleNode = new TripleNode(request.getNodeId(), request.getIp(), request.getPort());
            this.node.tryToAddNode(tripleNode);
            if(this.node.getData().containsKey(request.getNodeId())){
                KBucket kbucket = KBucket.newBuilder()
                        .setNodeId(this.node.getNode().getNodeId())
                        .setIp(this.node.getNode().getIp())
                        .setPort(this.node.getNode().getPort())
                        .build();
                responseObserver.onNext(
                        Found.newBuilder()
                                .setFound(true)
                                .setValue(ByteString.copyFrom(this.node.getData().get(request.getNodeId())))
                                .setKBucket(kbucket)
                                .build()
                );
            }
            else {
                ArrayList<TripleNode> kClosestNodes = this.node.findKClosestNodes(tripleNode);
                for (TripleNode t : kClosestNodes) {
                    KBucket kbucket = KBucket.newBuilder()
                            .setNodeId(t.getNodeId())
                            .setIp(t.getIp())
                            .setPort(t.getPort())
                            .build();
                    responseObserver.onNext(
                            Found.newBuilder()
                                    .setFound(false)
                                    .setValue(ByteString.EMPTY)
                                    .setKBucket(kbucket)
                                    .build()
                    );
                }
            }
            responseObserver.onCompleted();
        }

        @Override
        public void store(Data request, StreamObserver<Empty> responseObserver) {
            this.node.data.put(request.getKey(),request.getValue().toByteArray());
            TripleNode tripleNode = new TripleNode(request.getPing().getNodeId(), request.getPing().getIp(), request.getPing().getPort());
            this.node.tryToAddNode(tripleNode);
            responseObserver.onNext(Empty.newBuilder().build());
            responseObserver.onCompleted();
        }

        @Override
        public void broadcast(BlockData request, StreamObserver<Empty> responseObserver) {
            if(this.node.broadcastId.contains(request.getIdentifier())){
                //System.out.println("Nó para Não guardar"+this.node.getNodeId());
                responseObserver.onNext(Empty.newBuilder().build());
                responseObserver.onCompleted();
                return;
            }
            this.node.broadcastId.add(request.getIdentifier());
            //System.out.println("Nó para guardar"+this.node.getNodeId());
            try {
                if(request.getDatatype()==DataType.BLOCK) {
                    myBlockchain.Block b = (myBlockchain.Block) utils.deserialize(request.getData().toByteArray());
                    this.node.setBlock(b);
                    System.out.println("Guardou bloco em:" +this.node.getNodeId());
                }
                else if(request.getDatatype()==DataType.TRANSACTION) {
                    myBlockchain.Transaction t = (myBlockchain.Transaction) utils.deserialize(request.getData().toByteArray());
                    this.node.getTransactionPool().add(t);

                    System.out.println("Guardou transaçao em:" +this.node.getNodeId()+" , identifier: "+request.getIdentifier());
                }
                else if(request.getDatatype()==DataType.BLOCKCHAIN) {
                    Chain c = (myBlockchain.Chain) utils.deserialize(request.getData().toByteArray());
                    if(this.node.getChain().blockchain!=null && this.node.getChain().blockchain.size()<c.blockchain.size()) {
                        this.node.setChain(c);
                        System.out.println("Guardou blockchain em:" + this.node.getNodeId());
                    }
                }
                else if(request.getDatatype()==DataType.AUCTION){
                    Service service = (Service) utils.deserialize(request.getData().toByteArray());
                    this.node.setAuctionHouse(service);
                }
                Context ctx = Context.current().fork();
                // Set ctx as the current context within the Runnable
                ctx.run(() -> {
                    this.node.broadcast(request.getData().toByteArray(),request.getIdentifier(),request.getDatatype());
                });
                responseObserver.onNext(Empty.newBuilder().build());
                responseObserver.onCompleted();
            } catch (IOException e) {
                System.out.println(e);
            } catch (ClassNotFoundException e) {
                System.out.println(e);
            }
        }
    }
}