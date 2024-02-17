package it.uniba.dib.sms2324FF2.network;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.TreeMap;

import it.uniba.dib.sms2324FF2.R;
import it.uniba.dib.sms2324FF2.therapist.appointments.TherapistAppointment;
import it.uniba.dib.sms2324FF2.patient.child.exercises.Exercise;
import it.uniba.dib.sms2324FF2.login.FirebaseAuthenticationModel;
import it.uniba.dib.sms2324FF2.patient.child.ChildActivity;
import it.uniba.dib.sms2324FF2.login.LoginActivity;
import it.uniba.dib.sms2324FF2.patient.PatientActivity;
import it.uniba.dib.sms2324FF2.patient.child.ranking.Ranking;
import it.uniba.dib.sms2324FF2.patient.parent.ParentActivity;
import it.uniba.dib.sms2324FF2.therapist.TherapistActivity;
import it.uniba.dib.sms2324FF2.utility.SharedViewModel;

public class NetworkError extends AppCompatActivity {
    private NetworkUtils networkUtils;
    private IntentFilter intentFilter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Imposta la modalità a schermo intero
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_network_error);

        //verifico se il device è connesso ad internet utilizzando un listener
        //per catturare cambi di stato di connessione
        networkUtils = new NetworkUtils();
        intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        networkUtils.setNetworkChangeListener(new NetworkUtils.NetworkChangeListener() {
            @Override
            public void onNetworkChange(boolean isConnected) {
                if (isConnected) {
                    isConnectedIsTrue();
                }
            }
        });

        // Registra il BroadcastReceiver all'interno del metodo onResume
        registerReceiver(networkUtils, intentFilter);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Deregistra il BroadcastReceiver all'interno del metodo onPause
        unregisterReceiver(networkUtils);
    }

    void isConnectedIsTrue(){
        // Visualizza l'immagine per l'intera durata dello splash screen
        //verifico se l'utente ha l'accesso "memorizzato"
        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        String email = sharedPreferences.getString("email", null);
        String password = sharedPreferences.getString("password", null);
        String role = sharedPreferences.getString("role", "");
        String profile = sharedPreferences.getString("profile", "");

        //effettuo il login dell'utente
        if (isLoggedIn) {
                    FirebaseAuthenticationModel.autoLogin(email, password, profile, role, new FirebaseAuthenticationModel.OnAutoLoginListener() {
                        @Override
                        public void onAutoLoginChildSuccess(TreeMap<ArrayList<Object>, ArrayList<Exercise>> exercises, ArrayList<Ranking> rankingList) {
                            // Avvia l'activity del bambino
                            Intent intent = new Intent(NetworkError.this, ChildActivity.class);
                            SharedViewModel.getInstance().setExercises(exercises);
                            SharedViewModel.getInstance().setRankingList(rankingList);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onAutoLoginParentSuccess(TreeMap<ArrayList<Object>, ArrayList<Exercise>> exercises, ArrayList<TherapistAppointment> therapistAppointments) {
                            // Avvia l'activity del genitore
                                if(!getIntent().hasExtra("firstAccess")) {
                                    Intent intent = new Intent(NetworkError.this, PatientActivity.class);
                                    SharedViewModel.getInstance().setExercises(exercises);
                                    SharedViewModel.getInstance().setAppointments(therapistAppointments);
                                    startActivity(intent);
                                    finish();
                                } else{
                                    Intent intent = new Intent(NetworkError.this, ParentActivity.class);
                                    SharedViewModel.getInstance().setExercises(exercises);
                                    SharedViewModel.getInstance().setAppointments(therapistAppointments);
                                    startActivity(intent);
                                    finish();
                                }

                        }

                        @Override
                        public void onAutoLoginTherapistSuccess(ArrayList<TherapistAppointment> therapistAppointments, ArrayList<String> patients, ArrayList<String> patientsId ) {
                            // Avvia l'activity del dottore
                            Intent intent = new Intent(NetworkError.this, TherapistActivity.class);
                            SharedViewModel.getInstance().setAppointments(therapistAppointments);
                            SharedViewModel.getInstance().setPatients(patients);
                            SharedViewModel.getInstance().setPatientsId(patientsId);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onAutoLoginFailure() {}
                    });
        } else {
            // Avvia l'activity del login, utente non autenticato
            Intent intent = new Intent(NetworkError.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

    }

}

