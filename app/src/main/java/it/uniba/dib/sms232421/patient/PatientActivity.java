package it.uniba.dib.sms232421.patient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.animation.LayoutTransition;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import it.uniba.dib.sms232421.appointments.Appointment;
import it.uniba.dib.sms232421.exercises.Exercise;
import it.uniba.dib.sms232421.exercises.FirebaseExerciseModel;
import it.uniba.dib.sms232421.appointments.FirebaseAppointmentsModel;
import it.uniba.dib.sms232421.user.FirebaseUserModel;
import it.uniba.dib.sms232421.network.NetworkError;
import it.uniba.dib.sms232421.network.NetworkUtils;
import it.uniba.dib.sms232421.R;
import it.uniba.dib.sms232421.patient.child.ChildActivity;
import it.uniba.dib.sms232421.patient.parent.ParentActivity;
import it.uniba.dib.sms232421.utility.SharedViewModel;

public class PatientActivity extends AppCompatActivity {
    private TreeMap<ArrayList<Object>, ArrayList<Exercise>> exercises;
    private ArrayList<Appointment> appointments;
    private View layoutBambino;
    private View layoutPassword;
    private AlertDialog permissionDialog;
    private AlertDialog dialog;
    private EditText password;
    private Button accessButton;
    private ImageButton buttonBambino;
    private ImageButton buttonGenitore;
    private Button skipButton;
    private LinearLayout layout;

    private static final int REQUEST_NOTIFICATION = 16;
    private NetworkUtils networkUtils;
    private IntentFilter intentFilter;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private boolean isPermissionDialogShown = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Imposta la modalità a schermo intero
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_patient);

        sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

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

    //metodo per proseguire in caso di presenza di connessione
    private void isConnectedIsTrue() {
        FirebaseExerciseModel.getPatientExercises(Patient.getInstance().getId(), new FirebaseExerciseModel.OnAllExercisesCreatedListener() {
            @Override
            public void onExercisesCreated(TreeMap<ArrayList<Object>, ArrayList<Exercise>> exercises) {
                FirebaseAppointmentsModel.getPatientAppointments(new FirebaseAppointmentsModel.OnReadAppointmentsListener() {
                    @Override
                    public void onAppointmentsRead(ArrayList<Appointment> appointments) {
                        //imposto i nomi degli utenti
                        TextView textParent = findViewById(R.id.textGenitore);
                        textParent.setText(Patient.getInstance().getParentName());
                        TextView textChild = findViewById(R.id.textBambino);
                        textChild.setText(Patient.getInstance().getChildName());

                        buttonBambino = findViewById(R.id.buttonBambino);
                        buttonBambino.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                childAreaEnter(exercises);
                            }

                        });

                        buttonGenitore = findViewById(R.id.buttonGenitore);
                        layout = findViewById(R.id.buttonGenitoreLayout);
                        layout.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
                        buttonGenitore.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //nascondo il layout del bambino e mostro il campo per la password
                                layoutBambino = findViewById(R.id.buttonBambinoLayout);
                                layoutBambino.setVisibility(View.GONE);
                                layoutPassword = findViewById(R.id.passwordLayout);
                                layoutPassword.setVisibility(View.VISIBLE);
                                //autentica genitore
                                password = findViewById(R.id.passwordInputText);
                                accessButton = findViewById(R.id.AccessButton);
                                accessButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
                                        String patientPassword = sharedPreferences.getString("password",null);
                                        if (patientPassword != null && patientPassword.equals(password.getText().toString())) {
                                            if(exercises!=null)
                                                parentAreaEnter(exercises, appointments);
                                            else{
                                                FirebaseExerciseModel.getPatientExercises(Patient.getInstance().getId(),new FirebaseExerciseModel.OnAllExercisesCreatedListener() {
                                                    @Override
                                                    public void onExercisesCreated(TreeMap<ArrayList<Object>, ArrayList<Exercise>> exercises) {
                                                        FirebaseAppointmentsModel.getPatientAppointments(new FirebaseAppointmentsModel.OnReadAppointmentsListener() {
                                                            @Override
                                                            public void onAppointmentsRead(ArrayList<Appointment> appointments) {
                                                                parentAreaEnter(exercises,appointments);
                                                            }
                                                            @Override
                                                            public void onAppointmentsReadFailed() {}
                                                        });
                                                    }
                                                    @Override
                                                    public void onExercisesCreationFailed() {}
                                                });
                                            }
                                        } else {
                                            Toast.makeText(PatientActivity.this, getString(R.string.wrong_password),
                                                    Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });
                                skipButton = findViewById(R.id.buttonSkip);
                                skipButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        password.setText("Paziente");
                                        accessButton.performClick();
                                    }
                                });
                            }
                        });

                        if (!isPermissionDialogShown &&
                                ContextCompat.checkSelfPermission(PatientActivity.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED &&
                                !sharedPreferences.getBoolean("isNotificationPermissionAlreadyRequested", false)) {
                            showPermissionDialog();
                            editor.putBoolean("isNotificationPermissionAlreadyRequested", true).apply();
                            isPermissionDialogShown = true;
                        }
                    }

                    @Override
                    public void onAppointmentsReadFailed() {

                    }
                });
            }

            @Override
            public void onExercisesCreationFailed() {

            }
        });
    }


    // Mostra la finestra di dialogo per la richiesta di permesso di accesso alle notifiche
    private void showPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View customView = inflater.inflate(R.layout.permission_dialog, null);

        TextView description = customView.findViewById(R.id.description);
        Button continueButton = customView.findViewById(R.id.continueButton);
        continueButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));

        builder.setView(customView);
        permissionDialog = builder.create();
        permissionDialog.setCanceledOnTouchOutside(false);
        permissionDialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.dialog_rounded_corner_gray));

        // Stampare opportuno messaggio in base alla richiesta di permesso
        description.setText(R.string.permissionDescription3);

        // Imposta l'azione del pulsante "Avanti"
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permissionDialog.dismiss();
                // Richiedi il permesso solo dopo aver chiuso la finestra di dialogo
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION);
            }
        });

        // Mostra la finestra di dialogo
        permissionDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (layoutPassword != null && layoutBambino != null) {
            layoutBambino.setVisibility(View.VISIBLE);
            layoutPassword.setVisibility(View.GONE);
        }
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
        Intent intent = new Intent(PatientActivity.this, NetworkError.class);
        startActivity(intent);
        finish();
    }

    //metodo per mostrare una finestra di dialogo per chiedere se si vuole
    //usare questo dispositivo come principale e ricevere qui le notifiche
    private void requestPrincipalDevice(String rule) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View customView = inflater.inflate(R.layout.principal_device_request_dialog, null);

        Button positiveButton = customView.findViewById(R.id.custom_positive_button);
        Button negativeButton = customView.findViewById(R.id.custom_negative_button);

        builder.setView(customView);
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.dialog_rounded_corner_gray));
        dialog.show();

        // Imposta l'azione del pulsante SI
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Chiudi la finestra di dialogo e imposta come principale il dispositivo e reindirizzamento
                Intent intent;
                dialog.dismiss();
                switch(rule) {
                    case "parent":
                        //aggiornamento token genitore
                        FirebasePatientModel.updateParentToken();
                        //entro nell'area genitore
                        intent = new Intent(PatientActivity.this, ParentActivity.class);
                        startActivity(intent);
                        finish();
                        break;

                    case "child":
                        //aggiornamento token bambino
                        FirebasePatientModel.updateChildToken();
                        //entro nell'area bambino
                        intent = new Intent(PatientActivity.this, ChildActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                }

            }
        });

        // Imposta l'azione del pulsante NO
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chiudi la finestra di dialogo e reindirizzamento
                dialog.dismiss();

                //vedo chi devo loggare
                Intent intent;
                switch (rule) {
                    case "parent":
                        //entro nell'area genitore
                        intent = new Intent(PatientActivity.this, ParentActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                    case "child":
                        //entro nell'area bambino
                        intent = new Intent(PatientActivity.this, ChildActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                }

            }
        });

    }


    //metodo per effettuare il normale login all'area genitore
    void parentAreaEnter(TreeMap<ArrayList<Object>, ArrayList<Exercise>> exercises, ArrayList<Appointment> appointments){
        //salvo la scelta del profilo
        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("profile", "parent").apply();
        //Controllo il token
        FirebasePatientModel.checkParentToken(new FirebasePatientModel.UpdateTokenListener() {
            @Override
            public void onTokenChecked(boolean isTokenDifferent) {
                if(isTokenDifferent){
                    //richiesta
                    requestPrincipalDevice("parent");
                } else{
                    //entro nell'area genitore
                    Intent intent = new Intent(PatientActivity.this, ParentActivity.class);
                    SharedViewModel.getInstance().setExercises(exercises);
                    SharedViewModel.getInstance().setAppointments(appointments);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onTokenCheckFailed() {}
        });

    }

    //metodo per eseguire passaggio nell'area bambino
    void childAreaEnter(TreeMap<ArrayList<Object>, ArrayList<Exercise>> exercises){
        //salvo la scelta del profilo
        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("profile","child").apply();
        //Controllo il token
        FirebasePatientModel.checkChildToken(new FirebasePatientModel.UpdateTokenListener() {
            @Override
            public void onTokenChecked(boolean isTokenDifferent) {
                if(isTokenDifferent){
                    requestPrincipalDevice("child");
                } else{
                    //entro nell'area bambino
                    Intent intent = new Intent(PatientActivity.this, ChildActivity.class);
                    SharedViewModel.getInstance().setExercises(exercises);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onTokenCheckFailed() {}
        });

    }
}
