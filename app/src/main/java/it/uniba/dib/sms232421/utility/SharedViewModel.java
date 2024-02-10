package it.uniba.dib.sms232421.utility;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;

import java.util.ArrayList;
import java.util.TreeMap;
import it.uniba.dib.sms232421.appointments.Appointment;
import it.uniba.dib.sms232421.exercises.Exercise;
import it.uniba.dib.sms232421.patient.child.ranking.Ranking;

public class SharedViewModel {
    private static SharedViewModel instance;

    private TreeMap<ArrayList<Object>, ArrayList<Exercise>> exercises;
    private ArrayList<Ranking> rankingList;
    private ArrayList<Appointment> appointments;
    private ArrayList<String> patients;
    private ArrayList<String> patientsId;

    // Costruttore
    private SharedViewModel() {

    }

    // Singleton getInstance
    public static synchronized SharedViewModel getInstance() {
        if (instance == null) {
            instance = new SharedViewModel();
        }
        return instance;
    }

    // Metodi getter e setter per exercises
    public TreeMap<ArrayList<Object>, ArrayList<Exercise>> getExercises() {
        return exercises;
    }

    public void setExercises(TreeMap<ArrayList<Object>, ArrayList<Exercise>> exercises) {
        this.exercises = exercises;
    }

    // Metodi getter e setter per rankingList
    public ArrayList<Ranking> getRankingList() {
        return rankingList;
    }

    public void setRankingList(ArrayList<Ranking> rankingList) {
        this.rankingList = rankingList;
    }

    // Metodi getter e setter per appointments
    public ArrayList<Appointment> getAppointments() {
        return appointments;
    }

    public void setAppointments(ArrayList<Appointment> appointments) {
        this.appointments = appointments;
    }

    // Metodi getter e setter per patients
    public ArrayList<String> getPatients() {
        return patients;
    }

    public void setPatients(ArrayList<String> patients) {
        this.patients = patients;
    }

    // Metodi getter e setter per patientsId
    public ArrayList<String> getPatientsId() {
        return patientsId;
    }

    public void setPatientsId(ArrayList<String> patientsId) {
        this.patientsId = patientsId;
    }

}
