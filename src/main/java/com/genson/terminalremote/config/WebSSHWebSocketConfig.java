package com.genson.terminalremote.config;

import com.genson.terminalremote.interceptor.WebSocketInterceptor;
import com.genson.terminalremote.websocket.WebSSHWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSSHWebSocketConfig implements WebSocketConfigurer {
    final
    WebSSHWebSocketHandler webSSHWebSocketHandler;

    public WebSSHWebSocketConfig(WebSSHWebSocketHandler webSSHWebSocketHandler) {
        this.webSSHWebSocketHandler = webSSHWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry.addHandler(webSSHWebSocketHandler, "/webssh")
                .addInterceptors(new WebSocketInterceptor())
                .setAllowedOrigins("*");
    }
}
