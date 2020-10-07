package com.example.avi.ChatRoom;


import java.util.HashMap;

public class User {
    String id;
    String name;
    String email;
    HashMap<String, String> friends;
    HashMap<String, String> requests;

    public User() {}

    public User(String id, String name, String email, HashMap<String, String> friends, HashMap<String, String> requests){
        this.id = id;
        this.name =  name;
        this.email = email;
        this.friends = friends;
        this.requests = requests;
    }

    public String getId(){
        return this.id;
    }

    public String getName(){
        return this.name;
    }

    public String getEmail(){ return this.email; }

    public HashMap<String, String> getFriends(){
        return this.friends;
    }

    public HashMap<String, String> getRequests(){
        return this.requests;
    }

}
