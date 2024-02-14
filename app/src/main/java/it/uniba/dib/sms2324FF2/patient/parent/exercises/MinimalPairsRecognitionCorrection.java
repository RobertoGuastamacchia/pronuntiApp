package it.uniba.dib.sms2324FF2.patient.parent.exercises;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.IOException;
import it.uniba.dib.sms2324FF2.R;
import it.uniba.dib.sms2324FF2.exercises.Exercise;
import it.uniba.dib.sms2324FF2.exercises.FirebaseExerciseModel;
import it.uniba.dib.sms2324FF2.firestore.FirestoreModel;
import it.uniba.dib.sms2324FF2.notifications.FCMClient;
import it.uniba.dib.sms2324FF2.notifications.FCMNotification;
import it.uniba.dib.sms2324FF2.notifications.FCMRequest;
import it.uniba.dib.sms2324FF2.patient.FirebasePatientModel;
import it.uniba.dib.sms2324FF2.patient.Patient;


public class MinimalPairsRecognitionCorrection extends Fragment {
    private static final String IMG_PATH = "Images/";
    private static final String IMG_EXT = ".jpeg";
    private static final String AUDIO_PATH = "Audio/";
    private static final String AUDIO_EXT = ".mp3";
    private Exercise exercise;
    private int num_chapter;
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private ImageButton playButton;
    private ImageButton stopAudio;
    private AlertDialog dialog;
    private MaterialButton right;
    private MaterialButton wrong;
    private ImageView imageOne;
    private ImageView imageTwo;
    private TextView buttonDescription;
    private ImageButton exit;
    private Context context;
    private boolean isPortrait = true;
    private OnExerciseListener mCallback;

    public interface OnExerciseListener {
        void refreshExercisesData();
        void showBottomBar();
        void hideBottomBar();
    }

    public MinimalPairsRecognitionCorrection(int num_chapter, Exercise exercise) {
        this.exercise = exercise;
        this.num_chapter = num_chapter;
    }

    public MinimalPairsRecognitionCorrection(int num_chapter, Exercise exercise, boolean isPortrait){
        this.isPortrait = isPortrait;
        this.exercise = exercise;
        this.num_chapter = num_chapter;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnExerciseListener) {
            mCallback = (OnExerciseListener) context;
        } else {
            throw new RuntimeException(context + " must implement OnExerciseListener");
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
            view = inflater.inflate(R.layout.fragment_minimal_pairs_recognition_correction, container, false);
        }else{
            view = inflater.inflate(R.layout.landscape_layout, container, false);
        }

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(isPortrait) {

            //recupero dei widget
            stopAudio = view.findViewById(R.id.stopButton);
            playButton = view.findViewById(R.id.playButton);

            buttonDescription = view.findViewById(R.id.playButtonDescription);
            buttonDescription.setText(getString(R.string.listenTherapistAudio));

            right = view.findViewById(R.id.right);
            wrong = view.findViewById(R.id.wrong);

            exit = view.findViewById(R.id.exit);


            //Prendo dal DB l'audio
            StorageReference audioRef = storageReference.child(AUDIO_PATH + Patient.getInstance().getId() + "/" + num_chapter + "/" + exercise.getAudio() + AUDIO_EXT);
            FirestoreModel.downloadAudio(audioRef, context, new FirestoreModel.DownloadAudioCallback() {
                @Override
                public void onAudioDownloaded(String audioPath) {
                    try {
                        mediaPlayer.setDataSource(audioPath);
                        mediaPlayer.prepare();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void onDownloadFailure() {
                    Toast.makeText(context, getString(R.string.errore_durante_il_download_del_file_audio), Toast.LENGTH_SHORT).show();
                }
            });

            //Prendo dal DB le due immagini dell'esercizio
            imageOne = view.findViewById(R.id.imageOne);
            imageTwo = view.findViewById(R.id.imageTwo);
            StorageReference firstImagePath = storageReference.child(IMG_PATH + Patient.getInstance().getId() + "/" + num_chapter + "/" + exercise.getFirst_img() + IMG_EXT);
            StorageReference secondImagePath = storageReference.child(IMG_PATH + Patient.getInstance().getId() + "/" + num_chapter + "/" + exercise.getSecond_img() + IMG_EXT);

            //Scarico le immagini e controllo se sono quella selezionata dal bambino
            FirestoreModel.downloadImage(firstImagePath, context, new FirestoreModel.DownloadImageCallback() {
                @Override
                public void onImageDownloaded(Uri imageUri) {
                    imageOne.setImageURI(imageUri);
                    String answer = exercise.getAnswer();
                    //vedo se l'immagine coincide con la risposta del bambino
                    if (imageOne.getDrawable() != null) {
                        //mostrare un contorno verde attorno all'immagine risposta del bambino
                        if (answer.contains("first_img")) {
                            Drawable borderDrawable = getResources().getDrawable(R.drawable.image_border_green);
                            LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{imageOne.getDrawable(), borderDrawable});
                            imageOne.setImageDrawable(layerDrawable);
                        }
                    }

                }

                @Override
                public void onDownloadFailure() {
                    Toast.makeText(context, getString(R.string.errore_durante_il_download_dell_immagine), Toast.LENGTH_SHORT).show();
                }
            });

            FirestoreModel.downloadImage(secondImagePath, context, new FirestoreModel.DownloadImageCallback() {
                @Override
                public void onImageDownloaded(Uri imageUri) {
                    imageTwo.setImageURI(imageUri);
                    String answer = exercise.getAnswer();
                    //vedo se l'immagine coincide con la risposta del bambino
                    if (imageTwo.getDrawable() != null) {
                        //mostrare un contorno verde attorno all'immagine risposta del bambino
                        if (answer.contains("second_img")) {
                            Drawable borderDrawable = getResources().getDrawable(R.drawable.image_border_green);
                            LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{imageTwo.getDrawable(), borderDrawable});
                            imageTwo.setImageDrawable(layerDrawable);
                        }
                    }
                }

                @Override
                public void onDownloadFailure() {
                    Toast.makeText(context, getString(R.string.errore_durante_il_download_dell_immagine), Toast.LENGTH_SHORT).show();
                }
            });


            //gestione correzione esercizio se giusto o sbagliato
            right.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseExerciseModel.correctExercise(num_chapter, exercise, true, new FirebaseExerciseModel.OnCorrectExerciseListener() {
                        @Override
                        public void onSuccess() {
                            sendNotification();
                            mCallback.refreshExercisesData();
                            mCallback.showBottomBar();
                        }

                        @Override
                        public void onFailure() {

                        }
                    });

                }
            });
            wrong.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseExerciseModel.correctExercise(num_chapter, exercise, false, new FirebaseExerciseModel.OnCorrectExerciseListener() {
                        @Override
                        public void onSuccess() {
                            sendNotification();
                            mCallback.refreshExercisesData();
                            mCallback.showBottomBar();
                        }

                        @Override
                        public void onFailure() {

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

            //gestione inizio ascolto dell'audio
            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopAudio.setVisibility(View.VISIBLE);
                    playButton.setVisibility(View.GONE);
                    startAudio();
                }
            });

            //gestione della fine dell'ascolto dell'audio
            stopAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playButton.setVisibility(View.VISIBLE);
                    stopAudio.setVisibility(View.GONE);
                    //fermo l'audio
                    stopAudio();
                }
            });

            //listener per rilevare la fine della riproduzione audio
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    // Quando la riproduzione audio è completata, cambia l'immagine del pulsante
                    playButton.setVisibility(View.VISIBLE);
                    stopAudio.setVisibility(View.GONE);
                    buttonDescription.setText(getString(R.string.listenTherapistAudio));
                }
            });

            ImageButton helpButton = view.findViewById(R.id.help);
            //mostrare al click una finestra di dialogo che spiega come va corretto l'esercizio
            helpButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showParentExerciseExplanationDialog();
                }
            });

        }

    }

    //metodo per mostrare finestra di dialogo per spiegare al genitore come va corretto l'esercizio
    private void showParentExerciseExplanationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View customView = inflater.inflate(R.layout.parent_exercise_explanation_dialog, null);

        builder.setView(customView);
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        // Blocca l'orientamento in portrait quando la finestra di dialogo è aperta
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        dialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.dialog_rounded_corner_blue));

        Button closeButton = customView.findViewById(R.id.continueButton);

        TextView message = customView.findViewById(R.id.description);
        message.setText(getResources().getString(R.string.minimalPairsRecognitionExplanation));


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

    private void startAudio() {
        // Inizia la riproduzione audio
        mediaPlayer.start();
        buttonDescription.setText(getString(R.string.stopButtonLabel));
    }

    private void stopAudio() {
        // Ferma la riproduzione audio
        mediaPlayer.pause();
        mediaPlayer.seekTo(0); // Riporta l'audio all'inizio
        buttonDescription.setText(getString(R.string.listenTherapistAudio));

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Rilascia le risorse del MediaPlayer quando la vista del fragment viene distrutta
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
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

        TextView message = customView.findViewById(R.id.custom_message);
        message.setText(getResources().getString(R.string.exitExerciseParent));

        //caso in cui si confermi l'uscita dall'esercizio
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                // Ripristina l'orientamento predefinito
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                //Reindirizzamento
                //mostro di nuovo la bottom bar
                mCallback.showBottomBar();
                mCallback.refreshExercisesData();
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new MinimalPairsRecognitionCorrection(num_chapter, exercise, false )).commit();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new MinimalPairsRecognitionCorrection(num_chapter, exercise, true )).commit();
        }

    }

    private void sendNotification(){
        FirebasePatientModel.getChildToken(Patient.getInstance().getId(),new FirebasePatientModel.TokenListener() {
            @Override
            public void onTokenReceived(String parentToken) {
                FCMClient fcmClient = new FCMClient();
                FCMNotification notification = new FCMNotification(getString(R.string.Notification), getString(R.string.ExerciseCorrectedDescription));
                FCMRequest fcmRequest = new FCMRequest(parentToken, notification);
                fcmClient.sendNotification(fcmRequest);
            }

            @Override
            public void onTokenReceiveFailed() {}
        });
    }
}