package com.skripsi.Fluency.Config;

import com.corundumstudio.socketio.SocketIOServer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class WebSocketHandler {

    private final SocketIOServer server;

    @Autowired
    public WebSocketHandler(SocketIOServer server) {
        this.server = server;
    }

    @PostConstruct
    public void startServer(){
        server.addEventListener("clientMessage", String.class, (client, data, ackRequest) -> {
            System.out.println("Received message: " + data);
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            client.sendEvent("responseMessage", currentTime);
        });

        server.addConnectListener(client -> {
            System.out.println("Client connected: " + client.getSessionId());
        });

        server.addDisconnectListener(client -> {
            System.out.print("Client disconnected: " + client.getSessionId());
        });

        server.start();
    }

    @PreDestroy
    private void stopServer(){
        server.stop();
    }
}
