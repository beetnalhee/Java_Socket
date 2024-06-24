package com.ezen.network.chat.server;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * 채팅서버에 연결한 클라이언트와 소켓을 이용한 1:1 메시지 송수신 (스레드 아님)
 * 채팅 서버 관점에서 SocketClient는 접속한 클라이언트를 의미한다.
 */
public class SocketClient /*extends Thread*/ {

    private ChatServer chatServer;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean stop;


    private String clientIp; // 접속 클라이언트 아이피
    private String nickName; // 접속 클라이언트 대화명

    public SocketClient(Socket socket, ChatServer chatServer) {
        this.socket = socket;
        this.chatServer = chatServer;
        InetSocketAddress isa = (InetSocketAddress) socket.getRemoteSocketAddress();
        clientIp = isa.getAddress().getHostAddress();
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            receiveNsendMessage(); // 스레드가 아니게 되어서
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getClientIp() {
        return clientIp;
    }

    public String getNickName() {
        return nickName;
    }

    /**
     * 채팅 클라이언트가 전송한 다양한 메시지 수신 및 전송
     */
    private void receiveNsendMessage() {
        // 스레드풀에 관리되고 있는 스레드를 통해 실행할 작업
        ExecutorService threadPool = chatServer.getThreadPool();
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                String messageDelimiter = "\\|\\*\\|";
                try {
                    while (!stop) {
                        String jsonMessage = in.readUTF();
                        // 디버깅 차원의 클라이언트 파싱되지 않은 메시지 출력
                        System.out.println("[채팅서버] ChatClient -> ChatServer : " + jsonMessage);

                        // 클라이언트 전송한 JSON파싱 (비교해보기)
                        JSONObject jsonObject = new JSONObject(jsonMessage);
                        String command = jsonObject.getString("command");
                        nickName = jsonObject.getString("nickName");
                        switch (command) {
                            case "CONNECT":
                                chatServer.addSocketClient(SocketClient.this); // 연결시 map에 담아주는 메소드
                                // ----- 현재 접속한 클라이언트 목록(닉네임) 챗서버==================
                                List<String> clients = chatServer.getSocketClients();

                                JSONArray jsonArray = new JSONArray();
                                for (Object nickName : clients) {
                                    jsonArray.put(nickName); // 접속사리스트 불러와서 어레이에 담아주는 작업
                                }
                                jsonObject.put("command","CONNECTION_LIST");
                                jsonObject.put("nickName","SERVER"); // 서버에서 보내주는 목록(임의)
                                jsonObject.put("list",jsonArray);
                                chatServer.sendAllMessage(jsonMessage);
                                chatServer.sendAllMessage(jsonObject.toString());
                                break;
                            case "MULTI_CHAT":
                                String chatMessage = jsonObject.getString("message");
                                chatServer.sendAllMessage(jsonMessage);
                                break;

                            case "DM" :
                                String directMessage = jsonObject.getString("message");
                                String receiverName = jsonObject.getString("receiver");
                                chatServer.sendDirectMessage(receiverName,nickName,jsonMessage);
                                break;

                            case "DIS_CONNECT": // map에서 소켓클라이언트 제거함
                                chatServer.sendAllMessage(jsonMessage);
                                chatServer.removeSocketClient(SocketClient.this);
                                return;
                        }
                    }
                } catch (IOException e) {

                } finally {
                    close();
                }

            }
        });

    }

    /**
     * 현재 연결한 클라이언트에게 메시지 전송 (1:1)
     *
     * @param message
     * @throws IOException
     */
    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
            out.flush();
        } catch (IOException e) {
        }

    }

    public void close() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
        }
    }

}
