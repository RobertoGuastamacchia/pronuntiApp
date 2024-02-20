package it.uniba.dib.sms2324FF2.patient.child.homepage;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import it.uniba.dib.sms2324FF2.R;
import it.uniba.dib.sms2324FF2.firestore.FirestoreModel;
import it.uniba.dib.sms2324FF2.patient.PatientFirebaseModel;
import it.uniba.dib.sms2324FF2.patient.child.exercises.Exercise;
import it.uniba.dib.sms2324FF2.patient.child.exercises.FirebaseExerciseModel;
import it.uniba.dib.sms2324FF2.patient.child.PathPulseAnimation;
import it.uniba.dib.sms2324FF2.patient.child.homepage.exercises.ImageDenominationFragment;
import it.uniba.dib.sms2324FF2.patient.child.homepage.exercises.MinimalPairsRecognitionFragment;
import it.uniba.dib.sms2324FF2.patient.child.homepage.exercises.WordsSequencesRepetitionFragment;
import it.uniba.dib.sms2324FF2.patient.Patient;

public class ChildHomePageFragment extends Fragment {

    private onHomePageListener mCallback;
    private Context context;
    private String theme, themePersonalized, principalCharacter;
    private float dX, dY;
    private float minX, minY, maxX, maxY;
    ImageView principalCharacterImg;
    RelativeLayout principalLayout;
    ArrayList<ImageButton> imageButtonList;
    ImageButton imageExerciseToDo;
    Exercise ExerciseToDo;
    int chapterExerciseToDo;
    LinearLayout.LayoutParams layoutExerciseToDo;
    TextView coinTxt;
    private int num_chapter, currentChapter;
    private boolean isPulsating = false;
    private PathPulseAnimation pathPulseAnimation;
    private LinearLayout linearLayout;
    private boolean isPortrait = true, isDialogOpen = false;
    private TreeMap<ArrayList<Object>, ArrayList<Exercise>> exercises;

    public ChildHomePageFragment(TreeMap<ArrayList<Object>, ArrayList<Exercise>> exercises) {
        this.exercises = exercises;
    }

    public ChildHomePageFragment(boolean isPortrait, TreeMap<ArrayList<Object>, ArrayList<Exercise>> exercises) {
        this.exercises = exercises;
        this.isPortrait = isPortrait;
    }

    public interface onHomePageListener {
        void refreshHomePageData();

        void showBottomBar();

        void hideBottomBar();

        void setToolbar(Context context, Toolbar toolbar);

        void goBack();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof onHomePageListener) {
            mCallback = (onHomePageListener) context;
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
        //QUESTO CONTROLLO SERVE PER VEDERE SE LO SCHERMO E' ORIZZONTALE O VERTICALE
        //PER CAPIRE QUALE LAYOUT CARICARE
        View view;
        if (isPortrait && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            view = inflater.inflate(R.layout.fragment_home_page_child, container, false);
            mCallback.showBottomBar();
        } else {
            view = inflater.inflate(R.layout.landscape_layout, container, false);
            mCallback.hideBottomBar();
        }

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        mCallback.setToolbar(context, toolbar);
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (isPortrait && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mCallback.showBottomBar();
            //Gestisco il refresh
            SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    mCallback.refreshHomePageData();
                }
            });

            //mi assicuro che la bottom bar sia visibile
            mCallback.showBottomBar();

            //inserisco nello schermo i coin del bambino
            int coin = Patient.getInstance().getCoin(); // = prendo dal DB i coin
            coinTxt = view.findViewById(R.id.coin);
            coinTxt.setText(String.valueOf(coin));

            if (exercises != null) {

                ImageButton weeklyGoals = view.findViewById(R.id.goals);
                weeklyGoals.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MediaPlayer.create(getActivity(), R.raw.button_click_sound).start();
                        //lancio il popup che mostra gli obieetivi settimanali
                        showWeeklyGoalsDialog(exercises);
                    }
                });

                //"disegno" il percorso di gioco degli esercizi
                setGameMap(view, exercises);
            }

        }
    }

    //popup per mostrare gli incarichi settimanali
    private void showWeeklyGoalsDialog(TreeMap<ArrayList<Object>, ArrayList<Exercise>> exercises) {
        //Ottengo gli esercizi del capitolo corrente

        FirebaseExerciseModel.getExercisesForChapter(Patient.getInstance().getId(), currentChapter, new FirebaseExerciseModel.OnExercisesRead() {
            @Override
            public void onSuccess(ArrayList<Exercise> currentExercises, int n) {
                //Creo il dialog e ci faccio inflate del layout
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                LayoutInflater inflater = requireActivity().getLayoutInflater();
                View customView = inflater.inflate(R.layout.weekly_goals_dialog, null);
                builder.setView(customView);
                AlertDialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.dialog_rounded_corner_gray));

                // Blocca l'orientamento in portrait quando la finestra di dialogo è aperta
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

                //Ottengo il riferimento a tutti gli oggetti
                View firstGoalRelativeLayout = customView.findViewById(R.id.firstGoalRelativeLayout);
                TextView firstGoalCoin = customView.findViewById(R.id.firstGoalCoin);
                ImageView firstGoalSymbolCoin = customView.findViewById(R.id.firstGoalSymbolCoin);
                ImageView firstGoalSymbolDone = customView.findViewById(R.id.firstGoalSymbolDone);
                ProgressBar firstGoalProgressBar = customView.findViewById(R.id.firstGoalProgressBar);

                View secondGoalRelativeLayout = customView.findViewById(R.id.secondGoalRelativeLayout);
                TextView secondGoalCoin = customView.findViewById(R.id.secondGoalCoin);
                ImageView secondGoalSymbolCoin = customView.findViewById(R.id.secondGoalSymbolCoin);
                ImageView secondGoalSymbolDone = customView.findViewById(R.id.secondGoalSymbolDone);
                ProgressBar secondGoalProgressBar = customView.findViewById(R.id.secondGoalProgressBar);

                View thirdGoalRelativeLayout = customView.findViewById(R.id.thirdGoalRelativeLayout);
                TextView thirdGoalCoin = customView.findViewById(R.id.thirdGoalCoin);
                ImageView thirdGoalSymbolCoin = customView.findViewById(R.id.thirdGoalSymbolCoin);
                ImageView thirdGoalSymbolDone = customView.findViewById(R.id.thirdGoalSymbolDone);
                ProgressBar thirdGoalProgressBar = customView.findViewById(R.id.thirdGoalProgressBar);

                View fourthGoalRelativeLayout = customView.findViewById(R.id.fourthGoalRelativeLayout);
                TextView fourthGoalCoin = customView.findViewById(R.id.fourthGoalCoin);
                ImageView fourthGoalSymbolCoin = customView.findViewById(R.id.fourthGoalSymbolCoin);
                ImageView fourthGoalSymbolDone = customView.findViewById(R.id.fourthGoalSymbolDone);
                ProgressBar fourthGoalProgressBar = customView.findViewById(R.id.fourthGoalProgressBar);

                TextView fifthGoalCoin = customView.findViewById(R.id.fifthGoalCoin);
                ImageView fifthGoalSymbolCoin = customView.findViewById(R.id.fifthGoalSymbolCoin);
                ImageView fifthGoalSymbolDone = customView.findViewById(R.id.fifthGoalSymbolDone);
                ProgressBar fifthGoalProgressBar = customView.findViewById(R.id.fifthGoalProgressBar);


                //Logica di controllo degli incarichi da mostrare in base al num di esercizi della settimana
                switch (currentExercises.size()) {
                    case 1:
                        secondGoalRelativeLayout.setVisibility(View.GONE);
                        secondGoalProgressBar.setVisibility(View.GONE);
                        thirdGoalRelativeLayout.setVisibility(View.GONE);
                        thirdGoalProgressBar.setVisibility(View.GONE);
                        fourthGoalRelativeLayout.setVisibility(View.GONE);
                        fourthGoalProgressBar.setVisibility(View.GONE);
                        break;
                    case 2:
                    case 3:
                        thirdGoalRelativeLayout.setVisibility(View.GONE);
                        thirdGoalProgressBar.setVisibility(View.GONE);
                        fourthGoalRelativeLayout.setVisibility(View.GONE);
                        fourthGoalProgressBar.setVisibility(View.GONE);
                        break;
                    case 4:
                    case 5:
                        fourthGoalRelativeLayout.setVisibility(View.GONE);
                        fourthGoalProgressBar.setVisibility(View.GONE);
                        break;

                }

                //Gestisco le barre di avanzamento
                int counterRight = 0;
                int counterDone = 0;
                for (Exercise ex : currentExercises) {
                    if (!ex.getState().equals("to-do") && !ex.getState().equals("locked"))
                        counterDone++;
                    if (ex.isExerciseRight()) {
                        counterRight++;
                    }
                }
                if (currentExercises.size() != 0)
                    firstGoalRelativeLayout.setVisibility(View.VISIBLE);
                if (counterDone != 0) {
                    firstGoalProgressBar.setProgress(100);
                    firstGoalSymbolCoin.setVisibility(View.INVISIBLE);
                    firstGoalSymbolDone.setVisibility(View.VISIBLE);
                    firstGoalCoin.setVisibility(View.INVISIBLE);
                }
                int lastPercent;
                switch (counterRight) {
                    case 1:
                        if (secondGoalProgressBar.getVisibility() != View.GONE) {
                            secondGoalProgressBar.setProgress(100);
                            secondGoalSymbolCoin.setVisibility(View.INVISIBLE);
                            secondGoalSymbolDone.setVisibility(View.VISIBLE);
                            secondGoalCoin.setVisibility(View.INVISIBLE);
                        }
                        if (thirdGoalProgressBar.getVisibility() != View.GONE)
                            thirdGoalProgressBar.setProgress(33);
                        if (fourthGoalProgressBar.getVisibility() != View.GONE)
                            fourthGoalProgressBar.setProgress(20);
                        if (fifthGoalProgressBar.getVisibility() != View.GONE) {
                            lastPercent = lastPercentCalculator(1, currentExercises.size());
                            fifthGoalProgressBar.setProgress(lastPercent);
                            if (lastPercent == 100) {
                                fifthGoalSymbolCoin.setVisibility(View.INVISIBLE);
                                fifthGoalSymbolDone.setVisibility(View.VISIBLE);
                                fifthGoalCoin.setVisibility(View.INVISIBLE);
                            }
                        }
                        break;
                    case 2:
                        if (secondGoalProgressBar.getVisibility() != View.GONE) {
                            secondGoalProgressBar.setProgress(100);
                            secondGoalSymbolCoin.setVisibility(View.INVISIBLE);
                            secondGoalSymbolDone.setVisibility(View.VISIBLE);
                            secondGoalCoin.setVisibility(View.INVISIBLE);
                        }
                        if (thirdGoalProgressBar.getVisibility() != View.GONE)
                            thirdGoalProgressBar.setProgress(66);
                        if (fourthGoalProgressBar.getVisibility() != View.GONE)
                            fourthGoalProgressBar.setProgress(40);
                        if (fifthGoalProgressBar.getVisibility() != View.GONE) {
                            lastPercent = lastPercentCalculator(2, currentExercises.size());
                            fifthGoalProgressBar.setProgress(lastPercent);
                            if (lastPercent == 100) {
                                fifthGoalSymbolCoin.setVisibility(View.INVISIBLE);
                                fifthGoalSymbolDone.setVisibility(View.VISIBLE);
                                fifthGoalCoin.setVisibility(View.INVISIBLE);
                            }
                        }
                        break;
                    case 3:
                        if (secondGoalProgressBar.getVisibility() != View.GONE) {
                            secondGoalProgressBar.setProgress(100);
                            secondGoalSymbolCoin.setVisibility(View.INVISIBLE);
                            secondGoalSymbolDone.setVisibility(View.VISIBLE);
                            secondGoalCoin.setVisibility(View.INVISIBLE);
                        }
                        if (thirdGoalProgressBar.getVisibility() != View.GONE) {
                            thirdGoalProgressBar.setProgress(100);
                            thirdGoalSymbolCoin.setVisibility(View.INVISIBLE);
                            thirdGoalSymbolDone.setVisibility(View.VISIBLE);
                            thirdGoalCoin.setVisibility(View.INVISIBLE);
                        }
                        if (fourthGoalProgressBar.getVisibility() != View.GONE)
                            fourthGoalProgressBar.setProgress(60);
                        if (fifthGoalProgressBar.getVisibility() != View.GONE) {
                            lastPercent = lastPercentCalculator(3, currentExercises.size());
                            fifthGoalProgressBar.setProgress(lastPercent);
                            if (lastPercent == 100) {
                                fifthGoalSymbolCoin.setVisibility(View.INVISIBLE);
                                fifthGoalSymbolDone.setVisibility(View.VISIBLE);
                                fifthGoalCoin.setVisibility(View.INVISIBLE);
                            }
                        }
                        break;
                    case 4:
                        if (secondGoalProgressBar.getVisibility() != View.GONE) {
                            secondGoalProgressBar.setProgress(100);
                            secondGoalSymbolCoin.setVisibility(View.INVISIBLE);
                            secondGoalSymbolDone.setVisibility(View.VISIBLE);
                            secondGoalCoin.setVisibility(View.INVISIBLE);
                        }
                        if (thirdGoalProgressBar.getVisibility() != View.GONE) {
                            thirdGoalProgressBar.setProgress(100);
                            thirdGoalSymbolCoin.setVisibility(View.INVISIBLE);
                            thirdGoalSymbolDone.setVisibility(View.VISIBLE);
                            thirdGoalCoin.setVisibility(View.INVISIBLE);
                        }
                        if (fourthGoalProgressBar.getVisibility() != View.GONE)
                            fourthGoalProgressBar.setProgress(80);
                        if (fifthGoalProgressBar.getVisibility() != View.GONE) {
                            lastPercent = lastPercentCalculator(4, currentExercises.size());
                            fifthGoalProgressBar.setProgress(lastPercent);
                            if (lastPercent == 100) {
                                fifthGoalSymbolCoin.setVisibility(View.INVISIBLE);
                                fifthGoalSymbolDone.setVisibility(View.VISIBLE);
                                fifthGoalCoin.setVisibility(View.INVISIBLE);
                            }
                        }
                        break;
                    case 5:
                        if (secondGoalProgressBar.getVisibility() != View.GONE) {
                            secondGoalProgressBar.setProgress(100);
                            secondGoalSymbolCoin.setVisibility(View.INVISIBLE);
                            secondGoalSymbolDone.setVisibility(View.VISIBLE);
                            secondGoalCoin.setVisibility(View.INVISIBLE);
                        }
                        if (thirdGoalProgressBar.getVisibility() != View.GONE) {
                            thirdGoalProgressBar.setProgress(100);
                            thirdGoalSymbolCoin.setVisibility(View.INVISIBLE);
                            thirdGoalSymbolDone.setVisibility(View.VISIBLE);
                            thirdGoalCoin.setVisibility(View.INVISIBLE);
                        }
                        if (fourthGoalProgressBar.getVisibility() != View.GONE) {
                            fourthGoalProgressBar.setProgress(100);
                            fourthGoalSymbolCoin.setVisibility(View.INVISIBLE);
                            fourthGoalSymbolDone.setVisibility(View.VISIBLE);
                            fourthGoalCoin.setVisibility(View.INVISIBLE);
                        }
                        if (fifthGoalProgressBar.getVisibility() != View.GONE) {
                            lastPercent = lastPercentCalculator(5, currentExercises.size());
                            fifthGoalProgressBar.setProgress(lastPercent);
                            if (lastPercent == 100) {
                                fifthGoalSymbolCoin.setVisibility(View.INVISIBLE);
                                fifthGoalSymbolDone.setVisibility(View.VISIBLE);
                                fifthGoalCoin.setVisibility(View.INVISIBLE);
                            }
                        }
                        break;
                    case 6:
                        if (secondGoalProgressBar.getVisibility() != View.GONE) {
                            secondGoalProgressBar.setProgress(100);
                            secondGoalSymbolCoin.setVisibility(View.INVISIBLE);
                            secondGoalSymbolDone.setVisibility(View.VISIBLE);
                            secondGoalCoin.setVisibility(View.INVISIBLE);
                        }
                        if (thirdGoalProgressBar.getVisibility() != View.GONE) {
                            thirdGoalProgressBar.setProgress(100);
                            thirdGoalSymbolCoin.setVisibility(View.INVISIBLE);
                            thirdGoalSymbolDone.setVisibility(View.VISIBLE);
                            thirdGoalCoin.setVisibility(View.INVISIBLE);
                        }
                        if (fourthGoalProgressBar.getVisibility() != View.GONE) {
                            fourthGoalProgressBar.setProgress(100);
                            fourthGoalSymbolCoin.setVisibility(View.INVISIBLE);
                            fourthGoalSymbolDone.setVisibility(View.VISIBLE);
                            fourthGoalCoin.setVisibility(View.INVISIBLE);
                        }
                        if (fifthGoalProgressBar.getVisibility() != View.GONE) {
                            lastPercent = lastPercentCalculator(6, currentExercises.size());
                            fifthGoalProgressBar.setProgress(lastPercent);
                            if (lastPercent == 100) {
                                fifthGoalSymbolCoin.setVisibility(View.INVISIBLE);
                                fifthGoalSymbolDone.setVisibility(View.VISIBLE);
                                fifthGoalCoin.setVisibility(View.INVISIBLE);
                            }
                        }
                        break;
                    case 7:
                        if (secondGoalProgressBar.getVisibility() != View.GONE) {
                            secondGoalProgressBar.setProgress(100);
                            secondGoalSymbolCoin.setVisibility(View.INVISIBLE);
                            secondGoalSymbolDone.setVisibility(View.VISIBLE);
                            secondGoalCoin.setVisibility(View.INVISIBLE);
                        }
                        if (thirdGoalProgressBar.getVisibility() != View.GONE) {
                            thirdGoalProgressBar.setProgress(100);
                            thirdGoalSymbolCoin.setVisibility(View.INVISIBLE);
                            thirdGoalSymbolDone.setVisibility(View.VISIBLE);
                            thirdGoalCoin.setVisibility(View.INVISIBLE);
                        }
                        if (fourthGoalProgressBar.getVisibility() != View.GONE) {
                            fourthGoalProgressBar.setProgress(100);
                            fourthGoalSymbolCoin.setVisibility(View.INVISIBLE);
                            fourthGoalSymbolDone.setVisibility(View.VISIBLE);
                            fourthGoalCoin.setVisibility(View.INVISIBLE);
                        }
                        if (fifthGoalProgressBar.getVisibility() != View.GONE) {
                            fifthGoalProgressBar.setProgress(100);
                            fifthGoalSymbolCoin.setVisibility(View.INVISIBLE);
                            fifthGoalSymbolDone.setVisibility(View.VISIBLE);
                            fifthGoalCoin.setVisibility(View.INVISIBLE);
                        }
                        break;
                }

                //Chiudo il dialog
                Button closeButton = customView.findViewById(R.id.closeButton);
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Azioni per il pulsante "No"
                        // Chiudi la finestra di dialogo
                        dialog.dismiss();
                        isDialogOpen = false;
                        // Ripristina l'orientamento predefinito
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    }
                });

                //Mostro il dialog
                if (isDialogOpen) {
                    // Evita di aprire il dialog se è già aperto
                    return;
                }
                isDialogOpen = true;
                dialog.show();
            }

            @Override
            public void onFailure() {

            }
        });
    }


    private int lastPercentCalculator(float value, int tot) {
        return Math.round((value / tot) * 100);
    }

    //metodo per "disegnare" sullo schermo il percorso di gioco degli esercizi
    void setGameMap(View view, TreeMap<ArrayList<Object>, ArrayList<Exercise>> exercises) {
        PatientFirebaseModel.getPatientPrincipalCharacters(new PatientFirebaseModel.OnCharactersListener<HashMap<String, String>>() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onCharactersRead(HashMap<String, String> principalCharacters) {
                //imposto il tema
                setTheme(view);
                //ottengo i riferimenti
                linearLayout = view.findViewById(R.id.gameMap);
                //leggo il capitolo corrente
                for (Map.Entry<ArrayList<Object>, ArrayList<Exercise>> entry : exercises.entrySet()) {
                    ArrayList<Exercise> exercises = entry.getValue();
                    ArrayList<Object> exercisesData = entry.getKey();
                    if (exercisesData.get(1).equals("to-do"))
                        currentChapter = (Integer) exercisesData.get(0);
                    num_chapter = (Integer) exercisesData.get(0);


                    // Setta il titolo
                    TextView title = new TextView(context);
                    title.setText(new StringBuilder().append(getString(R.string.chapter)).append(" ").append(String.valueOf(num_chapter)).toString());
                    title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.title_dimen)); // Imposta la dimensione del testo
                    Typeface bubblegumSansTypeface = ResourcesCompat.getFont(requireContext(), R.font.bubblegum_sans); //imposta il font
                    title.setTypeface(bubblegumSansTypeface, Typeface.BOLD);
                    // Setta il colore
                    title.setTextColor(getActivity().getColor(R.color.characterLockedColorTextAndBackground));
                    // Setta l'allineamento del testo
                    title.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                    linearLayout.addView(title);

                    // Calcola la distanza desiderata tra i bottoni
                    int marginBetweenButtons = 70;

                    // Imposta le dimensioni desiderate in pixel
                    int widthInPixels = 300;
                    int heightInPixels = 300;

                    // Imposta la scala dell'immagine per adattarla alle dimensioni specificate
                    ImageView.ScaleType scaleType = ImageView.ScaleType.FIT_CENTER;

                    boolean isLeftAligned = true;

                    //Array di esercizi per capitolo
                    imageButtonList = new ArrayList<>();

                    //per ogni esercizio letto dal DB
                    boolean isToDo;
                    int counter = 0;

                    for (int i = 0; i <= exercises.size() - 1; i++) {

                        Exercise exercise = exercises.get(i);
                        ImageButton imageButton = new ImageButton(context);
                        imageButton.setBackground(new ColorDrawable(Color.TRANSPARENT));
                        imageButton.setColorFilter(getActivity().getColor(R.color.characterLockedColorImage));


                        //stampo il corretto button e mi occupo della gestione di esso
                        isToDo = setButton(num_chapter, imageButton, exercise, view, linearLayout);

                        // Imposta le dimensioni desiderate in pixel
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(widthInPixels, heightInPixels);
                        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;

                        //aggiungi margine maggiore tra il primo button ed il titolo
                        if (i == 0) {
                            float scale = getResources().getDisplayMetrics().density;
                            int marginInDp = (int) (40 * scale + 0.5f); // 20dp convertiti in pixel
                            layoutParams.topMargin = marginInDp;
                        }

                        // Aggiungi la distanza desiderata tra i bottoni
                        if (isLeftAligned) {
                            layoutParams.rightMargin = marginBetweenButtons;
                        } else {
                            layoutParams.leftMargin = marginBetweenButtons;
                        }

                        // Imposta i parametri di layout per l'ImageButton
                        imageButton.setLayoutParams(layoutParams);

                        if (isToDo && counter == 0) {
                            imageExerciseToDo = imageButton;
                            layoutExerciseToDo = layoutParams;
                            ExerciseToDo = exercise;
                            chapterExerciseToDo = num_chapter;
                            counter++;
                        }

                        // Imposta la scala dell'immagine
                        imageButton.setScaleType(scaleType);

                        linearLayout.addView(imageButton);

                        imageButtonList.add(imageButton); //salvo il button nella lista per l'appearance del chapter

                        // Inverti la direzione per il prossimo ImageButton
                        isLeftAligned = !isLeftAligned;

                    }

                    //imposto l'appearance corretta per i capitoli sbloccati
                    if (!exercisesData.get(1).equals("locked")) {
                        setChapterAppearance(title, imageButtonList);
                    }

                    // aggiunta di una distanza tra un capitolo e l'altro
                    if (exercisesData.size() - num_chapter != 0) {
                        TextView blankTextView = new TextView(context);
                        //4 spazi vuoti di distanza
                        blankTextView.setText("\n\n\n\n");
                        linearLayout.addView(blankTextView);
                    }
                }
                //Aggiungo il personaggio
                principalLayout = view.findViewById(R.id.principalLayout);

                String name = null;
                int drawableId = 0;
                for (Map.Entry<String, String> character : principalCharacters.entrySet()) {
                    if (character.getKey().equals(theme)) {
                        principalCharacter = character.getValue().toLowerCase();
                        name = theme + "_" + principalCharacter;
                        drawableId = getResources().getIdentifier(name, "drawable", context.getPackageName());
                    }
                }
                //Creo la view per il personaggio
                principalCharacterImg = new ImageView(context);
                principalCharacterImg.setImageResource(drawableId);
                float density = getResources().getDisplayMetrics().density;
                principalCharacterImg.setLayoutParams(new RelativeLayout.LayoutParams((int) ((100 * density)), (int) ((100 * density))));
                principalCharacterImg.setBackground(getResources().getDrawable(R.drawable.circle_background));
                //La aggiungo al layout
                principalLayout.addView(principalCharacterImg);
                //Definisco la posizione sullo schermo
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) principalCharacterImg.getLayoutParams();
                layoutParams.topMargin = 900;
                layoutParams.leftMargin = 80;
                principalCharacterImg.requestLayout();
                //Faccio pulsare il personaggio
                startPulsating(principalCharacterImg);

                // Aggiungo un OnTouchListener all'ImageView per gestire gli eventi di tocco e trascinamento
                principalCharacterImg.setOnTouchListener(new View.OnTouchListener() {

                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                stopPulsating();
                                if (imageExerciseToDo != null) {
                                    startPulsating(imageExerciseToDo);
                                }
                                // Salva le coordinate iniziali del tocco
                                dX = view.getX() - event.getRawX();
                                dY = view.getY() - event.getRawY();
                                // Imposta i limiti in base alle dimensioni del LinearLayout interno
                                int margin = 270;
                                minX = 0;
                                minY = margin;
                                maxX = linearLayout.getWidth() - principalCharacterImg.getWidth();
                                maxY = linearLayout.getHeight() - principalCharacterImg.getHeight() + margin;
                                break;

                            case MotionEvent.ACTION_MOVE:
                                float newX = event.getRawX() + dX;
                                float newY = event.getRawY() + dY;

                                // Applica i limiti alle nuove coordinate
                                newX = Math.max(minX, Math.min(newX, maxX));
                                newY = Math.max(minY, Math.min(newY, maxY));

                                // Aggiorna le coordinate della View durante lo spostamento
                                view.animate()
                                        .x(newX)
                                        .y(newY)
                                        .setDuration(0)
                                        .start();
                                // Controlla la collisione con gli ImageButton
                                if (imageExerciseToDo != null)
                                    checkCollisionWithButtons(view,false);

                                break;

                            case MotionEvent.ACTION_UP:
                                // Gestisci l'evento di rilascio qui
                                if (imageExerciseToDo != null)
                                    checkCollisionWithButtons(view,true);
                                break;

                            default:
                                return false;
                        }
                        return true;
                    }
                });
            }

            @Override
            public void onCharactersReadFailure(HashMap<String, String> principalCharacters) {
            }
        });
    }

    // Funzione per controllare la collisione con gli ImageButton
    private void checkCollisionWithButtons(View characterView,boolean run) {

        int[] location1 = new int[2];
        int[] location2 = new int[2];

        characterView.getLocationOnScreen(location1);
        imageExerciseToDo.getLocationOnScreen(location2);

        Rect characterRect = new Rect(location1[0], location1[1], location1[0] + characterView.getWidth(), location1[1] + characterView.getHeight());
        Rect buttonRect = new Rect(location2[0], location2[1], location2[0] + imageExerciseToDo.getWidth(), location2[1] + imageExerciseToDo.getHeight());

       // margin = (int) (80 * density); // Imposta il valore del margine
        //buttonRect.right += margin;
        // Controlla la collisione
        if (Rect.intersects(characterRect, buttonRect)) {
            // Sovrapposizione tra il personaggio e il pulsante
            // Esegui l'azione desiderata
            handleCollisionWithButton(imageExerciseToDo);
            if(run)
                handleRelease();
        } else {
            // Non c'è collisione
            handleNonCollisionWithButton(imageExerciseToDo);
        }

    }

    // Funzione per gestire l'azione quando c'è una collisione con un pulsante
    private void handleCollisionWithButton(ImageButton imageButton) {
        // Esegui l'azione desiderata quando il personaggio e il pulsante si sovrappongono
        //ingrandisco il bottone
        float density = getResources().getDisplayMetrics().density;
        layoutExerciseToDo.width = (int) (100 * density);
        layoutExerciseToDo.height = (int) (100 * density);
        imageButton.setLayoutParams(layoutExerciseToDo);
        //Setto il contorno verde
        String name = "circle_background_" + theme;
        int drawableId = getResources().getIdentifier(name, "drawable", context.getPackageName());
        imageButton.setBackgroundResource(drawableId);


    }

    private void handleNonCollisionWithButton(ImageButton imageButton) {
        // Esegui azioni quando non c'è collisione con il pulsante
        //Ripristino la dimensione
        layoutExerciseToDo.width = 300;
        layoutExerciseToDo.height = 300;
        imageButton.setLayoutParams(layoutExerciseToDo);
        //Ripristino il contorno
        imageButton.setBackground(null);
    }

    private boolean isCollision(View view1, View view2) {
        Rect rect1 = new Rect();
        view1.getHitRect(rect1);

        Rect rect2 = new Rect();
        view2.getHitRect(rect2);

        return Rect.intersects(rect1, rect2);
    }

    private void handleRelease() {
        // Controlla se c'è una collisione con l'immagine della collisione
            //riproduco suono
            MediaPlayer startexercise = MediaPlayer.create(getActivity(), R.raw.start_exercise);
            startexercise.start();

            //nascondo la bottom bar
            mCallback.hideBottomBar();


            //fermo l'animazione di pulsazione
            stopPulsating();

            //inserisco il fragment corretto in base al tipo di esercizio da svolgere
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            switch (ExerciseToDo.getType()) {
                case 1:
                    ft.replace(R.id.frameLayout, new ImageDenominationFragment(chapterExerciseToDo, ExerciseToDo));
                    break;
                case 2:
                    ft.replace(R.id.frameLayout, new MinimalPairsRecognitionFragment(chapterExerciseToDo, ExerciseToDo));
                    break;
                case 3:
                    ft.replace(R.id.frameLayout, new WordsSequencesRepetitionFragment(chapterExerciseToDo, ExerciseToDo));
                    break;
            }

            ft.commit();
    }

    //metodo per mostrare popup degli esercizi ancora bloccati
    private void showPopup(View anchorView) {
        View popupView = getLayoutInflater().inflate(R.layout.locked_exercise_popup, null);

        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        // Mostra il popup attaccato al pulsante
        popupWindow.showAsDropDown(anchorView, 0, -anchorView.getHeight());

        Button closeButton = popupView.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }

    //metodo per mostrare popup per informare l'utente su come far partire l'esercizio
    private void showPopupExerciseToDo(View anchorView) {

        View popupView = getLayoutInflater().inflate(R.layout.todo_exercise_popup, null);
        TextView storyDescription = popupView.findViewById(R.id.storyDescription);
        String name = theme + "_story";
        int stringId = getResources().getIdentifier(name, "string", context.getPackageName());
        storyDescription.setText(stringId);


        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        // Mostra il popup attaccato al pulsante
        popupWindow.showAsDropDown(anchorView, 0, -anchorView.getHeight());

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button closeButton = popupView.findViewById(R.id.closeButton);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }

    //mostro un popup per informare l'utente che l'esercizio è in attesa di correzione
    private void showPopupWaitForCorrection(View ancorView) {
        View popupView = getLayoutInflater().inflate(R.layout.waiting_for_correction_popup_dialog, null);

        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        // Mostra il popup attaccato al pulsante
        popupWindow.showAsDropDown(ancorView, 0, -ancorView.getHeight());


        Button closeButton = popupView.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }


    //metodo per gestire la corretta stampa ed aggiunta dei listener per gli opportuni button
    //gestendo la diversità dei tipi di esercizi
    private boolean setButton(int numChapter, ImageButton imageButton, Exercise exercise, View view, LinearLayout linearLayout) {

        boolean isToDo = false;
        //state = locked -> esercizio futuro ancora bloccato
        //state = corrected -> esercizio già fatto e corretto (feedback se giusto o sbagliato)
        //state = to-do -> esercizio disponibile
        //state = done -> esercizio già fatto ma non ancora corretto
        switch (exercise.getState()) {
            //esercizio bloccato non ancora disponibile
            case "locked":
                imageButton.setImageResource(R.drawable.play_button_locked);
                //gestisco il click su un esercizio bloccato per un messaggio informativo
                imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MediaPlayer.create(getActivity(), R.raw.button_click_sound).start();
                        showPopup(imageButton);
                    }
                });
                break;

            //esercizio svolto e corretto
            case "corrected":

                //verifico se l'esercizio è giusto o sbagliato e carico
                //l'immagine corretta
                if (exercise.isExerciseRight()) {
                    //esercizio esatto
                    imageButton.setImageResource(R.drawable.completed_button);
                } else {
                    //esercizio sbagliato
                    imageButton.setImageResource(R.drawable.wrong_exercise);
                }
                break;

            //esercizio disponibile non ancora svolto
            case "to-do":
                String name = "start_game_" + theme;
                int drawableId = getResources().getIdentifier(name, "drawable", context.getPackageName());
                imageButton.setImageResource(drawableId);
                isToDo = true;
                imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showPopupExerciseToDo(imageButton);
                    }
                });

                break;

            //esercizio svolto in attesa di correzione
            case "done":

                imageButton.setImageResource(R.drawable.waiting_correction);
                //mostro un popup informativo all'utente
                imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MediaPlayer.create(getActivity(), R.raw.button_click_sound).start();
                        showPopupWaitForCorrection(imageButton);
                    }
                });

                break;
        }
        return isToDo;
    }

    //metodi per creare un'animazione a pulsazione
    private void startPulsating(View imageView) {
        if (!isPulsating) {
            isPulsating = true;
            pathPulseAnimation = new PathPulseAnimation(imageView);
            pathPulseAnimation.start();
        }
    }

    private void stopPulsating() {
        isPulsating = false;
        if (pathPulseAnimation != null) {
            pathPulseAnimation.cancel();
        }
    }

    void setChapterAppearance(TextView title, ArrayList<ImageButton> images) {
        title.setTextColor(getActivity().getColor(R.color.white));
        for (ImageView image : images) {
            image.setColorFilter(null);
        }
    }

    void setTheme(View view) {
        //ottengo il tema del bambino e inserisco nella home immagini del tema
        theme = Patient.getInstance().getTheme();
        if (theme.contains("_personalized")) {
            themePersonalized = theme;
            theme = theme.replace("_personalized", "");
        }

        ImageView firstThemeImage;
        ImageView secondThemeImage;
        ImageView thirdThemeImage;
        ImageView fourthThemeImage;
        RelativeLayout.LayoutParams params1;
        RelativeLayout.LayoutParams params2;
        RelativeLayout.LayoutParams params3;
        RelativeLayout.LayoutParams params4;
        RelativeLayout relativeLayout;
        FrameLayout frameLayout = view.findViewById(R.id.gameFrameLayout);

        if (themePersonalized == null) {
            switch (theme) {
                case "mountain":

                    frameLayout.setBackgroundColor(Color.parseColor("#A2DEFF"));

                    //Creare un RelativeLayout
                    relativeLayout = new RelativeLayout(context);
                    relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.MATCH_PARENT));


                    // Terza immagine in basso a sinistra
                    thirdThemeImage = new ImageView(context);
                    thirdThemeImage.setImageResource(R.drawable.mountain_three);
                    params3 = new RelativeLayout.LayoutParams(1500, 410);
                    params3.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    params3.addRule(RelativeLayout.ALIGN_PARENT_START);
                    thirdThemeImage.setLayoutParams(params3);
                    relativeLayout.addView(thirdThemeImage);


                    // Prima immagine in alto a sinistra
                    firstThemeImage = new ImageView(context);
                    firstThemeImage.setImageResource(R.drawable.mountain_one);
                    params1 = new RelativeLayout.LayoutParams(350, 350);
                    params1.addRule(RelativeLayout.ALIGN_PARENT_START);
                    params1.addRule(RelativeLayout.ABOVE, thirdThemeImage.getId());
                    firstThemeImage.setLayoutParams(params1);
                    relativeLayout.addView(firstThemeImage);

                    // Aggiungi il RelativeLayout al tuo layout principale
                    frameLayout.addView(relativeLayout);
                    frameLayout.postInvalidate();
                    break;

                case "desert":

                    frameLayout.setBackgroundColor(Color.parseColor("#F0DC82"));

                    //Creare un RelativeLayout
                    relativeLayout = new RelativeLayout(context);
                    relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.MATCH_PARENT));

                    // Terza immagine in basso a sinistra
                    thirdThemeImage = new ImageView(context);
                    thirdThemeImage.setImageResource(R.drawable.desert_three);
                    params3 = new RelativeLayout.LayoutParams(1500, 355);
                    params3.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    params3.addRule(RelativeLayout.ALIGN_PARENT_START);
                    thirdThemeImage.setLayoutParams(params3);
                    relativeLayout.addView(thirdThemeImage);


                    // Prima immagine in alto a sinistra
                    firstThemeImage = new ImageView(context);
                    firstThemeImage.setImageResource(R.drawable.desert_one);
                    params1 = new RelativeLayout.LayoutParams(300, 300);
                    params1.addRule(RelativeLayout.ALIGN_PARENT_START);
                    params1.addRule(RelativeLayout.ABOVE, thirdThemeImage.getId());
                    firstThemeImage.setLayoutParams(params1);
                    relativeLayout.addView(firstThemeImage);

                    // Aggiungi il RelativeLayout al tuo layout principale
                    frameLayout.addView(relativeLayout);
                    frameLayout.postInvalidate();
                    break;
                case "polar":

                    frameLayout.setBackgroundColor(Color.parseColor("#1E213D"));
                    coinTxt.setTextColor(Color.WHITE);

                    //Creare un RelativeLayout
                    relativeLayout = new RelativeLayout(context);
                    relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.MATCH_PARENT));

                    // Gif neve
                    secondThemeImage = new ImageView(context);
                    secondThemeImage.findViewById(R.id.gif_view);
                    Glide.with(this).load(R.drawable.polar_two).override(1200, 4000)
                            .into(secondThemeImage);

                    params2 = new RelativeLayout.LayoutParams(1200, 4000);
                    params2.addRule(RelativeLayout.ALIGN_PARENT_END);
                    secondThemeImage.setLayoutParams(params2);
                    relativeLayout.addView(secondThemeImage);


                    // Immagine in basso
                    fourthThemeImage = new ImageView(context);
                    fourthThemeImage.setImageResource(R.drawable.polar_three);
                    params4 = new RelativeLayout.LayoutParams(1500, 540);
                    params4.addRule(RelativeLayout.ALIGN_PARENT_END);
                    params4.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    fourthThemeImage.setLayoutParams(params4);
                    relativeLayout.addView(fourthThemeImage);

                    // Prima immagine in alto a sinistra
                    firstThemeImage = new ImageView(context);
                    firstThemeImage.setImageResource(R.drawable.polar_one);
                    params1 = new RelativeLayout.LayoutParams(500, 300);
                    params1.addRule(RelativeLayout.ALIGN_PARENT_START);
                    params1.addRule(RelativeLayout.ABOVE, fourthThemeImage.getId());
                    firstThemeImage.setLayoutParams(params1);
                    relativeLayout.addView(firstThemeImage);

                    // Prima immagine in alto a sinistra
                    firstThemeImage = new ImageView(context);
                    firstThemeImage.setScaleX(-1);
                    firstThemeImage.setImageResource(R.drawable.polar_one);
                    params1 = new RelativeLayout.LayoutParams(500, 300);
                    params1.addRule(RelativeLayout.ALIGN_PARENT_END);
                    params2.addRule(RelativeLayout.ABOVE, fourthThemeImage.getId());
                    firstThemeImage.setLayoutParams(params1);
                    relativeLayout.addView(firstThemeImage);

                    // Aggiungi il RelativeLayout al tuo layout principale
                    frameLayout.addView(relativeLayout);
                    frameLayout.postInvalidate();
                    break;

            }
        } else {
            //imposto lo sfondo dopo averlo scaricato
            StorageReference imgPath = FirebaseStorage.getInstance().getReference().child("Wallpapers/background.jpeg");
            FirestoreModel.downloadImage(imgPath, context, new FirestoreModel.DownloadImageCallback() {
                @Override
                public void onImageDownloaded(Uri imageUri) {
                    switch (themePersonalized) {
                        case "mountain_personalized":
                        case "desert_personalized":
                        case "polar_personalized":

                            //imposto lo sfondo
                            Bitmap bitmap = BitmapFactory.decodeFile(imageUri.getPath());
                            frameLayout.setBackground(new BitmapDrawable(getResources(), bitmap));

                            break;
                    }
                }

                @Override
                public void onDownloadFailure() {

                }
            });
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ChildHomePageFragment(false, exercises)).commit();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ChildHomePageFragment(true, exercises)).commit();
        }

    }


}