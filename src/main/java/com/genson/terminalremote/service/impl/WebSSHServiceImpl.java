package com.genson.terminalremote.service.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.genson.terminalremote.constant.ConstantPool;
import com.genson.terminalremote.model.SSHConnectInfo;
import com.genson.terminalremote.model.WebSSHData;
import com.genson.terminalremote.service.WebSSHService;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Service
public class WebSSHServiceImpl implements WebSSHService {
    //map lưu thông tin ssh connection
    private static Map<String, Object> sshMap = new ConcurrentHashMap<>();

    private Logger logger = LoggerFactory.getLogger(WebSSHServiceImpl.class);
    // Thread pool
    private ExecutorService executorService = Executors.newCachedThreadPool();


    // khởi tạo connection
    @Override
    public void initConnection(WebSocketSession session) {
        JSch jSch = new JSch();
        SSHConnectInfo sshConnectInfo = new SSHConnectInfo();
        sshConnectInfo.setjSch(jSch);
        sshConnectInfo.setWebSocketSession(session);
        String uuid = String.valueOf(session.getAttributes().get(ConstantPool.USER_UUID_KEY));
        // put thông tin ssh connection vào map
        sshMap.put(uuid, sshConnectInfo);
    }

    // xử lý data được gửi bởi connection
    @Override
    public void recvHandle(String buffer, WebSocketSession session) {
        ObjectMapper objectMapper = new ObjectMapper();
        WebSSHData webSSHData = null;
        try {
            webSSHData = objectMapper.readValue(buffer, WebSSHData.class);
        } catch (IOException e) {
            logger.error("Json conversion exception");
            logger.error("Exception message:{}" , e.getMessage ());
            return;
        }
        String userId = String.valueOf(session.getAttributes().get(ConstantPool.USER_UUID_KEY));

        // xử lý connect operate
        if (ConstantPool.WEBSSH_OPERATE_CONNECT.equals(webSSHData.getOperate())) {
            //Lấy ssh connection đã lưu
            SSHConnectInfo sshConnectInfo = (SSHConnectInfo) sshMap.get(userId);
            //Start thread xử lý asynchronous
            WebSSHData finalWebSSHData = webSSHData;
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        connectToSSH(sshConnectInfo, finalWebSSHData, session);
                    } catch (JSchException | IOException e) {
                        logger.error ( "webssh connection exception" ) ;
                        logger.error ( "Exception message:{}" , e.getMessage ());
                        close(session);
                    }
                }
            });

            // xử lý command operate
        } else if (ConstantPool.WEBSSH_OPERATE_COMMAND.equals(webSSHData.getOperate())) {
            String command = webSSHData.getCommand();
            SSHConnectInfo sshConnectInfo = (SSHConnectInfo) sshMap.get(userId);
            if (sshConnectInfo != null) {
                try {
                    transToSSH(sshConnectInfo.getChannel(), command);
                    System.out.println("-------------||||||||||-----------------");
                    System.out.println(sshMap.toString());
                } catch (IOException e) {
                    logger.error ( "webssh connection exception" ) ;
                    logger.error ("Exception message:{}" , e.getMessage ());
                    close(session);
                }
            }
        } else {
            logger.error (" unsupported operation") ;
            close(session);
        }
    }

    @Override
    public void sendMessage(WebSocketSession session, byte[] buffer) throws IOException {
        session.sendMessage(new TextMessage(buffer));
    }

    @Override
    public void close(WebSocketSession session) {
        String userId = String.valueOf(session.getAttributes().get(ConstantPool.USER_UUID_KEY));
        SSHConnectInfo sshConnectInfo = (SSHConnectInfo) sshMap.get(userId);
        if (sshConnectInfo != null) {
            //Disconnect
            if (sshConnectInfo.getChannel() != null) sshConnectInfo.getChannel().disconnect();
            // xoá khởi map
            sshMap.remove(userId);
        }
    }

    // connect đến terminal
    private void connectToSSH(SSHConnectInfo sshConnectInfo, WebSSHData webSSHData, WebSocketSession webSocketSession) throws JSchException, IOException {
        Session session = null;
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        //Get session của jsch
        session = sshConnectInfo.getjSch().getSession(webSSHData.getUsername(), webSSHData.getHost(), webSSHData.getPort());
        session.setConfig(config);

        //set password
        session.setPassword(webSSHData.getPassword());

        //Connection timeout là 30s
        session.connect(30000);

        //Open shell channel
        Channel channel = session.openChannel("shell");

        //Channel connection timeout 3s
        channel.connect(3000);

        //set channel
        sshConnectInfo.setChannel(channel);

        //forward the message
        transToSSH(channel, "\r");

        //Đọc thông tin được trả lại bởi terminal
        InputStream inputStream = channel.getInputStream();
        try {
            //loop read
            byte[] buffer = new byte[1024];
            int i = 0;
            // nếu không có data thì thread sẽ luôn luôn block để đợi data
            while ((i = inputStream.read(buffer)) != -1) {
                sendMessage(webSocketSession, Arrays.copyOfRange(buffer, 0, i));
            }

        } finally {
            // đóng session sau khi disconnect
            session.disconnect();
            channel.disconnect();
            if (inputStream != null) {
                inputStream.close();
            }
        }

    }

    // forward message đến terminal
    private void transToSSH(Channel channel, String command) throws IOException {
        if (channel != null) {
            OutputStream outputStream = channel.getOutputStream();
            outputStream.write(command.getBytes());
            outputStream.flush();
        }
    }
}
