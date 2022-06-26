package com.genson.terminalremote.websocket;

import com.genson.terminalremote.constant.ConstantPool;
import com.genson.terminalremote.service.WebSSHService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;


// WebSocket handler cho WebSSH
@Component
public class WebSSHWebSocketHandler implements WebSocketHandler{
    private final WebSSHService webSSHService;
    private Logger logger = LoggerFactory.getLogger(WebSSHWebSocketHandler.class);

    public WebSSHWebSocketHandler(WebSSHService webSSHService) {
        this.webSSHService = webSSHService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) throws Exception {
        logger.info ("User:{},Connecting WebSSH " , webSocketSession.getAttributes().get(ConstantPool.USER_UUID_KEY)) ;
        // khởi tạo kết nối ssh
        webSSHService.initConnection(webSocketSession);
    }

    // callback để receive message
    @Override
    public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage) throws Exception {
        if (webSocketMessage instanceof TextMessage) {
            logger.info( "User:{},Send Command: { }" , webSocketSession.getAttributes().get(ConstantPool.USER_UUID_KEY), webSocketMessage.toString()) ;
            //call service to receive message
            webSSHService.recvHandle(((TextMessage) webSocketMessage).getPayload(),webSocketSession);
        } else if (webSocketMessage instanceof BinaryMessage) {

        } else if (webSocketMessage instanceof PongMessage) {

        } else {
            System.out.println("Unexpected WebSocket message type: " + webSocketMessage);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) throws Exception {
        logger.error("Data transfer error") ;
    }

    @Override
    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) throws Exception {
        logger.info("User:{} disconnected from webssh" , String.valueOf (webSocketSession.getAttributes().get(ConstantPool.USER_UUID_KEY))) ;
        //đóng ssh connection
        webSSHService.close(webSocketSession);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
