package it.uniba.dib.sms2324FF2.patient.child.homepage.exercises;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;
import java.util.TreeMap;

import it.uniba.dib.sms2324FF2.R;
import it.uniba.dib.sms2324FF2.patient.child.exercises.Exercise;
import it.uniba.dib.sms2324FF2.patient.child.exercises.FirebaseExerciseModel;
import it.uniba.dib.sms2324FF2.firestore.FirestoreModel;
import it.uniba.dib.sms2324FF2.patient.Patient;
import it.uniba.dib.sms2324FF2.notifications.FCMClient;
import it.uniba.dib.sms2324FF2.notifications.FCMNotification;
import it.uniba.dib.sms2324FF2.notifications.FCMRequest;
import it.uniba.dib.sms2324FF2.patient.PatientFirebaseModel;

public class LoadingPostExerciseFragment extends Fragment {
    Thread thread = Thread.currentThread();
    private static final String TYPE_DATA_ANSWER = "answer";
    private static final String IMAGE_DENOMINATION_TYPE_DATA_HINTS = "num_hints_used";
    private static final String DONE = "done";
    private int type, num_chapter, helpsCount;
    private String outputFile, answer;
    private Exercise exercise;
    private Context context;
    private FragmentActivity activity;
    private MediaPlayer successSound;
    private OnExerciseListener mCallback;

    public interface OnExerciseListener {
        void refreshHomePageData();
        void showBottomBar();
        void hideBottomBar();
        void goBack();
    }

    public interface OnSaveResponseListener {
        void onSaveResponseSuccess();
        void onSaveResponseFailure();
    }

    public LoadingPostExerciseFragment(int type, int num_chapter,Exercise exercise,String outputFile,int helpsCount,Context context, FragmentActivity activity){
        this.type = type;
        this.num_chapter = num_chapter;
        this.exercise = exercise;
        this.outputFile = outputFile;
        this.helpsCount = helpsCount;
        this.context = context;
        this.activity = activity;
    }
    public LoadingPostExerciseFragment(int type, int num_chapter,Exercise exercise,String answer,Context context,FragmentActivity activity){
        this.type = type;
        this.num_chapter = num_chapter;
        this.exercise = exercise;
        this.answer = answer;
        this.outputFile = answer;
        this.context = context;
        this.activity = activity;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnExerciseListener) {
            mCallback = (OnExerciseListener) context;
        } else {
            throw new RuntimeException(context + " must implement OnExerciseListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_loading_post_exercise, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

            //recupero dei widget
            successSound = MediaPlayer.create(getActivity(), R.raw.success_sound);

            // Creare un oggetto Random
            Random random = new Random();

            // Generare un numero casuale compreso tra 1 e 15 (estremi inclusi)
            // rappresenterà l'immagine da inserire nella schermata
            int randomNumber = random.nextInt(15) + 1;

            ImageView img = view.findViewById(R.id.image);
            TextView txt = view.findViewById(R.id.imageDescription);

            switch (randomNumber) {
                case 1:
                    img.setImageResource(R.drawable.crown);
                    txt.setText(R.string.crown_description);
                    break;
                case 2:
                    img.setImageResource(R.drawable.glasses);
                    txt.setText(R.string.glasses_description);
                    break;
                case 3:
                    img.setImageResource(R.drawable.drums);
                    txt.setText(R.string.drums_description);
                    break;
                case 4:
                    img.setImageResource(R.drawable.love);
                    txt.setText(R.string.love_description);
                    break;
                case 5:
                    img.setImageResource(R.drawable.confused);
                    txt.setText(R.string.confused_description);
                    break;
                case 6:
                    img.setImageResource(R.drawable.firefighter);
                    txt.setText(R.string.firefighter_description);
                    break;
                case 7:
                    img.setImageResource(R.drawable.surprised);
                    txt.setText(R.string.surprised_description);
                    break;
                case 8:
                    img.setImageResource(R.drawable.singing);
                    txt.setText(R.string.singing_description);
                    break;
                case 9:
                    img.setImageResource(R.drawable.hiding);
                    txt.setText(R.string.hiding_description);
                    break;
                case 10:
                    img.setImageResource(R.drawable.no_word);
                    txt.setText(R.string.no_word_description);
                    break;
                case 11:
                    img.setImageResource(R.drawable.sleeping);
                    txt.setText(R.string.sleeping_description);
                    break;
                case 12:
                    img.setImageResource(R.drawable.party);
                    txt.setText(R.string.party_description);
                    break;
                case 13:
                    img.setImageResource(R.drawable.wink);
                    txt.setText(R.string.wink_description);
                    break;
                case 14:
                    img.setImageResource(R.drawable.hello);
                    txt.setText(R.string.hello_description);
                    break;
                case 15:
                    img.setImageResource(R.drawable.explorer);
                    txt.setText(R.string.explorer_description);
                    break;

            }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Creare un handler associato al thread principale (UI thread)
        Handler mainHandler = new Handler(Looper.getMainLooper());

        // Creare un thread per l'operazione con una durata di 4 secondi
        HandlerThread handlerThread = new HandlerThread("MyHandlerThread");
        handlerThread.start();

        Handler handler = new Handler(handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                FirebaseExerciseModel.getPatientExercises(Patient.getInstance().getId(),new FirebaseExerciseModel.OnAllExercisesCreatedListener() {
                    @Override
                    public void onExercisesCreated(TreeMap<ArrayList<Object>, ArrayList<Exercise>> exercises) {
                        //salvo la risposta in base al type dell'esercizio
                        switch (type){
                            case 1:
                                imageDenominationSaveResponse(new OnSaveResponseListener() {
                                    @Override
                                    public void onSaveResponseSuccess() {
                                        mainHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                // Sostituire il fragment corrente con un altro
                                                successSound.start();
                                                sendNotification();
                                                mCallback.refreshHomePageData();

                                            }
                                        });
                                    }
                                    @Override
                                    public void onSaveResponseFailure() {
                                        mainHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(context, getString(R.string.errore_durante_il_caricamento_della_risposta), Toast.LENGTH_SHORT).show();
                                                // Torna indietro
                                                mCallback.goBack();
                                            }
                                        });
                                    }
                                });
                                break;
                            case 2:
                                minimalPairsSaveResponse(new OnSaveResponseListener() {
                                    @Override
                                    public void onSaveResponseSuccess() {
                                        mainHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                // Sostituire il fragment corrente con un altro
                                                successSound.start();
                                                sendNotification();
                                                mCallback.refreshHomePageData();
                                            }
                                        });
                                    }
                                    @Override
                                    public void onSaveResponseFailure() {
                                        mainHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(context, getString(R.string.errore_durante_il_caricamento_della_risposta), Toast.LENGTH_SHORT).show();
                                                // Torna indietro
                                                mCallback.goBack();
                                            }
                                        });
                                    }
                                });
                                break;
                            case 3:
                                wordsSequencesRepetitionSaveResponse(new OnSaveResponseListener() {
                                    @Override
                                    public void onSaveResponseSuccess() {
                                        mainHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                // Sostituire il fragment corrente con un altro
                                                successSound.start();
                                                sendNotification();
                                                mCallback.refreshHomePageData();
                                            }
                                        });
                                    }
                                    @Override
                                    public void onSaveResponseFailure() {
                                        mainHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(context, getString(R.string.errore_durante_il_caricamento_della_risposta), Toast.LENGTH_SHORT).show();
                                                // Torna indietro
                                                mCallback.goBack();
                                            }
                                        });
                                    }
                                });
                                break;
                        }
                    }
                    @Override
                    public void onExercisesCreationFailed() {}
                });
            }
        });
    }

    private void imageDenominationSaveResponse(OnSaveResponseListener listener){
        //salvo l'audio di risposta
        String answer = "answer_" + exercise.getId();
        String pathRef = Patient.getInstance().getId() + "/" + num_chapter + "/" + answer;
        FirestoreModel.uploadAudio(pathRef, outputFile, new FirestoreModel.UploadCallback() {
            @Override
            public void onUploadSuccess() {
                //salvo il nome dell'audio
                FirebaseExerciseModel.putExerciseAnswer(num_chapter, exercise, TYPE_DATA_ANSWER, answer, new FirebaseExerciseModel.TransactionCallback() {
                    @Override
                    public void onTransactionSuccess() {
                        //salvo il numero di aiuti utilizzato
                        FirebaseExerciseModel.putExerciseAnswer(num_chapter, exercise, IMAGE_DENOMINATION_TYPE_DATA_HINTS, helpsCount, new FirebaseExerciseModel.TransactionCallback() {
                            @Override
                            public void onTransactionSuccess() {
                                //Aggiorno l'esercizio
                                FirebaseExerciseModel.changeExerciseState(num_chapter, exercise, DONE, new FirebaseExerciseModel.TransactionCallback() {
                                    @Override
                                    public void onTransactionSuccess() {
                                        //success callback
                                        if (listener != null) {
                                            listener.onSaveResponseSuccess();
                                        }
                                    }
                                    @Override
                                    public void onTransactionFailure() {
                                        // failure callback
                                        if (listener != null) {
                                            listener.onSaveResponseFailure();
                                        }
                                    }
                                });
                            }


                            @Override
                            public void onTransactionFailure() {
                                // failure callback
                                if (listener != null) {
                                    listener.onSaveResponseFailure();
                                }
                            }
                        });
                    }
                    @Override
                    public void onTransactionFailure() {
                        // failure callback
                        if (listener != null) {
                            listener.onSaveResponseFailure();
                        }
                    }
                });
            }
            @Override
            public void onUploadFailure() {
                // failure callback
                if (listener != null) {
                    listener.onSaveResponseFailure();
                }
            }
        });
    }

    private void minimalPairsSaveResponse(OnSaveResponseListener listener){
        FirebaseExerciseModel.putExerciseAnswer(num_chapter, exercise, TYPE_DATA_ANSWER, answer, new FirebaseExerciseModel.TransactionCallback() {
            @Override
            public void onTransactionSuccess() {
                FirebaseExerciseModel.changeExerciseState(num_chapter, exercise, DONE, new FirebaseExerciseModel.TransactionCallback() {
                    @Override
                    public void onTransactionSuccess() {
                        //callback
                        if (listener != null) {
                            listener.onSaveResponseSuccess();
                        }
                    }
                    @Override
                    public void onTransactionFailure() {
                        // failure callback
                        if (listener != null) {
                            listener.onSaveResponseFailure();
                        }
                    }
                });
            }
            @Override
            public void onTransactionFailure() {
                // failure callback
                if (listener != null) {
                    listener.onSaveResponseFailure();
                }
            }
        });
    }

    private void wordsSequencesRepetitionSaveResponse(OnSaveResponseListener listener){
        //Salvo la risposta e l'audio nel database
        String answer = "answer_"+exercise.getAudio();
        String pathRef = Patient.getInstance().getId() + "/" + num_chapter + "/" + answer;
        FirestoreModel.uploadAudio(pathRef,outputFile, new FirestoreModel.UploadCallback() {
            @Override
            public void onUploadSuccess() {
                FirebaseExerciseModel.putExerciseAnswer(num_chapter,exercise,TYPE_DATA_ANSWER, answer, new FirebaseExerciseModel.TransactionCallback() {
                    @Override
                    public void onTransactionSuccess() {
                        //Aggiorno l'esercizio
                        FirebaseExerciseModel.changeExerciseState(num_chapter, exercise, DONE, new FirebaseExerciseModel.TransactionCallback() {
                            @Override
                            public void onTransactionSuccess() {
                                //callback
                                if (listener != null) {
                                    listener.onSaveResponseSuccess();
                                }
                            }
                            @Override
                            public void onTransactionFailure() {
                                // failure callback
                                if (listener != null) {
                                    listener.onSaveResponseFailure();
                                }
                            }
                        });
                    }
                    @Override
                    public void onTransactionFailure() {
                        // failure callback
                        if (listener != null) {
                            listener.onSaveResponseFailure();
                        }
                    }
                });
            }

            @Override
            public void onUploadFailure() {
                // failure callback
                if (listener != null) {
                    listener.onSaveResponseFailure();
                }
            }
        });
    }

    private void sendNotification(){
        PatientFirebaseModel.getParentToken(Patient.getInstance().getId(), new PatientFirebaseModel.TokenListener() {
            @Override
            public void onTokenReceived(String parentToken) {
                FCMClient fcmClient = new FCMClient();
                FCMNotification notification = new FCMNotification(getString(R.string.Notification), getString(R.string.ExerciseCompleteDescription));
                FCMRequest fcmRequest = new FCMRequest(parentToken, notification);
                fcmClient.sendNotification(fcmRequest);
            }

            @Override
            public void onTokenReceiveFailed() {}
        });

    }

}