package grpcClient;

import ServiceGRPC.*;
import com.google.gson.GsonBuilder;
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
import myBlockchain.Block;
import myBlockchain.Chain;
import myBlockchain.Transaction;
import pubsubAuction.Auction;
import pubsubAuction.Service;

import java.io.IOException;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
@Setter
@Getter
public class ServerService implements Serializable {
    private static final Logger logger = Logger.getLogger(ServerService.class.getName());
    private Server server;
    public String ip;
    public int port;
    private Node serviceNode;
    private TripleNode serviceTripleNode;
    private DistributedClient distributedClient;
    public ServerService(String ip, int port) throws NoSuchAlgorithmException, InvalidKeySpecException {
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
            Ping ping = Ping.newBuilder()
                    .setNodeId(this.node.getNode().getNodeId())
                    .setIp(this.node.getNode().getIp())
                    .setPort(this.node.getNode().getPort())
                    .build();
            responseObserver.onNext(ping);
            TripleNode tripleNode = new TripleNode(request.getIp(),request.getPort());
            tripleNode.setNodeId(request.getNodeId());
            this.node.tryToAddNode(tripleNode);
            responseObserver.onCompleted();
        }

        @Override
        public void askMessage(Empty request, StreamObserver<Message> responseObserver) {
            try {
                Message message = Message.newBuilder()
                    .setBlockchain(ByteString.copyFrom(utils.serialize(this.node.getChain())))
                    .setService(ByteString.copyFrom(utils.serialize(this.node.getAuctionHouse())))
                    .setPool(ByteString.copyFrom(utils.serialize(this.node.getTransactionPool())))
                    .build();
                responseObserver.onNext(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
            TripleNode tripleNode = new TripleNode(request.getPing().getNodeId(), request.getPing().getIp(), request.getPing().getPort());
            this.node.tryToAddNode(tripleNode);
            if(this.node.broadcastId.contains(request.getIdentifier())){
                //System.out.println("Nó para Não guardar"+this.node.getNodeId());
                responseObserver.onNext(Empty.newBuilder().build());
                responseObserver.onCompleted();
                return;
            }
            this.node.broadcastId.add(request.getIdentifier());
            //System.out.println("Nó para guardar"+this.node.getNodeId());
                if(request.getDatatype()==DataType.BLOCK) {
                    this.node.handlerNewBlock();
                    Block b = new GsonBuilder().create().fromJson(utils.getStringFromBytes(request.getData().toByteArray()),Block.class);
                    this.node.setBlock(b);
                    this.node.getChain().append(b);
                    //System.out.println("Block received");
                }
                else if(request.getDatatype()==DataType.TRANSACTION) {
                    Transaction t = new GsonBuilder().create().fromJson(utils.getStringFromBytes(request.getData().toByteArray()),Transaction.class);
                    this.node.getTransactionPool().add(t);
                    //System.out.println("Transaction received");
                    if(this.node.transactionPool.size()>=constraints.MAX_TRANSACTIONS_PER_BLOCK){
                        Context ctx = Context.current().fork();
                        // Set ctx as the current context within the Runnable
                        ctx.run(() -> {
                            this.node.handlerNewTransaction();
                        });
                    }
                }
                else if(request.getDatatype()==DataType.BLOCKCHAIN) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    Chain c = new GsonBuilder().create().fromJson(utils.getStringFromBytes(request.getData().toByteArray()),Chain.class);
                    if(this.node.getChain().blockchain.size()<c.blockchain.size()) {
                        this.node.handlerNewBlock();
                        this.node.setChain(c);
                        //System.out.println("blockchain received");
                        this.node.wallet.setBlockchain(c);
                    }
                }
                else if(request.getDatatype()==DataType.AUCTION){
                    //System.out.println("Guardou auction em:" + this.node.getNodeId());
                    Service s = new GsonBuilder().create().fromJson(utils.getStringFromBytes(request.getData().toByteArray()),Service.class);
                    this.node.setAuctionHouse(s);
                }
                Context ctx = Context.current().fork();
                // Set ctx as the current context within the Runnable
                ctx.run(() -> {
                    try {
                        this.node.broadcast(request.getData().toByteArray(),request.getIdentifier(),request.getDatatype());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                responseObserver.onNext(Empty.newBuilder().build());
                responseObserver.onCompleted();
        }

        @Override
        public void getWallet(Ping request, StreamObserver<Wallet> responseObserver) {
            try {
                TripleNode tripleNode = new TripleNode(request.getNodeId(), request.getIp(), request.getPort());
                this.node.tryToAddNode(tripleNode);
                Ping ping = Ping.newBuilder()
                        .setNodeId(this.node.getNode().getNodeId())
                        .setIp(this.node.getNode().getIp())
                        .setPort(this.node.getNode().getPort())
                        .build();
                //System.out.println("Wallet do lado do servidor:"+ Arrays.toString(this.node.wallet.publicKey));
                //System.out.println(ByteString.copyFrom(utils.serialize(this.node.getWallet())));
                Wallet wallet = Wallet.newBuilder().setWallet(ByteString.copyFrom(utils.serialize(this.node.getWallet()))).setPing(ping).build();
                responseObserver.onNext(wallet);
                responseObserver.onCompleted();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}