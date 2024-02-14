package it.uniba.dib.sms2324FF2.appointments;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import it.uniba.dib.sms2324FF2.user.User;

public class Appointment implements Serializable, Comparable<Appointment> {
    private String city, address, date, hour, doctorName, patientName, patientId, doctorId;


    public Appointment(String city, String address, String date, String hour, String name){
        this.city = city;
        this.address = address;
        this.date = date;
        this.hour = hour;
        //ci interessa solo uno dei due, a seconda che l'appuntamento venga visualizzato dal genitore o dal logopedista
        this.doctorName = name;
        this.patientName = name;
    }
    //Secondo costruttore
    public Appointment(String city, String address, Calendar calendar, String name, String patientId){
        this.city = city;
        this.address = address;
        // Formatto data e ora
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        this.date = dateFormat.format(calendar.getTime());
        this.hour = timeFormat.format(calendar.getTime());
        //ci interessa solo uno dei due, a seconda che l'appuntamento venga visualizzato dal genitore o dal logopedista
        this.doctorName = name;
        this.patientName = name;
        this.patientId = patientId;
        this.doctorId = User.getInstance().getId();
    }
    public Appointment(String city, String address, String date, String hour, String name, String patientId){
        this.city = city;
        this.address = address;
        this.date = date;
        this.hour = hour;
        this.patientId = patientId;
        this.patientName = name;
    }
    @Override
    public int compareTo(Appointment other) {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        try {
            // Confronto le date
            Date thisDateTime = dateTimeFormat.parse(this.date + " " + this.hour);
            Date otherDateTime = dateTimeFormat.parse(other.date + " " + other.hour);
            int dateComparison = thisDateTime.compareTo(otherDateTime);

            if (dateComparison != 0) {
                // Se le date sono diverse, restituisci il risultato del confronto delle date
                return dateComparison;
            } else {
                // Se le date sono uguali, confronta le ore
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                Date thisTime = timeFormat.parse(this.hour);
                Date otherTime = timeFormat.parse(other.hour);
                return thisTime.compareTo(otherTime);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public String getCity() {
        return city;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }
}
