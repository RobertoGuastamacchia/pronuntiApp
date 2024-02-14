package it.uniba.dib.sms2324FF2.patient.child.homepage.exercises;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.IOException;
import it.uniba.dib.sms2324FF2.R;
import it.uniba.dib.sms2324FF2.exercises.Exercise;
import it.uniba.dib.sms2324FF2.firestore.FirestoreModel;
import it.uniba.dib.sms2324FF2.patient.Patient;


public class MinimalPairsRecognitionFragment extends Fragment {
    private static final String IMG_PATH = "Images/";
    private static final String IMG_EXT = ".jpeg";
    private static final String AUDIO_PATH = "Audio/";
    private static final String AUDIO_EXT = ".mp3";
    public static final String DONE = "done";
    private Exercise exercise;
    private int num_chapter;
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private ImageButton playButton;
    private AlertDialog dialog;
    private MaterialButton finishButton;
    private ImageButton stopAudioButton;
    private ImageButton imageOne;
    private ImageButton imageTwo;
    private TextView playButtonDescription;
    private ImageButton exit;
    private Context context;
    private boolean isPortrait = true;
    private OnExerciseListener mCallback;

    public interface OnExerciseListener {
        void refreshHomePageData();
        void showBottomBar();
        void hideBottomBar();
    }

    public MinimalPairsRecognitionFragment(int num_chapter, Exercise exercise){
        this.exercise = exercise;
        this.num_chapter = num_chapter;
    }

    public MinimalPairsRecognitionFragment(boolean isPortrait, int num_chapter, Exercise exercise){
        this.exercise = exercise;
        this.num_chapter = num_chapter;
        this.isPortrait = isPortrait;
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
            view = inflater.inflate(R.layout.fragment_minimal_pairs_recognition, container, false);
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
            stopAudioButton = view.findViewById(R.id.stopAudio);

            playButton = view.findViewById(R.id.playButton);
            playButton.setImageResource(R.drawable.play_button);
            playButtonDescription = view.findViewById(R.id.playButtonDescription);
            playButtonDescription.setText(getString(R.string.playButtonLabel));

            finishButton = view.findViewById(R.id.finish);

            exit = view.findViewById(R.id.exit);

            stopAudioButton = view.findViewById(R.id.stopButton);

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
            FirestoreModel.downloadImage(firstImagePath, context, new FirestoreModel.DownloadImageCallback() {
                @Override
                public void onImageDownloaded(Uri imageUri) {
                    imageOne.setImageURI(imageUri);
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
                }

                @Override
                public void onDownloadFailure() {
                    Toast.makeText(context, getString(R.string.errore_durante_il_download_dell_immagine), Toast.LENGTH_SHORT).show();
                }
            });


            //gestione della fine dell'esercizio
            finishButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MediaPlayer.create(getActivity(), R.raw.button_click_sound).start();
                    String answer = null;
                    if (!imageOne.isEnabled())
                        answer = exercise.getFirst_img();
                    else if (!imageTwo.isEnabled())
                        answer = exercise.getSecond_img();

                    //carico la risposta
                    //chiamo la pagina post-esercizio di caricamento
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.frameLayout, new LoadingPostExerciseFragment(2, num_chapter, exercise, answer, context, getActivity()));
                    ft.addToBackStack(null);
                    ft.commit();

                }
            });


            //gestisco l'uscita dalla schermata degli esercizi
            exit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MediaPlayer.create(getActivity(), R.raw.button_click_sound).start();
                    //mostra finestra di dialogo per l'uscita
                    showExitDialog();
                }
            });

            //gestione inizio ascolto dell'audio
            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopAudioButton.setVisibility(View.VISIBLE);
                    playButton.setVisibility(View.GONE);
                    startAudio();
                }
            });

            //gestione della fine dell'ascolto dell'audio
            stopAudioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playButton.setVisibility(View.VISIBLE);
                    stopAudioButton.setVisibility(View.GONE);
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
                    stopAudioButton.setVisibility(View.GONE);
                    playButtonDescription.setText(getString(R.string.playButtonLabel));
                }
            });

            //gestione selezione prima immagine
            imageOne.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MediaPlayer.create(getActivity(), R.raw.button_click_sound).start();
                    //selezione dell'immagine 1 e deselezione dell'immagine 2
                    finishButton.setVisibility(View.VISIBLE);
                    v.setSelected(!v.isSelected());
                    imageTwo.setSelected(false);
                    imageOne.setEnabled(false);
                    imageTwo.setEnabled(true);

                    //produco l'animazione
                    Animation pulseAnimation = AnimationUtils.loadAnimation(context, R.anim.pulse_animation);
                    finishButton.startAnimation(pulseAnimation);

                }
            });

            //gestione selezione seconda immagine
            imageTwo.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    MediaPlayer.create(getActivity(), R.raw.button_click_sound).start();
                    //selezione dell'immagine 2 e deselezione dell'immagine 1
                    finishButton.setVisibility(View.VISIBLE);
                    v.setSelected(!v.isSelected());
                    imageOne.setSelected(false);
                    imageOne.setEnabled(true);
                    imageTwo.setEnabled(false);

                    //produco l'animazione
                    Animation pulseAnimation = AnimationUtils.loadAnimation(context, R.anim.pulse_animation);
                    finishButton.startAnimation(pulseAnimation);
                }
            });
        }

    }

    private void startAudio() {
        // Inizia la riproduzione audio
        mediaPlayer.start();
        playButtonDescription.setText(getString(R.string.stopButtonLabel));
    }

    private void stopAudio() {
        // Ferma la riproduzione audio
        mediaPlayer.pause();
        mediaPlayer.seekTo(0); // Riporta l'audio all'inizio
        playButtonDescription.setText(getString(R.string.playButtonLabel));

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

        //caso in cui si confermi l'uscita dall'esercizio
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                // Ripristina l'orientamento predefinito
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                mCallback.refreshHomePageData();
                //mostro di nuovo la bottom bar
                mCallback.showBottomBar();

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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new MinimalPairsRecognitionFragment(false, num_chapter, exercise)).commit();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new MinimalPairsRecognitionFragment(true, num_chapter, exercise)).commit();
        }

    }
}