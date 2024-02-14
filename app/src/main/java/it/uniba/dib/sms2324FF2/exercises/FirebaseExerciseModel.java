package it.uniba.dib.sms2324FF2.exercises;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import it.uniba.dib.sms2324FF2.firestore.FirestoreModel;
import it.uniba.dib.sms2324FF2.patient.FirebasePatientModel;
import it.uniba.dib.sms2324FF2.patient.Patient;

public class FirebaseExerciseModel {
    public static final String TO_DO = "to-do";
    static FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface OnExercisesRead {
        void onSuccess(ArrayList<Exercise> exercises, int num_chapter);
        void onFailure();
    }

    public interface OnExercisesStateCheckedListener {
        void onExercisesStateChecked();
        void onExercisesStateCheckFailed();
    }
    public interface OnAllExercisesCreatedListener {
        void onExercisesCreated(TreeMap<ArrayList<Object>, ArrayList<Exercise>> exercises);
        void onExercisesCreationFailed();
    }
    public interface  OnDocumentIdListener {
        void onDocumentIdRead(String documentId);
    }

    public interface TransactionCallback {
        void onTransactionSuccess();

        void onTransactionFailure();
    }

    public interface  OnRewardsListener {
        void onRewardsRead(int rewardCoins);
        void onRewardsReadFailed();
    }
    public interface OnCoinsUpdateListener {
        void onCoinsUpdateSuccess(int newCoins);
        void onCoinsUpdateFailure();
    }

    public static void getPatientExercises(String patient,OnAllExercisesCreatedListener listener){
        db.collection("exercises")
                .whereEqualTo("patient", patient)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        //creo la lista da restituire alla fine
                        Comparator<ArrayList<Object>> comparator = Comparator.comparing(o -> (Integer) o.get(0));
                        TreeMap<ArrayList<Object>,ArrayList<Exercise>> exercises = new TreeMap<>(comparator);

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Ottenere tutti i campi del documento
                            Map<String, Object> documentData = document.getData();
                            ArrayList<Exercise> chapter_exercises = new ArrayList<>();
                            ArrayList<Object> exercise_data = new ArrayList<>();
                            // Iterare su tutti i campi per trovare quelli che corrispondono allo schema "exercise_num"
                            for (Map.Entry<String, Object> entry : documentData.entrySet()) {
                                String fieldName = entry.getKey();
                                if (fieldName.startsWith("exercise_")) {
                                    // Ottieni il riferimento all'array
                                    Map<String,Object> exerciseMap = (Map<String,Object>) entry.getValue();
                                    // Itera su ogni elemento dell'array
                                    int type = 0, coin = 0, numhintsUsed = 0;
                                    Timestamp day = null;
                                    boolean exerciseRight = false;
                                    String answer = null, id = null, state = null, first_img = null, second_img = null, first_hint = null, second_hint = null, third_hint = null, audio = null;
                                    for (Map.Entry<String,Object> exerciseItem : exerciseMap.entrySet()) {
                                        //sono dentro l'esercizio
                                        switch (exerciseItem.getKey()) {
                                            case "id":
                                                id = (String) exerciseItem.getValue();
                                                break;
                                            case "type":
                                                Long type_ob = (Long) exerciseItem.getValue();
                                                type = type_ob.intValue();
                                                break;
                                            case "state":
                                                state = (String) exerciseItem.getValue();
                                                break;
                                            case "coin":
                                                Long coin_ob = (Long) exerciseItem.getValue();
                                                coin = coin_ob.intValue();
                                                break;
                                            case "first_img":
                                                first_img = (String) exerciseItem.getValue();
                                                break;
                                            case "second_img":
                                                second_img = (String) exerciseItem.getValue();
                                                break;
                                            case "first_hint":
                                                first_hint = (String) exerciseItem.getValue();
                                                break;
                                            case "second_hint":
                                                second_hint = (String) exerciseItem.getValue();
                                                break;
                                            case "third_hint":
                                                third_hint = (String) exerciseItem.getValue();
                                                break;
                                            case "audio":
                                                audio = (String) exerciseItem.getValue();
                                                break;
                                            case "day":
                                                day = (Timestamp) exerciseItem.getValue();
                                                break;
                                            case "exercise_right":
                                                exerciseRight = (boolean) exerciseItem.getValue();
                                                break;
                                            case "num_hints_used":
                                                Long numhintUsed_ob = (Long) exerciseItem.getValue();
                                                numhintsUsed = numhintUsed_ob.intValue();
                                                break;
                                            case "answer":
                                                answer = (String) exerciseItem.getValue();
                                                break;

                                        }
                                    }
                                    //ho preso tutti i dati dell'esercizio, quindi lo creo
                                    Exercise exercise = null;
                                    switch (type){
                                        case 1:
                                            exercise = new Exercise(id, type, state, day, first_img, first_hint, second_hint, third_hint, coin, exerciseRight, numhintsUsed,answer);
                                            break;
                                        case 2:
                                            exercise = new Exercise(id, type, state, day, audio, first_img, second_img, coin, exerciseRight,answer);
                                            break;
                                        case 3:
                                            exercise = new Exercise(id, type, state, day, audio, coin, exerciseRight,answer);
                                            break;
                                    }
                                    chapter_exercises.add(exercise);
                                }
                            }
                            int num_chapter = document.getLong("num_chapter").intValue();
                            String state = document.getString("state");
                            int date = document.getLong("number_of_week").intValue();
                            exercise_data.add(num_chapter);
                            exercise_data.add(state);
                            exercise_data.add(date);
                            Collections.reverse(chapter_exercises);
                            exercises.put(exercise_data,chapter_exercises);

                        }
                        //Restituisco gli esercizi
                        if (listener != null) {
                            listener.onExercisesCreated(exercises);
                        }
                    } else{
                        Exception exception = task.getException();
                        if (exception != null) {
                            exception.printStackTrace();
                        }
                        listener.onExercisesCreationFailed();
                    }
                });
    }

    public static void checkExercisesState(OnExercisesStateCheckedListener listener) {
        // Ottengo il numero della settimana corrente da confrontare con quello del db
        Calendar calendar = Calendar.getInstance();
        int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);

        getPatientExercises(Patient.getInstance().getId(), new OnAllExercisesCreatedListener() {
            @Override
            public void onExercisesCreated(TreeMap<ArrayList<Object>, ArrayList<Exercise>> weekExercises) {
                if (!weekExercises.isEmpty()) {
                    int num_chapter;
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String currentDay = dateFormat.format(Timestamp.now().toDate());

                    for (Map.Entry<ArrayList<Object>, ArrayList<Exercise>> entry : weekExercises.entrySet()) {
                        ArrayList<Object> exercisesData = entry.getKey();
                        num_chapter = (Integer) exercisesData.get(0);

                        if (weekOfYear == (Integer) exercisesData.get(2)) {

                            if (!exercisesData.get(1).equals(TO_DO)) {
                                //assegno le monete di premio per gli esercizi del capitolo precedente
                                setWeeklyRewards(weekExercises, num_chapter);
                                changeChapter(num_chapter, TO_DO);
                            }
                            getExercisesForChapter(Patient.getInstance().getId(),num_chapter, new OnExercisesRead() {
                                @Override
                                public void onSuccess(ArrayList<Exercise> exercises, int num_chapter) {
                                    for (Exercise ex : exercises) {
                                        String day = ex.getDay();
                                        if (currentDay.equals(day)) {
                                            if (ex.getState().equals("locked")) {
                                                changeExerciseState(num_chapter, ex, TO_DO, new TransactionCallback() {
                                                    @Override
                                                    public void onTransactionSuccess() {
                                                        if (listener != null) {
                                                            listener.onExercisesStateChecked();
                                                        }
                                                    }

                                                    @Override
                                                    public void onTransactionFailure() {
                                                        if (listener != null) {
                                                            listener.onExercisesStateCheckFailed();
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onFailure() {
                                    if (listener != null) {
                                        listener.onExercisesStateCheckFailed();
                                    }
                                }
                            });

                        } else if (listener != null){
                            listener.onExercisesStateChecked();
                        }
                    }
                } else{
                    //Non ci sono esercizi
                    listener.onExercisesStateChecked();
                }
            }

            @Override
            public void onExercisesCreationFailed() {
                listener.onExercisesStateCheckFailed();
            }
        });
    }

    private static void setWeeklyRewards(TreeMap<ArrayList<Object>, ArrayList<Exercise>> exercises,int chapter){
        getDocumentIdbyChapter(chapter, new OnDocumentIdListener() {
            @Override
            public void onDocumentIdRead(String documentId) {
                //Calcolo le monete premio in base agli esercizi svolti correttamente nel capitolo precedente
                getExercisesForChapter(Patient.getInstance().getId(),chapter - 1, new OnExercisesRead() {
                    @Override
                    public void onSuccess(ArrayList<Exercise> exercisesOfPreviousChapter, int num_chapter) {
                        int counter = 0;
                        for(Exercise ex : exercisesOfPreviousChapter){
                            if(ex.isExerciseRight())
                                counter++;
                        }
                        int rewardCoins = counter * 15;
                        db.collection("exercises").document(documentId).update("reward_coins", rewardCoins)
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        e.printStackTrace();
                                    }
                                });
                    }

                    @Override
                    public void onFailure() {

                    }
                });

            }
        });
    }
    public static void getWeeklyRewards(OnRewardsListener listener){
        int weekOfYear = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);

        db.collection("exercises")
                .whereEqualTo("number_of_week", weekOfYear)
                .whereEqualTo("patient", Patient.getInstance().getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                int rewardCoins = document.getLong("reward_coins").intValue();
                                listener.onRewardsRead(rewardCoins);
                            } if(task.getResult().size() == 0)
                                listener.onRewardsRead(0);
                        }else{
                            listener.onRewardsReadFailed();
                        }
                    }
                });

    }

    public static void restoreRewardCoins(){
        int weekOfYear = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
        getDocumentIdbyWeek(Patient.getInstance().getId(),weekOfYear, new OnDocumentIdListener() {
            @Override
            public void onDocumentIdRead(String documentId) {
                db.collection("exercises").document(documentId).update("reward_coins", 0);
            }
        });
    }

    public static void updateCoins(int rewardCoins, OnCoinsUpdateListener listener) {
        FirebasePatientModel.getPatientDocument( new FirebasePatientModel.PatientDocumentListener() {
            @Override
            public void onDocumentReceived(String userDocumentId) {
                DocumentReference userDocRef = db.collection("users").document(userDocumentId);
                db.runTransaction(new Transaction.Function<Void>() {
                    @Override
                    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                        try {
                            DocumentSnapshot snapshot = transaction.get(userDocRef);

                            // Ottenere il valore corrente delle monete e aggiornarlo
                            Long currentCoinsLong = snapshot.getLong("coin");
                            int currentCoins = (currentCoinsLong != null) ? currentCoinsLong.intValue() : 0;

                            int newCoins = currentCoins + rewardCoins;
                            transaction.update(userDocRef, "coin", newCoins);
                            Patient.getInstance().setCoin(newCoins); // Aggiorno anche il paziente statico che ha già effettuato il login

                            // Notifica il chiamante del successo con il nuovo valore delle monete
                            listener.onCoinsUpdateSuccess(newCoins);
                        } catch (Exception e) {
                            // Notifica il chiamante in caso di errore
                            e.printStackTrace();
                            listener.onCoinsUpdateFailure();
                        }

                        return null;
                    }
                });

            }
            @Override
            public void onDocumentReceiveFailed() {
                listener.onCoinsUpdateFailure();
            }
        });
    }

    public static void getExercisesForChapter( String patient, int num_chapter, OnExercisesRead listener) {
        getPatientExercises(patient, new OnAllExercisesCreatedListener() {
            @Override
            public void onExercisesCreated(TreeMap<ArrayList<Object>, ArrayList<Exercise>> exercises) {
                ArrayList<Exercise> values = new ArrayList<>();
                for (Map.Entry<ArrayList<Object>, ArrayList<Exercise>> entry : exercises.entrySet()) {
                    ArrayList<Object> key = entry.getKey();
                    if (!key.isEmpty() && key.get(0).equals(num_chapter)) {
                        values.addAll(entry.getValue());
                        listener.onSuccess(values, num_chapter);
                    } else {
                        listener.onFailure();
                    }
                }
                if(exercises.isEmpty() && listener!=null)
                    listener.onFailure();
            }

            @Override public void onExercisesCreationFailed() {listener.onFailure();}
        });
    }
    private static void changeChapter(int num_chapter, String state){
        db.collection("exercises")
                .whereEqualTo("num_chapter", num_chapter)
                .whereEqualTo("patient", Patient.getInstance().getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for(QueryDocumentSnapshot document : task.getResult()){
                            document.getReference().update("state",state);
                        }
                    }
                });
    }
    private static void getDocumentIdbyChapter(int num_chapter, OnDocumentIdListener listener){
        db.collection("exercises")
                .whereEqualTo("num_chapter", num_chapter)
                .whereEqualTo("patient", Patient.getInstance().getId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String documentId = null;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Ottieni l'ID del documento trovato
                            documentId = document.getId();
                        }
                        listener.onDocumentIdRead(documentId);
                    }
                });
    }
    private static void getDocumentIdbyWeek( String patient, int weekOfYear, OnDocumentIdListener listener){
        db.collection("exercises")
                .whereEqualTo("patient", patient)
                .whereEqualTo("number_of_week", weekOfYear)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String documentId = null;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Ottieni l'ID del documento trovato
                            documentId = document.getId();
                        }
                        listener.onDocumentIdRead(documentId);
                    }
                });
    }
    public static void changeExerciseState(int num_chapter, Exercise exercise, String state, TransactionCallback callback){
        getDocumentIdbyChapter(num_chapter, new OnDocumentIdListener() {
            @Override
            public void onDocumentIdRead(String documentId) {
                db.runTransaction(transaction -> {
                    DocumentReference docRef = db.collection("exercises").document(documentId);

                    DocumentSnapshot snapshot = transaction.get(docRef);
                    String fieldName = exercise.getId();

                    // Verifica se il campo array esiste nel documento
                    if (snapshot.contains(fieldName)) {
                        // Il campo esiste, aggiungi il nuovo valore all'array
                        HashMap<String, Object> exerciseMap = (HashMap<String, Object>) snapshot.get(fieldName);
                        if (exerciseMap != null) {
                            exerciseMap.put("state", state);
                            transaction.update(docRef, fieldName, exerciseMap);
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
        });
    }


    public static void putExerciseAnswer( int num_chapter, Exercise exercise, String typeData, String answer, TransactionCallback callback) {
        getDocumentIdbyChapter(num_chapter, new OnDocumentIdListener() {
            @Override
            public void onDocumentIdRead(String documentId) {
                db.runTransaction(transaction -> {
                    DocumentReference docRef = db.collection("exercises").document(documentId);

                    DocumentSnapshot snapshot = transaction.get(docRef);
                    String fieldName = exercise.getId();

                    // Verifica se il campo array esiste nel documento
                    if (snapshot.contains(fieldName)) {
                        // Il campo esiste, aggiungi il nuovo valore all'array
                        HashMap<String, Object> exerciseMap = (HashMap<String, Object>) snapshot.get(fieldName);
                        if (exerciseMap != null) {
                            exerciseMap.put(typeData, answer);
                            transaction.update(docRef, fieldName, exerciseMap);
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
        });
    }

    public static void putExerciseAnswer( int num_chapter, Exercise exercise, String typeData, int answer, TransactionCallback callback) {
        getDocumentIdbyChapter(num_chapter, new OnDocumentIdListener() {
            @Override
            public void onDocumentIdRead(String documentId) {
                db.runTransaction(transaction -> {
                    DocumentReference docRef = db.collection("exercises").document(documentId);

                    DocumentSnapshot snapshot = transaction.get(docRef);
                    String fieldName = exercise.getId();

                    // Verifica se il campo array esiste nel documento
                    if (snapshot.contains(fieldName)) {
                        // Il campo esiste, aggiungi il nuovo valore all'array
                        HashMap<String, Object> exerciseMap = (HashMap<String, Object>) snapshot.get(fieldName);
                        if (exerciseMap != null) {
                            exerciseMap.put(typeData, answer);
                            transaction.update(docRef, fieldName, exerciseMap);
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
        });
    }

    public interface OnCorrectExerciseListener {
        void onSuccess();
        void onFailure();
    }

    public static void correctExercise(int num_chapter, Exercise exercise,boolean state, OnCorrectExerciseListener listener){
        getDocumentIdbyChapter(num_chapter, new OnDocumentIdListener() {
            @Override
            public void onDocumentIdRead(String documentId) {
                db.runTransaction(transaction -> {
                    DocumentReference docRef = db.collection("exercises").document(documentId);

                    DocumentSnapshot snapshot = transaction.get(docRef);
                    String fieldName = exercise.getId();

                    // Verifica se il campo array esiste nel documento
                    if (snapshot.contains(fieldName)) {
                        // Il campo esiste, aggiungi il nuovo valore all'array
                        HashMap<String, Object> exerciseMap = (HashMap<String, Object>) snapshot.get(fieldName);
                        if (exerciseMap != null) {
                            exerciseMap.put("state", "corrected");
                            exerciseMap.put("exercise_right", state);
                            transaction.update(docRef, fieldName, exerciseMap);
                        }
                    }

                    return null;
                }).addOnSuccessListener(aVoid -> {
                    if (listener != null) {
                        listener.onSuccess();
                    }
                }).addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure();
                    }
                });
            }
        });
    }
    private interface OnLastChapterListener {
        void onSuccess(int numLastChapter);
        void onFailure();
    }
    public static void getLastNumChapter(String patient, OnLastChapterListener listener){
        db.collection("exercises")
                .whereEqualTo("patient", patient)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    listener.onSuccess(task.getResult().size());
                }
            }
        });
    }

    public interface OnSaveExerciseListener {
        void onSuccess();
        void onFailure(String error);
    }
    public static void saveImageDenomination(String patient, Uri imageUri, String firstHintPath, String secondHintPath, String thirdHintPath, int coin, Timestamp date, OnSaveExerciseListener listener) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(date.toDate().getTime()));
        int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);

        checkIfChapterIsPresent(patient, weekOfYear, new OnChapterListener() {
            @Override
            public void onResult(String documentId, int numDiEx, int numChapter) {
                int numExToAdd = numDiEx + 1;
                //Creo il nome degli oggetti da salvare
                String firstImg = "img_" + numExToAdd;
                String firstHint = "first_hint_" + numExToAdd;
                String secondHint = "second_hint_" + numExToAdd;
                String thirdHint = "third_hint_" + numExToAdd;
                String id = "exercise_" + numExToAdd;
                //Salvo l'esercizio sul database
                getDocumentIdbyWeek(patient, weekOfYear, new OnDocumentIdListener() {
                    @Override
                    public void onDocumentIdRead(String documentId) {
                        //Creo l'esercizio
                        HashMap<String, Object> exMap = new HashMap<>();
                        exMap.put("id", id);
                        exMap.put("type", 1);
                        exMap.put("state", "locked");
                        exMap.put("day", date);
                        exMap.put("first_img", firstImg);
                        exMap.put("first_hint", firstHint);
                        exMap.put("second_hint", secondHint);
                        exMap.put("third_hint", thirdHint);
                        exMap.put("coin", coin);
                        exMap.put("exercise_right", false);
                        exMap.put("num_hints_used", 0);
                        exMap.put("answer", "");
                        HashMap<String, Object> exercise = new HashMap<>();
                        exercise.put(id, exMap);
                        db.runTransaction(transaction -> {
                            DocumentReference docRef = db.collection("exercises").document(documentId);
                            DocumentSnapshot snapshot = transaction.get(docRef);
                            if (snapshot.exists()) {
                                transaction.set(docRef, exercise, SetOptions.merge());
                            }
                            return null;
                        });
                    }

                });
                //Salvo i file sullo storage
                String imageRef = "Images/" + patient + "/" + numChapter + "/" + firstImg + ".jpeg";
                FirestoreModel.uploadImage(imageRef, imageUri, new FirestoreModel.UploadCallback() {
                    @Override
                    public void onUploadSuccess() {
                        String audioRef = patient + "/" + numChapter + "/" + firstHint;
                        FirestoreModel.uploadAudio(audioRef, firstHintPath, new FirestoreModel.UploadCallback() {
                            @Override
                            public void onUploadSuccess() {
                                String audioRef = patient + "/" + numChapter + "/" + secondHint;
                                FirestoreModel.uploadAudio(audioRef, secondHintPath, new FirestoreModel.UploadCallback() {
                                    @Override
                                    public void onUploadSuccess() {
                                        String audioRef = patient + "/" + numChapter + "/" + thirdHint;
                                        FirestoreModel.uploadAudio(audioRef, thirdHintPath, new FirestoreModel.UploadCallback() {
                                            @Override
                                            public void onUploadSuccess() {
                                                //Ho caricato tutti i file
                                                listener.onSuccess();
                                            }

                                            @Override
                                            public void onUploadFailure() {
                                                //Non ho caricato il terzo indizio
                                                listener.onFailure("Errore durante il caricamento del terzo indizio");
                                            }
                                        });
                                    }

                                    @Override
                                    public void onUploadFailure() {
                                        //Non ho caricato il secondo indizio
                                        listener.onFailure("Errore durante il caricamento del secondo indizio");

                                    }
                                });
                            }

                            @Override
                            public void onUploadFailure() {
                                //Non ho caricato il primo indizio
                                listener.onFailure("Errore durante il caricamento del primo indizio");

                            }
                        });
                    }

                    @Override
                    public void onUploadFailure() {
                        //Non ho caricato l'immagine
                        listener.onFailure("Errore durante il caricamento dell'immagine");

                    }
                });
            }

            @Override
            public void onFailure() {
                //Non c'è il capitolo quindi lo creo
                createChapter(patient, weekOfYear, new OnChapterCreatedListener() {
                    @Override
                    public void onChapterCreated(int numChapter) {
                        //C'è il capitolo quindi aggiungo l'esercizio
                        int numExToAdd = 1;
                        //Creo il nome degli oggetti da salvare
                        String firstImg = "img_" + numExToAdd;
                        String firstHint = "first_hint_" + numExToAdd;
                        String secondHint = "second_hint_" + numExToAdd;
                        String thirdHint = "third_hint_" + numExToAdd;
                        String id = "exercise_" + numExToAdd;
                        //Salvo l'esercizio sul database
                        getDocumentIdbyWeek(patient, weekOfYear, new OnDocumentIdListener() {
                            @Override
                            public void onDocumentIdRead(String documentId) {
                                //Creo l'esercizio
                                HashMap<String, Object> exMap = new HashMap<>();
                                exMap.put("id", id);
                                exMap.put("type", 1);
                                exMap.put("state", "locked");
                                exMap.put("day", date);
                                exMap.put("first_img", firstImg);
                                exMap.put("first_hint", firstHint);
                                exMap.put("second_hint", secondHint);
                                exMap.put("third_hint", thirdHint);
                                exMap.put("coin", coin);
                                exMap.put("exercise_right", false);
                                exMap.put("num_hints_used", 0);
                                exMap.put("answer", "");
                                HashMap<String, Object> exercise = new HashMap<>();
                                exercise.put(id, exMap);
                                db.runTransaction(transaction -> {
                                    DocumentReference docRef = db.collection("exercises").document(documentId);
                                    DocumentSnapshot snapshot = transaction.get(docRef);
                                    if (snapshot.exists()) {
                                        transaction.set(docRef, exercise, SetOptions.merge());
                                    }
                                    return null;
                                });
                            }

                        });
                        //Salvo i file sullo storage
                        String imageRef = "Images/" + patient + "/" + numChapter + "/" + firstImg + ".jpeg";
                        FirestoreModel.uploadImage(imageRef, imageUri, new FirestoreModel.UploadCallback() {
                            @Override
                            public void onUploadSuccess() {
                                String audioRef = patient + "/" + numChapter + "/" + firstHint;
                                FirestoreModel.uploadAudio(audioRef, firstHintPath, new FirestoreModel.UploadCallback() {
                                    @Override
                                    public void onUploadSuccess() {
                                        String audioRef = patient + "/" + numChapter + "/" + secondHint;
                                        FirestoreModel.uploadAudio(audioRef, secondHintPath, new FirestoreModel.UploadCallback() {
                                            @Override
                                            public void onUploadSuccess() {
                                                String audioRef = patient + "/" + numChapter + "/" + thirdHint;
                                                FirestoreModel.uploadAudio(audioRef, thirdHintPath, new FirestoreModel.UploadCallback() {
                                                    @Override
                                                    public void onUploadSuccess() {
                                                        //Ho caricato tutti i file
                                                        listener.onSuccess();
                                                    }

                                                    @Override
                                                    public void onUploadFailure() {
                                                        //Non ho caricato il terzo indizio
                                                        listener.onFailure("Errore durante il caricamento del terzo indizio");
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onUploadFailure() {
                                                //Non ho caricato il secondo indizio
                                                listener.onFailure("Errore durante il caricamento del secondo indizio");

                                            }
                                        });
                                    }

                                    @Override
                                    public void onUploadFailure() {
                                        //Non ho caricato il primo indizio
                                        listener.onFailure("Errore durante il caricamento del primo indizio");

                                    }
                                });
                            }

                            @Override
                            public void onUploadFailure() {
                                //Non ho caricato l'immagine
                                listener.onFailure("Errore durante il caricamento dell'immagine");

                            }
                        });
                    }

                    @Override
                    public void onChapterCreationFailure() {
                        listener.onFailure("errore durante la creazione del chapter");
                    }
                });
            }
        });
    }

    public static void saveMinimalPairs(String patient, Uri firstImageUri, Uri secondImageUri, String audioPath, int coin, Timestamp date, OnSaveExerciseListener listener) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(date.toDate().getTime()));
        int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);

        checkIfChapterIsPresent(patient, weekOfYear, new OnChapterListener() {
            @Override
            public void onResult(String documentId, int numDiEx, int numChapter) {
                int numExToAdd = numDiEx + 1;
                //Creo il nome degli oggetti da salvare
                String firstImg = "first_img_" + numExToAdd;
                String secondImg = "second_img_" + numExToAdd;
                String id = "exercise_" + numExToAdd;
                String audio = "audio_" + numExToAdd;
                //Salvo l'esercizio sul database
                getDocumentIdbyWeek(patient, weekOfYear, new OnDocumentIdListener() {
                    @Override
                    public void onDocumentIdRead(String documentId) {
                        //Creo l'esercizio
                        HashMap<String, Object> exMap = new HashMap<>();
                        exMap.put("id", id);
                        exMap.put("type", 2);
                        exMap.put("state", "locked");
                        exMap.put("day", date);
                        exMap.put("first_img", firstImg);
                        exMap.put("second_img", secondImg);
                        exMap.put("audio", audio);
                        exMap.put("coin", coin);
                        exMap.put("exercise_right", false);
                        exMap.put("answer", "");
                        HashMap<String, Object> exercise = new HashMap<>();
                        exercise.put(id, exMap);
                        db.runTransaction(transaction -> {
                            DocumentReference docRef = db.collection("exercises").document(documentId);
                            DocumentSnapshot snapshot = transaction.get(docRef);
                            if (snapshot.exists()) {
                                transaction.set(docRef, exercise, SetOptions.merge());
                            }
                            return null;
                        });
                    }

                });
                //Salvo i file sullo storage
                String imageRef = "Images/" + patient + "/" + numChapter + "/" + firstImg + ".jpeg";
                FirestoreModel.uploadImage(imageRef, firstImageUri, new FirestoreModel.UploadCallback() {
                    @Override
                    public void onUploadSuccess() {
                        String imageRef = "Images/" + patient + "/" + numChapter + "/" + secondImg + ".jpeg";
                        FirestoreModel.uploadImage(imageRef, secondImageUri, new FirestoreModel.UploadCallback() {
                            @Override
                            public void onUploadSuccess() {
                                String audioRef = patient + "/" + numChapter + "/" + audio;
                                FirestoreModel.uploadAudio(audioRef, audioPath, new FirestoreModel.UploadCallback() {
                                    @Override
                                    public void onUploadSuccess() {
                                        //Ho caricato tutti i file
                                        listener.onSuccess();
                                    }

                                    @Override
                                    public void onUploadFailure() {
                                        //Non ho caricato l'audio
                                        listener.onFailure("Errore durante il caricamento dell'audio");

                                    }
                                });
                            }

                            @Override
                            public void onUploadFailure() {
                                //Non ho caricato la seconda immagine
                                listener.onFailure("Errore durante il caricamento della seconda immagine");

                            }
                        });
                    }

                    @Override
                    public void onUploadFailure() {
                        //Non ho caricato la prima immagine
                        listener.onFailure("Errore durante il caricamento della prima immagine");

                    }
                });
            }

            @Override
            public void onFailure() {
                createChapter(patient, weekOfYear, new OnChapterCreatedListener() {
                    @Override
                    public void onChapterCreated(int numChapter) {
                        //C'e il capitolo quindi aggiungo l'esercizio
                        int numExToAdd = 1;
                        //Creo il nome degli oggetti da salvare
                        String firstImg = "first_img_" + numExToAdd;
                        String secondImg = "second_img_" + numExToAdd;
                        String id = "exercise_" + numExToAdd;
                        String audio = "audio_" + numExToAdd;
                        //Salvo l'esercizio sul database
                        getDocumentIdbyWeek(patient, weekOfYear, new OnDocumentIdListener() {
                            @Override
                            public void onDocumentIdRead(String documentId) {
                                //Creo l'esercizio
                                HashMap<String, Object> exMap = new HashMap<>();
                                exMap.put("id", id);
                                exMap.put("type", 2);
                                exMap.put("state", "locked");
                                exMap.put("day", date);
                                exMap.put("first_img", firstImg);
                                exMap.put("second_img", secondImg);
                                exMap.put("audio", audio);
                                exMap.put("coin", coin);
                                exMap.put("exercise_right", false);
                                exMap.put("answer", "");
                                HashMap<String, Object> exercise = new HashMap<>();
                                exercise.put(id, exMap);
                                db.runTransaction(transaction -> {
                                    DocumentReference docRef = db.collection("exercises").document(documentId);
                                    DocumentSnapshot snapshot = transaction.get(docRef);
                                    if (snapshot.exists()) {
                                        transaction.set(docRef, exercise, SetOptions.merge());
                                    }
                                    return null;
                                });
                            }

                        });
                        //Salvo i file sullo storage
                        String imageRef = "Images/" + patient + "/" + numChapter + "/" + firstImg + ".jpeg";
                        FirestoreModel.uploadImage(imageRef, firstImageUri, new FirestoreModel.UploadCallback() {
                            @Override
                            public void onUploadSuccess() {
                                String imageRef = "Images/" + patient + "/" + numChapter + "/" + secondImg + ".jpeg";
                                FirestoreModel.uploadImage(imageRef, secondImageUri, new FirestoreModel.UploadCallback() {
                                    @Override
                                    public void onUploadSuccess() {
                                        String audioRef = patient + "/" + numChapter + "/" + audio;
                                        FirestoreModel.uploadAudio(audioRef, audioPath, new FirestoreModel.UploadCallback() {
                                            @Override
                                            public void onUploadSuccess() {
                                                //Ho caricato tutti i file
                                                listener.onSuccess();
                                            }

                                            @Override
                                            public void onUploadFailure() {
                                                //Non ho caricato l'audio
                                                listener.onFailure("Errore durante il caricamento dell'audio");

                                            }
                                        });
                                    }

                                    @Override
                                    public void onUploadFailure() {
                                        //Non ho caricato la seconda immagine
                                        listener.onFailure("Errore durante il caricamento della seconda immagine");

                                    }
                                });
                            }

                            @Override
                            public void onUploadFailure() {
                                //Non ho caricato la prima immagine
                                listener.onFailure("Errore durante il caricamento della prima immagine");

                            }
                        });
                    }

                    @Override
                    public void onChapterCreationFailure() {
                        listener.onFailure("errore durante la creazione del chapter");
                    }
                });
            }
        });
    }

    public static void saveWordsSequencesRepetition(String patient, String audioPath, int coin, Timestamp date, OnSaveExerciseListener listener) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(date.toDate().getTime()));
        int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
        //controllo l'esistenza del capitolo
        checkIfChapterIsPresent(patient,weekOfYear,new OnChapterListener() {
            @Override
            public void onResult(String documentId, int numDiEx, int numChapter) {
                //Il capitolo c'è quindi posso aggiungere l'esercizio
                int numExToAdd = numDiEx + 1;
                //Creo il nome degli oggetti da salvare
                String id = "exercise_" + numExToAdd;
                String audio = "audio_" + numExToAdd;
                //Salvo l'esercizio sul database
                getDocumentIdbyWeek(patient, weekOfYear, new OnDocumentIdListener() {
                    @Override
                    public void onDocumentIdRead(String documentId) {
                        //Creo l'esercizio
                        HashMap<String, Object> exMap = new HashMap<>();
                        exMap.put("id", id);
                        exMap.put("type", 3);
                        exMap.put("state", "locked");
                        exMap.put("day", date);
                        exMap.put("audio", audio);
                        exMap.put("coin", coin);
                        exMap.put("exercise_right", false);
                        exMap.put("answer", "");
                        HashMap<String, Object> exercise = new HashMap<>();
                        exercise.put(id, exMap);

                        db.runTransaction(transaction -> {
                            DocumentReference docRef = db.collection("exercises").document(documentId);
                            DocumentSnapshot snapshot = transaction.get(docRef);
                            if (snapshot.exists()) {
                                transaction.set(docRef, exercise, SetOptions.merge());
                            }
                            return null;
                        });
                    }

                });
                //Salvo i file sullo storage
                String audioRef = patient + "/" + numChapter + "/" + audio;
                FirestoreModel.uploadAudio(audioRef, audioPath, new FirestoreModel.UploadCallback() {
                    @Override
                    public void onUploadSuccess() {
                        //Ho caricato tutti i file
                        listener.onSuccess();
                    }

                    @Override
                    public void onUploadFailure() {
                        //Non ho caricato l'audio
                        listener.onFailure("Errore durante il caricamento dell'audio");

                    }
                });
            }

            @Override
            public void onFailure() {
                //Non c'è il capitolo quindi devo crearlo e aggiungere l'esercizio
                createChapter(patient, weekOfYear, new OnChapterCreatedListener() {
                    @Override
                    public void onChapterCreated(int numChapter) {
                        //Il capitolo c'è quindi posso aggiungere l'esercizio
                        int numExToAdd = 1;
                        //Creo il nome degli oggetti da salvare
                        String id = "exercise_" + numExToAdd;
                        String audio = "audio_" + numExToAdd;
                        //Salvo l'esercizio sul database
                        getDocumentIdbyWeek(patient, weekOfYear, new OnDocumentIdListener() {
                            @Override
                            public void onDocumentIdRead(String documentId) {
                                //Creo l'esercizio
                                HashMap<String, Object> exMap = new HashMap<>();
                                exMap.put("id", id);
                                exMap.put("type", 3);
                                exMap.put("state", "locked");
                                exMap.put("day", date);
                                exMap.put("audio", audio);
                                exMap.put("coin", coin);
                                exMap.put("exercise_right", false);
                                exMap.put("answer", "");
                                HashMap<String, Object> exercise = new HashMap<>();
                                exercise.put(id, exMap);

                                db.runTransaction(transaction -> {
                                    DocumentReference docRef = db.collection("exercises").document(documentId);
                                    DocumentSnapshot snapshot = transaction.get(docRef);
                                    if (snapshot.exists()) {
                                        transaction.set(docRef, exercise, SetOptions.merge());
                                    }
                                    return null;
                                });
                            }

                        });
                        //Salvo i file sullo storage
                        String audioRef = patient + "/" + numChapter + "/" + audio;
                        FirestoreModel.uploadAudio(audioRef, audioPath, new FirestoreModel.UploadCallback() {
                            @Override
                            public void onUploadSuccess() {
                                //Ho caricato tutti i file
                                listener.onSuccess();
                            }

                            @Override
                            public void onUploadFailure() {
                                //Non ho caricato l'audio
                                listener.onFailure("Errore durante il caricamento dell'audio");

                            }
                        });
                    }

                    @Override
                    public void onChapterCreationFailure() {
                        listener.onFailure("Errore durante la creazione del capitolo");
                    }
                });
            }
        });
    }

    public interface OnChapterCreatedListener {
        void onChapterCreated(int numChapter);

        void onChapterCreationFailure();
    }

    public static void createChapter(String patient, int weekOfYear, OnChapterCreatedListener listener) {
        //Non c'è il documento degli esercizi, quindi devo crearlo
        getLastNumChapter(patient, new OnLastChapterListener() {
            @Override
            public void onSuccess(int numLastChapter) {
                int numChapter = numLastChapter + 1;
                HashMap<String, Object> exercise = new HashMap<>();
                exercise.put("patient", patient);
                exercise.put("num_chapter", numChapter);
                exercise.put("state", "locked");
                exercise.put("number_of_week", weekOfYear);
                exercise.put("reward_coins", 0);
                db.collection("exercises").add(exercise).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            listener.onChapterCreated(numChapter);

                        } else {
                            listener.onChapterCreationFailure();
                        }
                    }
                });
            }

            @Override
            public void onFailure() {
                listener.onChapterCreationFailure();
            }
        });
    }

    public interface OnChapterListener {
        void onResult(String documentId, int numDiEx, int numChapter);
        void onFailure();
    }
    public static void checkIfChapterIsPresent(String patientId, int weekOfYear, OnChapterListener listener){
        //controllo se c'è il capitolo
        db.collection("exercises")
                .whereEqualTo("patient", patientId)
                .whereEqualTo("number_of_week", weekOfYear)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            int numDiEx = 0;
                            if(task.getResult().size() == 1) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    for (Map.Entry<String, Object> entry : document.getData().entrySet()) {
                                        String fieldName = entry.getKey();
                                        if (fieldName.startsWith("exercise_")){
                                            numDiEx++;
                                        }
                                    }
                                    listener.onResult(document.getId(), numDiEx, document.getLong("num_chapter").intValue());
                                }
                            } else {
                                listener.onFailure();
                            }
                        } else {
                            listener.onFailure();
                        }
                    }
                });
    }


}

