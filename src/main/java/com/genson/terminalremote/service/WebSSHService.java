package com.genson.terminalremote.service;

import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;


// business logic của ssh
public interface WebSSHService {

    // khởi tạo một ssh connection
    public void initConnection(WebSocketSession session);

    // xử lý data được gửi bởi client segment
    public void recvHandle(String buffer, WebSocketSession session);

    // gửi data lại phía frontend
    public void sendMessage(WebSocketSession session, byte[] buffer) throws IOException;

    // đóng kết nối
    public void close(WebSocketSession session);
}
