package it.uniba.dib.sms2324FF2.utility;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;

import java.util.ArrayList;
import java.util.TreeMap;

import it.uniba.dib.sms2324FF2.R;
import it.uniba.dib.sms2324FF2.therapist.appointments.TherapistAppointment;
import it.uniba.dib.sms2324FF2.login.FirebaseAuthenticationModel;
import it.uniba.dib.sms2324FF2.patient.child.ChildActivity;
import it.uniba.dib.sms2324FF2.login.LoginActivity;
import it.uniba.dib.sms2324FF2.patient.PatientActivity;
import it.uniba.dib.sms2324FF2.patient.child.exercises.Exercise;
import it.uniba.dib.sms2324FF2.patient.child.ranking.Ranking;
import it.uniba.dib.sms2324FF2.therapist.TherapistActivity;
import it.uniba.dib.sms2324FF2.network.NetworkError;
import it.uniba.dib.sms2324FF2.network.NetworkUtils;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DURATION = 0; // Durata del delay della splash screen
    private ImageView imageView;
    private NetworkUtils networkUtils;
    private IntentFilter intentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseApp.initializeApp(this);
        // Imposta la modalità a schermo intero
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        //verifico se il device è connesso ad internet utilizzando un listener
        //per catturare cambi di stato di connessione
        networkUtils = new NetworkUtils();
        intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

        //verifico a priori se c'è connessione o meno
        if (networkUtils.isConnected(this)) {
            isConnectedIsTrue();
        } else {
            isConnectedIsFalse();
        }

        //verifico se il device è connesso ad internet utilizzando un listener
        //per catturare cambi di stato di connessione
        networkUtils.setNetworkChangeListener(new NetworkUtils.NetworkChangeListener() {
            @Override
            public void onNetworkChange(boolean isConnected) {
                //verifico di che tipo di cambio di rete si tratta
                if (isConnected) {
                    isConnectedIsTrue();
                } else {
                    isConnectedIsFalse();
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
        //setto l'immagine logo dell'app
        imageView = findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.logo_app);

        // Imposto le dimensioni desiderate in pixel dell'immagine logo dell'app
        int larghezzaDesiderata = 800;
        int altezzaDesiderata = 800;

        // Imposta le nuove dimensioni
        imageView.setLayoutParams(new LinearLayout.LayoutParams(larghezzaDesiderata, altezzaDesiderata));

        // Visualizza l'immagine per l'intera durata dello splash screen
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                //verifico se l'utente ha l'accesso "memorizzato"
                SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
                boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
                String email = sharedPreferences.getString("email", null);
                String password = sharedPreferences.getString("password", null);
                String profile = sharedPreferences.getString("profile", "");
                String role = sharedPreferences.getString("role", "");

                //effettuo il login dell'utente
                if (isLoggedIn) {
                    FirebaseAuthenticationModel.autoLogin(email, password, profile, role, new FirebaseAuthenticationModel.OnAutoLoginListener() {
                        @Override
                        public void onAutoLoginChildSuccess(TreeMap<ArrayList<Object>, ArrayList<Exercise>> exercises, ArrayList<Ranking> rankingList) {
                            // Avvia l'activity del bambino
                            Intent intent = new Intent(SplashActivity.this, ChildActivity.class);
                            SharedViewModel.getInstance().setExercises(exercises);
                            SharedViewModel.getInstance().setRankingList(rankingList);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onAutoLoginParentSuccess(TreeMap<ArrayList<Object>, ArrayList<Exercise>> exercises, ArrayList<TherapistAppointment> therapistAppointments) {
                            // Avvia l'activity del genitore
                            Intent intent = new Intent(SplashActivity.this, PatientActivity.class);
                            SharedViewModel.getInstance().setExercises(exercises);
                            SharedViewModel.getInstance().setAppointments(therapistAppointments);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onAutoLoginTherapistSuccess(ArrayList<TherapistAppointment> therapistAppointments, ArrayList<String> patients, ArrayList<String> patientsId) {
                            // Avvia l'activity del genitore
                            Intent intent = new Intent(SplashActivity.this, TherapistActivity.class);
                            SharedViewModel.getInstance().setAppointments(therapistAppointments);
                            SharedViewModel.getInstance().setPatientsId(patientsId);
                            SharedViewModel.getInstance().setPatients(patients);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onAutoLoginFailure() {
                            // Avvia l'activity del login, utente non autenticato
                            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                } else {
                    // Avvia l'activity del login, utente non autenticato
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }

            }
        }, SPLASH_DURATION);
    }

    void isConnectedIsFalse(){
        // Avvia l'activity di errore di connessione
        Intent intent = new Intent(SplashActivity.this, NetworkError.class);
        startActivity(intent);
        finish();
    }

}


