package grpcClient;

import ServiceGRPC.*;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import kademlia.Node;
import kademlia.TripleNode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
/*
Agora faz tudo mais sentido, como comunico com os vários serviços? crio um stub para o channel do nó que quero comunicar
e não preciso de mandar o ip do nó que quero comunicar ahaha troll
*/
@Getter
@Setter
public class DistributedClient {
    private static final Logger logger = Logger.getLogger(DistributedClient.class.getName());
    public String ip;
    public int port;
    public Node node;
    /*private ManagedChannel channel;
    private P2PServiceGrpc.P2PServiceBlockingStub blockingStub;
    private P2PServiceGrpc.P2PServiceStub asyncStub;*/
    public DistributedClient(String ip, int port){
        this.ip=ip;
        this.port=port;
    }
    public P2PServiceGrpc.P2PServiceStub newAsyncStub(TripleNode node){
        ManagedChannel channel= ManagedChannelBuilder.forTarget(node.getIp()+":"+node.getPort())
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                // needing certificates.
                .usePlaintext()
                .build();
        return P2PServiceGrpc.newStub(channel);
    }
    public P2PServiceGrpc.P2PServiceBlockingStub newBlockingStub(TripleNode node){
        ManagedChannel channel= ManagedChannelBuilder.forTarget(node.getIp()+":"+node.getPort())
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                // needing certificates.
                .usePlaintext()
                .build();
        return P2PServiceGrpc.newBlockingStub(channel);
    }
    //use Node ID to ping node
    public void sendPing(TripleNode node){
        P2PServiceGrpc.P2PServiceStub asyncStub = newAsyncStub(node);
        StreamObserver<Ping> responseObserver = new StreamObserver<Ping>(){
            @Override
            public void onNext(Ping value){
                System.out.println("ping received");
            }
            @Override
            public void onError(Throwable t) {
                System.out.println("No connection, removed node");
            }
            @Override
            public void onCompleted() {
            }
        };
        try {
            Ping ping = Ping.newBuilder()
                    .setNodeId(this.node.getNode().getNodeId())
                    .setIp(this.node.getNode().getIp())
                    .setPort(this.node.getNode().getPort())
                    .build();
            asyncStub.ping(ping,responseObserver);
        } catch(StatusRuntimeException e){
            logger.info("RPC failed: {0}"+ e.getStatus()+"!");
        }

    }
    public void findNode(List<TripleNode> list,TripleNode node,TripleNode target){
        P2PServiceGrpc.P2PServiceStub asyncStub = newAsyncStub(node);
        StreamObserver<KBucket> responseObserver = new StreamObserver<KBucket> (){
            @Override
            public void onNext(KBucket value) {
                TripleNode newNode = new TripleNode(value.getNodeId(), value.getIp(), value.getPort());
                synchronized (list) {
                    list.add(newNode);
                }
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("Não respondeu!");
            }

            @Override
            public void onCompleted() {
                System.out.println("Acabou com sucesso");
            }
        };
        try {
            Ping ping = Ping.newBuilder()
                    .setNodeId(target.getNodeId())
                    .setIp(target.getIp())
                    .setPort(target.getPort())
                    .build();
            asyncStub.findNode(ping,responseObserver);
        } catch(StatusRuntimeException e){
            logger.info("RPC failed: {0}"+ e.getStatus()+"!");
        }
    }
    public void storeValue(TripleNode node, String key, byte[] value){
        P2PServiceGrpc.P2PServiceStub asyncStub = newAsyncStub(node);
        StreamObserver<Empty> responseObserver = new StreamObserver<Empty> (){
            @Override
            public void onNext(Empty value) {
                System.out.println("Value Stored");
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("Value not stored, peer must be down");
            }

            @Override
            public void onCompleted() {
            }
        };
        try {
            Data data = Data.newBuilder()
                    .setKey(key)
                    .setValue(ByteString.copyFrom(value))
                    .build();
            asyncStub.store(data,responseObserver);
        } catch(StatusRuntimeException e){
            logger.info("RPC failed: {0}"+ e.getStatus()+"!");
        }
    }
}
