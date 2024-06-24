package com.ezen.network.chat.client;

import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Frame을 이용한 채팅 메인 화면
 */
public class ChatFrame extends Frame{
    TextField nickNameTF, messageTF;
    Button loginButton, sendButton;
    TextArea messageTA;
    List nickNameList;
    Choice nickNameChoice;

    ChatClient chatClient;



    public ChatFrame(String title){
        super(title);
        nickNameTF =  new TextField("대화명을 입력하세요..");
        loginButton = new Button("로그인");
        sendButton = new Button("전  송");
        messageTA = new TextArea();
//        messageTA.setEnabled(false);
//        messageTA.setFont(new Font("Sans-serif", Font.PLAIN, 11));
        messageTA.setEditable(false);
        nickNameList = new List();
//        nickNameList.add("바나나");
//        nickNameList.add("오렌지");
//        nickNameList.add("토마토");
//        nickNameList.add("(T_T)");
//        nickNameList.add("＞_＜");
        nickNameList.setPreferredSize(new Dimension(100, 410));
//        nickNameList.add();

        messageTF = new TextField();


        nickNameChoice = new Choice();
        nickNameChoice.add("-전체에게-");
//        nickNameChoice.add();

        messageTF = new TextField("내용을 입력하세요...");
    }

    public void initComponents(){
        // 개발 편의상 레이아웃매니저 사용X
        // 좌표값 배치 (실제로 권장X)
//        setLayout(null);
//
//        nickNameTF.setBounds(30,45,220,25);
//        loginButton.setBounds(260,45,65,25);
//        messageTA.setBounds(30,80,220,250);
//        nicknameList.setBounds(260,80,65,250);
//        messageTF.setBounds(30,340,295,55);
//        sendButton.setBounds(260,400,65,25);

        // add까지 해야 실제로 붙어서 나온다
//        add(nickNameTF);
//        add(loginButton);
//        add(messageTA);
//        add(nickNameList);
//        add(messageTF);
//        add(sendButton);

        Panel topPanel = new Panel();
        topPanel.setLayout(new BorderLayout(5, 5));
        topPanel.add(nickNameTF, BorderLayout.CENTER);
        topPanel.add(loginButton,BorderLayout.EAST);

        Panel bottomPanel = new Panel();
        bottomPanel.setLayout(new BorderLayout(5, 5));
        bottomPanel.add(messageTF, BorderLayout.CENTER);
        bottomPanel.add(nickNameChoice, BorderLayout.WEST); //-------과제
        bottomPanel.add(sendButton, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
        add(messageTA, BorderLayout.CENTER);
        add(nickNameList, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);


    }

    /**
     * 로그인 처리
     */
    private void login(){
       chatClient = new ChatClient(this);  // chatClient.java
    //  chatClient = new ChatClient("192.168.0.10",2024, this);
        try {
            chatClient.connect();
            String inputNickName = nickNameTF.getText();
            chatClient.setNickName(inputNickName);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("command","CONNECT");
            jsonObject.put("nickName",inputNickName);
            chatClient.sendMessage(jsonObject.toString());
            nickNameTF.setEnabled(false); // 닉네임 수정불가
            loginButton.setEnabled(false);
            chatClient.receive();
        } catch (IOException e) {
            messageTA.append("[채팅서버]에 연결할 수 없습니다.\n");
            messageTA.append("네트워크 상태를 확인하여 주세요.\n");
        }
    }

    /**
     * 메시지 창에 메시지 출력 메소드
     */
    public void appendMessage(String message){
        messageTA.append(message + "\n");
    }

    /**
     * 채팅메시지 전송
     */
    private  void sendMessage(){
        String inputMessage = messageTF.getText();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("command", "MULTI_CHAT");
        jsonObject.put("nickName", chatClient.getNickName());
        jsonObject.put("message", inputMessage);
        try {
            chatClient.sendMessage(jsonObject.toString());
            messageTF.setText("");
        } catch (IOException e) { }
    }

    /**
     * DM 전송 메소드
     */
    private void sendDirectMessage(String receiverNickName){
        String inputMessage = messageTF.getText();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("command", "DM"); // 개별 메시지를 보내는 명령으로 변경
        jsonObject.put("nickName", chatClient.getNickName());
        jsonObject.put("message", inputMessage);
        jsonObject.put("receiver", nickNameChoice.getSelectedItem()); // 수신자의 닉네임을 전달
        try {
            chatClient.sendMessage(jsonObject.toString());
            messageTF.setText("");
        } catch (IOException e) { }
    }


    /**
     * 윈도우 종료
     */
    private void exit(){
        if(chatClient != null) {
            // 서버에 연결 종료메시지 전송
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("command", "DIS_CONNECT");
            jsonObject.put("nickName", chatClient.getNickName());
            try {
                chatClient.sendMessage(jsonObject.toString());
                chatClient.unConnect();

            } catch (IOException e) {
            }
        }
        setVisible(false);
        dispose();
        System.exit(0);
    }



    public void addEventRegister(){
        // 종료 이벤트 처리
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });

        // 대화명 클릭 필드 초기화 이벤트 처리
        nickNameTF.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                nickNameTF.setText("");
            }
        });

        // 로그인 아이디 입력 엔터 이벤트 처리
        nickNameTF.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });

        // 로그인 버튼 이벤트 처리
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 여기는 서버도 연결해야 하고 해야할 일이 많음!! 그래서 따로 메소드 만들기
                login();

            }
        });

        // 메시지 전송버튼 이벤트 처리
        messageTF.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 얘도 마찬가지로 바쁘다. 메소드 따로 생성

                if (nickNameChoice.getSelectedIndex() != 0){
                    sendDirectMessage(nickNameChoice.getSelectedItem());
                } else {
                    sendMessage();
                }

            }
        });


        // 대화명 클릭 필드 초기화 이벤트 처리
        messageTF.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                messageTF.setText("");
            }
        });

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

    }


}
