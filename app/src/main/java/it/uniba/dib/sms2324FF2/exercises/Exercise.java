package it.uniba.dib.sms2324FF2.exercises;

import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

public class Exercise implements Serializable {
    //variabile intera che mi tiene traccia di che tipo di esercizio (dei 3 esistenti) si tratta
    private int type, coin;
    private String day;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private String answer;

    private String audio, first_img, second_img, first_hint, second_hint, third_hint;

    private String state, id;
    private boolean exerciseRight;

    private int num_hints_used; //numero di aiuti usati nello svolgimento dell'esercizio di tipo 1

    //Costruttore per esercizio di tipo 1, Image Denomination
    public Exercise(String id, int type, String state, Timestamp day, String first_img, String first_hint, String second_hint, String third_hint, int coin, boolean exerciseRight, int num_hints_used, String answer){
        this.id = id;
        this.type = type;
        this.state = state;
        this.day = dateFormat.format(day.toDate());
        this.first_img = first_img;
        this.first_hint = first_hint;
        this.second_hint = second_hint;
        this.third_hint = third_hint;
        this.coin = coin;
        this.exerciseRight = exerciseRight;
        this.num_hints_used = num_hints_used;
        this.answer = answer;
    }
    //Costruttore per esercizio di tipo 2, Minimal Pairs Recognition
    public Exercise(String id, int type, String state, Timestamp day, String audio, String first_img, String second_img, int coin, boolean exerciseRight, String answer){
        this.id = id;
        this.type = type;
        this.state = state;
        this.day = dateFormat.format(day.toDate());
        this.audio = audio;
        this.first_img = first_img;
        this.second_img = second_img;
        this.coin = coin;
        this.exerciseRight = exerciseRight;
        this.answer = answer;
    }
    //Costruttore per esercizio tipo 3, Word Sequence Repetition
    public Exercise(String id, int type, String state, Timestamp day, String audio, int coin, boolean exerciseRight, String answer){
        this.id = id;
        this.type = type;
        this.state = state;
        this.day = dateFormat.format(day.toDate());
        this.audio = audio;
        this.coin = coin;
        this.exerciseRight = exerciseRight;
        this.answer = answer;

    }

    public String getDay() {
        return day;
    }

    public String getAnswer() {
        return answer;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }
    public void setState(String state) {
        this.state = state;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public boolean isExerciseRight() {
        return exerciseRight;
    }

    public void setExerciseRight(boolean exerciseRight) {
        this.exerciseRight = exerciseRight;
    }

    public String getAudio() {
        return audio;
    }

    public String getFirst_img() {
        return first_img;
    }

    public String getSecond_img() {
        return second_img;
    }

    public String getState() {
        return state;
    }

    public int getCoin() {
        return coin;
    }

    public String getFirst_hint() {
        return first_hint;
    }

    public String getSecond_hint() {
        return second_hint;
    }

    public String getThird_hint() {
        return third_hint;
    }

    public int getNum_hints_used() {
        return num_hints_used;
    }

    public void setNum_hints_used(int num_hints_used) {
        this.num_hints_used = num_hints_used;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Exercise exercise = (Exercise) o;
        return id.equals(exercise.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
