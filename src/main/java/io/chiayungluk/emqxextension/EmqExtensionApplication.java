package io.chiayungluk.emqxextension;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

@Slf4j
@SpringBootApplication
public class EmqExtensionApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(EmqExtensionApplication.class, args);
        AppConfig appConfig = ctx.getBean(AppConfig.class);
        HookProviderService hookProviderService = ctx.getBean(HookProviderService.class);
        try {
            EmqExtensionServer emqExtensionServer = new EmqExtensionServer(hookProviderService, appConfig);
            emqExtensionServer.start();
            emqExtensionServer.blockUntilShutdown();
        } catch (IOException | InterruptedException e) {
            log.error("EmqExtension Server start failed", e);
            throw new RuntimeException(e);
        }
    }

}
