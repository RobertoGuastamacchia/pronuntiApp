package it.uniba.dib.sms2324FF2.patient.child;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.TreeMap;

import it.uniba.dib.sms2324FF2.R;
import it.uniba.dib.sms2324FF2.exercises.Exercise;
import it.uniba.dib.sms2324FF2.exercises.FirebaseExerciseModel;
import it.uniba.dib.sms2324FF2.patient.child.homepage.HomePageChildFragment;

public class RewardsFragment extends Fragment {

    private ImageView openTreasure;
    private ImageView characters;
    private ImageView coinImage;
    private ImageButton treasure;
    private TextView rewardsDescription;
    private TextView coin;
    private MaterialButton closeButton;
    private boolean isPulsating = false;
    private PulseAnimation pulseAnimation;
    private int rewardCoins;
    private TreeMap<ArrayList<Object>, ArrayList<Exercise>> exercises;
    private onRewardsListener mCallback;
    private boolean isPortrait = true;


    public interface onRewardsListener {
        void showBottomBar();
        void hideBottomBar();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public RewardsFragment(int newCoin, TreeMap<ArrayList<Object>, ArrayList<Exercise>> exercises){
        this.rewardCoins = newCoin;
        this.exercises = exercises;
    }

    public RewardsFragment(boolean isPortrait, int newCoin, TreeMap<ArrayList<Object>, ArrayList<Exercise>> exercises){
        this.rewardCoins = newCoin;
        this.exercises = exercises;
        this.isPortrait = isPortrait;
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        if(isPortrait && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            view = inflater.inflate(R.layout.fragment_rewards, container, false);
            mCallback.showBottomBar();
        }else{
            view = inflater.inflate(R.layout.landscape_layout, container, false);
            mCallback.hideBottomBar();
        }

        // Inflate the layout for this fragment
        return view;
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof onRewardsListener) {
            mCallback = (onRewardsListener) context;
        } else {
            throw new RuntimeException(context + " must implement onRewardsListener");
        }
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(isPortrait && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            MediaPlayer.create(getActivity(), R.raw.win_sound).start();

            //mi assicuro che non sia visibile la bottom bar
            mCallback.hideBottomBar();

            treasure = view.findViewById(R.id.treasure);
            openTreasure = view.findViewById(R.id.openTreasure);
            rewardsDescription = view.findViewById(R.id.rewardsDescription);
            characters = view.findViewById(R.id.characters);
            coin = view.findViewById(R.id.coin);
            coinImage = view.findViewById(R.id.coinImage);
            closeButton = view.findViewById(R.id.closeButton);

            treasure.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Nascondi il bottone del tesoro e mostra l'immagine del tesoro aperto
                    treasure.setVisibility(View.GONE);
                    openTreasure.setVisibility(View.VISIBLE);
                    coin.setVisibility(View.VISIBLE);
                    coinImage.setVisibility(View.VISIBLE);
                    coin.setText("+" + String.valueOf(rewardCoins) + " COIN");
                    rewardsDescription.setVisibility(View.GONE);
                    characters.setVisibility(View.GONE);

                    // Arresta la pulsazione quando il bottone del tesoro viene cliccato
                    stopPulsating();

                    // Simula un aumento graduale dei coin
                    simulateCoinIncrease();
                }
            });

            // Avvia la pulsazione quando la vista del fragment viene creata
            startPulsating();
        }
    }

    //metodi per creare un'animazione a pulsazione
    private void startPulsating() {
        if (!isPulsating) {
            isPulsating = true;
            pulseAnimation = new PulseAnimation(treasure);
            pulseAnimation.start();
        }
    }

    private void stopPulsating() {
        isPulsating = false;
        if (pulseAnimation != null) {
            pulseAnimation.cancel();
        }
    }

    //metodo per creare un'animazione di aumento incrementale dei coin sullo schermo
    private void simulateCoinIncrease() {
        //valore da raggiungere
        final int targetCoinCount = rewardCoins;

        // ValueAnimator per animare il valore dei coin
        ValueAnimator animator = ValueAnimator.ofInt(0, targetCoinCount);
        animator.setDuration(2000); // Durata dell'animazione in millisecondi
        MediaPlayer.create(getActivity(), R.raw.coin).start();

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                // Aggiorna il testo con il valore corrente dell'animazione
                int animatedValue = (int) valueAnimator.getAnimatedValue();
                coin.setText("+" + String.valueOf(animatedValue));
            }

        });

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(@NonNull Animator animation, boolean isReverse) {

                closeButton.setVisibility(View.VISIBLE);

                //aggiungo il listener per il ritorno alla home
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //aggiorno le monete del bambino
                        FirebaseExerciseModel.updateCoins(rewardCoins, new FirebaseExerciseModel.OnCoinsUpdateListener() {
                            @Override
                            public void onCoinsUpdateSuccess(int newCoins) {
                                //ripristino a 0 le monete premio
                                FirebaseExerciseModel.restoreRewardCoins();
                                //ritorno alla home
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new HomePageChildFragment(exercises)).commit();
                            }

                            @Override
                            public void onCoinsUpdateFailure() {}
                        });
                    }
                });
            }

            //metodi non implementati in quanto non utili
            @Override
            public void onAnimationStart(@NonNull Animator animation) {

            }

            @Override
            public void onAnimationEnd(@NonNull Animator animation) {

            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {

            }
        });


        closeButton.setVisibility(View.INVISIBLE);
        // Avvia l'animazione
        animator.start();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new RewardsFragment(false, rewardCoins, exercises)).commit();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new RewardsFragment(true, rewardCoins, exercises)).commit();
        }

    }
}