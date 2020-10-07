package com.example.avi.ChatRoom;


import java.util.List;

public class User {
    String id;
    String name;
    String email;
    List<String> friends;
    List<String> requests;

    public User() {}

    public User(String id, String name, String email, List<String> friends, List<String> requests){
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

    public List<String> getFriends(){
        return this.friends;
    }

    public List<String> getRequests(){
        return this.requests;
    }

}
