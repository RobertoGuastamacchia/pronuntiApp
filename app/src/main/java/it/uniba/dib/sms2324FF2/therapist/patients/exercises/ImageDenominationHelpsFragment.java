package it.uniba.dib.sms2324FF2.therapist.patients.exercises;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

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

import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import it.uniba.dib.sms2324FF2.R;
import it.uniba.dib.sms2324FF2.exercises.FirebaseExerciseModel;
import it.uniba.dib.sms2324FF2.notifications.FCMClient;
import it.uniba.dib.sms2324FF2.notifications.FCMNotification;
import it.uniba.dib.sms2324FF2.notifications.FCMRequest;
import it.uniba.dib.sms2324FF2.patient.FirebasePatientModel;

public class ImageDenominationHelpsFragment extends Fragment {

    private static final int TOT_HELPS = 3;
    private int helpsCount = 1; //tiene traccia del numero di audio di aiuto che ha registrato il logoepdista
    private static final int REQUEST_AUDIO_PERMISSION_CODE = 156; //Numero intero costante, codice richiesta del permesso del microfono
    private static final int REQUEST_AUDIO_FILE = 2002; //numero intero costante, codice richiesta del permesso per carica audio dalla memoria
    private MediaRecorder mediaRecorder1;
    private MediaPlayer mediaPlayerListenAnswer1 = new MediaPlayer();
    private MediaRecorder mediaRecorder2;
    private MediaPlayer mediaPlayerListenAnswer2 = new MediaPlayer();
    private MediaRecorder mediaRecorder3;
    private MediaPlayer mediaPlayerListenAnswer3 = new MediaPlayer();
    private ImageButton refresh;
    private ImageButton helpButton;
    private ImageButton startAudio1;
    private ImageButton uploadAudio1;
    private TextView uploadAudioDescription1;
    private ImageView soundWave1;
    private MaterialButton finishButton;
    private ImageButton stopAudioButton1;
    private ImageButton listenYourAudio1;
    private ImageButton stopYourAudio1;
    private TextView stopRecordingButtonDescription1;
    private TextView startAudioDescription1;
    private TextView audioDoneDescription1;
    private String outputFile1;
    private ImageButton startAudio2;
    private ImageButton uploadAudio2;
    private TextView uploadAudioDescription2;
    private ImageView soundWave2;
    private ImageButton stopAudioButton2;
    private ImageButton listenYourAudio2;
    private ImageButton stopYourAudio2;
    private TextView stopRecordingButtonDescription2;
    private TextView startAudioDescription2;
    private TextView audioDoneDescription2;
    private String outputFile2;
    private ImageButton startAudio3;
    private ImageButton uploadAudio3;
    private TextView uploadAudioDescription3;
    private ImageView soundWave3;
    private ImageButton stopAudioButton3;
    private ImageButton listenYourAudio3;
    private ImageButton stopYourAudio3;
    private TextView stopRecordingButtonDescription3;
    private TextView startAudioDescription3;
    private TextView audioDoneDescription3;
    private String outputFile3;
    private AlertDialog dialog;
    private AlertDialog permissionDialog;
    private ImageButton exit;
    private Context context;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    boolean isPortrait = true;
    String patientId;
    Uri imageUri;
    private View view;
    private onImageDenominationListener mCallback;
    private ProgressBar progressBar;
    int coin;
    Timestamp date;

    public interface onImageDenominationListener {
        void refreshPatientsData();
    }

    public ImageDenominationHelpsFragment(boolean isPortrait, String patientId, Uri imageUri, int coin, Timestamp date){
        this.isPortrait = isPortrait;
        this.patientId = patientId;
        this.imageUri = imageUri;
        this.date = date;
        this.coin = coin;
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
            view = inflater.inflate(R.layout.fragment_image_denomination_helps, container, false);
        }else{
            view = inflater.inflate(R.layout.landscape_layout, container, false);
        }

        // Inflate the layout for this fragment
        return view;

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ImageDenominationFragment.onImageDenominationListener) {
            mCallback = (onImageDenominationListener) context;
        } else {
            throw new RuntimeException(context + " must implement onImageDenominationListener");
        }
        this.context = context;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        editor = sharedPreferences.edit();

        if(isPortrait) {
            exit = view.findViewById(R.id.exit);
            refresh = view.findViewById(R.id.refresh);
            finishButton = view.findViewById(R.id.finish);
            helpButton = view.findViewById(R.id.help);
            progressBar = view.findViewById(R.id.progressBar);

            //recupero dei widget pt1
            stopRecordingButtonDescription1 = view.findViewById(R.id.stopAudioButtonDescription);
            stopAudioButton1 = view.findViewById(R.id.stopAudio);
            audioDoneDescription1 = view.findViewById(R.id.audioDoneDescription);
            soundWave1 = view.findViewById(R.id.sound_wave);
            startAudio1 = view.findViewById(R.id.startAudio);
            startAudioDescription1 = view.findViewById(R.id.startAudioButtonDescription);
            startAudioDescription1.setText(getString(R.string.record_audio));
            stopYourAudio1 = view.findViewById(R.id.stopYourAudio);
            listenYourAudio1 = view.findViewById(R.id.listenYourAudio);
            uploadAudio1 = view.findViewById(R.id.upload_audio);
            uploadAudioDescription1 = view.findViewById(R.id.upload_audio_description);


            //recupero dei widget pt2
            stopRecordingButtonDescription2 = view.findViewById(R.id.stopAudioButtonDescription2);
            stopAudioButton2 = view.findViewById(R.id.stopAudio2);
            audioDoneDescription2 = view.findViewById(R.id.audioDoneDescription2);
            soundWave2 = view.findViewById(R.id.sound_wave2);
            startAudio2 = view.findViewById(R.id.startAudio2);
            startAudioDescription2 = view.findViewById(R.id.startAudioButtonDescription2);
            startAudioDescription2.setText(getString(R.string.record_audio));
            stopYourAudio2 = view.findViewById(R.id.stopYourAudio2);
            listenYourAudio2 = view.findViewById(R.id.listenYourAudio2);
            uploadAudio2 = view.findViewById(R.id.upload_audio2);
            uploadAudioDescription2 = view.findViewById(R.id.upload_audio_description2);


            //recupero dei widget pt3
            stopRecordingButtonDescription3 = view.findViewById(R.id.stopAudioButtonDescription3);
            stopAudioButton3 = view.findViewById(R.id.stopAudio3);
            audioDoneDescription3 = view.findViewById(R.id.audioDoneDescription3);
            soundWave3 = view.findViewById(R.id.sound_wave3);
            startAudio3 = view.findViewById(R.id.startAudio3);
            startAudioDescription3 = view.findViewById(R.id.startAudioButtonDescription3);
            startAudioDescription3.setText(getString(R.string.record_audio));
            stopYourAudio3 = view.findViewById(R.id.stopYourAudio3);
            listenYourAudio3 = view.findViewById(R.id.listenYourAudio3);
            uploadAudio3 = view.findViewById(R.id.upload_audio3);
            uploadAudioDescription3 = view.findViewById(R.id.upload_audio_description3);


            //listener per i button condivisi in tutta la schermata e sempre presenti
            //gestione fine esercizio, completato
            finishButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressBar.setVisibility(View.VISIBLE);
                    //Salvo l'esercizio
                    FirebaseExerciseModel.saveImageDenomination(patientId, imageUri, outputFile1, outputFile2, outputFile3,  coin, date, new FirebaseExerciseModel.OnSaveExerciseListener() {
                        @Override
                        public void onSuccess() {
                            //torno alla schermata dei pazienti
                            sendNotification(patientId);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(context, "Esercizio caricato correttamente", Toast.LENGTH_SHORT).show();
                            mCallback.refreshPatientsData();
                        }

                        @Override
                        public void onFailure(String error) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                            mCallback.refreshPatientsData();
                        }
                    });
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

            //gestione del refresh per ripetere la risposta audio
            refresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //aggiorno la pagina ricaricandola per poter ripetere l'audio
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.frameLayout, new ImageDenominationHelpsFragment(true, patientId, imageUri, coin, date));
                    ft.commit();
                }
            });

            //gestione riascolto del proprio audio
            listenYourAudio1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startPlayback1();
                    listenYourAudio1.setVisibility(View.GONE);
                    stopYourAudio1.setVisibility(View.VISIBLE);
                }
            });

            //gestione stop riascolto del proprio audio
            stopYourAudio1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopPlayback1();
                    listenYourAudio1.setVisibility(View.VISIBLE);
                    stopYourAudio1.setVisibility(View.GONE);
                }
            });


            //gestione riascolto del proprio audio
            listenYourAudio2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startPlayback2();
                    listenYourAudio2.setVisibility(View.GONE);
                    stopYourAudio2.setVisibility(View.VISIBLE);
                }
            });

            //gestione stop riascolto del proprio audio
            stopYourAudio2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopPlayback2();
                    listenYourAudio2.setVisibility(View.VISIBLE);
                    stopYourAudio2.setVisibility(View.GONE);
                }
            });

            //gestione riascolto del proprio audio
            listenYourAudio3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startPlayback3();
                    listenYourAudio3.setVisibility(View.GONE);
                    stopYourAudio3.setVisibility(View.VISIBLE);
                }
            });

            //gestione stop riascolto del proprio audio
            stopYourAudio3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopPlayback3();
                    listenYourAudio3.setVisibility(View.VISIBLE);
                    stopYourAudio3.setVisibility(View.GONE);
                }
            });


            //listener per rilevare la fine della riproduzione audio
            mediaPlayerListenAnswer1.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    // Quando la riproduzione audio è completata, cambia l'immagine del pulsante
                    listenYourAudio1.setVisibility(View.VISIBLE);
                    stopYourAudio1.setVisibility(View.GONE);
                }
            });


            //listener per rilevare la fine della riproduzione audio
            mediaPlayerListenAnswer2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    // Quando la riproduzione audio è completata, cambia l'immagine del pulsante
                    listenYourAudio2.setVisibility(View.VISIBLE);
                    stopYourAudio2.setVisibility(View.GONE);
                }
            });


            //listener per rilevare la fine della riproduzione audio
            mediaPlayerListenAnswer3.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    // Quando la riproduzione audio è completata, cambia l'immagine del pulsante
                    listenYourAudio3.setVisibility(View.VISIBLE);
                    stopYourAudio3.setVisibility(View.GONE);
                }
            });

            //indicazioni per il logopedista
            helpButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showHelpDialog();
                }
            });

            recordHelp(stopRecordingButtonDescription1, stopAudioButton1, audioDoneDescription1, soundWave1,
                    startAudio1, startAudioDescription1, stopYourAudio1,
                    listenYourAudio1, uploadAudio1, uploadAudioDescription1); //primo aiuto
        }
    }



    //gestione dell'n-esimo aiuto
    private void recordHelp(TextView stopRecordingButtonDescription, ImageButton stopAudioButton, TextView audioDoneDescription, ImageView soundWave, ImageButton startAudio, TextView startAudioDescription, ImageButton stopYourAudio, ImageButton listenYourAudio, ImageButton uploadAudio, TextView uploadAudioDescription) {

        switch (helpsCount){
            case 1:
                TextView helpTitle = view.findViewById(R.id.helpTitle);
                helpTitle.setText(getString(R.string.helpWindowDialogTitle) + "1");
                break;

            case 2:
                TextView helpTitle2 = view.findViewById(R.id.helpTitle2);
                helpTitle2.setText(getString(R.string.helpWindowDialogTitle) + "2");
                break;

            case 3:
                TextView helpTitle3 = view.findViewById(R.id.helpTitle3);
                helpTitle3.setText(getString(R.string.helpWindowDialogTitle) + "3");
                break;

        }


        //verifico a priori se il permesso è concesso per capire quale widget caricare
        //se quello del microfono, per la registrazione audio
        //o la folder, per caricare l'audio dalla memoria
        if (sharedPreferences.getBoolean("permissionDenied", false) &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            uploadAudioDescription.setVisibility(View.VISIBLE);
            uploadAudio.setVisibility(View.VISIBLE);
            startAudio.setVisibility(View.GONE);
            startAudioDescription.setVisibility(View.GONE);
        } else {
            editor.putBoolean("permissionDenied", false);
            editor.commit();
            uploadAudioDescription.setVisibility(View.GONE);
            uploadAudio.setVisibility(View.GONE);
            startAudio.setVisibility(View.VISIBLE);
            startAudioDescription.setVisibility(View.VISIBLE);
        }


        //gestione della registrazione della risposta audio del bambino
        startAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //richiesta del permesso per accedere al microfono
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    showPermissionDialog(REQUEST_AUDIO_PERMISSION_CODE);
                } else {
                    //si puo procedere con l'utilizzo del microfono, permesso già concesso
                    editor.putBoolean("permissionDenied", false);
                    editor.commit();

                    uploadAudioDescription.setVisibility(View.VISIBLE);
                    uploadAudio.setVisibility(View.GONE);
                    uploadAudioDescription.setVisibility(View.GONE);
                    startAudio.setVisibility(View.VISIBLE);
                    startAudioDescription.setVisibility(View.VISIBLE);

                    //eseguo azioni, il permesso è concesso
                    // Il permesso è stato concesso, si può procedere con l'utilizzo del microfono
                    editor.putBoolean("permissionDenied", false);
                    editor.commit();

                    switch (helpsCount) {
                        case 1:
                            //cambio icone
                            startAudio1.setVisibility(View.GONE);
                            startAudioDescription1.setVisibility(View.GONE);
                            stopRecordingButtonDescription1.setVisibility(View.VISIBLE);
                            stopAudioButton1.setVisibility(View.VISIBLE);
                            soundWave1.setVisibility(View.VISIBLE);

                            //registro l'audio e lo salvo in un oggetto di tipo MediaRecorder
                            mediaRecorder1 = new MediaRecorder();
                            mediaRecorder1.setAudioSource(MediaRecorder.AudioSource.MIC);
                            mediaRecorder1.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                            mediaRecorder1.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                            // Ottieni il percorso del file temporaneo
                            outputFile1 = requireContext().getExternalCacheDir() + "/audio_temp1.mp3";

                            mediaRecorder1.setOutputFile(outputFile1);

                            try {
                                mediaRecorder1.prepare();
                                mediaRecorder1.start();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            break;

                        case 2:
                            //cambio icone
                            startAudio2.setVisibility(View.GONE);
                            startAudioDescription2.setVisibility(View.GONE);
                            stopRecordingButtonDescription2.setVisibility(View.VISIBLE);
                            stopAudioButton2.setVisibility(View.VISIBLE);
                            soundWave2.setVisibility(View.VISIBLE);

                            //registro l'audio e lo salvo in un oggetto di tipo MediaRecorder
                            mediaRecorder2 = new MediaRecorder();
                            mediaRecorder2.setAudioSource(MediaRecorder.AudioSource.MIC);
                            mediaRecorder2.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                            mediaRecorder2.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                            // Ottieni il percorso del file temporaneo
                            outputFile2 = requireContext().getExternalCacheDir() + "/audio_temp2.mp3";

                            mediaRecorder2.setOutputFile(outputFile2);

                            try {
                                mediaRecorder2.prepare();
                                mediaRecorder2.start();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            break;

                        case 3:
                            //cambio icone
                            startAudio3.setVisibility(View.GONE);
                            startAudioDescription3.setVisibility(View.GONE);
                            stopRecordingButtonDescription3.setVisibility(View.VISIBLE);
                            stopAudioButton3.setVisibility(View.VISIBLE);
                            soundWave3.setVisibility(View.VISIBLE);

                            //registro l'audio e lo salvo in un oggetto di tipo MediaRecorder
                            mediaRecorder3 = new MediaRecorder();
                            mediaRecorder3.setAudioSource(MediaRecorder.AudioSource.MIC);
                            mediaRecorder3.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                            mediaRecorder3.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                            // Ottieni il percorso del file temporaneo
                            outputFile3 = requireContext().getExternalCacheDir() + "/audio_temp3.mp3";

                            mediaRecorder3.setOutputFile(outputFile3);

                            try {
                                mediaRecorder3.prepare();
                                mediaRecorder3.start();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            break;

                    }
                }

            }
        });



        uploadAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Verifica se il permesso è già concesso
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    showPermissionDialog(REQUEST_AUDIO_FILE);
                } else {
                    switch(helpsCount){
                        case 1:
                            openFileSelector1();
                            break;
                        case 2:
                            openFileSelector2();
                            break;
                        case 3:
                            openFileSelector3();
                            break;

                    }
                }
            }
        });

        //gestione del tasto di fine registrazione audio
        stopAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //cambio icone
                stopRecordingButtonDescription.setVisibility(View.GONE);
                stopAudioButton.setVisibility(View.GONE);
                helpButton.setVisibility(View.GONE);
                listenYourAudio.setVisibility(View.VISIBLE);
                audioDoneDescription.setVisibility(View.VISIBLE);
                soundWave.setVisibility(View.GONE);
                //rendo disponibile i bottoni di fine e di refresh per ripetere la risposta
                if(helpsCount == 3)
                    finishButton.setVisibility(View.VISIBLE);

                refresh.setVisibility(View.VISIBLE);

                switch(helpsCount){
                    case 1:
                        //fine audio
                        if (mediaRecorder1 != null) {
                            mediaRecorder1.stop();
                            mediaRecorder1.release();
                            mediaRecorder1 = null;
                        }
                        helpsCount++;
                        recordHelp(stopRecordingButtonDescription2, stopAudioButton2, audioDoneDescription2, soundWave2,
                                startAudio2, startAudioDescription2, stopYourAudio2,
                                listenYourAudio2, uploadAudio2, uploadAudioDescription2); //secondo aiuto
                        LinearLayout help2 = view.findViewById(R.id.help2);
                        help2.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        //fine audio
                        if (mediaRecorder2 != null) {
                            mediaRecorder2.stop();
                            mediaRecorder2.release();
                            mediaRecorder2 = null;
                        }
                        helpsCount++;
                        recordHelp(stopRecordingButtonDescription3, stopAudioButton3, audioDoneDescription3, soundWave3,
                                startAudio3, startAudioDescription3, stopYourAudio3,
                                listenYourAudio3, uploadAudio3, uploadAudioDescription3); //terzo aiuto
                        LinearLayout help3 = view.findViewById(R.id.help3);
                        help3.setVisibility(View.VISIBLE);

                        break;
                    case 3:
                        //fine audio
                        if (mediaRecorder3 != null) {
                            mediaRecorder3.stop();
                            mediaRecorder3.release();
                            mediaRecorder3 = null;
                        }
                        //mostro button finale per salvare e terminare
                        finishButton.setVisibility(View.VISIBLE);
                        break;
                }

            }
        });


    }


    //metodo per mostrare una finestra di dialogo quando si clicca sull'uscita dall'esercizio
    private void showExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View customView = inflater.inflate(R.layout.exit_exercise_dialog, null);

        builder.setView(customView);
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        // Blocca l'orientamento in portrait quando la finestra di dialogo è aperta
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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
        // Blocca l'orientamento in portrait quando la finestra di dialogo è aperta
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.dialog_rounded_corner_blue));

        Button closeButton = customView.findViewById(R.id.continueButton);

        TextView message = customView.findViewById(R.id.description);
        message.setText(getResources().getString(R.string.helpsForTherapist));


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




    // Gestisci il risultato del selettore di file del primo audio di aiuto
    private final ActivityResultLauncher<Intent> fileSelectorLauncher1 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode() == Activity.RESULT_OK) {
                Intent intent = result.getData();
                if (intent != null) {
                    Uri uri = intent.getData();
                    try {
                        // Ottieni un oggetto DocumentFile dall'Uri
                        DocumentFile documentFile = DocumentFile.fromSingleUri(context, uri);

                        // Ottieni un flusso di input dal DocumentFile
                        InputStream inputStream = context.getContentResolver().openInputStream(uri);

                        // Crea un file temporaneo
                        File tempFile = File.createTempFile("temp_audio1", ".mp3", context.getCacheDir());

                        // Copia il contenuto del flusso di input nel file temporaneo
                        try (OutputStream outputStream = new FileOutputStream(tempFile)) {
                            byte[] buffer = new byte[8192];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
                        }

                        // Chiudi il flusso di input
                        inputStream.close();

                        // Imposta il percorso di file ottenuto nel MediaPlayer
                        outputFile1 = tempFile.getAbsolutePath();

                        //mostro il tasto per riprodurre audio.
                        listenYourAudio1.setVisibility(View.VISIBLE);
                        uploadAudio1.setVisibility(View.GONE);
                        audioDoneDescription1.setVisibility(View.VISIBLE);
                        uploadAudioDescription1.setVisibility(View.GONE);

                        helpsCount++;
                        recordHelp(stopRecordingButtonDescription2, stopAudioButton2, audioDoneDescription2, soundWave2,
                                startAudio2, startAudioDescription2, stopYourAudio2,
                                listenYourAudio2, uploadAudio2, uploadAudioDescription2); //secondo aiuto
                        LinearLayout help2 = view.findViewById(R.id.help2);
                        help2.setVisibility(View.VISIBLE);


                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

        }
    });



    // Gestisci il risultato del selettore di file del secondo audio di aiuto
    private final ActivityResultLauncher<Intent> fileSelectorLauncher2 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode() == Activity.RESULT_OK) {
                Intent intent = result.getData();
                if (intent != null) {
                    Uri uri = intent.getData();
                    try {
                        // Ottieni un oggetto DocumentFile dall'Uri
                        DocumentFile documentFile = DocumentFile.fromSingleUri(context, uri);

                        // Ottieni un flusso di input dal DocumentFile
                        InputStream inputStream = context.getContentResolver().openInputStream(uri);

                        // Crea un file temporaneo
                        File tempFile = File.createTempFile("temp_audio2", ".mp3", context.getCacheDir());

                        // Copia il contenuto del flusso di input nel file temporaneo
                        try (OutputStream outputStream = new FileOutputStream(tempFile)) {
                            byte[] buffer = new byte[8192];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
                        }

                        // Chiudi il flusso di input
                        inputStream.close();

                        // Imposta il percorso di file ottenuto nel MediaPlayer
                        outputFile2 = tempFile.getAbsolutePath();

                        //mostro il tasto per riprodurre audio.
                        listenYourAudio2.setVisibility(View.VISIBLE);
                        uploadAudio2.setVisibility(View.GONE);
                        audioDoneDescription2.setVisibility(View.VISIBLE);
                        uploadAudioDescription2.setVisibility(View.GONE);

                        helpsCount++;
                        recordHelp(stopRecordingButtonDescription3, stopAudioButton3, audioDoneDescription3, soundWave3,
                                startAudio3, startAudioDescription3, stopYourAudio3,
                                listenYourAudio3, uploadAudio3, uploadAudioDescription3); //terzo aiuto
                        LinearLayout help3 = view.findViewById(R.id.help3);
                        help3.setVisibility(View.VISIBLE);

                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

        }
    });



    // Gestisci il risultato del selettore di file del terzo audio di aiuto
    private final ActivityResultLauncher<Intent> fileSelectorLauncher3 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode() == Activity.RESULT_OK) {
                Intent intent = result.getData();
                if (intent != null) {
                    Uri uri = intent.getData();
                    try {
                        // Ottieni un oggetto DocumentFile dall'Uri
                        DocumentFile documentFile = DocumentFile.fromSingleUri(context, uri);

                        // Ottieni un flusso di input dal DocumentFile
                        InputStream inputStream = context.getContentResolver().openInputStream(uri);

                        // Crea un file temporaneo
                        File tempFile = File.createTempFile("temp_audio3", ".mp3", context.getCacheDir());

                        // Copia il contenuto del flusso di input nel file temporaneo
                        try (OutputStream outputStream = new FileOutputStream(tempFile)) {
                            byte[] buffer = new byte[8192];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
                        }

                        // Chiudi il flusso di input
                        inputStream.close();

                        // Imposta il percorso di file ottenuto nel MediaPlayer
                        outputFile3 = tempFile.getAbsolutePath();

                        //mostro il tasto per riprodurre audio.
                        listenYourAudio3.setVisibility(View.VISIBLE);
                        uploadAudio3.setVisibility(View.GONE);
                        audioDoneDescription3.setVisibility(View.VISIBLE);
                        uploadAudioDescription3.setVisibility(View.GONE);

                        //mostro button finale per salvare e terminare
                        finishButton.setVisibility(View.VISIBLE);

                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

        }
    });

    private void openFileSelector1() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");  // Filtra solo i file audio
        fileSelectorLauncher1.launch(intent);
    }
    private void openFileSelector2() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");  // Filtra solo i file audio
        fileSelectorLauncher2.launch(intent);
    }
    private void openFileSelector3() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");  // Filtra solo i file audio
        fileSelectorLauncher3.launch(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Rilascia le risorse del MediaPlayer quando la vista del fragment viene distrutta
        if (mediaPlayerListenAnswer1 != null) {
            mediaPlayerListenAnswer1.release();
            mediaPlayerListenAnswer1 = null;
        }

        // Rilascia le risorse del MediaRecorder quando la vista del fragment viene distrutta
        if (mediaRecorder1 != null) {
            try {
                // Cerca di fermare la registrazione solo se è in uno stato valido
                mediaRecorder1.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            mediaRecorder1.release();
            mediaRecorder1 = null;
        }


        // Rilascia le risorse del MediaPlayer quando la vista del fragment viene distrutta
        if (mediaPlayerListenAnswer2 != null) {
            mediaPlayerListenAnswer2.release();
            mediaPlayerListenAnswer2 = null;
        }

        // Rilascia le risorse del MediaRecorder quando la vista del fragment viene distrutta
        if (mediaRecorder2 != null) {
            try {
                // Cerca di fermare la registrazione solo se è in uno stato valido
                mediaRecorder2.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            mediaRecorder2.release();
            mediaRecorder2 = null;
        }


        // Rilascia le risorse del MediaPlayer quando la vista del fragment viene distrutta
        if (mediaPlayerListenAnswer3 != null) {
            mediaPlayerListenAnswer3.release();
            mediaPlayerListenAnswer3 = null;
        }

        // Rilascia le risorse del MediaRecorder quando la vista del fragment viene distrutta
        if (mediaRecorder3 != null) {
            try {
                // Cerca di fermare la registrazione solo se è in uno stato valido
                mediaRecorder3.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            mediaRecorder3.release();
            mediaRecorder3 = null;
        }

    }



    //metodo per gestire le risposte a richieste di permessi
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //gestisco in base al tipo di permesso richiesto (grazie al codice di richiesta)
        //azioni da intraprendere
        switch (requestCode) {
            case REQUEST_AUDIO_PERMISSION_CODE : {
                // permesso concesso con successo
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Il permesso è stato concesso, si può procedere con l'utilizzo del microfono
                    editor.putBoolean("permissionDenied", false);
                    editor.commit();

                    switch (helpsCount) {
                        case 1:
                            //cambio icone
                            startAudio1.setVisibility(View.GONE);
                            startAudioDescription1.setVisibility(View.GONE);
                            stopRecordingButtonDescription1.setVisibility(View.VISIBLE);
                            stopAudioButton1.setVisibility(View.VISIBLE);
                            soundWave1.setVisibility(View.VISIBLE);

                            //registro l'audio e lo salvo in un oggetto di tipo MediaRecorder
                            mediaRecorder1 = new MediaRecorder();
                            mediaRecorder1.setAudioSource(MediaRecorder.AudioSource.MIC);
                            mediaRecorder1.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                            mediaRecorder1.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                            // Ottieni il percorso del file temporaneo
                            outputFile1 = requireContext().getExternalCacheDir() + "/audio_temp1.mp3";

                            mediaRecorder1.setOutputFile(outputFile1);

                            try {
                                mediaRecorder1.prepare();
                                mediaRecorder1.start();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            break;

                        case 2:
                            //cambio icone
                            startAudio2.setVisibility(View.GONE);
                            startAudioDescription2.setVisibility(View.GONE);
                            stopRecordingButtonDescription2.setVisibility(View.VISIBLE);
                            stopAudioButton2.setVisibility(View.VISIBLE);
                            soundWave2.setVisibility(View.VISIBLE);

                            //registro l'audio e lo salvo in un oggetto di tipo MediaRecorder
                            mediaRecorder2 = new MediaRecorder();
                            mediaRecorder2.setAudioSource(MediaRecorder.AudioSource.MIC);
                            mediaRecorder2.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                            mediaRecorder2.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                            // Ottieni il percorso del file temporaneo
                            outputFile2 = requireContext().getExternalCacheDir() + "/audio_temp2.mp3";

                            mediaRecorder2.setOutputFile(outputFile2);

                            try {
                                mediaRecorder2.prepare();
                                mediaRecorder2.start();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            break;

                        case 3:
                            //cambio icone
                            startAudio3.setVisibility(View.GONE);
                            startAudioDescription3.setVisibility(View.GONE);
                            stopRecordingButtonDescription3.setVisibility(View.VISIBLE);
                            stopAudioButton3.setVisibility(View.VISIBLE);
                            soundWave3.setVisibility(View.VISIBLE);

                            //registro l'audio e lo salvo in un oggetto di tipo MediaRecorder
                            mediaRecorder3 = new MediaRecorder();
                            mediaRecorder3.setAudioSource(MediaRecorder.AudioSource.MIC);
                            mediaRecorder3.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                            mediaRecorder3.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                            // Ottieni il percorso del file temporaneo
                            outputFile3 = requireContext().getExternalCacheDir() + "/audio_temp3.mp3";

                            mediaRecorder3.setOutputFile(outputFile3);

                            try {
                                mediaRecorder3.prepare();
                                mediaRecorder3.start();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            break;

                    }

                } else {

                    switch (helpsCount){
                        case 1:
                            //Gestire il fatto che l'utente ha rifiutato di concededere il permesso
                            editor.putBoolean("permissionDenied", true);
                            editor.commit();
                            uploadAudio1.setVisibility(View.VISIBLE);
                            uploadAudioDescription1.setVisibility(View.VISIBLE);
                            startAudio1.setVisibility(View.GONE);
                            startAudioDescription1.setVisibility(View.GONE);
                            //mostro messaggio informativo
                            Toast.makeText(context, getString(R.string.infoPermission), Toast.LENGTH_LONG).show();
                            break;

                        case 2:
                            //Gestire il fatto che l'utente ha rifiutato di concededere il permesso
                            editor.putBoolean("permissionDenied", true);
                            editor.commit();
                            uploadAudio2.setVisibility(View.VISIBLE);
                            uploadAudioDescription2.setVisibility(View.VISIBLE);
                            startAudio2.setVisibility(View.GONE);
                            startAudioDescription2.setVisibility(View.GONE);
                            //mostro messaggio informativo
                            Toast.makeText(context, getString(R.string.infoPermission), Toast.LENGTH_LONG).show();
                            break;

                        case 3:
                            //Gestire il fatto che l'utente ha rifiutato di concededere il permesso
                            editor.putBoolean("permissionDenied", true);
                            editor.commit();
                            uploadAudio3.setVisibility(View.VISIBLE);
                            uploadAudioDescription3.setVisibility(View.VISIBLE);
                            startAudio3.setVisibility(View.GONE);
                            startAudioDescription3.setVisibility(View.GONE);
                            //mostro messaggio informativo
                            Toast.makeText(context, getString(R.string.infoPermission), Toast.LENGTH_LONG).show();
                            break;
                    }

                }
                break;
            }

            case REQUEST_AUDIO_FILE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    switch(helpsCount){
                        case 1:
                            openFileSelector1();
                            break;

                        case 2:
                            openFileSelector2();
                            break;

                        case 3:
                            openFileSelector3();
                            break;
                    }

                }else{
                    //mostro messaggio d'errore
                    Toast.makeText(context, getString(R.string.errorPermission), Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }



    private void startPlayback1() {
        mediaPlayerListenAnswer1 = new MediaPlayer();
        try {
            mediaPlayerListenAnswer1.setDataSource(outputFile1);
            //listener per rilevare la fine della riproduzione dell'audio risposta
            mediaPlayerListenAnswer1.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    //cambio icone
                    stopYourAudio1.setVisibility(View.GONE);
                    listenYourAudio1.setVisibility(View.VISIBLE);
                }
            });
            mediaPlayerListenAnswer1.prepare();
            mediaPlayerListenAnswer1.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopPlayback1() {
        if (mediaPlayerListenAnswer1 != null) {
            mediaPlayerListenAnswer1.stop();
            mediaPlayerListenAnswer1.release();
            mediaPlayerListenAnswer1 = null;
            //cambio icone
            stopYourAudio1.setVisibility(View.GONE);
            listenYourAudio1.setVisibility(View.VISIBLE);
        }
    }


    private void startPlayback2() {
        mediaPlayerListenAnswer2 = new MediaPlayer();
        try {
            mediaPlayerListenAnswer2.setDataSource(outputFile2);
            //listener per rilevare la fine della riproduzione dell'audio risposta
            mediaPlayerListenAnswer2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    //cambio icone
                    stopYourAudio2.setVisibility(View.GONE);
                    listenYourAudio2.setVisibility(View.VISIBLE);
                }
            });
            mediaPlayerListenAnswer2.prepare();
            mediaPlayerListenAnswer2.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopPlayback2() {
        if (mediaPlayerListenAnswer2 != null) {
            mediaPlayerListenAnswer2.stop();
            mediaPlayerListenAnswer2.release();
            mediaPlayerListenAnswer2 = null;
            //cambio icone
            stopYourAudio2.setVisibility(View.GONE);
            listenYourAudio2.setVisibility(View.VISIBLE);
        }
    }



    private void startPlayback3() {
        mediaPlayerListenAnswer3 = new MediaPlayer();
        try {
            mediaPlayerListenAnswer3.setDataSource(outputFile3);
            //listener per rilevare la fine della riproduzione dell'audio risposta
            mediaPlayerListenAnswer3.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    //cambio icone
                    stopYourAudio3.setVisibility(View.GONE);
                    listenYourAudio3.setVisibility(View.VISIBLE);
                }
            });
            mediaPlayerListenAnswer3.prepare();
            mediaPlayerListenAnswer3.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopPlayback3() {
        if (mediaPlayerListenAnswer3 != null) {
            mediaPlayerListenAnswer3.stop();
            mediaPlayerListenAnswer3.release();
            mediaPlayerListenAnswer3 = null;
        }
    }



    // Mostra la finestra di dialogo per la richiesta di permesso di accesso al microfono
    private void showPermissionDialog(int reqCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View customView = inflater.inflate(R.layout.permission_dialog, null);

        TextView description = customView.findViewById(R.id.description);
        Button button = customView.findViewById(R.id.continueButton);
        button.setBackgroundColor(getResources().getColor(R.color.childAccent));

        builder.setView(customView);
        permissionDialog = builder.create();
        // Blocca l'orientamento in portrait quando la finestra di dialogo è aperta
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        permissionDialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.dialog_rounded_corner_gray));
        permissionDialog.setCanceledOnTouchOutside(false);
        //stampare opportuno messaggio in base alla richiesta di permesso
        switch(reqCode){
            case REQUEST_AUDIO_PERMISSION_CODE:
                description.setText(R.string.permissionDescriptionTherapist);
                break;
            case REQUEST_AUDIO_FILE:
                description.setText(R.string.permissionDescription2Therapist);
                break;
        }


        // Imposta l'azione del pulsante
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chiudi la finestra di dialogo
                permissionDialog.dismiss();
                // Ripristina l'orientamento predefinito
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                switch (reqCode){
                    case REQUEST_AUDIO_FILE:
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_AUDIO_FILE);
                        } else {
                            requestPermissions(new String[]{Manifest.permission.READ_MEDIA_AUDIO}, REQUEST_AUDIO_FILE);
                        }
                        break;

                    case REQUEST_AUDIO_PERMISSION_CODE:
                        //verifico se il permesso è già/ancora concesso (a run-time, ogni volta, ripetere questa procedura)
                        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO_PERMISSION_CODE);
                        break;

                }
            }
        });

        permissionDialog.show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ImageDenominationHelpsFragment(false, patientId, imageUri, coin, date)).commit();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ImageDenominationHelpsFragment(true, patientId, imageUri, coin, date)).commit();
        }

    }

    public void sendNotification(String patientId){
        FirebasePatientModel.getChildToken(patientId, new FirebasePatientModel.TokenListener() {
            @Override
            public void onTokenReceived(String token) {
                FCMClient fcmClient = new FCMClient();
                FCMNotification notification = new FCMNotification(getString(R.string.Notification), getString(R.string.ExerciseCreatedDescription));
                FCMRequest fcmRequest = new FCMRequest(token, notification);
                fcmClient.sendNotification(fcmRequest);
            }

            @Override
            public void onTokenReceiveFailed() {

            }
        });
    }

}