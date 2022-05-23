package grpcClient;

import ServiceGRPC.P2PServiceGrpc;
import ServiceGRPC.Ping;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import kademlia.Node;

import java.util.logging.Logger;
/*
Agora faz tudo mais sentido, como comunico com os vários serviços? crio um stub para o channel do nó que quero comunicar
e não preciso de mandar o ip do nó que quero comunicar ahaha troll
*/
public class DistributedClient {
    private static final Logger logger = Logger.getLogger(DistributedClient.class.getName());
    public String ip;
    public int port;
    private ManagedChannel channel;
    private P2PServiceGrpc.P2PServiceBlockingStub blockingStub;
    private P2PServiceGrpc.P2PServiceStub asyncStub;
    public DistributedClient(String ip, int port){
        this.ip=ip;
        this.port=port;
        this.channel= ManagedChannelBuilder.forTarget(ip+":"+port)
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                // needing certificates.
                .usePlaintext()
                .build();
        blockingStub = P2PServiceGrpc.newBlockingStub(this.channel);
        asyncStub = P2PServiceGrpc.newStub(this.channel);
    }
    //use Node ID to ping node
    public void sendPing(Node node){
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
                    .setNodeId("1")
                    .build();
            asyncStub.ping(ping,responseObserver);
        } catch(StatusRuntimeException e){
            logger.info("RPC failed: {0}"+ e.getStatus()+"!");
        }

    }

}
