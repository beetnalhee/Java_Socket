package com.ezen.network.chat.client;

public class EzenTalk2 {


    public static void main(String[] args) {

        ChatFrame chatFrame = new ChatFrame(":::EzenTalk:::"); // 타이틀
        chatFrame.initComponents();
        chatFrame.addEventRegister();
        chatFrame.setSize(400,500);
 //       chatFrame.setResizable(false); // 좌표값 배치시, 화면조정 불가
        chatFrame.setVisible(true);


    }
}
