package it.uniba.dib.sms2324FF2.patient.parent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.TreeMap;

import it.uniba.dib.sms2324FF2.R;
import it.uniba.dib.sms2324FF2.therapist.appointments.TherapistAppointment;
import it.uniba.dib.sms2324FF2.patient.child.exercises.Exercise;
import it.uniba.dib.sms2324FF2.patient.child.exercises.FirebaseExerciseModel;
import it.uniba.dib.sms2324FF2.login.FirebaseAuthenticationModel;
import it.uniba.dib.sms2324FF2.login.LoginActivity;
import it.uniba.dib.sms2324FF2.patient.PatientFirebaseModel;
import it.uniba.dib.sms2324FF2.patient.Patient;
import it.uniba.dib.sms2324FF2.patient.PatientActivity;
import it.uniba.dib.sms2324FF2.patient.parent.exercises.ExercisesFragment;
import it.uniba.dib.sms2324FF2.therapist.appointments.TherapistFirebaseAppointmentsModel;
import it.uniba.dib.sms2324FF2.patient.parent.exercises.ImageDenominationCorrection;
import it.uniba.dib.sms2324FF2.patient.parent.exercises.MinimalPairsRecognitionCorrection;
import it.uniba.dib.sms2324FF2.patient.parent.exercises.WordsSequencesRepetitionCorrection;
import it.uniba.dib.sms2324FF2.patient.parent.homepage.ParentHomePageFragment;
import it.uniba.dib.sms2324FF2.patient.parent.settings.SettingsFragment;
import it.uniba.dib.sms2324FF2.network.NetworkError;
import it.uniba.dib.sms2324FF2.network.NetworkUtils;
import it.uniba.dib.sms2324FF2.utility.SharedViewModel;


public class ParentActivity extends AppCompatActivity implements
        ParentHomePageFragment.onHomePageListener,
        ExercisesFragment.onExercisesListener,
        ImageDenominationCorrection.OnExerciseListener,
        WordsSequencesRepetitionCorrection.OnExerciseListener,
        MinimalPairsRecognitionCorrection.OnExerciseListener,
        SettingsFragment.onSettingsListener{
    private TreeMap<ArrayList<Object>, ArrayList<Exercise>> exercises;
    private ArrayList<TherapistAppointment> therapistAppointments;
    private BottomNavigationView bottomBar;
    MenuItem lastUsedItem; //ultimo item usato che va disattivato
    boolean isFirstTimeUsed = true; //booleana che mi serve per capire se è la prima volta che viene usata la bottom bar
    private NetworkUtils networkUtils;
    private IntentFilter intentFilter;
    private boolean firstAccess = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        exercises = SharedViewModel.getInstance().getExercises();
        therapistAppointments = SharedViewModel.getInstance().getAppointments();

        // Imposta la modalità a schermo intero
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_parent);

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

        // Aggiungi il listener per i cambiamenti di configurazione
        registerReceiver(networkUtils, intentFilter);
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Configuration newConfig = getResources().getConfiguration();
                // Esegui azioni necessarie quando cambia la configurazione
                for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                    if (fragment.isAdded()) {
                        fragment.onConfigurationChanged(newConfig);
                    }
                }
            }
        });

    }

    //in caso di assenza di connessione
    void isConnectedIsFalse(){
        // Avvia l'activity di errore di connessione
        Intent intent = new Intent(ParentActivity.this, NetworkError.class);
        intent.putExtra("firstAccess", firstAccess);
        startActivity(intent);
        finish();
    }
    void isConnectedIsTrue(){

            //gestisco la bottom bar legata all'activity
            bottomBar = findViewById(R.id.bottomNavigationView);
            bottomBar.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {

                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    //gestisco gli eventi nella bottom bar per la navigazione
                    if (item.getItemId() == R.id.home && !isFirstTimeUsed && lastUsedItem != item) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ParentHomePageFragment(therapistAppointments)).commit();
                        isFirstTimeUsed = false;
                        lastUsedItem = item;
                    } else if (item.getItemId() == R.id.exercises && lastUsedItem != item) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ExercisesFragment(exercises)).commit();
                        isFirstTimeUsed = false;
                        lastUsedItem = item;
                    } else if (item.getItemId() == R.id.settings && lastUsedItem != item) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new SettingsFragment()).commit();
                        isFirstTimeUsed = false;
                        lastUsedItem = item;
                    }
                    return true;
                }
            });

            //imposto la home come schermata iniziale nella bottom bar
            bottomBar.setSelectedItemId(R.id.home);
            //imposto la tinta a null per mostrare i colori delle icone
            bottomBar.setItemIconTintList(null);

            //aggiungo il fragment dell'home page del bambino/adulto
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.frameLayout, new ParentHomePageFragment(therapistAppointments));
            ft.commit();
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


    public void refreshHomePageData() {
        TherapistFirebaseAppointmentsModel.getPatientAppointments(new TherapistFirebaseAppointmentsModel.OnReadAppointmentsListener() {
            @Override
            public void onAppointmentsRead(ArrayList<TherapistAppointment> therapistAppointments) {
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ParentHomePageFragment(therapistAppointments)).commit();
            }
            @Override
            public void onAppointmentsReadFailed() {}
        });
    }

    public void refreshExercisesData() {
        FirebaseExerciseModel.getPatientExercises(Patient.getInstance().getId(),new FirebaseExerciseModel.OnAllExercisesCreatedListener() {
            @Override
            public void onExercisesCreated(TreeMap<ArrayList<Object>, ArrayList<Exercise>> exercises) {
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ExercisesFragment(exercises)).commit();
            }
            @Override
            public void onExercisesCreationFailed() {}
        });
    }

    public void refreshSettingsData() {
        PatientFirebaseModel.refreshPatient(new PatientFirebaseModel.OnRefreshListener() {
            @Override
            public void onRefreshSuccess() {
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new SettingsFragment()).commit();
            }

            @Override
            public void onRefreshFailure() {}
        });
    }
    public void hideBottomBar(){
        bottomBar.setVisibility(View.GONE);
    }

    public void showBottomBar(){
        bottomBar.setVisibility(View.VISIBLE);
    }

    public void finishActivity(){
        finish();
    }

    public void setToolbar(Context context, Toolbar toolbar){
        if(toolbar!=null) {
            setSupportActionBar(toolbar);
            TextView customTitle = new TextView(this);
            customTitle.setText(toolbar.getTitle());
            customTitle.setTextColor(getColor(R.color.white));
            customTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_dimen));  // Imposta la dimensione del testo in sp
            Typeface bubblegumSansTypeface = ResourcesCompat.getFont(context, R.font.bubblegum_sans); //imposta il font
            customTitle.setTypeface(bubblegumSansTypeface, Typeface.BOLD);
            toolbar.setTitle(null);
            toolbar.addView(customTitle, new Toolbar.LayoutParams(Gravity.START));

            addMenuProvider(new MenuProvider() {

                @Override
                public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                    menu.clear();
                    menuInflater.inflate(R.menu.menu_toolbar_parent, menu);
                }

                @Override
                public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                    if (menuItem.getItemId() == R.id.profileItem) {
                        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("profile", "").apply();
                        //torno alla schermata del profilo
                        Intent intent = new Intent(context, PatientActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                    } else if (menuItem.getItemId() == R.id.logoutItem) {
                        AlertDialog dialog;
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);

                        LayoutInflater inflater = getLayoutInflater();
                        View customView = inflater.inflate(R.layout.logout_dialog, null);

                        builder.setView(customView);
                        dialog = builder.create();
                        // Blocca l'orientamento in portrait quando la finestra di dialogo è aperta
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.dialog_rounded_corner_gray));

                        Button positiveButton = customView.findViewById(R.id.custom_positive_button);
                        Button negativeButton = customView.findViewById(R.id.custom_negative_button);

                        positiveButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Azioni per il pulsante "Sì"
                                SharedPreferences sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean("isLoggedIn", false);
                                editor.apply();

                                //Effettuo il logout
                                FirebaseAuthenticationModel.logout(Patient.getInstance());
                                //Reindirizzamento
                                Intent intent = new Intent(ParentActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                                // Ripristina l'orientamento predefinito
                                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                            }
                        });

                        negativeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Azioni per il pulsante "No"
                                // Chiudi la finestra di dialogo
                                dialog.dismiss();
                                // Ripristina l'orientamento predefinito
                                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                            }
                        });

                        dialog.show();
                        return true;
                    }
                    return false;
                }
            });
        }
    }

}