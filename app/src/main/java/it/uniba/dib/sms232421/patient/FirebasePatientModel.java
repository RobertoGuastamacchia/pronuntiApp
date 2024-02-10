package it.uniba.dib.sms232421.patient;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import it.uniba.dib.sms232421.appointments.FirebaseAppointmentsModel;
import it.uniba.dib.sms232421.user.User;


public class FirebasePatientModel {
    static FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface OnPatientUpdateListener {
        void onPatientUpdate(boolean result);
    }
    public interface TokenListener {
        void onTokenReceived(String token);
        void onTokenReceiveFailed();
    }
    public interface PatientDocumentListener {
        void onDocumentReceived(String patientDocument);
        void onDocumentReceiveFailed();
    }
    public interface UpdateTokenListener {
        void onTokenChecked(boolean isTokenDifferent);
        void onTokenCheckFailed();
    }
    public interface  OnDocumentIdListener {
        void onDocumentIdRead(String documentId);
    }
    public interface TransactionCallback {
        void onTransactionSuccess();

        void onTransactionFailure();
    }
    public interface OnCharactersListener<T> {
        void onCharactersRead(T charactersRead);
        void onCharactersReadFailure(T value);
    }
    public interface OnPatientNameListener {
        void onNameRead(String name);
        void onNameReadFailed();
    }
    public interface OnUserCreatedListener {
        void onUserCreated(Patient patient);
        void onUserCreationFailed();
    }
    public interface OnRefreshListener {
        void onRefreshSuccess();
        void onRefreshFailure();
    }

    public static void createPatient( OnUserCreatedListener listener) {
        String id = User.getInstance().getId();

        db.collection("users")
                .whereEqualTo("id", id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        for (QueryDocumentSnapshot user : querySnapshot) {
                            Patient patient = Patient.getInstance(
                                    user.getString("id"),
                                    user.getString("child_name"),
                                    user.getString("parent_name"),
                                    user.getString("doctor_id"),
                                    user.getString("theme"),
                                    user.getLong("coin").intValue(),
                                    (ArrayList<String>) user.get("characters_unlocked"),
                                    user.getString("parent_token"),
                                    user.getString("child_token"));

                            if (listener != null) {
                                listener.onUserCreated(patient);
                            }
                        }
                    }else{
                        if (listener != null) {
                            listener.onUserCreationFailed();
                        }
                    }
                });
    }
    //metodo per aggiornare i dati del paziente
    public static void updatePatientData(OnPatientUpdateListener listener) {
        db.collection("users")
                .whereEqualTo("id", Patient.getInstance().getId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        for (QueryDocumentSnapshot user : querySnapshot) {
                            Patient.getInstance().setCoin(user.getLong("coin").intValue());
                            Patient.getInstance().setTheme(user.getString("theme"));

                            if (listener != null) {
                                listener.onPatientUpdate(true);
                            }
                        }
                    }else{
                        if (listener != null) {
                            listener.onPatientUpdate(false);
                        }
                    }
                });
    }

    public static void getChildToken(String patient, TokenListener listener){
        db.collection("users")
                .whereEqualTo("id", patient)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){
                                String token = document.getString("child_token");
                                listener.onTokenReceived(token);
                            }
                        } else{
                            listener.onTokenReceiveFailed();
                        }
                    }
                });
    }
    public static void getParentToken(String patient, TokenListener listener){
        db.collection("users")
                .whereEqualTo("id", patient)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){
                                String token = document.getString("parent_token");
                                listener.onTokenReceived(token);
                            }
                        }else{
                            listener.onTokenReceiveFailed();
                        }
                    }
                });
    }

    public static void getPatientDocument(PatientDocumentListener listener){
        db.collection("users")
                .whereEqualTo("id", Patient.getInstance().getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String patientDocument = document.getId();
                                listener.onDocumentReceived(patientDocument);
                            }
                        } else{
                            listener.onDocumentReceiveFailed();
                        }
                    }
                });
    }

    public static void updateChildToken(){
        getPatientDocument(new PatientDocumentListener() {
            @Override
            public void onDocumentReceived(String patientDocument) {
                FirebaseMessaging.getInstance().getToken()
                        .addOnSuccessListener(new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(String token) {
                                db.collection("users").document(patientDocument)
                                        .update("child_token", token);
                            }
                        });
            }

            @Override
            public void onDocumentReceiveFailed() {}
        });
    }
    public static void updateParentToken(){
        getPatientDocument(new PatientDocumentListener() {
            @Override
            public void onDocumentReceived(String patientDocument) {
                FirebaseMessaging.getInstance().getToken()
                        .addOnSuccessListener(new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(String token) {
                                db.collection("users").document(patientDocument)
                                        .update("parent_token", token);
                            }
                        });
            }

            @Override
            public void onDocumentReceiveFailed() {}
        });
    }
    public static void checkChildToken(UpdateTokenListener listener){
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String token) {
                        getChildToken(Patient.getInstance().getId(), new FirebasePatientModel.TokenListener() {
                            @Override
                            public void onTokenReceived(String childToken) {
                                listener.onTokenChecked(!token.equals(childToken));
                            }

                            @Override
                            public void onTokenReceiveFailed() {
                                listener.onTokenCheckFailed();
                            }
                        });
                    }
                });
    }
    public static void checkParentToken(UpdateTokenListener listener){
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String token) {
                        getParentToken(Patient.getInstance().getId(), new FirebasePatientModel.TokenListener() {
                            @Override
                            public void onTokenReceived(String parentToken) {
                                listener.onTokenChecked(!token.equals(parentToken));
                            }

                            @Override
                            public void onTokenReceiveFailed() {
                                listener.onTokenCheckFailed();
                            }
                        });
                    }
                });
    }
    public static void updateCharactersUnlocked(String character, TransactionCallback callback){
        getPatientDocument( new PatientDocumentListener() {
            @Override
            public void onDocumentReceived(String documentId) {
                db.runTransaction(transaction -> {
                    DocumentReference docRef = db.collection("users").document(documentId);

                    DocumentSnapshot snapshot = transaction.get(docRef);
                    String fieldName = "characters_unlocked";

                    // Verifica se il campo array esiste nel documento
                    if (snapshot.contains(fieldName)) {
                        // Il campo esiste, aggiungi il nuovo valore all'array
                        ArrayList<String> currentArray = (ArrayList<String>) snapshot.get(fieldName);
                        if (currentArray != null) {
                            currentArray.add(character);
                            transaction.update(docRef, fieldName, currentArray);
                        }
                    }

                    return null;
                }).addOnSuccessListener(aVoid -> {
                    if (callback != null) {
                        callback.onTransactionSuccess();
                    }
                }).addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onTransactionFailure();
                    }
                });
            }
            @Override
            public void onDocumentReceiveFailed() {}
        });
    }

    public static void getPatientCharactersUnlocked(OnCharactersListener<ArrayList<String>> listener){
        String userId = Patient.getInstance().getId();
        if(userId!=null){
            db.collection("users")
                    .whereEqualTo("id",userId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()){
                                ArrayList<String> charactersUnlocked = null;
                                for(QueryDocumentSnapshot document : task.getResult()){
                                    charactersUnlocked = (ArrayList<String>) document.get("characters_unlocked");
                                }
                                listener.onCharactersRead(charactersUnlocked);
                            }else{
                                listener.onCharactersReadFailure(null);
                            }
                        }
                    });
        }
    }

    public static void getPatientPrincipalCharacters(OnCharactersListener<HashMap<String,String>> listener) {
        String userId = Patient.getInstance().getId();
        if (userId != null) {
            db.collection("users")
                    .whereEqualTo("id", userId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                HashMap<String,String> principalCharacters = null;
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    principalCharacters = (HashMap<String,String>) document.get("principal_characters");
                                }
                                listener.onCharactersRead(principalCharacters);
                            } else {
                                listener.onCharactersReadFailure(null);
                            }
                        }
                    });
        }
    }

    public static void setPrincipalCharacter(String character, OnCharactersListener<Boolean> callback){
        getPatientDocument(new PatientDocumentListener() {
            @Override
            public void onDocumentReceived(String patientDocument) {
                db.runTransaction(transaction -> {
                    DocumentReference docRef = db.collection("users").document(patientDocument);

                    DocumentSnapshot snapshot = transaction.get(docRef);
                    String fieldName = "principal_characters";

                    // Verifica se il campo array esiste nel documento
                    if (snapshot.contains(fieldName)) {
                        // Il campo esiste, aggiungi il nuovo valore all'array
                       HashMap<String,String> map = (HashMap<String,String>) snapshot.get(fieldName);
                        if (map != null) {
                                String theme = (String) snapshot.get("theme");
                                if (theme.contains("_personalized"))
                                    theme = theme.replace("_personalized", "");
                                map.put(theme ,character);

                            transaction.update(docRef, fieldName, map);
                        }
                    }

                    return null;
                }).addOnSuccessListener(aVoid -> {
                    if (callback != null) {
                        callback.onCharactersRead(true);
                    }
                }).addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onCharactersReadFailure(false);
                    }
                });

            }
            @Override
            public void onDocumentReceiveFailed() {
                if (callback != null) {
                    callback.onCharactersReadFailure(false);
                }
            }
        });
    }

    public static void getPatientNameById(String id, OnPatientNameListener listener){
        db.collection("users")
                .whereEqualTo("id", id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){
                                listener.onNameRead(document.getString("name"));
                            }
                        } else{
                            listener.onNameReadFailed();
                        }
                    }
                });
    }

    public static void changeParentUsername(String newUsername, OnPatientUpdateListener listener){
        getPatientDocument(new PatientDocumentListener() {
            @Override
            public void onDocumentReceived(String patientDocument) {
                db.collection("users").document(patientDocument).update("parent_name",newUsername).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        listener.onPatientUpdate(task.isSuccessful());
                    }
                });
            }
            @Override
            public void onDocumentReceiveFailed() {}
        });
    }
    public static void changeChildUsername(String newUsername, OnPatientUpdateListener listener){
        getPatientDocument(new PatientDocumentListener() {
            @Override
            public void onDocumentReceived(String patientDocument) {
                db.collection("users").document(patientDocument).update("child_name",newUsername).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        listener.onPatientUpdate(task.isSuccessful());
                    }
                });
            }
            @Override
            public void onDocumentReceiveFailed() {}
        });
    }

    public static void updateTheme(String newTheme, OnPatientUpdateListener listener){
        getPatientDocument(new PatientDocumentListener() {
            @Override
            public void onDocumentReceived(String patientDocument) {
                db.collection("users").document(patientDocument).update("theme",newTheme).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        listener.onPatientUpdate(task.isSuccessful());
                    }
                });
            }

            @Override
            public void onDocumentReceiveFailed() {
            listener.onPatientUpdate(false);
            }
        });
    }

    public static void refreshPatient(OnRefreshListener listener){
        Patient.disconnect();
        createPatient(new OnUserCreatedListener() {
            @Override
            public void onUserCreated(Patient patient) {
                listener.onRefreshSuccess();
            }

            @Override
            public void onUserCreationFailed() {
                listener.onRefreshFailure();
            }
        });
    }

    public interface OnPatientCreatedListener {
        void onSuccess();
        void onFailure();
    }

    public static void addPatientData(String email, String childName, String parentName, String uid, OnPatientCreatedListener listener){

        ArrayList<String> charactersUnlocked = new ArrayList<>();

        HashMap<String, String> principalCharacters = new HashMap<>();
        principalCharacters.put("jungle","Jumbojango");
        principalCharacters.put("sea","Delphy");
        principalCharacters.put("space","Zorgon");

        String childToken = "", parentToken = "";
        String doctorId = User.getInstance().getId();
        String role = "patient";
        String theme = "jungle";

        HashMap<Object, Object> patient = new HashMap<>();
        patient.put("email", email);
        patient.put("child_name", childName);
        patient.put("parent_name", parentName);
        patient.put("id", uid);
        patient.put("role", role);
        patient.put("coin",0);
        patient.put("theme", theme);
        patient.put("doctor_id", doctorId);
        patient.put("child_token", childToken);
        patient.put("parent_token", parentToken);
        patient.put("principal_characters", principalCharacters);
        patient.put("characters_unlocked", charactersUnlocked);


        db.collection("users").add(patient).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if(task.isSuccessful())
                    listener.onSuccess();
                else
                    listener.onFailure();

            }
        });

    }
    public interface OnDeleteListener {
        void onPatientDeleted();
        void onPatientDeleteFailed();
    }
    public static void deletePatient(String patientId, OnDeleteListener listener){
        db.collection("users")
                .whereEqualTo("id", patientId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            String documentId = null;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Ottieni l'ID del documento trovato
                                documentId = document.getId();
                                db.collection("users").document(documentId).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            FirebaseAppointmentsModel.deleteAllAppointments(patientId, new FirebaseAppointmentsModel.OnDeleteListener() {
                                                @Override
                                                public void onAppointmentDeleted() {
                                                    listener.onPatientDeleted();
                                                }

                                                @Override
                                                public void onAppointmentDeleteFailed() {
                                                    listener.onPatientDeleteFailed();
                                                }
                                            });
                                        } else {
                                            listener.onPatientDeleteFailed();
                                        }
                                    }
                                });
                            } if (task.getResult().size() == 0)
                                listener.onPatientDeleteFailed();
                        } else {
                            listener.onPatientDeleteFailed();
                        }
                    }
                });

    }


}
