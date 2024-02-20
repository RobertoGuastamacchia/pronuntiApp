package it.uniba.dib.sms2324FF2.therapist;

import android.annotation.SuppressLint;
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
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Objects;

import it.uniba.dib.sms2324FF2.R;
import it.uniba.dib.sms2324FF2.login.FirebaseAuthenticationModel;
import it.uniba.dib.sms2324FF2.login.LoginActivity;
import it.uniba.dib.sms2324FF2.network.NetworkError;
import it.uniba.dib.sms2324FF2.network.NetworkUtils;
import it.uniba.dib.sms2324FF2.therapist.appointments.TherapistAppointment;
import it.uniba.dib.sms2324FF2.therapist.appointments.TherapistFirebaseAppointmentsModel;
import it.uniba.dib.sms2324FF2.therapist.homepage.TherapistHomePageFragment;
import it.uniba.dib.sms2324FF2.therapist.management_patients.TherapistPatientsManagementFragment;
import it.uniba.dib.sms2324FF2.therapist.management_patients.exercises.AddAudioToExerciseFragment;
import it.uniba.dib.sms2324FF2.therapist.management_patients.exercises.ImageDenominationFragment;
import it.uniba.dib.sms2324FF2.therapist.management_patients.exercises.ImageDenominationHelpsFragment;
import it.uniba.dib.sms2324FF2.therapist.management_patients.exercises.MinimalPairsRecognitionFragment;
import it.uniba.dib.sms2324FF2.therapist.settings.TherapistSettings;
import it.uniba.dib.sms2324FF2.user.FirebaseUserModel;
import it.uniba.dib.sms2324FF2.user.User;
import it.uniba.dib.sms2324FF2.utility.SharedViewModel;


public class TherapistActivity extends AppCompatActivity implements
        TherapistHomePageFragment.onHomePageListener,
        TherapistPatientsManagementFragment.onPatientsListener,
        TherapistSettings.onSettingsListener,
        ImageDenominationFragment.onImageDenominationListener,
        ImageDenominationHelpsFragment.onImageDenominationListener,
        MinimalPairsRecognitionFragment.onMinimalPairsRecognition,
        AddAudioToExerciseFragment.onMinimalPairsRecognitionAudioRecording {
    private AlertDialog dialogEmail;
    private MenuItem lastUsedItem; //ultimo item usato che va disattivato
    private boolean isFirstTimeUsed = true; //booleana che mi serve per capire se è la prima volta che viene usata la bottom bar
    private ActivityResultLauncher<Intent> emailLauncher;
    private ArrayList<TherapistAppointment> therapistAppointments;
    private ArrayList<String> patients, patientsId;
    private NetworkUtils networkUtils;
    private BottomNavigationView bottomBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Imposta la modalità a schermo intero
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_therapist);

        //verifico se il device è connesso ad internet utilizzando un listener
        //per catturare cambi di stato di connessione
        networkUtils = new NetworkUtils();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

        //verifico a priori se c'è connessione o meno
        if (networkUtils.isConnected(this)) {
            isConnectedIsTrue();
        } else {
            isConnectedIsFalse();
        }

        //verifico se il device è connesso ad internet utilizzando un listener
        //per catturare cambi di stato di connessione
        networkUtils.setNetworkChangeListener(isConnected -> {
            //verifico di che tipo di cambio di rete si tratta
            if (isConnected) {
                isConnectedIsTrue();
            } else {
                isConnectedIsFalse();
            }
        });

        // Registra il BroadcastReceiver
        registerReceiver(networkUtils, intentFilter);

        // Aggiungi il listener per i cambiamenti di configurazione
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Configuration newConfig = getResources().getConfiguration();
            // Esegui azioni necessarie quando cambia la configurazione
            // Puoi passare l'evento ai tuoi fragment se necessario
            for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                if (fragment.isAdded()) {
                    fragment.onConfigurationChanged(newConfig);
                }
            }
        });

        emailLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            // L'invio dell'email è stato completato con successo
            // Puoi gestire le azioni post-invio qui
            if (dialogEmail != null)
                dialogEmail.dismiss();


        }
        );
    }

    //in caso di assenza di connessione
    void isConnectedIsFalse() {
        // Avvia l'activity di errore di connessione
        Intent intent = new Intent(TherapistActivity.this, NetworkError.class);
        startActivity(intent);
        finish();
    }

    //caso di presenza di connessione
    private void isConnectedIsTrue() {

        therapistAppointments = SharedViewModel.getInstance().getAppointments();
        patients = SharedViewModel.getInstance().getPatients();
        patientsId = SharedViewModel.getInstance().getPatientsId();

        bottomBar = findViewById(R.id.bottomNavigationView);
        //imposto la home come schermata iniziale nella bottom bar
        bottomBar.setSelectedItemId(R.id.home);
        //imposto la tinta a null per mostrare i colori delle icone
        bottomBar.setItemIconTintList(null);

        //gestione della bottom bar
        bottomBar.setOnItemSelectedListener(item -> {

            //gestisco la navigazione con la bottom bar
            if (item.getItemId() == R.id.home && !isFirstTimeUsed && lastUsedItem != item) {
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new TherapistHomePageFragment(true, therapistAppointments, patients, patientsId)).commit();
                isFirstTimeUsed = false;
                lastUsedItem = item;
            } else if (item.getItemId() == R.id.patients && !isFirstTimeUsed && lastUsedItem != item) {
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new TherapistPatientsManagementFragment(true, patients, patientsId)).commit();
                isFirstTimeUsed = false;
                lastUsedItem = item;
            } else if (item.getItemId() == R.id.settings && !isFirstTimeUsed && lastUsedItem != item) {
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new TherapistSettings(true)).commit();
                isFirstTimeUsed = false;
                lastUsedItem = item;
            }
            return true;
        });

        //aggiungo il fragment dell'home page del logopedista
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.frameLayout, new TherapistHomePageFragment(true, therapistAppointments, patients, patientsId));
        ft.commit();
        isFirstTimeUsed=false;
    }

    @Override
    public void onBackPressed() {
        //disattivazione tasto back
        super.onBackPressed();
    }

    @Override
    public void refreshHomePageData() {
        TherapistFirebaseAppointmentsModel.getDoctorAppointments(new TherapistFirebaseAppointmentsModel.OnReadAppointmentsListener() {
            @Override
            public void onAppointmentsRead(ArrayList<TherapistAppointment> therapistAppointments) {
                FirebaseUserModel.getPatientsListforDoctor(new FirebaseUserModel.OnPatientsListListener() {
                    @Override
                    public void onPatientsListRead(ArrayList<String> patients, ArrayList<String> patientsId, ArrayList<String> emailList) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new TherapistHomePageFragment(true, therapistAppointments, patients, patientsId)).commit();

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
    }

    @Override
    public void refreshPatientsData() {
        FirebaseUserModel.getPatientsListforDoctor(new FirebaseUserModel.OnPatientsListListener() {
            @Override
            public void onPatientsListRead(ArrayList<String> patients, ArrayList<String> patientsId, ArrayList<String> emailList) {
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new TherapistPatientsManagementFragment(true, patients, patientsId)).commit();

            }

            @Override
            public void onPatientsListReadFailure() {

            }
        });
    }

    public void activityFinish() {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Deregistra il BroadcastReceiver all'interno del metodo onPause
        unregisterReceiver(networkUtils);
    }


    @Override
    public void refreshSettingsData() {
        FirebaseUserModel.refreshUser(new FirebaseUserModel.OnRefreshListener() {
            @Override
            public void onRefreshSuccess() {
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new TherapistSettings()).commit();
            }

            @Override
            public void onRefreshFailure() {
            }
        });
    }

    public void hideBottomBar() {
        bottomBar.setVisibility(View.GONE);
    }

    public void showBottomBar() {
        bottomBar.setVisibility(View.VISIBLE);
    }

    public void setToolbar(Context context, Toolbar toolbar) {
        setSupportActionBar(toolbar);

        TextView customTitle = new TextView(this);
        customTitle.setText(toolbar.getTitle());
        customTitle.setTextColor(getColor(R.color.white));
        customTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_dimen));  // Imposta la dimensione del testo in sp
        Typeface bubblegumSansTypeface = ResourcesCompat.getFont(getApplicationContext(), R.font.bubblegum_sans); // Imposta il font
        customTitle.setTypeface(bubblegumSansTypeface, Typeface.BOLD);
        toolbar.setTitle(null);
        toolbar.addView(customTitle, new Toolbar.LayoutParams(Gravity.START));
        addMenuProvider(new MenuProvider() {

            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menu.clear();
                menuInflater.inflate(R.menu.menu_toolbar_therapist, menu);
            }

            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.logoutItem) {
                    AlertDialog dialog;
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);

                    LayoutInflater inflater = getLayoutInflater();
                    View customView = inflater.inflate(R.layout.logout_dialog, null);

                    builder.setView(customView);
                    dialog = builder.create();
                    // Blocca l'orientamento in portrait quando la finestra di dialogo è aperta
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    dialog.setCanceledOnTouchOutside(false);
                    Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(getResources().getDrawable(R.drawable.dialog_rounded_corner_gray));

                    Button positiveButton = customView.findViewById(R.id.custom_positive_button);
                    Button negativeButton = customView.findViewById(R.id.custom_negative_button);

                    positiveButton.setOnClickListener(v -> {
                        // Azioni per il pulsante "Sì"
                        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("isLoggedIn", false);
                        editor.apply();

                        //Effettuo il logout
                        FirebaseAuthenticationModel.logout(User.getInstance());
                        //Reindirizzamento
                        Intent intent = new Intent(TherapistActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        // Ripristina l'orientamento predefinito
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    });

                    negativeButton.setOnClickListener(v -> {
                        // Azioni per il pulsante "No"
                        // Chiudi la finestra di dialogo
                        dialog.dismiss();
                        // Ripristina l'orientamento predefinito
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    });

                    dialog.show();
                    return true;
                } else if (menuItem.getItemId() == R.id.sendEmailItem) {
                    // Ottieni l'oggetto LayoutInflater
                    LayoutInflater inflater = LayoutInflater.from(context);

                    // Carica il layout del tuo dialog personalizzato
                    View dialogView = inflater.inflate(R.layout.send_email_dialog, null);

                    // Crea un AlertDialog
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                    alertDialogBuilder.setView(dialogView);


                    // Mostra il tuo AlertDialog
                    dialogEmail = alertDialogBuilder.create();
                    // Blocca l'orientamento in portrait quando la finestra di dialogo è aperta
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    dialogEmail.setCanceledOnTouchOutside(false);
                    Objects.requireNonNull(dialogEmail.getWindow()).setBackgroundDrawable(getResources().getDrawable(R.drawable.dialog_rounded_corner_gray));

                    dialogEmail.show();

                    // Ottieni le referenze agli elementi UI all'interno del layout del dialog
                    MaterialButton send = dialogView.findViewById(R.id.send);
                    MaterialButton cancel = dialogView.findViewById(R.id.cancel);

                    cancel.setOnClickListener(v -> {
                        dialogEmail.dismiss();
                        // Ripristina l'orientamento predefinito
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    });

                    //invio nuova password
                    send.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            EditText emailTxtView = dialogView.findViewById(R.id.email);
                            String email = emailTxtView.getText().toString();

                            //controllo correttezza email
                            if (email.isEmpty()) {
                                Toast.makeText(context, getString(R.string.emptyText), Toast.LENGTH_SHORT).show();
                                return;
                            }

                            sendEmails(email, context);

                            // Ripristina l'orientamento predefinito
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                        }

                    });

                    EditText emailTxtView = dialogView.findViewById(R.id.email);
                    final int maxLength = 3001;

                    // Imposta il limite massimo
                    InputFilter[] filters = new InputFilter[1];
                    filters[0] = new InputFilter.LengthFilter(maxLength);
                    emailTxtView.setFilters(filters);

                    TextView characterCountTextView = dialogView.findViewById(R.id.characterCountTextView);
                    // Monitora le modifiche al testo
                    emailTxtView.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                            // Non necessario implementare
                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                            // Non necessario implementare
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                            int currentLength = editable.length();
                            int maxLength = 3000;

                            if (editable.length() > maxLength) {
                                // Se il testo supera il limite, rimuovi i caratteri in eccesso
                                emailTxtView.setText(editable.subSequence(0, maxLength));
                                emailTxtView.setSelection(maxLength);

                                String countText = maxLength + "/" + maxLength;
                                characterCountTextView.setText(countText);
                            } else {
                                String countText = currentLength + "/" + maxLength;
                                characterCountTextView.setText(countText);
                            }
                        }
                    });
                    return true;
                }
                return false;
            }
        });

    }

    private void sendEmails(String content, Context context) {
        FirebaseUserModel.getPatientsListforDoctor(new FirebaseUserModel.OnPatientsListListener() {
            @Override
            public void onPatientsListRead(ArrayList<String> nameList, ArrayList<String> idList, ArrayList<String> emailList) {


                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_EMAIL, emailList.toArray(new String[0]));
                intent.putExtra(Intent.EXTRA_SUBJECT, "Comunicazione broadcast");
                intent.putExtra(Intent.EXTRA_TEXT, content);
                intent.setType("message/rfc822");
                emailLauncher.launch(Intent.createChooser(intent, "Scegli il client email"));
            }

            @Override
            public void onPatientsListReadFailure() {
                Toast.makeText(context, "Errore nella lettura dei pazienti", Toast.LENGTH_SHORT).show();
            }
        });
    }

}

