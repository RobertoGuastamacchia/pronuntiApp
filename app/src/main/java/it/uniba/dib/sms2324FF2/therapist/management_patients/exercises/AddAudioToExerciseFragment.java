package it.uniba.dib.sms2324FF2.therapist.management_patients.exercises;

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
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import it.uniba.dib.sms2324FF2.R;
import it.uniba.dib.sms2324FF2.patient.child.exercises.FirebaseExerciseModel;
import it.uniba.dib.sms2324FF2.notifications.FCMClient;
import it.uniba.dib.sms2324FF2.notifications.FCMNotification;
import it.uniba.dib.sms2324FF2.notifications.FCMRequest;
import it.uniba.dib.sms2324FF2.patient.PatientFirebaseModel;

public class AddAudioToExerciseFragment extends Fragment {

    private static final int REQUEST_AUDIO_PERMISSION_CODE = 156; //Numero intero costante, codice richiesta del permesso del microfono
    private static final int REQUEST_AUDIO_FILE = 2002; //numero intero costante, codice richiesta del permesso per carica audio dalla memoria
    private MediaPlayer mediaPlayerListenAnswer = new MediaPlayer();
    private MediaRecorder mediaRecorder;
    private ImageButton exit;
    private ImageButton help;
    private ImageButton refresh;
    private ImageButton uploadAudio;
    private TextView uploadAudioDescription;
    private ImageButton startAudio;
    private AlertDialog permissionDialog;
    private ImageButton listenYourAudio;
    private ImageButton stopYourAudio;
    private ImageView soundWave;
    private AlertDialog dialog;
    private MaterialButton finishButton;
    private ImageButton stopAudio;
    private TextView stopRecordingButtonDescription;
    private TextView startAudioDescription;
    private TextView audioDoneDescription;
    private Context context;
    private String outputFile;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private boolean isPortrait = true;
    Uri firstImgUri, secondImgUri;
    TextView title;
    ProgressBar progressBar;
    private String patientId;
    int coin;
    Timestamp date;
    private AddAudioToExerciseFragment.onMinimalPairsRecognitionAudioRecording mCallback;

    public interface onMinimalPairsRecognitionAudioRecording {
        void showBottomBar();
        void hideBottomBar();
        void refreshPatientsData();
    }

    public AddAudioToExerciseFragment(boolean isPortrait, String id, Uri firstImgUri, Uri secondImgUri, int coin, Timestamp date){
        this.isPortrait = isPortrait;
        this.patientId = id;
        this.firstImgUri = firstImgUri;
        this.secondImgUri = secondImgUri;
        this.coin = coin;
        this.date = date;
    }
    public AddAudioToExerciseFragment(boolean isPortrait, String id, int coin, Timestamp date){
        this.isPortrait = isPortrait;
        this.patientId = id;
        this.coin = coin;
        this.date = date;
    }
    public AddAudioToExerciseFragment(boolean isPortrait, String id, Uri firstImgUri, Uri secondImgUri, String outputFile, int coin, Timestamp date){
        this.isPortrait = isPortrait;
        this.patientId = id;
        this.firstImgUri = firstImgUri;
        this.secondImgUri = secondImgUri;
        this.coin = coin;
        this.date = date;
        this.outputFile = outputFile;
    }
    public AddAudioToExerciseFragment(boolean isPortrait, String id, int coin, String outputFile, Timestamp date){
        this.isPortrait = isPortrait;
        this.patientId = id;
        this.coin = coin;
        this.date = date;
        this.outputFile = outputFile;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ImageDenominationFragment.onImageDenominationListener) {
            mCallback = (AddAudioToExerciseFragment.onMinimalPairsRecognitionAudioRecording) context;
        } else {
            throw new RuntimeException(context + " must implement onMinimalPairsRecognitionAudioRecording");
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
            view = inflater.inflate(R.layout.fragment_minimal_pairs_audio_recording, container, false);
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
            progressBar = view.findViewById(R.id.progressBar);

            stopRecordingButtonDescription = view.findViewById(R.id.stopAudioButtonDescription);
            stopAudio = view.findViewById(R.id.stopAudio);

            startAudio = view.findViewById(R.id.startAudio);
            startAudioDescription = view.findViewById(R.id.startAudioButtonDescription);

            audioDoneDescription = view.findViewById(R.id.audioDoneDescription);
            soundWave = view.findViewById(R.id.sound_wave);

            refresh = view.findViewById(R.id.refresh);

            stopYourAudio = view.findViewById(R.id.stopYour_Audio);
            listenYourAudio = view.findViewById(R.id.listenYour_Audio);

            uploadAudio = view.findViewById(R.id.upload_audio);
            uploadAudioDescription = view.findViewById(R.id.upload_audio_description);

            finishButton = view.findViewById(R.id.finish);

            exit = view.findViewById(R.id.exit);

            help = view.findViewById(R.id.help);
            title = view.findViewById(R.id.title);

            if(firstImgUri == null && secondImgUri == null)
                title.setText(getString(R.string.WordsSequenceRepetition));
            else
                title.setText(getString(R.string.MinimalPairsRecognition));


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


            //gestione della fine dell'esercizio
            finishButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressBar.setVisibility(View.VISIBLE);
                    //carico la risposta in base all'esercizio da caricare
                    if(firstImgUri!=null && secondImgUri!=null) {
                        //Salvo MinimalPairsRecognition
                        FirebaseExerciseModel.saveMinimalPairs(patientId, firstImgUri, secondImgUri, outputFile, coin, date, new FirebaseExerciseModel.OnSaveExerciseListener() {
                            @Override
                            public void onSuccess() {
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
                    } else {
                        //Salvo WordsSequencesRepetition
                        FirebaseExerciseModel.saveWordsSequencesRepetition(patientId, outputFile, coin, date, new FirebaseExerciseModel.OnSaveExerciseListener() {
                            @Override
                            public void onSuccess() {
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
                }
            });

            //gestione riascolto del proprio audio risposta
            listenYourAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //cambio icone
                    stopYourAudio.setVisibility(View.VISIBLE);
                    listenYourAudio.setVisibility(View.GONE);
                    startPlayback();
                }
            });

            //gestione stop riascolto del proprio audio risposta
            stopYourAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //cambio icone
                    stopYourAudio.setVisibility(View.GONE);
                    listenYourAudio.setVisibility(View.VISIBLE);
                    stopPlayback();
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



            //Gestione dell'inizio della registrazione
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

                        uploadAudio.setVisibility(View.GONE);
                        uploadAudioDescription.setVisibility(View.GONE);
                        stopAudio.setVisibility(View.VISIBLE);
                        stopRecordingButtonDescription.setVisibility(View.VISIBLE);
                        startAudio.setVisibility(View.GONE);
                        startAudioDescription.setVisibility(View.GONE);

                        // Il permesso è stato concesso, si può procedere con l'utilizzo del microfono
                        editor.putBoolean("permissionDenied", false);
                        editor.commit();
                        //cambio icone
                        startAudio.setVisibility(View.GONE);
                        startAudioDescription.setVisibility(View.GONE);
                        stopAudio.setVisibility(View.VISIBLE);
                        stopRecordingButtonDescription.setVisibility(View.VISIBLE);
                        soundWave.setVisibility(View.VISIBLE);

                        //registro l'audio e lo salvo in un oggetto di tipo MediaRecorder
                        mediaRecorder = new MediaRecorder();
                        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                        // Ottieni il percorso del file temporaneo
                        outputFile = requireContext().getExternalCacheDir() + "/audio_temp.mp3";

                        mediaRecorder.setOutputFile(outputFile);

                        try {
                            mediaRecorder.prepare();
                            mediaRecorder.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });


            stopAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //cambio icone
                    stopRecordingButtonDescription.setVisibility(View.GONE);
                    stopAudio.setVisibility(View.GONE);
                    audioDoneDescription.setVisibility(View.VISIBLE);
                    soundWave.setVisibility(View.GONE);
                    finishButton.setVisibility(View.VISIBLE);
                    refresh.setVisibility(View.VISIBLE);
                    listenYourAudio.setVisibility(View.VISIBLE);

                    //fine audio
                    if (mediaRecorder != null) {
                        mediaRecorder.stop();
                        mediaRecorder.release();
                        mediaRecorder = null;
                    }
                }
            });

            //listener per il click per caricare un file audio
            uploadAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Verifica se il permesso è già concesso
                    //verifico se il permesso è già/ancora concesso (a run-time, ogni volta, ripetere questa procedura)
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                            && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        showPermissionDialog(REQUEST_AUDIO_FILE);
                    } else {
                        openFileSelector();
                    }

                }
            });


            //gestione del tasto di refresh per ripetere la risposta audio
            refresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //aggiorno la pagina ricaricandola per poter ripetere l'audio
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.frameLayout, new AddAudioToExerciseFragment(true, patientId, firstImgUri, secondImgUri, coin, date));
                    ft.commit();
                }
            });

            //dialog di aiuto su come l'esercizio si struttura
            help.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showHelpDialog();
                }
            });

        }
    }

    // Gestisci il risultato del selettore di file
    private final ActivityResultLauncher<Intent> fileSelectorLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode() == Activity.RESULT_OK) {
                Intent intent = result.getData();
                if (intent != null) {
                    Uri uri = intent.getData();

                    try {
                        // Ottieni un flusso di input dal DocumentFile
                        InputStream inputStream = context.getContentResolver().openInputStream(uri);

                        // Crea un file temporaneo
                        File tempFile = File.createTempFile("temp_audio", ".mp3", context.getCacheDir());

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
                        outputFile = tempFile.getAbsolutePath();

                        //mostro il tasto per riprodurre audio.
                        listenYourAudio.setVisibility(View.VISIBLE);
                        uploadAudio.setVisibility(View.GONE);
                        finishButton.setVisibility(View.VISIBLE);
                        audioDoneDescription.setVisibility(View.VISIBLE);
                        uploadAudioDescription.setVisibility(View.GONE);

                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    });


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Rilascia le risorse del MediaPlayer quando la vista del fragment viene distrutta
        if (mediaPlayerListenAnswer != null) {
            mediaPlayerListenAnswer.release();
            mediaPlayerListenAnswer = null;
        }

        // Rilascia le risorse del MediaRecorder quando la vista del fragment viene distrutta
        if (mediaRecorder != null) {
            try {
                // Cerca di fermare la registrazione solo se è in uno stato valido
                mediaRecorder.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            mediaRecorder.release();
            mediaRecorder = null;
        }
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
                // Ripristina l'orientamento predefinito
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                dialog.dismiss();
                mCallback.refreshPatientsData();
            }
        });

        //caso in cui si annulli l'uscita dall'esercizio
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Azioni per il pulsante "No"
                // Chiudi la finestra di dialogo
                dialog.dismiss();
                // Ripristina l'orientamento predefinito
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        });

        dialog.show();
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
                    //cambio icone
                    startAudio.setVisibility(View.GONE);
                    startAudioDescription.setVisibility(View.GONE);
                    stopAudio.setVisibility(View.VISIBLE);
                    stopRecordingButtonDescription.setVisibility(View.VISIBLE);
                    soundWave.setVisibility(View.VISIBLE);

                    //registro l'audio e lo salvo in un oggetto di tipo MediaRecorder
                    mediaRecorder = new MediaRecorder();
                    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                    // Ottieni il percorso del file temporaneo
                    outputFile = requireContext().getExternalCacheDir() + "/audio_temp.mp3";

                    mediaRecorder.setOutputFile(outputFile);

                    try {
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    //Gestire il fatto che l'utente ha rifiutato di concededere il permesso
                    editor.putBoolean("permissionDenied", true);
                    editor.commit();
                    uploadAudio.setVisibility(View.VISIBLE);
                    uploadAudioDescription.setVisibility(View.VISIBLE);
                    startAudio.setVisibility(View.GONE);
                    startAudioDescription.setVisibility(View.GONE);
                    //mostro messaggio informativo
                    Toast.makeText(context, getString(R.string.infoPermission), Toast.LENGTH_LONG).show();
                }
                break;
            }

            case REQUEST_AUDIO_FILE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openFileSelector();
                }else{
                    //mostro messaggio d'errore
                    Toast.makeText(context, getString(R.string.errorPermissionTherapist), Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    private void openFileSelector() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");  // Filtra solo i file audio
        fileSelectorLauncher.launch(intent);
    }


    private void startPlayback() {
        mediaPlayerListenAnswer = new MediaPlayer();
        try {
            mediaPlayerListenAnswer.setDataSource(outputFile);
            //listener per rilevare la fine della riproduzione dell'audio risposta
            mediaPlayerListenAnswer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    //cambio icone
                    stopYourAudio.setVisibility(View.GONE);
                    listenYourAudio.setVisibility(View.VISIBLE);
                }
            });
            mediaPlayerListenAnswer.prepare();
            mediaPlayerListenAnswer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopPlayback() {
        if (mediaPlayerListenAnswer != null) {
            mediaPlayerListenAnswer.stop();
            mediaPlayerListenAnswer.release();
            mediaPlayerListenAnswer = null;
            //cambio icone
            stopYourAudio.setVisibility(View.GONE);
            listenYourAudio.setVisibility(View.VISIBLE);
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
        permissionDialog.setCanceledOnTouchOutside(false);
        // Blocca l'orientamento in portrait quando la finestra di dialogo è aperta
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        permissionDialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.dialog_rounded_corner_gray));

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
            if(firstImgUri!=null && secondImgUri!=null) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new it.uniba.dib.sms2324FF2.therapist.management_patients.exercises.AddAudioToExerciseFragment(false, patientId, firstImgUri, secondImgUri, outputFile, coin, date)).commit();
            } else {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new it.uniba.dib.sms2324FF2.therapist.management_patients.exercises.AddAudioToExerciseFragment(false, patientId, coin, outputFile, date)).commit();
            }
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if(firstImgUri!=null && secondImgUri!=null) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new it.uniba.dib.sms2324FF2.therapist.management_patients.exercises.AddAudioToExerciseFragment(true, patientId, firstImgUri, secondImgUri, outputFile, coin, date)).commit();
            } else {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new it.uniba.dib.sms2324FF2.therapist.management_patients.exercises.AddAudioToExerciseFragment(true, patientId, coin, outputFile, date)).commit();
            }
        }

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
        if(firstImgUri==null && secondImgUri==null)
            message.setText(getResources().getString(R.string.descriptionWordsInSequence));
        else
            message.setText(getResources().getString(R.string.descriptionMinimalPairs));


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

    public void sendNotification(String patientId){
        PatientFirebaseModel.getChildToken(patientId, new PatientFirebaseModel.TokenListener() {
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
