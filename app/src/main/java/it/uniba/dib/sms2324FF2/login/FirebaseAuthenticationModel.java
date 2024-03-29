package it.uniba.dib.sms2324FF2.login;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.TreeMap;

import it.uniba.dib.sms2324FF2.therapist.appointments.TherapistAppointment;
import it.uniba.dib.sms2324FF2.therapist.appointments.TherapistFirebaseAppointmentsModel;
import it.uniba.dib.sms2324FF2.patient.child.exercises.Exercise;
import it.uniba.dib.sms2324FF2.patient.child.exercises.FirebaseExerciseModel;
import it.uniba.dib.sms2324FF2.patient.PatientFirebaseModel;
import it.uniba.dib.sms2324FF2.patient.Patient;
import it.uniba.dib.sms2324FF2.patient.child.ranking.FirebaseRankingModel;
import it.uniba.dib.sms2324FF2.patient.child.ranking.Ranking;
import it.uniba.dib.sms2324FF2.user.FirebaseUserModel;
import it.uniba.dib.sms2324FF2.user.User;


public class FirebaseAuthenticationModel {
    public interface OnLoginListener {
        void onLoginSuccess();
        void onLoginFailure();
    }
    public interface OnAutoLoginListener {
        void onAutoLoginChildSuccess(TreeMap<ArrayList<Object>, ArrayList<Exercise>> exercises, ArrayList<Ranking> rankingList);

        void onAutoLoginParentSuccess(TreeMap<ArrayList<Object>, ArrayList<Exercise>> exercises, ArrayList<TherapistAppointment> therapistAppointments);

        void onAutoLoginTherapistSuccess(ArrayList<TherapistAppointment> therapistAppointments, ArrayList<String> patients, ArrayList<String> patientsId);

        void onAutoLoginFailure();
    }
    public interface OnPatientAddedListener {
        void onSuccess(String uid);
        void onFailure();
    }


    //metodo per effettuare il login
    public static void login(String email, String password, OnLoginListener listener){

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //creo l'utente e lo loggo
                            FirebaseUserModel.createUser(FirebaseAuth.getInstance(), new FirebaseUserModel.OnUserCreatedListener() {
                                @Override
                                public void onUserCreated(User user) {
                                    listener.onLoginSuccess();
                                }
                                @Override
                                public void onUserCreationFailed() {
                                    listener.onLoginFailure();
                                }
                            });
                        } else {
                            listener.onLoginFailure();
                        }
                    }
                });
    }

    //metodo per eseguire automaticamente l'accesso, se l'utente ha l'accesso "memorizzato"
    public static void autoLogin(String email, String password, String profile, String role, OnAutoLoginListener listener){
        login(email, password, new OnLoginListener() {
            @Override
            public void onLoginSuccess() {
                if(role.equals("patient")) {
                    //Creo il paziente
                    PatientFirebaseModel.createPatient(new PatientFirebaseModel.OnUserCreatedListener() {
                        @Override
                        public void onUserCreated(Patient patient) {
                            //Ottengo gli esercizi dopo averli aggiornati
                            FirebaseExerciseModel.checkExercisesState(new FirebaseExerciseModel.OnExercisesStateCheckedListener() {
                                @Override
                                public void onExercisesStateChecked() {
                                    FirebaseExerciseModel.getPatientExercises(Patient.getInstance().getId(),new FirebaseExerciseModel.OnAllExercisesCreatedListener() {
                                        @Override
                                        public void onExercisesCreated(TreeMap<ArrayList<Object>, ArrayList<Exercise>> exercises) {

                                            if (profile.equals("child")) {
                                                //Ottengo la classifica
                                                FirebaseRankingModel.getRankingList(new FirebaseRankingModel.RankingCallback() {
                                                    @Override
                                                    public void onSuccess(ArrayList<Ranking> rankingList) {
                                                        listener.onAutoLoginChildSuccess(exercises, rankingList);

                                                    }

                                                    @Override
                                                    public void onFailure() {
                                                        listener.onAutoLoginFailure();
                                                    }
                                                });

                                            } else {
                                                TherapistFirebaseAppointmentsModel.getPatientAppointments(new TherapistFirebaseAppointmentsModel.OnReadAppointmentsListener() {
                                                    @Override
                                                    public void onAppointmentsRead(ArrayList<TherapistAppointment> therapistAppointments) {
                                                        listener.onAutoLoginParentSuccess(exercises, therapistAppointments);

                                                    }

                                                    @Override
                                                    public void onAppointmentsReadFailed() {
                                                        listener.onAutoLoginFailure();
                                                    }
                                                });
                                            }
                                        }

                                        @Override
                                        public void onExercisesCreationFailed() {
                                            listener.onAutoLoginFailure();

                                        }
                                    });
                                }

                                @Override
                                public void onExercisesStateCheckFailed() {
                                    listener.onAutoLoginFailure();

                                }
                            });

                        }

                        @Override
                        public void onUserCreationFailed() {
                            listener.onAutoLoginFailure();

                        }
                    });
                } else if(role.equals("therapist")){
                    TherapistFirebaseAppointmentsModel.getDoctorAppointments(new TherapistFirebaseAppointmentsModel.OnReadAppointmentsListener() {
                        @Override
                        public void onAppointmentsRead(ArrayList<TherapistAppointment> therapistAppointments) {
                            FirebaseUserModel.getPatientsListforDoctor(new FirebaseUserModel.OnPatientsListListener() {
                                @Override
                                public void onPatientsListRead(ArrayList<String> patients, ArrayList<String> patientsId,ArrayList<String> emailList) {
                                    listener.onAutoLoginTherapistSuccess(therapistAppointments, patients, patientsId);

                                }

                                @Override
                                public void onPatientsListReadFailure() {
                                    listener.onAutoLoginFailure();


                                }
                            });
                        }

                        @Override
                        public void onAppointmentsReadFailed() {
                            listener.onAutoLoginFailure();

                        }
                    });
                }
            }
            @Override
            public void onLoginFailure() {
                listener.onAutoLoginFailure();
            }
        });
    }

    public static void logout(Object instance){
        if(instance instanceof Patient) {
            Patient.disconnect();
            User.disconnect();
        }else
            User.disconnect();
        FirebaseAuth.getInstance().signOut();
    }

    public static void addPatient(String email, OnPatientAddedListener listener){
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, email.split("@")[0])
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUserModel.sendEmailforChangePassword(email, new FirebaseUserModel.OnSendEmailListener() {
                                @Override
                                public void onEmailSent() {
                                    // Sign in success, update UI with the signed-in user's information
                                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    listener.onSuccess(userId);
                                }

                                @Override
                                public void onFailure() {
                                    listener.onFailure();
                                }
                            });
                        } else {
                            // If sign in fails, display a message to the user.
                            listener.onFailure();
                        }
                    }
                });
    }
}
