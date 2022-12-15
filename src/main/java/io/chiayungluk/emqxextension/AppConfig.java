package io.chiayungluk.emqxextension;

import io.chiayungluk.imauth.client.ImAuthClient;
//import io.chiayungluk.imstore.ImStoreServiceGrpc;
//import io.grpc.Grpc;
//import io.grpc.InsecureChannelCredentials;
//import io.grpc.ManagedChannel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class AppConfig {
    private int port;
    private String imStoreUri;

    private String imAuthUri;

//    @Bean
//    public ImStoreServiceGrpc.ImStoreServiceStub imStoreServiceStub() {
//        ManagedChannel channel = Grpc.newChannelBuilder(imStoreUri, InsecureChannelCredentials.create())
//                .build();
//        return ImStoreServiceGrpc.newStub(channel);
//    }

    @Bean
    public ImAuthClient imAuthClient() {
        return new ImAuthClient(imAuthUri);
    }
}
