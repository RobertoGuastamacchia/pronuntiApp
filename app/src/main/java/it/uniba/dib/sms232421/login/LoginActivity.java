package it.uniba.dib.sms232421.login;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import it.uniba.dib.sms232421.appointments.Appointment;
import it.uniba.dib.sms232421.exercises.Exercise;
import it.uniba.dib.sms232421.exercises.FirebaseExerciseModel;
import it.uniba.dib.sms232421.appointments.FirebaseAppointmentsModel;
import it.uniba.dib.sms232421.patient.FirebasePatientModel;
import it.uniba.dib.sms232421.therapist.TherapistActivity;
import it.uniba.dib.sms232421.user.FirebaseUserModel;
import it.uniba.dib.sms232421.user.User;
import it.uniba.dib.sms232421.network.NetworkError;
import it.uniba.dib.sms232421.network.NetworkUtils;
import it.uniba.dib.sms232421.R;
import it.uniba.dib.sms232421.patient.Patient;
import it.uniba.dib.sms232421.patient.PatientActivity;
import it.uniba.dib.sms232421.utility.SharedViewModel;

public class LoginActivity extends AppCompatActivity {
    public static final String PATIENT = "patient";
    public static final String THERAPIST = "therapist";
    FirebaseAuth mAuth;
    EditText email, password;
    Button buttonLogin, skipLogin;
    MaterialButton loginDoctorButton,loginPatientButton;
    String role;
    FirebaseFirestore db;
    ProgressBar progressBar;

    ImageButton info;
    private NetworkUtils networkUtils;
    private IntentFilter intentFilter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Imposta la modalità a schermo intero
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //imposta il layout
        setContentView(R.layout.activity_login);

        //verifico se il device è connesso ad internet utilizzando un listener
        //per catturare cambi di stato di connessione
        networkUtils = new NetworkUtils();
        intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

        //verifico a priori se c'è connessione o meno
        if(networkUtils.isConnected(this)) {
            isConnectedIsTrue();
        }else{
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
                }else{
                    isConnectedIsFalse();
                }
            }
        });

        // Registra il BroadcastReceiver all'interno del metodo onResume
        registerReceiver(networkUtils, intentFilter);

    }

    //caso in cui la connessione sia presente
    private void isConnectedIsTrue() {
        //ottieni i riferimenti dal layout
        progressBar = findViewById(R.id.progressBar);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginDoctorButton = findViewById(R.id.loginDoctor);
        loginPatientButton = findViewById(R.id.loginPatient);
        buttonLogin = findViewById(R.id.loginBtn);
        skipLogin = findViewById(R.id.buttonSkip);
        info = findViewById(R.id.information);

        //ottieni istanza del database su firebase
        db = FirebaseFirestore.getInstance();
        //ottengo l'istanza per l'autenticazione su firebase
        mAuth = FirebaseAuth.getInstance();

        role= PATIENT; //avvaloro il ruolo
        //  pulsante del dottore
        loginDoctorButton.setBackgroundColor(getColor(R.color.switchOffColor));
        loginDoctorButton.setTextColor(getColor(R.color.white));

        //gestisco l'evento di click sullo spostamento nella sezione login del paziente
        loginPatientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //nascondo il button informativo per i logopedisti
                info.setVisibility(View.GONE);

                //cambia colori
                loginDoctorButton.setBackgroundColor(getColor(R.color.switchOffColor));
                loginDoctorButton.setTextColor(getColor(android.R.color.white));

                loginPatientButton.setBackgroundColor(getColor(R.color.primary));
                loginPatientButton.setTextColor(getColor(android.R.color.white));

                role= PATIENT;

                //nuovo fragment
            }
        });

        //gestisco l'evento di click sullo spostamento nella sezione login del logopedista
        loginDoctorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //mostro un button informativo per i logopedisti
                info.setVisibility(View.VISIBLE);
                //click su pannello informativo
                info.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showInformationDialog();
                    }
                });

                //cambia colori
                loginPatientButton.setBackgroundColor(getColor(R.color.switchOffColor));
                loginPatientButton.setTextColor(getColor(android.R.color.white));

                loginDoctorButton.setBackgroundColor(getColor(R.color.primary));
                loginDoctorButton.setTextColor(getColor(android.R.color.white));

                role= THERAPIST;

                //nuovo fragment
                //getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new LoginFragmentDoctor()).commit();
            }
        });

        //gestisco l'evento nel momento in cui l'utente clicca sul pulsante di login
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    //se l'email è vuota
                    if (TextUtils.isEmpty(String.valueOf(email.getText()))) {
                        //mostro messaggio d'errore
                        Toast.makeText(LoginActivity.this, getString(R.string.emptyEmail), Toast.LENGTH_LONG).show();
                        return;
                    }
                    //se la password è vuota
                    if (TextUtils.isEmpty(String.valueOf(password.getText()))) {
                        //mostro messaggio d'errore
                        Toast.makeText(LoginActivity.this, getString(R.string.emptyPassword), Toast.LENGTH_LONG).show();
                        return;
                    }
                    progressBar.setVisibility(View.VISIBLE);

                    //Dopo aver superato i controlli, effettuo il login
                    FirebaseAuthenticationModel.login(String.valueOf(email.getText()), String.valueOf(password.getText()), new FirebaseAuthenticationModel.OnLoginListener() {
                        @Override
                        public void onLoginSuccess() {
                            //Controllo che tipo di utente si è loggato
                            valutateUser(User.getInstance().getRole());
                        }

                        @Override
                        public void onLoginFailure() {
                            //Nascondo la progress bar
                            progressBar.setVisibility(View.GONE);
                            // autenticazione fallita, mostro messaggio d'errore all'utente
                            Toast.makeText(LoginActivity.this, getString(R.string.loginFail),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

            }
        });

        //metodo per saltare la procedura di login, utilizzato per fini didattici
        skipLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    if(role.equals(PATIENT)) {
                        email.setText("paziente@gmail.com");
                        password.setText("Paziente");
                    }else{
                        email.setText("logopedista@gmail.com");
                        password.setText("Logopedista");
                    }
                    buttonLogin.performClick();
            }
        });
    }

    //metodo per mostrare una finestra di dialogo informativa agli utenti
    private void showInformationDialog() {
        // Ottieni l'oggetto LayoutInflater
        LayoutInflater inflater = LayoutInflater.from(LoginActivity.this);

        // Carica il layout del tuo dialog personalizzato
        View dialogView = inflater.inflate(R.layout.information_dialog_login, null);

        // Crea un AlertDialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(LoginActivity.this);
        alertDialogBuilder.setView(dialogView);


        // Mostra il tuo AlertDialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.dialog_rounded_corner_gray));

        @SuppressLint("MissingInflatedId") MaterialButton close = dialogView.findViewById(R.id.close);
        //gestione evento annulla operazione
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chiudi il dialog senza fare alcuna operazione
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    //effettuo il login dell'utente
    public void valutateUser(String roleFromDb){
        if(role.equals(PATIENT) && role.equals(roleFromDb)) {

            //memorizzo l'utente per "ricordarlo" dopo aver effettuato il primo accesso
            //in modo tale da evitare il login ogni qualvolta si apre l'app
            SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isLoggedIn", true);
            editor.putString("role", PATIENT);
            //PROBLEMA DI SICUREZZA. Cifrare la password
            editor.putString("password", password.getText().toString());
            editor.putString("email",email.getText().toString());
            editor.apply();

            //Creo il paziente
            FirebasePatientModel.createPatient(new FirebasePatientModel.OnUserCreatedListener() {
                @Override
                public void onUserCreated(Patient patient) {
                    FirebaseExerciseModel.checkExercisesState(new FirebaseExerciseModel.OnExercisesStateCheckedListener() {
                        @Override
                        public void onExercisesStateChecked() {
                            //Ottengo gli esercizi
                            FirebaseExerciseModel.getPatientExercises(Patient.getInstance().getId(),new FirebaseExerciseModel.OnAllExercisesCreatedListener() {
                                @Override
                                public void onExercisesCreated(TreeMap<ArrayList<Object>, ArrayList<Exercise>> exercises) {
                                    //Ottengo gli appuntamenti
                                    FirebaseAppointmentsModel.getPatientAppointments(new FirebaseAppointmentsModel.OnReadAppointmentsListener() {
                                        @Override
                                        public void onAppointmentsRead(ArrayList<Appointment> appointments) {
                                            //Nascondo la progress bar
                                            progressBar.setVisibility(View.GONE);
                                            //Creo l'intent per l'activity del paziente
                                            Intent intent = new Intent(getApplicationContext(), PatientActivity.class);
                                            SharedViewModel.getInstance().setExercises(exercises);
                                            SharedViewModel.getInstance().setAppointments(appointments);
                                            startActivity(intent);
                                            finish();
                                        }
                                        @Override
                                        public void onAppointmentsReadFailed() {}
                                    });
                                }
                                @Override
                                public void onExercisesCreationFailed() {}
                            });
                        }

                        @Override
                        public void onExercisesStateCheckFailed() {

                        }
                    });
                }
                @Override
                public void onUserCreationFailed() {}
            });


        }else if(role.equals(THERAPIST) && role.equals(roleFromDb)){
            //Nascondo la progress bar
            progressBar.setVisibility(View.GONE);

            //memorizzo l'utente per "ricordarlo" dopo aver effettuato il primo accesso
            //in modo tale da evitare il login ogni qualvolta si apre l'app
            SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isLoggedIn", true);
            editor.putString("role", THERAPIST);
            //PROBLEMA DI SICUREZZA. Cifrare la password
            editor.putString("password", password.getText().toString());
            editor.putString("email",email.getText().toString());
            editor.apply();

            //Creo l'intent per l'activity del logopedista
            FirebaseAppointmentsModel.getDoctorAppointments(new FirebaseAppointmentsModel.OnReadAppointmentsListener() {
                @Override
                public void onAppointmentsRead(ArrayList<Appointment> appointments) {
                    FirebaseUserModel.getPatientsListforDoctor(new FirebaseUserModel.OnPatientsListListener() {
                        @Override
                        public void onPatientsListRead(ArrayList<String> patients, ArrayList<String> patientsId,ArrayList<String> emailList) {
                            Intent intent = new Intent(getApplicationContext(), TherapistActivity.class);
                            SharedViewModel.getInstance().setAppointments(appointments);
                            SharedViewModel.getInstance().setPatients(patients);
                            SharedViewModel.getInstance().setPatientsId(patientsId);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onPatientsListReadFailure() {

                        }
                    });
                }

                @Override
                public void onAppointmentsReadFailed() {

                }
            });
        } else{
            //Nascondo la progress bar
            progressBar.setVisibility(View.GONE);
            //L'utente è nella sezione sbagliata
            Toast.makeText(LoginActivity.this, getString(R.string.wrongSection),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        //disattivazione tasto back, implementazione mancante
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Deregistra il BroadcastReceiver all'interno del metodo onPause
        unregisterReceiver(networkUtils);
    }

    //messaggio d'errore in caso di assenza di connessione
    void isConnectedIsFalse(){
        // Avvia l'activity di errore di connessione
        Intent intent = new Intent(LoginActivity.this, NetworkError.class);
        startActivity(intent);
        finish();
    }

    //gestione cambio orientamento schermo
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.landscape_layout);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_login);
        }
    }

}