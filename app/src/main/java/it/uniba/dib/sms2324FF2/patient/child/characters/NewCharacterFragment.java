package it.uniba.dib.sms2324FF2.patient.child.characters;

import android.content.Context;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

import it.uniba.dib.sms2324FF2.R;
import it.uniba.dib.sms2324FF2.patient.PatientFirebaseModel;
import it.uniba.dib.sms2324FF2.patient.Patient;

public class NewCharacterFragment extends Fragment {

    private String character;
    private ImageButton cage;
    private ImageView newCharacterImage;
    private TextView newCharacterName;
    private TextView newCharacterDescription;
    private TextView newCharacterTitle;
    private MaterialButton closeButton;
    private Context context;
    private LinearLayout linearLayout;
    private boolean isPortrait = true;
    private OnCharactersListener mCallback;

    public interface OnCharactersListener {
        void refreshCharactersData();
        void showBottomBar();
        void hideBottomBar();

    }
    public NewCharacterFragment(String character){
        this.character = character;
    }

    public NewCharacterFragment(boolean isPortrait, String character){
        this.character = character;
        this.isPortrait = isPortrait;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnCharactersListener) {
            mCallback = (OnCharactersListener) context;
        } else {
            throw new RuntimeException(context + " must implement OnCharactersListener");
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
            view = inflater.inflate(R.layout.fragment_new_characters, container, false);
        }else{
            view = inflater.inflate(R.layout.landscape_layout, container, false);
        }

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //mi assicuro che non sia visibile la bottom bar
        mCallback.hideBottomBar();

        if(isPortrait) {

            Animation shakeAnimation = AnimationUtils.loadAnimation(context, R.anim.shake_animation);

            cage = view.findViewById(R.id.cage);
            newCharacterImage = view.findViewById(R.id.newCharacter);
            newCharacterName = view.findViewById(R.id.newCharacterName);
            newCharacterDescription = view.findViewById(R.id.newCharacterDescription);
            closeButton = view.findViewById(R.id.closeButton);
            newCharacterTitle = view.findViewById(R.id.newCharacterTitle);
            linearLayout = view.findViewById(R.id.linearLayout);

            cage.startAnimation(shakeAnimation);

            cage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //ferma animazione di shake
                    cage.clearAnimation();

                    //riproduco suono quando compare il nuovo personaggio
                    MediaPlayer.create(getActivity(), R.raw.win_sound).start();

                    //cambia elementi sullo schermo visibili
                    cage.setVisibility(View.GONE);
                    newCharacterDescription.setVisibility(View.GONE);
                    newCharacterImage.setVisibility(View.VISIBLE);
                    newCharacterName.setVisibility(View.VISIBLE);
                    closeButton.setVisibility(View.VISIBLE);
                    newCharacterTitle.setVisibility(View.VISIBLE);
                    linearLayout.setVisibility(View.VISIBLE);

                    //imposto immagine e nome del personaggio sbloccato
                    newCharacterName.setText(character);
                    switch (character) {
                        case "Ember":
                            newCharacterImage.setImageResource(R.drawable.mountain_ember);
                            break;
                        case "Edvige":
                            newCharacterImage.setImageResource(R.drawable.mountain_edvige);
                            break;
                        case "Nutmeg":
                            newCharacterImage.setImageResource(R.drawable.mountain_nutmeg);
                            break;
                        case "Bambi":
                            newCharacterImage.setImageResource(R.drawable.mountain_bambi);
                            break;
                        case "Munin":
                            newCharacterImage.setImageResource(R.drawable.mountain_munin);
                            break;
                        case "Bruno":
                            newCharacterImage.setImageResource(R.drawable.mountain_bruno);
                            break;
                        case "Nagini":
                            newCharacterImage.setImageResource(R.drawable.desert_nagini);
                            break;
                        case "BeepBeep":
                            newCharacterImage.setImageResource(R.drawable.desert_beepbeep);
                            break;
                        case "Aragog":
                            newCharacterImage.setImageResource(R.drawable.desert_aragog);
                            break;
                        case "Zephir":
                            newCharacterImage.setImageResource(R.drawable.desert_zephir);
                            break;
                        case "Harum":
                            newCharacterImage.setImageResource(R.drawable.desert_harum);
                            break;
                        case "Rost":
                            newCharacterImage.setImageResource(R.drawable.polar_rost);
                            break;
                        case "Wally":
                            newCharacterImage.setImageResource(R.drawable.polar_wally);
                            break;
                        case "Splash":
                            newCharacterImage.setImageResource(R.drawable.polar_splash);
                            break;
                        case "Sigma":
                            newCharacterImage.setImageResource(R.drawable.polar_sigma);
                            break;
                        case "Linux":
                            newCharacterImage.setImageResource(R.drawable.polar_linux);
                            break;
                        case "Sirius":
                            newCharacterImage.setImageResource(R.drawable.polar_sirius);
                            break;
                        case "Flippy":
                            newCharacterImage.setImageResource(R.drawable.polar_flippy);
                            break;
                    }
                }
            });

            //aggiungo il listener per il ritorno alla home
            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Aggiungo il personaggio a quelli sbloccati
                    Patient.getInstance().addCharacters_unlocked(character);
                    //Salvo la modifica sul database
                    PatientFirebaseModel.updateCharactersUnlocked(character, new PatientFirebaseModel.TransactionCallback() {
                        @Override
                        public void onTransactionSuccess() {
                            //torna al fragment dei personaggi
                            ArrayList<String> charactersUnlocked = Patient.getInstance().getCharacters_unlocked();
                            mCallback.refreshCharactersData();
                            //mostro la bottom bar
                            mCallback.showBottomBar();
                        }

                        @Override
                        public void onTransactionFailure() {
                            Toast.makeText(context, getString(R.string.errore_durante_lo_sblocco_del_personaggio), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new NewCharacterFragment(false, character)).commit();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new NewCharacterFragment(true, character)).commit();
        }

    }
}