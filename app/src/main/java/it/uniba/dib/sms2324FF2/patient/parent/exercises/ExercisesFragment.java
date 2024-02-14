package it.uniba.dib.sms2324FF2.patient.parent.exercises;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import it.uniba.dib.sms2324FF2.R;
import it.uniba.dib.sms2324FF2.exercises.Exercise;
import it.uniba.dib.sms2324FF2.exercises.FirebaseExerciseModel;
import it.uniba.dib.sms2324FF2.patient.Patient;

public class ExercisesFragment extends Fragment {
    TreeMap<ArrayList<Object>, ArrayList<Exercise>> exercises;

    Context context;
    private onExercisesListener mCallback;

    private boolean isPortrait = true;

    //empty constructor
    public ExercisesFragment(TreeMap<ArrayList<Object>, ArrayList<Exercise>> exercises){
        this.exercises = exercises;
    }

    public ExercisesFragment(boolean isPortrait, TreeMap<ArrayList<Object>, ArrayList<Exercise>> exercises){
        this.isPortrait = isPortrait;
        this.exercises = exercises;
    }


    public interface onExercisesListener {
        void refreshExercisesData();
        void showBottomBar();
        void hideBottomBar();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof onExercisesListener) {
            mCallback = (onExercisesListener) context;
        } else {
            throw new RuntimeException(context + " must implement onExercisesListener");
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
            view = inflater.inflate(R.layout.fragment_excercises_parent, container, false);
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

            mCallback.showBottomBar(); //mi assicuro che la bottom bar sia visibile

            //Gestisco il refresh
            SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    mCallback.refreshExercisesData();
                }
            });

            if (exercises != null)
                //aggiungo gli esercizi alla schermata
                setExercise(view, exercises);
            else {
                FirebaseExerciseModel.getPatientExercises(Patient.getInstance().getId(),new FirebaseExerciseModel.OnAllExercisesCreatedListener() {
                    @Override
                    public void onExercisesCreated(TreeMap<ArrayList<Object>, ArrayList<Exercise>> exercises) {
                        setExercise(view, exercises);
                    }

                    @Override
                    public void onExercisesCreationFailed() {

                    }
                });
            }
        }

    }

    //metodo per mostrare gli esercizi sullo schermo
    void setExercise(View view, TreeMap<ArrayList<Object>, ArrayList<Exercise>> exercises ){

        ArrayList<Integer> chapter = new ArrayList<>();
        ArrayList<Integer> numOfExercise = new ArrayList<>();
        ArrayList<String> exerciseType = new ArrayList<>();
        ArrayList<Exercise> exerciseList = new ArrayList<>();

        for(Map.Entry<ArrayList<Object>, ArrayList<Exercise>> entry : exercises.entrySet()){
            int i = 0;
            for(Exercise exOfChapter : entry.getValue()){
                if(exOfChapter.getState().equals("done")) {
                    chapter.add((int) entry.getKey().get(0));
                    numOfExercise.add(++i);
                    exerciseList.add(exOfChapter);
                    switch (exOfChapter.getType()) {
                        case 1:
                            exerciseType.add("ImageDenomination");
                            break;
                        case 2:
                            exerciseType.add("MinimalPairsRecognition");
                            break;
                        case 3:
                            exerciseType.add("WordsSequenceRepetition");
                            break;
                    }
                }
            }
        }


        for(int i = 0; i < chapter.size(); i++) {

            LinearLayout linearLayout = view.findViewById(R.id.exerciseParentLinearLayout);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View frameLayoutView = inflater.inflate(R.layout.single_exercise_parent_fragment, null);
            FrameLayout frameLayout = new FrameLayout(context);
            frameLayout.addView(frameLayoutView);
            // Imposta i margini superiori e inferiori per il FrameLayout
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            if(i != 0 )
                layoutParams.setMargins(0, 30, 0, 20);
            else
                layoutParams.setMargins(0, 80, 0, 20);

            frameLayout.setLayoutParams(layoutParams);

            linearLayout.addView(frameLayout);

            String thisExerciseType = exerciseType.get(i);
            Exercise thisExercise = exerciseList.get(i);
            int thisChapter = chapter.get(i);

            //setto dettagli dell'esercizio
            TextView currentChapter = frameLayoutView.findViewById(R.id.chapter);
            currentChapter.setText(getResources().getString(R.string.chapterParent) + " " + String.valueOf(thisChapter));

            TextView currentNumOfExercise = frameLayoutView.findViewById(R.id.numOfExercise);
            currentNumOfExercise.setText(getResources().getString(R.string.ex).toString() + " " + String.valueOf(numOfExercise.get(i)));

            TextView currentExerciseType = frameLayoutView.findViewById(R.id.exerciseType);
            currentExerciseType.setText(String.valueOf(thisExerciseType));

            MaterialButton correct = frameLayoutView.findViewById(R.id.correct);

            //listener per iniziare a correggere l'esercizio
            correct.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //prendo l'esercizio corretto in base al tipo
                    switch(thisExerciseType){
                        case "ImageDenomination":
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ImageDenominationCorrection(thisChapter, thisExercise)).commit();
                            mCallback.hideBottomBar();
                            break;
                        case "MinimalPairsRecognition":
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new MinimalPairsRecognitionCorrection(thisChapter, thisExercise)).commit();
                            mCallback.hideBottomBar();
                            break;
                        case "WordsSequenceRepetition":
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new WordsSequencesRepetitionCorrection(thisChapter, thisExercise)).commit();
                            mCallback.hideBottomBar();
                            break;
                    }
                }
            });

        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ExercisesFragment(false, exercises)).commit();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ExercisesFragment(true, exercises)).commit();
        }

    }
}