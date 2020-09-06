package com.example.avi.ChatRoom;

public class Message {

    String message;
     User sender;
     boolean isCurrentUser;

     public Message(String msg, User usr, boolean isCurrUser){
        this.message = msg;
        this.sender = usr;
        this.isCurrentUser = isCurrUser;
     }

     public String getMessage(){
         return this.message;
     }

     public User getUser(){
         return this.sender;
     }

     public boolean getCreatedAt(){
         return this.isCurrentUser;
     }



}
