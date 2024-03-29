package it.uniba.dib.sms2324FF2.utility;

import java.util.ArrayList;
import java.util.TreeMap;
import it.uniba.dib.sms2324FF2.therapist.appointments.TherapistAppointment;
import it.uniba.dib.sms2324FF2.patient.child.exercises.Exercise;
import it.uniba.dib.sms2324FF2.patient.child.ranking.Ranking;

public class SharedViewModel {
    private static SharedViewModel instance;

    private TreeMap<ArrayList<Object>, ArrayList<Exercise>> exercises;
    private ArrayList<Ranking> rankingList;
    private ArrayList<TherapistAppointment> therapistAppointments;
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
    public ArrayList<TherapistAppointment> getAppointments() {
        return therapistAppointments;
    }

    public void setAppointments(ArrayList<TherapistAppointment> therapistAppointments) {
        this.therapistAppointments = therapistAppointments;
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
