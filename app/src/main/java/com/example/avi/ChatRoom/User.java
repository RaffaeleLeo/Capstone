package com.example.avi.ChatRoom;

public class User {
    String nickname;
    String profileUrl;


    public User(String name, String url){
        this.nickname = name;
        this.profileUrl = url;
    }

    public String getNickname(){
        return this.nickname;
    }

    public String getProfileUrl(){
        return this.profileUrl;
    }
}
