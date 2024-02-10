package it.uniba.dib.sms232421.patient.parent.exercises;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import it.uniba.dib.sms232421.R;
import it.uniba.dib.sms232421.exercises.Exercise;
import it.uniba.dib.sms232421.exercises.FirebaseExerciseModel;
import it.uniba.dib.sms232421.firestore.FirestoreModel;
import it.uniba.dib.sms232421.notifications.FCMClient;
import it.uniba.dib.sms232421.notifications.FCMNotification;
import it.uniba.dib.sms232421.notifications.FCMRequest;
import it.uniba.dib.sms232421.patient.FirebasePatientModel;
import it.uniba.dib.sms232421.patient.Patient;
import it.uniba.dib.sms232421.patient.child.PulseAnimation;
import it.uniba.dib.sms232421.patient.child.homepage.exercises.ImageDenominationFragment;


public class ImageDenominationCorrection extends Fragment {

    private Exercise exercise;
    private int num_chapter;
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private static final String IMAGE_PATH = "Images/";
    private static final String IMAGE_EXT = ".jpeg";
    private static final String AUDIO_PATH = "Audio/";
    private static final String AUDIO_EXT = ".mp3";
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private ImageButton startChildAudio;
    private ImageButton stopChildAudio;
    private TextView startAudioDescription;
    private TextView stopAudioDescription;
    private ImageView image;
    private AlertDialog dialog;
    private MaterialButton correct;
    private MaterialButton wrong;
    private ImageButton exit;
    private Context context;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private boolean isPortrait = true;
    private OnExerciseListener mCallback;

    public interface OnExerciseListener {
        void refreshExercisesData();
        void showBottomBar();
        void hideBottomBar();
    }

    //Costruttore della classe
    public ImageDenominationCorrection(int num_chapter, Exercise exercise){
        this.exercise = exercise;
        this.num_chapter = num_chapter;
    }

    public ImageDenominationCorrection(boolean isPortrait, int num_chapter, Exercise exercise){
        this.isPortrait = isPortrait;
        this.num_chapter = num_chapter;
        this.exercise = exercise;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        if(isPortrait){
            view = inflater.inflate(R.layout.fragment_image_denomination_correction, container, false);
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
            startChildAudio = view.findViewById(R.id.startAudio);
            stopChildAudio = view.findViewById(R.id.stopAudio);
            image = view.findViewById(R.id.image);
            correct = view.findViewById(R.id.right);
            wrong = view.findViewById(R.id.wrong);
            exit = view.findViewById(R.id.exit);
            startAudioDescription = view.findViewById(R.id.startAudioButtonDescription);
            stopAudioDescription = view.findViewById(R.id.stopAudioButtonDescription);

            //Prendo la risposta audio dl bambino dal db
            if (exercise.getAnswer() != null) {
                StorageReference audioRefResponse = storageReference.child(AUDIO_PATH + Patient.getInstance().getId() + "/" + num_chapter + "/" + exercise.getAnswer() + AUDIO_EXT);
                FirestoreModel.downloadAudio(audioRefResponse, context, new FirestoreModel.DownloadAudioCallback() {
                    @Override
                    public void onAudioDownloaded(String audioPath) {
                        try {
                            mediaPlayer.setDataSource(audioPath);
                            mediaPlayer.prepare();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onDownloadFailure() {

                    }
                });
            }

            //carico l'immagine dell'esercizio
            image = view.findViewById(R.id.image);
            StorageReference imgPath = storageReference.child(IMAGE_PATH  + Patient.getInstance().getId() + "/" + num_chapter + "/" + exercise.getFirst_img() + IMAGE_EXT);
            FirestoreModel.downloadImage(imgPath, context, new FirestoreModel.DownloadImageCallback() {
                @Override
                public void onImageDownloaded(Uri imageUri) {
                    image.setImageURI(imageUri);

                }

                @Override
                public void onDownloadFailure() {
                    Toast.makeText(context, getString(R.string.errore_durante_il_download_dell_immagine), Toast.LENGTH_SHORT).show();
                }
            });

            //gestione correzione esercizio se giusto o sbagliato
            correct.setOnClickListener(new View.OnClickListener() {
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

            //gestione ascolto audio bambino
            startChildAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //cambio icone
                    stopChildAudio.setVisibility(View.VISIBLE);
                    startChildAudio.setVisibility(View.GONE);
                    startAudioDescription.setVisibility(View.GONE);
                    stopAudioDescription.setVisibility(View.VISIBLE);
                    startAudio();
                }
            });

            //gestione stop ascolto audio bambino
            stopChildAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //cambio icone
                    stopChildAudio.setVisibility(View.GONE);
                    startChildAudio.setVisibility(View.VISIBLE);
                    startAudioDescription.setVisibility(View.VISIBLE);
                    stopAudioDescription.setVisibility(View.GONE);
                    stopAudio();
                }
            });

            //gestisco l'uscita dalla schermata
            exit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //mostra finestra di dialogo per l'uscita
                    showExitDialog();
                }
            });

            //listener per rilevare la fine della riproduzione audio bambino
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    //cambio icone
                    stopChildAudio.setVisibility(View.GONE);
                    startChildAudio.setVisibility(View.VISIBLE);
                    startAudioDescription.setVisibility(View.VISIBLE);
                    stopAudioDescription.setVisibility(View.GONE);
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


            int helpsUsedCount = exercise.getNum_hints_used(); //cancellare
            //stampo il numero di aiuti che il bambino ha utilizzato
            TextView helpsUsed = view.findViewById(R.id.helpsUsed);
            helpsUsed.setText(getString(R.string.helpsUsed) + " " + String.valueOf(helpsUsedCount));
        }

    }

    //metodo per mostrare finestra di dialogo per spiegare al genitore come va corretto l'esercizio
    private void showParentExerciseExplanationDialog() {
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
        message.setText(getResources().getString(R.string.imageDenominationExplanation));


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
    }

    private void stopAudio() {
        // Ferma la riproduzione audio
        mediaPlayer.pause();
        mediaPlayer.seekTo(0); // Riporta l'audio all'inizio

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
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ImageDenominationCorrection(false, num_chapter, exercise)).commit();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ImageDenominationCorrection(true, num_chapter, exercise)).commit();
        }

    }


    private void sendNotification(){
        FirebasePatientModel.getChildToken(Patient.getInstance().getId(), new FirebasePatientModel.TokenListener() {
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