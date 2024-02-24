package it.uniba.dib.sms2324FF2.patient.child;
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

import it.uniba.dib.sms2324FF2.login.FirebaseAuthenticationModel;
import it.uniba.dib.sms2324FF2.login.LoginActivity;
import it.uniba.dib.sms2324FF2.patient.PatientFirebaseModel;
import it.uniba.dib.sms2324FF2.patient.Patient;
import it.uniba.dib.sms2324FF2.patient.PatientActivity;
import it.uniba.dib.sms2324FF2.patient.child.characters.CharactersFragment;
import it.uniba.dib.sms2324FF2.patient.child.exercises.Exercise;
import it.uniba.dib.sms2324FF2.patient.child.exercises.FirebaseExerciseModel;
import it.uniba.dib.sms2324FF2.patient.child.characters.NewCharacterFragment;
import it.uniba.dib.sms2324FF2.patient.child.homepage.ChildHomePageFragment;
import it.uniba.dib.sms2324FF2.patient.child.homepage.exercises.ImageDenominationFragment;
import it.uniba.dib.sms2324FF2.patient.child.homepage.exercises.LoadingPostExerciseFragment;
import it.uniba.dib.sms2324FF2.patient.child.homepage.exercises.MinimalPairsRecognitionFragment;
import it.uniba.dib.sms2324FF2.patient.child.homepage.exercises.WordsSequencesRepetitionFragment;
import it.uniba.dib.sms2324FF2.patient.child.ranking.FirebaseRankingModel;
import it.uniba.dib.sms2324FF2.patient.child.ranking.Ranking;
import it.uniba.dib.sms2324FF2.patient.child.ranking.ChildRankingFragment;
import it.uniba.dib.sms2324FF2.network.NetworkError;
import it.uniba.dib.sms2324FF2.network.NetworkUtils;
import it.uniba.dib.sms2324FF2.R;
import it.uniba.dib.sms2324FF2.utility.PronuntiApp;
import it.uniba.dib.sms2324FF2.utility.SharedViewModel;

public class ChildActivity extends AppCompatActivity implements
        CharactersFragment.onCharactersListener,
        ChildRankingFragment.onRankingListener,
        ChildHomePageFragment.onHomePageListener,
        ImageDenominationFragment.OnExerciseListener,
        WordsSequencesRepetitionFragment.OnExerciseListener,
        MinimalPairsRecognitionFragment.OnExerciseListener,
        LoadingPostExerciseFragment.OnExerciseListener,
        NewCharacterFragment.OnCharactersListener,
        ChildRewardsFragment.onRewardsListener {

    private BottomNavigationView bottomBar;
    private MenuItem lastUsedItem; //ultimo item usato che va disattivato
    private boolean isFirstTimeUsed = true; //booleana che mi serve per capire se è la prima volta che viene usata la bottom bar
    private NetworkUtils networkUtils;
    private IntentFilter intentFilter;
    private TreeMap<ArrayList<Object>, ArrayList<Exercise>> exercises;
    private ArrayList<Ranking> rankingList;

    public ChildActivity(){
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Ottengo il riferimento agli esercizi
        exercises = SharedViewModel.getInstance().getExercises();
        rankingList = SharedViewModel.getInstance().getRankingList();
        // Imposta la modalità a schermo intero
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_child);

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

        // Aggiungi il listener per i cambiamenti di configurazione
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

    @Override
    public void onBackPressed() {
        // Ottenere il Fragment attuale sulla cima dello stack
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frameLayout);

        // Verificare se ci sono Fragment nello stack
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            // Se ci sono Fragment nello stack, torna indietro
            getSupportFragmentManager().popBackStack();
        }
    }


    //in caso di assenza di connessione
    void isConnectedIsFalse(){
        // Avvia l'activity di errore di connessione
        Intent intent = new Intent(ChildActivity.this, NetworkError.class);
        startActivity(intent);
        finish();
    }

    //in caso di presenza di connessione
    void isConnectedIsTrue(){

        //gestisco la bottom bar legata all'activity
        bottomBar = findViewById(R.id.bottomNavigationView);
        //imposto la home come schermata iniziale nella bottom bar
        bottomBar.setSelectedItemId(R.id.home);
        //imposto la tinta a null per mostrare i colori delle icone
        bottomBar.setItemIconTintList(null);

        bottomBar.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                //gestisco gli eventi nella bottom bar per la navigazione
                if(item.getItemId() == R.id.home && !isFirstTimeUsed && lastUsedItem != item) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ChildHomePageFragment(exercises)).commit();
                    isFirstTimeUsed = false;
                    lastUsedItem = item;
                }else if (item.getItemId() == R.id.characters && lastUsedItem != item) {
                    ArrayList<String> charactersUnlocked = Patient.getInstance().getCharacters_unlocked();
                    getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new CharactersFragment(charactersUnlocked)).commit();
                    isFirstTimeUsed = false;
                    lastUsedItem = item;
                }else if(item.getItemId() == R.id.ranking && lastUsedItem != item) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ChildRankingFragment(rankingList)).commit();
                    isFirstTimeUsed = false;
                    lastUsedItem = item;
                }
                return true;
            }
        });

        if(PronuntiApp.isAppJustStarted()) {
            //Controllo se ci sono nuovi premi da riscattare per il bambino, solo all'apertura dell'app
            FirebaseExerciseModel.getWeeklyRewards(new FirebaseExerciseModel.OnRewardsListener() {
                @Override
                public void onRewardsRead(int rewardCoins) {
                    PronuntiApp.setAppJustStarted(false);
                    //vedo se ci sono dei nuovi premi da mostrare al bambino
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

                    if (rewardCoins != 0) {
                        //aggiungo il fragment del riscatto premi
                        ft.add(R.id.frameLayout, new ChildRewardsFragment(rewardCoins, exercises)).commit();
                        //nascondo la bottom bar
                        hideBottomBar();
                    } else {
                        //aggiungo il fragment dell'home page del bambino/adulto
                        ft.add(R.id.frameLayout, new ChildHomePageFragment(exercises)).commit();
                    }
                }

                @Override
                public void onRewardsReadFailed() {
                }
            });
        } else{
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.frameLayout, new ChildHomePageFragment(exercises)).commit();
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Deregistra il BroadcastReceiver all'interno del metodo onPause
        unregisterReceiver(networkUtils);
    }


    public void hideBottomBar(){
        bottomBar.setVisibility(View.GONE);
    }

    public void showBottomBar(){
        bottomBar.setVisibility(View.VISIBLE);
    }

    public void refreshCharactersData(){
        //Aggiorno il paziente
        PatientFirebaseModel.updatePatientData(new PatientFirebaseModel.OnPatientUpdateListener() {
            @Override
            public void onPatientUpdate(boolean result) {
                PatientFirebaseModel.getPatientCharactersUnlocked(new PatientFirebaseModel.OnCharactersListener<ArrayList<String>>() {
                    @Override
                    public void onCharactersRead(ArrayList<String> newCharactersUnlocked) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new CharactersFragment(newCharactersUnlocked)).commit();
                    }
                    @Override
                    public void onCharactersReadFailure(ArrayList<String> newCharactersUnlocked) {}
                });
            }
        });

        }

    public void refreshHomePageData() {
        FirebaseExerciseModel.getPatientExercises(Patient.getInstance().getId(),new FirebaseExerciseModel.OnAllExercisesCreatedListener() {
            @Override
            public void onExercisesCreated(TreeMap<ArrayList<Object>, ArrayList<Exercise>> newExercises) {
                exercises = newExercises;
                PatientFirebaseModel.updatePatientData(new PatientFirebaseModel.OnPatientUpdateListener() {
                    @Override
                    public void onPatientUpdate(boolean result) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ChildHomePageFragment(newExercises)).commit();
                    }
                });
            }
            @Override
            public void onExercisesCreationFailed() {}
        });
    }
    public void refreshRankingData(){
        FirebaseRankingModel.getRankingList(new FirebaseRankingModel.RankingCallback() {
            @Override
            public void onSuccess(ArrayList<Ranking> rankingList) {
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ChildRankingFragment(rankingList)).commit();
            }
            @Override
            public void onFailure() {}
        });
    }


    public void goBack(){
        onBackPressed();
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
                    menuInflater.inflate(R.menu.menu_toolbar_child, menu);
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
                                Intent intent = new Intent(ChildActivity.this, LoginActivity.class);
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