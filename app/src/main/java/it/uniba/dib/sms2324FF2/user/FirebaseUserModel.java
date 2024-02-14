package it.uniba.dib.sms2324FF2.user;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class FirebaseUserModel {
    static FirebaseFirestore db = FirebaseFirestore.getInstance();


    public interface OnUserCreatedListener {
        void onUserCreated(User user);
        void onUserCreationFailed();
    }

    public interface OnSendEmailListener {
        void onEmailSent();
        void onFailure();
    }
    public interface OnPatientsListListener {
        void onPatientsListRead(ArrayList<String> nameList,ArrayList<String> idList,ArrayList<String> emailList);
        void onPatientsListReadFailure();
    }
    public interface OnRefreshListener {
        void onRefreshSuccess();
        void onRefreshFailure();
    }
    public interface OnUserUpdateListener {
        void onUserUpdate(boolean result);
    }

    //metodo per creare l'utente loggato
    public static void createUser(FirebaseAuth mAuth, OnUserCreatedListener listener) {
        User.setUser(mAuth.getCurrentUser());

        db.collection("users")
                .whereEqualTo("id", mAuth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            User user;
                            if(document.getString("role").equals("therapist"))
                                user = User.getInstance(document.getString("id"), document.getString("role"), document.getString("name"),
                                        document.getString("city"), document.getString("address"));
                            else
                                user = User.getInstance(document.getString("id"), document.getString("role"));

                            if (listener != null) {
                                listener.onUserCreated(user);
                            }
                        }
                    }else{
                        if (listener != null) {
                            listener.onUserCreationFailed();
                        }
                    }
                });
    }


    public static void sendEmailforChangePassword(String emailAddress, OnSendEmailListener listener){

        FirebaseAuth.getInstance().sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            listener.onEmailSent();
                        } else{
                            listener.onFailure();
                        }
                    }
                });
    }

    public static void getPatientsListforDoctor(OnPatientsListListener listener){
        db.collection("users")
                .whereEqualTo("doctor_id", User.getInstance().getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            ArrayList<String> nameList = new ArrayList<>();
                            ArrayList<String> idList = new ArrayList<>();
                            ArrayList<String> emailList = new ArrayList<>();
                            for(QueryDocumentSnapshot document : task.getResult()){
                                String element = document.getString("child_name");
                                String elementid = document.getString("id");
                                String elementEmail = document.getString("email");
                                nameList.add(element);
                                idList.add(elementid);
                                emailList.add(elementEmail);
                            }
                            listener.onPatientsListRead(nameList, idList, emailList);
                        }
                        else {
                            listener.onPatientsListReadFailure();
                        }
                    }
                });
    }

    public static void refreshUser(OnRefreshListener listener){
        User.disconnect();
        createUser(FirebaseAuth.getInstance(), new OnUserCreatedListener() {
            @Override
            public void onUserCreated(User user) {
                listener.onRefreshSuccess();
            }

            @Override
            public void onUserCreationFailed() {
                listener.onRefreshFailure();
            }
        });
    }

    public static void changeUserData(String data, String newData, OnUserUpdateListener listener){
        db.collection("users")
                .whereEqualTo("id", User.getInstance().getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String userDocument = document.getId();
                                db.collection("users").document(userDocument).update(data, newData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        listener.onUserUpdate(task.isSuccessful());
                                    }
                                });
                            }
                        } else{
                            listener.onUserUpdate(false);
                        }
                    }
                });
    }

}
