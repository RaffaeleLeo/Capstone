package com.example.avi.ChatRoom;

public class Message {

     String message;
     String sender;
     boolean isCurrentUser;

     public Message(String msg, String sndr, boolean isCurrUser){
        this.message = msg;
        this.sender = sndr;
        this.isCurrentUser = isCurrUser;
     }

     public String getMessage(){
         return this.message;
     }

     public String getSender(){
         return this.sender;
     }

     public boolean isCurrentUser(){ return this.isCurrentUser; }

     public void setSender(String sndr){
         this.sender = sndr;
     }



}
