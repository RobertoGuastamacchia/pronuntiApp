package it.uniba.dib.sms232421.therapist.patients.exercises;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Time;
import java.util.ArrayList;

import it.uniba.dib.sms232421.R;
import it.uniba.dib.sms232421.exercises.Exercise;
import it.uniba.dib.sms232421.firestore.FirestoreModel;
import it.uniba.dib.sms232421.patient.child.homepage.HomePageChildFragment;
import it.uniba.dib.sms232421.therapist.homepage.HomePageTherapistFragment;
import it.uniba.dib.sms232421.therapist.patients.PatientsManagementFragment;

public class ImageDenominationFragment extends Fragment {
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private static final String IMAGE_PATH = "Images/";
    private static final String IMAGE_EXT = ".jpeg";
    private ImageButton helpButton;
    private AlertDialog dialog;
    private ImageButton uploadImage;
    private MaterialButton finishButton;
    private ImageButton exit;
    private Context context;
    private boolean isPulsating = false;
    private boolean isPortrait = true;
    private Uri imageUri;
    private ImageView imageChosen;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String patientId;
    int coin;
    Timestamp date;


    private onImageDenominationListener mCallback;

    public interface onImageDenominationListener {
        void showBottomBar();
        void hideBottomBar();
        void refreshPatientsData();
    }

    public ImageDenominationFragment(boolean isPortrait, String patientId, int coin, Timestamp date){
        this.isPortrait = isPortrait;
        this.patientId = patientId;
        this.date = date;
        this.coin = coin;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof onImageDenominationListener) {
            mCallback = (onImageDenominationListener) context;
        } else {
            throw new RuntimeException(context + " must implement onImageDenominationListener");
        }
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        if(isPortrait){
            view = inflater.inflate(R.layout.fragment_create_image_denomination, container, false);
        }else{
            view = inflater.inflate(R.layout.landscape_layout, container, false);
        }

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        editor = sharedPreferences.edit();

        if(isPortrait) {

            //recupero dei widget
            finishButton = view.findViewById(R.id.continueButton);
            helpButton = view.findViewById(R.id.help);
            exit = view.findViewById(R.id.exit);
            uploadImage = view.findViewById(R.id.image_upload);
            imageChosen = view.findViewById(R.id.imageChosen);


            //gestione tasto prosegui
            finishButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //carico la risposta TODO
                    //chiamo la pagina post-esercizio di caricamento

                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ImageDenominationHelpsFragment(true, patientId, imageUri, coin, date)).commit();
                }
            });

            //gestisco l'uscita dalla schermata degli esercizi
            exit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //mostra finestra di dialogo per l'uscita
                    showExitDialog();
                }
            });

            //dialog di aiuto su come l'esercizio si struttura
            helpButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showHelpDialog();
                }
            });


            //caricamento immagine click
            uploadImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showCustomChooserDialog();
                }
            });


        }
    }


    //metodo per mostrare una finestra di dialogo quando si clicca sull'uscita dall'esercizio
    private void showExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View customView = inflater.inflate(R.layout.exit_exercise_dialog, null);

        builder.setView(customView);
        dialog = builder.create();
        // Blocca l'orientamento in portrait quando la finestra di dialogo è aperta
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.dialog_rounded_corner_gray));

        Button positiveButton = customView.findViewById(R.id.custom_positive_button);
        Button negativeButton = customView.findViewById(R.id.custom_negative_button);

        //caso in cui si confermi l'uscita dall'esercizio
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                // Ripristina l'orientamento predefinito
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                mCallback.refreshPatientsData();
            }
        });

        //caso in cui si annulli l'uscita dall'esercizio
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Azioni per il pulsante "No"
                // Chiudi la finestra di dialogo
                // Ripristina l'orientamento predefinito
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    //metodo per mostrare una finestra di dialogo quando si clicca sull'aiuto audio
    private void showHelpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View customView = inflater.inflate(R.layout.parent_exercise_explanation_dialog, null);

        builder.setView(customView);
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.dialog_rounded_corner_blue));
        // Blocca l'orientamento in portrait quando la finestra di dialogo è aperta
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Button closeButton = customView.findViewById(R.id.continueButton);

        TextView message = customView.findViewById(R.id.description);
        message.setText(getResources().getString(R.string.descriptionImage));


        //button di chisura finestra di dialogo
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                // Ripristina l'orientamento predefinito
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        });

        dialog.show();

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
                Intent data = result.getData();
                //Salvo l'uri dell'immagine
                imageUri = data.getData();
                if(imageUri!=null) {
                    Glide.with(context)
                            .load(imageUri)
                            .into(imageChosen);
                    imageChosen.setVisibility(View.VISIBLE);
                    finishButton.setVisibility(View.VISIBLE);
                }
            }
        }
    });

    private final ActivityResultLauncher<Intent> openCameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {

        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Bundle extras = result.getData().getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                // Fai qualcosa con l'immagine
                if (imageBitmap!=null) {
                    // Crea un file temporaneo
                    File imageFile = null;
                    try {
                        imageFile = File.createTempFile("image", "jpeg");
                        try (FileOutputStream out = new FileOutputStream(imageFile)) {
                            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                            imageUri = Uri.fromFile(imageFile);
                            if(imageUri!=null) {
                                Glide.with(context)
                                        .load(imageUri)
                                        .into(imageChosen);
                                imageChosen.setVisibility(View.VISIBLE);
                                finishButton.setVisibility(View.VISIBLE);
                            }

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
        // Blocca l'orientamento in portrait quando la finestra di dialogo è aperta
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.dialog_rounded_corner_gray));

        // Imposta l'aspetto grafico dei pulsanti (opzionale)
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Opzione Galleria
                manageGalleryPermission();
                alertDialog.dismiss();
                // Ripristina l'orientamento predefinito
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Opzione Fotocamera (da implementare)
                manageCameraPermission();
                alertDialog.dismiss();
                // Ripristina l'orientamento predefinito
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
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
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ImageDenominationFragment(false, patientId, coin, date)).commit();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ImageDenominationFragment(true, patientId, coin, date)).commit();
        }

    }

}