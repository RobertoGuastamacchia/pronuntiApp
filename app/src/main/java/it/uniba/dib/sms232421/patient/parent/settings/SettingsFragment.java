package it.uniba.dib.sms232421.patient.parent.settings;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import it.uniba.dib.sms232421.R;
import it.uniba.dib.sms232421.firestore.FirestoreModel;
import it.uniba.dib.sms232421.user.FirebaseUserModel;
import it.uniba.dib.sms232421.user.User;
import it.uniba.dib.sms232421.patient.FirebasePatientModel;
import it.uniba.dib.sms232421.patient.Patient;


public class SettingsFragment extends Fragment {

    public static final String WALLPAPERS_BACKGROUND_JPEG = "Wallpapers/background.jpeg";
    Context context;

    private static final int REQUEST_IMAGE_PERMISSION = 8;
    private static final int REQUEST_CAMERA_PERMISSION = 24;

    private String newTheme;
    private boolean themeCreated = false;
    private String oldTheme;
    private String email;
    private EditText childName;
    private EditText parentName;
    private ImageButton editChildName;
    private ImageButton editParentName;
    private MaterialButton saveProfile;
    private EditText emailEditText;
    private MaterialButton createTheme;
    private MaterialButton saveTheme;
    private MaterialButton resetPassword;
    private RadioGroup themeGroup;
    RadioButton jungle, sea, space;
    private String parentNameDB;
    private String childNameDB;
    SwipeRefreshLayout swipeRefreshLayout;
    private onSettingsListener mCallback;

    private AlertDialog alertDialog;
    private boolean isPortrait = true;

    //empty constructor
    public SettingsFragment(){
    }
    public SettingsFragment(boolean isPortrait){
        this.isPortrait = isPortrait;
    }
    public interface onSettingsListener {
        void refreshSettingsData();
        void hideBottomBar();
        void showBottomBar();
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof onSettingsListener) {
            mCallback = (onSettingsListener) context;
        } else {
            throw new RuntimeException(context + " must implement onHomePageListener");
        }
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view;
        if(isPortrait){
            view = inflater.inflate(R.layout.fragment_settings, container, false);
            mCallback.showBottomBar();
        }else{
            view = inflater.inflate(R.layout.landscape_layout, container, false);
            mCallback.hideBottomBar();
        }

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(isPortrait) {
            mCallback.showBottomBar();
            //Gestisco il refresh
            swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    mCallback.refreshSettingsData();
                }
            });

            //sezione credenziali
            emailEditText = view.findViewById(R.id.emailEditText);
            resetPassword = view.findViewById(R.id.resetPassword);

            //sezione profilo
            childName = view.findViewById(R.id.childName);
            parentName = view.findViewById(R.id.parentName);
            editChildName = view.findViewById(R.id.childNameEditSymbol);
            editParentName = view.findViewById(R.id.parentNameEditSymbol);
            saveProfile = view.findViewById(R.id.saveProfile);


            createTheme = view.findViewById(R.id.createTheme);
            saveTheme = view.findViewById(R.id.save);

            themeGroup = view.findViewById(R.id.themeGroup);
            jungle = view.findViewById(R.id.jungle);
            sea = view.findViewById(R.id.sea);
            space = view.findViewById(R.id.space);


            //prendo i dati attualmente impostati
            setInitialData();

            //chiamo i metodi per la gestione delle 3 sezioni delle impostazioni
            manageCredentialsSection();
            manageProfileSection();
            manageThemeSection(view);
        }

    }

    private void setInitialData() {

        email = User.getInstance().getEmail();
        parentNameDB = Patient.getInstance().getParentName();
        childNameDB = Patient.getInstance().getChildName();

        oldTheme = Patient.getInstance().getTheme();
        if (oldTheme.contains("_personalized"))
            oldTheme = oldTheme.replace("_personalized", "");

        switch (oldTheme){
            case "jungle":
                jungle.setTextColor(getActivity().getColor(R.color.colorAccent));
                break;
            case "sea":
                sea.setTextColor(getActivity().getColor(R.color.colorAccent));
                break;
            case "space":
                space.setTextColor(getActivity().getColor(R.color.colorAccent));
                break;
        }

        //dopo aver recuperato i dati, setto i valori iniziali nei campi
        emailEditText.setText(email);
        childName.setText(childNameDB);
        parentName.setText(parentNameDB);

        newTheme = Patient.getInstance().getTheme();

    }


    void manageProfileSection(){

        //click sull'edit del nome del bambino
        editChildName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                childName.setEnabled(true);
            }
        });

        //listener utile per vedere se ci sono state delle modifiche al dato
        //ed, eventualmente, mostrare il button di salvataggio
        childName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    if(!childNameDB.equals(childName.getText().toString())) {
                        childName.setEnabled(false);
                        saveProfile.setVisibility(View.VISIBLE);
                    }else{
                        saveProfile.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });

        childName.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Disabilita l'EditText dopo aver premuto Invio sulla tastiera
                    childName.setEnabled(false);

                    // Puoi anche nascondere la tastiera in modo che l'utente non possa più inserire testo
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(childName.getWindowToken(), 0);

                    return true; // Consuma l'evento
                }
                return false; // Non consuma l'evento
            }
        });

        //click sull'edit del nome del bambino
        editParentName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentName.setEnabled(true);
            }
        });

        //listener utile per vedere se ci sono state delle modifiche al dato
        //ed, eventualmente, mostrare il button di salvataggio
        parentName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    if(!parentNameDB.equals(parentName.getText().toString())) {
                        parentName.setEnabled(false);
                        saveProfile.setVisibility(View.VISIBLE);
                    }else{
                        saveProfile.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });

        parentName.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Disabilita l'EditText dopo aver premuto Invio sulla tastiera
                    parentName.setEnabled(false);

                    // Puoi anche nascondere la tastiera in modo che l'utente non possa più inserire testo
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(parentName.getWindowToken(), 0);

                    return true; // Consuma l'evento
                }
                return false; // Non consuma l'evento
            }
        });

        //gestione salvataggio nuovi dati
        saveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //è cambiato il nome del bambino, cambiare
                if(!childNameDB.equals(childName.getText().toString())){
                    FirebasePatientModel.changeChildUsername(childName.getText().toString(), new FirebasePatientModel.OnPatientUpdateListener() {
                        @Override
                        public void onPatientUpdate(boolean result) {
                            if(result) {
                                saveProfile.setVisibility(View.INVISIBLE);
                                childNameDB = childName.getText().toString();
                                Toast.makeText(context, getString(R.string.nameSaved), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(context, getString(R.string.impossibile_effettuare_la_modifica), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }else{
                    //se è cambiato il nome del genitore, cambiare
                    if(!parentNameDB.equals(parentName.getText().toString())){
                        FirebasePatientModel.changeParentUsername(parentName.getText().toString(), new FirebasePatientModel.OnPatientUpdateListener() {
                            @Override
                            public void onPatientUpdate(boolean result) {
                                if(result) {
                                    saveProfile.setVisibility(View.INVISIBLE);
                                    parentNameDB = parentName.getText().toString();
                                    Toast.makeText(context, getString(R.string.nameSaved), Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(context, getString(R.string.impossibile_effettuare_la_modifica), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }
            }
        });
    }


    void manageCredentialsSection(){
        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseUserModel.sendEmailforChangePassword(email, new FirebaseUserModel.OnSendEmailListener() {
                    @Override
                    public void onEmailSent() {
                        Toast.makeText(context, getString(R.string.email_inviata), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure() {
                        Toast.makeText(context, getString(R.string.errore_durante_l_invio), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    void manageThemeSection(View view){
        //listener per i 3 radio button dei temi
        themeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // Ottieni il RadioButton selezionato
                RadioButton radioButton = view.findViewById(checkedId);

                if (radioButton != null) {
                    // Ottieni il nuovo tema selezionato
                    newTheme = radioButton.getText().toString().toLowerCase();
                    saveTheme.setVisibility(View.VISIBLE);

                }
            }
        });


        //listener per la creazione del tema
        createTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCustomChooserDialog();
            }
        });


        //listener per il salvataggio del tema
        saveTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(themeCreated && !newTheme.contains("_personalized")) {
                    switch (newTheme){
                        case "giungla":
                            newTheme = "jungle_personalized"; //Tema da gestire con lo sfondo personalizzato
                            break;
                        case "spazio":
                            newTheme = "space_personalized"; //Tema da gestire con lo sfondo personalizzato
                            break;
                        case "mare":
                            newTheme = "sea_personalized"; //Tema da gestire con lo sfondo personalizzato
                            break;
                        default : //se nessuna delle precedenti, è in lingua inglese, posso direttamente salvare
                                newTheme += "_personalized";
                    }

                }else{

                    switch (newTheme){
                        case "giungla":
                            newTheme = "jungle";
                            break;
                        case "spazio":
                            newTheme = "space";
                            break;
                        case "mare":
                            newTheme = "sea";
                            break;
                    }

                }
                FirebasePatientModel.updateTheme(newTheme, new FirebasePatientModel.OnPatientUpdateListener() {
                    @Override
                    public void onPatientUpdate(boolean result) {
                        if(result)
                            Toast.makeText(context, getString(R.string.tema_cambiato), Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(context, getString(R.string.tema_non_cambiato), Toast.LENGTH_SHORT).show();
                        mCallback.refreshSettingsData();
                    }
                });
            }
        });
    }

    //gestione permesso apertura galleria foto
    private void manageGalleryPermission() {
        //verifico se il permesso non ancora concesso
        if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
            //richiesta permesso
            requestPermissionLauncherGallery.launch(Manifest.permission.READ_MEDIA_IMAGES);
        } else {
            //permesso già concesso, apro la galleria
            openGallery();
        }
    }

    //gestione permesso apertura fotocamere
    private void manageCameraPermission() {
        //verifico se il permesso non ancora concesso
        if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            //richiesta permesso
            requestPermissionLauncherCamera.launch(Manifest.permission.CAMERA);
        } else {
            //permesso già concesso, apro la fotocamera
            openCamera();
        }
    }

    private final ActivityResultLauncher<String> requestPermissionLauncherCamera = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if (result) {
                        openCamera();
                    }
                }
            }
    );
    private final ActivityResultLauncher<String> requestPermissionLauncherGallery = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if (result) {
                        openGallery();
                    }
                }
            }
    );


    // Gestisci il risultato del selettore di file
    private final ActivityResultLauncher<Intent> openGalleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                // Ottenere l'URI dell'immagine selezionata
                Intent data = result.getData();
                Uri selectedImageUri = data.getData();
                //Carico l'immagine sul Firestore
                FirestoreModel.uploadImage(WALLPAPERS_BACKGROUND_JPEG,selectedImageUri, new FirestoreModel.UploadCallback() {
                    @Override
                    public void onUploadSuccess() {
                        //Variabile che indica se l'utente ha caricato o no una foto per usarla come sfondo
                        themeCreated = true;
                        saveTheme.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onUploadFailure() {
                        Toast.makeText(context, getString(R.string.foto_non_caricata_riprova), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }
    });

    private final ActivityResultLauncher<Intent> openCameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {

        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                // L'immagine è stata catturata con successo, puoi gestire l'immagine qui
                Bundle extras = result.getData().getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                // Fai qualcosa con l'immagine
                if (imageBitmap!=null){
                    // Crea un file temporaneo
                    File imageFile = null;
                    try {
                        imageFile = File.createTempFile("image", "jpeg");
                        try (FileOutputStream out = new FileOutputStream(imageFile)) {
                            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                            Uri imageUri = Uri.fromFile(imageFile);
                            FirestoreModel.uploadImage(WALLPAPERS_BACKGROUND_JPEG,imageUri, new FirestoreModel.UploadCallback() {
                                @Override
                                public void onUploadSuccess() {
                                    //Variabile che indica se l'utente ha caricato o no una foto per usarla come sfondo
                                    themeCreated = true;
                                    saveTheme.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onUploadFailure() {
                                    Toast.makeText(context, getString(R.string.foto_non_caricata_riprova), Toast.LENGTH_SHORT).show();
                                }
                            });

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    });


    //apertura galleria
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        openGalleryLauncher.launch(intent);
    }

    //apertura fotocamera
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        openCameraLauncher.launch(cameraIntent);
    }

    private void showCustomChooserDialog() {
        // Infla il layout personalizzato
        View customView = getLayoutInflater().inflate(R.layout.custom_chooser_dialog, null);

        // Trova i pulsanti nel layout personalizzato
        Button galleryButton = customView.findViewById(R.id.galleryButton);
        Button cameraButton = customView.findViewById(R.id.cameraButton);
        Button cancelButton = customView.findViewById(R.id.cancelButton);

        // Costruisci l'AlertDialog utilizzando il layout personalizzato
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(customView);
        alertDialog = builder.create();
        // Blocca l'orientamento in portrait quando la finestra di dialogo è aperta
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.dialog_rounded_corner_gray));

        // Imposta l'aspetto grafico dei pulsanti (opzionale)
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ripristina l'orientamento predefinito
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                manageGalleryPermission();
                alertDialog.dismiss();
            }
        });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ripristina l'orientamento predefinito
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                manageCameraPermission();
                alertDialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Annulla
                alertDialog.dismiss();
                // Ripristina l'orientamento predefinito
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        });

        // Mostriamo il dialog
        alertDialog.show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new SettingsFragment(false)).commit();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new SettingsFragment(true)).commit();
        }

    }

}