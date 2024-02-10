package it.uniba.dib.sms232421.patient.child.ranking;

import java.io.Serializable;

public class Ranking implements Serializable {
    private String name;
    private String id;
    private int coin;

    public Ranking(){

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getCoin() {
        return coin;
    }

    public void setCoin(int coin) {
        this.coin = coin;
    }
}
