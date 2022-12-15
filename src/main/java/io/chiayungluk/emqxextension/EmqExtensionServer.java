package io.chiayungluk.emqxextension;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class EmqExtensionServer {
    private Server server;

    private final int port;

    private final HookProviderService hookProviderService;

    public EmqExtensionServer(HookProviderService hookProviderService, AppConfig appConfig) {
        this.hookProviderService = hookProviderService;
        this.port = appConfig.getPort();
    }

    public void start() throws IOException, InterruptedException {
        server = ServerBuilder.forPort(port)
                .addService(hookProviderService)
                .build()
                .start();
        log.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            try {
                EmqExtensionServer.this.stop();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
            System.err.println("*** server shut down");
        }));
    }

    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

}
