package it.uniba.dib.sms2324FF2.patient;

import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class Patient {
    private static Patient instance;
    public static FirebaseUser userLogged;

    private String parentToken, childToken;

    private String childName, parentName;
    private String id;
    private String doctorId;
    private String theme;
    private int coin;
    private ArrayList<String> characters_unlocked;

    private Patient(String id, String childName,String parentName, String doctorId, String theme, int coin, ArrayList<String> characters_unlocked, String parentToken, String childToken){
        this.id = id;
        this.childName = childName;
        this.parentName = parentName;
        this.doctorId = doctorId;
        this.theme = theme;
        this.coin = coin;
        this.characters_unlocked = characters_unlocked;
        this.parentToken = parentToken;
        this.childToken = childToken;
    }
    public static synchronized Patient getInstance(String id, String childName,String parentName, String doctorId, String theme, int coin, ArrayList<String> characters_unlocked,String parentToken, String childToken) {
        if (instance == null) {
            instance = new Patient(id, childName,parentName, doctorId, theme, coin, characters_unlocked, parentToken, childToken);
        }
        return instance;
    }

    public static synchronized Patient getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Patient instance not initialized. Call getInstance(String, String, String, String, int, ArrayList<String>) first.");
        }
        return instance;
    }

    public static void setUser(FirebaseUser user){
        userLogged = user;
    }

    public static synchronized  void disconnect(){
        if (instance == null) {
            throw new IllegalStateException("Patient instance not initialized. Call getInstance(String, String, String, String, int, ArrayList<String>) first.");
        }
        instance = null;
    }

    public String getParentName() {
        return parentName;
    }


    public String getId() {
        return id;
    }


    public String getChildName() {
        return childName;
    }


    public String getDoctorId() {
        return doctorId;
    }


    public String getTheme() {
        return theme;
    }


    public int getCoin() {
        return coin;
    }

    public void setCoin(int coin) {
        this.coin = coin;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public ArrayList<String> getCharacters_unlocked() {
        return characters_unlocked;
    }

    public void addCharacters_unlocked(String character) {
        this.characters_unlocked.add(character);
    }


}
