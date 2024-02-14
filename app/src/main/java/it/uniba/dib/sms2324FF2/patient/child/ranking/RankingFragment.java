package it.uniba.dib.sms2324FF2.patient.child.ranking;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import it.uniba.dib.sms2324FF2.R;
import it.uniba.dib.sms2324FF2.patient.Patient;

public class RankingFragment extends Fragment {
    private onRankingListener mCallback;

    ProgressBar progressBar;
    LinearLayout linearLayout;
    FirebaseFirestore db;
    ArrayList<Ranking> rankingList;
    private boolean isPortrait = true;

    public interface onRankingListener {
        void refreshRankingData();
        void hideBottomBar();
        void showBottomBar();

    }

    public RankingFragment(ArrayList<Ranking> rankingList){
        this.rankingList = rankingList;
    }
    public RankingFragment(boolean isPortrait, ArrayList<Ranking> rankingList){
        this.rankingList = rankingList;
        this.isPortrait = isPortrait;
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof onRankingListener) {
            mCallback = (onRankingListener) context;
        } else {
            throw new RuntimeException(context + " must implement onCharactersListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //ottieni istanza del database su firebase
        db = FirebaseFirestore.getInstance();
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view;
        if(isPortrait){
            view = inflater.inflate(R.layout.fragment_ranking, container, false);
            mCallback.showBottomBar();
        }else{
            view = inflater.inflate(R.layout.landscape_layout, container, false);
            mCallback.hideBottomBar();
        }

        // Inflate the layout for this fragment
        return view;
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(isPortrait) {
            mCallback.showBottomBar();
            //Gestisco il refresh
            SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    mCallback.refreshRankingData();
                }
            });
            //ottengo i riferimenti agli oggetti del layout
            progressBar = view.findViewById(R.id.progressBar);
            linearLayout = view.findViewById(R.id.linearLayout);
            if (rankingList != null) {
                //mostro la classifica
                setRanking(rankingList);
            } else {
                FirebaseRankingModel.getRankingList(new FirebaseRankingModel.RankingCallback() {
                    @Override
                    public void onSuccess(ArrayList<Ranking> rankingList) {
                        setRanking(rankingList);
                    }

                    @Override
                    public void onFailure() {
                    }
                });
            }
        }

    }

    //metodo per leggere la classifica e settarla
    public void setRanking(ArrayList<Ranking> rankingList){

                progressBar.setVisibility(View.GONE);
                // Ordino la lista in base ai valori

                Collections.sort(rankingList, Comparator.comparing(Ranking::getCoin));

                for (int i = rankingList.size() -1 ; i >= 0 ; i--) {

                    //Creo un nuovo RelativeLayout per ogni iterazione
                    //facendone inflate
                    LayoutInflater layoutInflater=(LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View childView = layoutInflater.inflate(R.layout.ranking_line_layout, null);

                    //ottengo i riferimenti alle view del layout
                    LinearLayout newLinearLayout = childView.findViewById(R.id.container);
                    TextView position = childView.findViewById(R.id.position);
                    TextView name = childView.findViewById(R.id.name);
                    TextView coinNumber = childView.findViewById(R.id.coin);

                    //ottengo il font da impostare per i testi
                    Typeface customTypeface = ResourcesCompat.getFont(getContext(), R.font.quicksand_medium);

                    //aggiungo i testi
                    position.setText(new StringBuilder().append("").append(rankingList.size() - i).append("").toString());
                    position.setTypeface(customTypeface);
                    //particolare attenzione alla gestione della lunghezza dei nomi degl utenti
                    if(rankingList.get(i).getName().length() <= 13) {
                        name.setText(new StringBuilder().append("").append(rankingList.get(i).getName()).append("").toString());
                    }else{
                        name.setText(new StringBuilder().append("").append(rankingList.get(i).getName().substring(0,12)).append("...").append("").toString());
                    }
                    name.setTypeface(customTypeface);
                    coinNumber.setText(new StringBuilder().append("").append(rankingList.get(i).getCoin()).append("").toString());
                    coinNumber.setTypeface(customTypeface);

                    //controllo se è il podio, quindi mostro la medaglia
                    ImageView podium;
                    if(i == rankingList.size()-1){
                        podium = childView.findViewById(R.id.firstplace);
                        podium.setVisibility(View.VISIBLE);
                    } else if (i == rankingList.size()-2) {
                        podium = childView.findViewById(R.id.secondplace);
                        podium.setVisibility(View.VISIBLE);
                    } else if (i == rankingList.size()-3) {
                        podium = childView.findViewById(R.id.thirdplace);
                        podium.setVisibility(View.VISIBLE);
                    }

                    //modifico colori per utente loggato
                    if(Patient.getInstance().getId().equals(rankingList.get(i).getId())) {
                        newLinearLayout.setBackgroundColor(getActivity().getColor(R.color.childAccent));
                    }

                    //aggiungo il nuovo linearlayout al parent
                    linearLayout.addView(newLinearLayout);
                }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new RankingFragment(false, rankingList)).commit();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new RankingFragment( true, rankingList)).commit();
        }

    }

}