package io.chiayungluk.emqxextension;

import com.google.protobuf.ByteString;
import io.chiayungluk.imauth.client.ImAuthClient;
//import io.chiayungluk.imstore.ImStoreServiceGrpc;
//import io.chiayungluk.imstore.Message;
//import io.chiayungluk.imstore.SaveOfflineMsgReq;
//import io.chiayungluk.imstore.SaveOfflineMsgResp;
import io.emqx.exhook.*;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class HookProviderService extends HookProviderGrpc.HookProviderImplBase {
//    private final ImStoreServiceGrpc.ImStoreServiceStub imStoreServiceStub;

    private final ImAuthClient imAuthClient;

    public HookProviderService(ImAuthClient imAuthClient) {
        this.imAuthClient = imAuthClient;
//        this.imStoreServiceStub = imStoreServiceStub;
    }

    @Override
    public void onProviderLoaded(ProviderLoadedRequest request, StreamObserver<LoadedResponse> responseObserver) {
        log.debug("onProviderLoaded: {}", request);
        responseObserver.onNext(LoadedResponse.newBuilder()
                .addHooks(HookSpec.newBuilder()
                        .setName("client.connected")
                        .addTopics("#").build())
                .addHooks(HookSpec.newBuilder()
                        .setName("client.authenticate")
                        .addTopics("#")
                        .build())
                .addHooks(HookSpec.newBuilder()
                        .setName("client.authorize")
                        .addTopics("#")
                        .build())
                .addHooks(HookSpec.newBuilder()
                        .setName("message.publish")
                        .addTopics("#")
                        .build())
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void onProviderUnloaded(ProviderUnloadedRequest request, StreamObserver<EmptySuccess> responseObserver) {
        responseObserver.onNext(EmptySuccess.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void onClientConnected(ClientConnectedRequest request, StreamObserver<EmptySuccess> responseObserver) {
        log.debug("onClientConnected: {}", request);
        responseObserver.onNext(EmptySuccess.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void onClientAuthenticate(ClientAuthenticateRequest request, StreamObserver<ValuedResponse> responseObserver) {
        log.debug("onClientAuthenticate: {}", request);
        imAuthClient.canConnect(request.getClientinfo().getUsername(),
                        request.getClientinfo().getClientid(),
                        request.getClientinfo().getPassword())
                .doOnNext(r -> log.debug("onClientAuthenticate res: {}", r))
                .doOnError(e -> log.error("onClientAuthenticate error: ", e))
                .subscribe(r -> {
                    responseObserver.onNext(ValuedResponse.newBuilder()
                            .setType(ValuedResponse.ResponsedType.CONTINUE)
                            .setBoolResult(true)
                            .build());

                }, responseObserver::onError, responseObserver::onCompleted);
    }

    @Override
    public void onClientAuthorize(ClientAuthorizeRequest request, StreamObserver<ValuedResponse> responseObserver) {
        log.debug("onClientAuthorize: {}", request);
        ValuedResponse.Builder valueRespBuilder = ValuedResponse.newBuilder()
                .setType(ValuedResponse.ResponsedType.CONTINUE);
        if (request.getType() == ClientAuthorizeRequest.AuthorizeReqType.SUBSCRIBE) {
            ClientInfo clientinfo = request.getClientinfo();
//            "$friend/username/clientId
//            "$group/groupId
//            "$event/user/name/clientId
            String[] topicTokens = request.getTopic().split("/");
            String first = topicTokens[0];
            if (first.equals("$friend") || first.equals("$event")) {
                if (topicTokens.length == 3
                        && topicTokens[1].equals(clientinfo.getUsername())
                        && topicTokens[2].equals(clientinfo.getClientid())) {
                    responseObserver.onNext(valueRespBuilder.setType(ValuedResponse.ResponsedType.CONTINUE)
                            .setBoolResult(true).build());
                } else {
                    responseObserver.onNext(valueRespBuilder.setType(ValuedResponse.ResponsedType.CONTINUE)
                            .setBoolResult(false).build());
                }
            } else {
                // TODO: 2022/11/27 add support for groups
                responseObserver.onNext(valueRespBuilder.setBoolResult(false).build());
            }
            responseObserver.onCompleted();
        } else if (request.getType() == ClientAuthorizeRequest.AuthorizeReqType.PUBLISH) {
            String senderName = request.getClientinfo().getUsername();
            String senderId = request.getClientinfo().getClientid();
            String[] topicLevel = request.getTopic().split("/");
            String receiverName = topicLevel[1];
            String receiverId = topicLevel[2];
            imAuthClient.canPublish(senderName, senderId, receiverName, receiverId)
                    .doOnNext(r -> log.debug("onClientAuthorize-publish res: {}", r))
                    .doOnError(e -> log.error("onClientAuthenticate-publish error: ", e))
                    .subscribe(r -> responseObserver.onNext(ValuedResponse.newBuilder()
                            .setType(ValuedResponse.ResponsedType.CONTINUE)
                            .setBoolResult(r)
                            .build()), responseObserver::onError, responseObserver::onCompleted);
        }
    }

    @Override
    public void onMessagePublish(MessagePublishRequest request, StreamObserver<ValuedResponse> responseObserver) {
        log.debug("onMessagePublish: {}", request);
        responseObserver.onNext(ValuedResponse.newBuilder()
                .setType(ValuedResponse.ResponsedType.CONTINUE)
                .setBoolResult(true).build());
        responseObserver.onCompleted();
//        imStoreServiceStub.saveOfflineMsg(SaveOfflineMsgReq.newBuilder()
//                .addMessage(Message.newBuilder()
//                        .setContent(ByteString.copyFromUtf8("this"))
//                        .setSeq(8)
//                        .build())
//                .build(), new StreamObserver<SaveOfflineMsgResp>() {
//            @Override
//            public void onNext(SaveOfflineMsgResp value) {
//                System.out.println("onNext: " + value);
//            }
//
//            @Override
//            public void onError(Throwable t) {
//                System.out.println("onError: " + t);
//            }
//
//            @Override
//            public void onCompleted() {
//                System.out.println("onCompleted");
//            }
//        });
    }

}
