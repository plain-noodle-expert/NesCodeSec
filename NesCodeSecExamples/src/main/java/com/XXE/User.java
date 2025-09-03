package com.XXE;

public class User {
    private String username;
    private String data;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}