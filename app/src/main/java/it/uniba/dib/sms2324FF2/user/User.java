package it.uniba.dib.sms2324FF2.user;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class User {
    private static User instance;
    public static FirebaseUser userLogged;

    private String id, role, email, name, city, address;


    private User(String id, String role){
        this.id = id;
        this.role = role;
        this.email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    }
    private User(String id, String role, String name, String city, String address){
        this.id = id;
        this.role = role;
        this.email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        this.name = name;
        this.city = city;
        this.address = address;
    }

    public static synchronized User getInstance(String id, String role) {
        if (instance == null) {
            instance = new User(id, role);
        }
        return instance;
    }
    public static synchronized User getInstance(String id, String role, String name, String city, String address) {
        if (instance == null) {
            instance = new User(id, role, name, city, address);
        }
        return instance;
    }
    public static synchronized User getInstance() {
        if (instance == null) {
            throw new IllegalStateException("User instance not initialized. Call getInstance(String, String, String, String, String, String) first.");
        }
        return instance;
    }

    public static void setUser(FirebaseUser user){
        userLogged = user;
    }

    public static synchronized  void disconnect(){
        if (instance == null) {
            throw new IllegalStateException("User instance not initialized. Call getInstance(String, String, String, String, String, String) first.");
        }
        instance = null;
    }
    public String getId() {
        return id;
    }

    public String getRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }

    public String getName(){
        return name;
    }

    public String getCity() {
        return city;
    }

    public String getAddress() {
        return address;
    }
}
