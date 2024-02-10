package it.uniba.dib.sms232421.appointments;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import it.uniba.dib.sms232421.exercises.FirebaseExerciseModel;
import it.uniba.dib.sms232421.patient.FirebasePatientModel;
import it.uniba.dib.sms232421.user.User;
import it.uniba.dib.sms232421.patient.Patient;

public class FirebaseAppointmentsModel {
    static FirebaseFirestore db = FirebaseFirestore.getInstance();
    static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    static DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());

    private static final String APPOINTMENTS_COLLECTION = "appointments";



    public interface OnReadAppointmentsListener {
        void onAppointmentsRead(ArrayList<Appointment> appointments);
        void onAppointmentsReadFailed();
    }
    public interface OnAddAppointmentsListener {
        void onAppointmentsAddSuccess();
        void onAppointmentsAddFailure();
    }

    public static void getPatientAppointments(OnReadAppointmentsListener listener){
        db.collection("appointments")
                .whereEqualTo("doctor_id", Patient.getInstance().getDoctorId())
                .whereEqualTo("patient_id", Patient.getInstance().getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            ArrayList<Appointment> appointments = new ArrayList<>();
                            for(QueryDocumentSnapshot document : task.getResult()){
                                Appointment appointment = new Appointment(
                                        document.getString("city"),
                                        document.getString("address"),
                                        dateFormat.format(((Timestamp)(document.get("date"))).toDate()),
                                        timeFormat.format(((Timestamp)(document.get("date"))).toDate()),
                                        document.getString("doctor_name"));

                                appointments.add(appointment);
                            }
                            Collections.sort(appointments);
                            listener.onAppointmentsRead(appointments);
                        }else{
                            listener.onAppointmentsReadFailed();
                        }
                    }
                });
    }

    public static void addAppointment(Appointment appointment, OnAddAppointmentsListener listener) {
        // Controlla che l'appuntamento non sia null
        if (appointment == null) {
            listener.onAppointmentsAddFailure();
        }

        //creo il timestamp per la data
        // Combinare la data e l'ora in una stringa nel formato "yyyy-MM-dd HH:mm:ss"
        String combinedDateTimeString = new StringBuilder().append(appointment.getDate()).append(" ").append(appointment.getHour()).append(":00").toString();
        // Formattare la data combinata
        SimpleDateFormat combinedDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date combinedDateTime = null;
        try {
            combinedDateTime = combinedDateTimeFormat.parse(combinedDateTimeString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // Creare un oggetto Timestamp
        Timestamp timestamp = null;
        if (combinedDateTime != null)
            timestamp = new Timestamp(combinedDateTime);

        //creo l'appuntamento
        Map<String, Object> data = new HashMap<>();
        data.put("city", appointment.getCity());
        data.put("address", appointment.getAddress());
        data.put("date", timestamp);
        data.put("patient_name", appointment.getPatientName());
        data.put("doctor_name", User.getInstance().getName());
        data.put("doctor_id", appointment.getDoctorId());
        data.put("patient_id",appointment.getPatientId());

        // Aggiungi un documento alla collezione "appointments"
        db.collection("appointments")
                .add(data)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if(task.isSuccessful()){
                            listener.onAppointmentsAddSuccess();
                        } else {
                            listener.onAppointmentsAddFailure();
                        }
                    }
                });
    }

    public static void getDoctorAppointments(OnReadAppointmentsListener listener){
        db.collection("appointments")
                .whereEqualTo("doctor_id", User.getInstance().getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            ArrayList<Appointment> appointments = new ArrayList<>();
                            for(QueryDocumentSnapshot document : task.getResult()){
                                Appointment appointment = new Appointment(
                                        document.getString("city"),
                                        document.getString("address"),
                                        dateFormat.format(((Timestamp)(document.get("date"))).toDate()),
                                        timeFormat.format(((Timestamp)(document.get("date"))).toDate()),
                                        document.getString("patient_name"),
                                        document.getString("patient_id"));

                                appointments.add(appointment);
                            }
                            Collections.sort(appointments);
                            listener.onAppointmentsRead(appointments);
                        }else{
                            listener.onAppointmentsReadFailed();
                        }
                    }
                });
    }

    public interface OnDeleteListener {
        void onAppointmentDeleted();
        void onAppointmentDeleteFailed();
    }
    public static void deleteAppointment(Appointment appointment, OnDeleteListener listener){
        String address = appointment.getAddress();
        String city = appointment.getCity();
            //creo il timestamp per la data
            // Combinare la data e l'ora in una stringa nel formato "yyyy-MM-dd HH:mm:ss"
            String combinedDateTimeString = new StringBuilder().append(appointment.getDate()).append(" ").append(appointment.getHour()).append(":00").toString();
            // Formattare la data combinata
            SimpleDateFormat combinedDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd h:mm a:ss", Locale.getDefault());
            Date combinedDateTime = null;
            try {
                combinedDateTime = combinedDateTimeFormat.parse(combinedDateTimeString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            // Creare un oggetto Timestamp
            Timestamp date = null;
            if (combinedDateTime != null)
                date = new Timestamp(combinedDateTime);
        String doctorId = User.getInstance().getId();
        String patientId = appointment.getPatientId();
        db.collection("appointments")
                .whereEqualTo("address", address)
                .whereEqualTo("city",city)
                .whereEqualTo("date",date)
                .whereEqualTo("doctor_id",doctorId)
                .whereEqualTo("patient_id",patientId)

                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            String documentId = null;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Ottieni l'ID del documento trovato
                                documentId = document.getId();
                                db.collection("appointments").document(documentId).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            listener.onAppointmentDeleted();
                                        } else {
                                            listener.onAppointmentDeleteFailed();
                                        }
                                    }
                                });
                            } if (task.getResult().size() == 0)
                                listener.onAppointmentDeleteFailed();

                        } else {
                            listener.onAppointmentDeleteFailed();
                        }
                    }
                });
    }

    public static void deleteAllAppointments(String patientId, OnDeleteListener listener){
        db.collection("appointments")
                .whereEqualTo("patient_id", patientId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            String documentId = null;
                            int counter = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Ottieni l'ID del documento trovato
                                documentId = document.getId();
                                db.collection("appointments").document(documentId).delete();
                                counter++;
                            }
                            if(counter == task.getResult().size())
                                listener.onAppointmentDeleted();
                            else
                                listener.onAppointmentDeleteFailed();
                        } else {
                            listener.onAppointmentDeleteFailed();
                        }
                    }
                });
    }

}
