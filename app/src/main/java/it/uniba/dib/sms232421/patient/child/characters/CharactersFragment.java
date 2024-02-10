package it.uniba.dib.sms232421.patient.child.characters;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import it.uniba.dib.sms232421.R;
import it.uniba.dib.sms232421.patient.FirebasePatientModel;
import it.uniba.dib.sms232421.patient.Patient;

public class CharactersFragment extends Fragment {

    String theme, principalCharacter;
    ProgressBar progressBar;
    ArrayList<String> charactersUnlocked;
    private boolean isPortrait = true;

    public interface onCharactersListener {
        void refreshCharactersData();
        void hideBottomBar();
        void showBottomBar();
    }

    private onCharactersListener mCallback;

    public CharactersFragment(ArrayList<String> charactersUnlocked){
        this.charactersUnlocked = charactersUnlocked;
    }

    public CharactersFragment(boolean isPortrait, ArrayList<String> charactersUnlocked){
        this.charactersUnlocked = charactersUnlocked;
        this.isPortrait = isPortrait;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof onCharactersListener) {
            mCallback = (onCharactersListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement onCharactersListener");
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
        View view;
        if(isPortrait){
            view = inflater.inflate(R.layout.fragment_characters, container, false);
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
            mCallback.showBottomBar();
            //Gestisco il refresh
            SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    mCallback.refreshCharactersData();
                }
            });
            //Ottengo il riferimento alla progressBar
            progressBar = view.findViewById(R.id.progressBar);

            //mostro i personaggi
            setCharacters(view);
        }
    }


    //metodo per prendere e mostrare i personaggi in base al tema impostato del bambino
    void setCharacters(View view){

        //seleziono i personaggi in base al tema impostato
        progressBar.setVisibility(View.GONE);
        ArrayList<Integer> idsimg = new ArrayList<Integer>();
        ArrayList<Integer> idstext = new ArrayList<Integer>();

        theme = Patient.getInstance().getTheme();
        //ottengo il tema effettivo per i personaggi
        if (theme.contains("_personalized"))
            theme = theme.replace("_personalized", "");

        switch (theme) {
                case "jungle":
                    idsimg.add(R.id.jungle_Jumbojango_img);
                    idsimg.add(R.id.t2_img);
                    idsimg.add(R.id.t3_img);
                    idsimg.add(R.id.t4_img);
                    idsimg.add(R.id.t5_img);
                    idsimg.add(R.id.t6_img);
                    idsimg.add(R.id.t7_img);

                    idstext.add(R.id.jungle_Jumbojango_name);
                    idstext.add(R.id.t2_name);
                    idstext.add(R.id.t3_name);
                    idstext.add(R.id.t4_name);
                    idstext.add(R.id.t5_name);
                    idstext.add(R.id.t6_name);
                    idstext.add(R.id.t7_name);

                    selectCharactersFromTheme(idsimg, idstext, view);
                    break;
                case "sea":
                    idsimg.add(R.id.a1_img);
                    idsimg.add(R.id.a2_img);
                    idsimg.add(R.id.a3_img);
                    idsimg.add(R.id.a4_img);
                    idsimg.add(R.id.a5_img);

                    idstext.add(R.id.a1_name);
                    idstext.add(R.id.a2_name);
                    idstext.add(R.id.a3_name);
                    idstext.add(R.id.a4_name);
                    idstext.add(R.id.a5_name);
                    selectCharactersFromTheme(idsimg, idstext, view);
                    break;
                case "space":
                    idsimg.add(R.id.p1_img);
                    idsimg.add(R.id.p2_img);
                    idsimg.add(R.id.p3_img);
                    idsimg.add(R.id.p4_img);
                    idsimg.add(R.id.p5_img);
                    idsimg.add(R.id.p6_img);
                    idsimg.add(R.id.p7_img);

                    idstext.add(R.id.p1_name);
                    idstext.add(R.id.p2_name);
                    idstext.add(R.id.p3_name);
                    idstext.add(R.id.p4_name);
                    idstext.add(R.id.p5_name);
                    idstext.add(R.id.p6_name);
                    idstext.add(R.id.p7_name);
                    selectCharactersFromTheme(idsimg, idstext, view);
                    break;
            }
        //prendo i personaggi dal firestore su firebase
        if(charactersUnlocked != null) {
            for (String character : charactersUnlocked) {
                switch (character) {
                    case "Jumbojango":
                        setCharacterAppearance(R.id.jungle_Jumbojango_img, R.id.jungle_Jumbojango_name, view);
                        break;
                    case "Kaida":
                        setCharacterAppearance(R.id.t2_img, R.id.t2_name, view);
                        break;
                    case "Rhinox":
                        setCharacterAppearance(R.id.t3_img, R.id.t3_name, view);
                        break;
                    case "Regaleon":
                        setCharacterAppearance(R.id.t4_img, R.id.t4_name, view);
                        break;
                    case "Doratall":
                        setCharacterAppearance(R.id.t5_img, R.id.t5_name, view);
                        break;
                    case "Monkin":
                        setCharacterAppearance(R.id.t6_img, R.id.t6_name, view);
                        break;
                    case "Koalinda":
                        setCharacterAppearance(R.id.t7_img, R.id.t7_name, view);
                        break;
                    case "Delphy":
                        setCharacterAppearance(R.id.a1_img, R.id.a1_name, view);
                        break;
                    case "Splashes":
                        setCharacterAppearance(R.id.a2_img, R.id.a2_name, view);
                        break;
                    case "Chelonide":
                        setCharacterAppearance(R.id.a3_img, R.id.a3_name, view);
                        break;
                    case "Inktonio":
                        setCharacterAppearance(R.id.a4_img, R.id.a4_name, view);
                        break;
                    case "Pinchington":
                        setCharacterAppearance(R.id.a5_img, R.id.a5_name, view);
                        break;
                    case "Zorgon":
                        setCharacterAppearance(R.id.p1_img, R.id.p1_name, view);
                        break;
                    case "Nebulox":
                        setCharacterAppearance(R.id.p2_img, R.id.p2_name, view);
                        break;
                    case "Xytron":
                        setCharacterAppearance(R.id.p3_img, R.id.p3_name, view);
                        break;
                    case "Quasarix":
                        setCharacterAppearance(R.id.p4_img, R.id.p4_name, view);
                        break;
                    case "Galactron":
                        setCharacterAppearance(R.id.p5_img, R.id.p5_name, view);
                        break;
                    case "Vortexar":
                        setCharacterAppearance(R.id.p6_img, R.id.p6_name, view);
                        break;
                    case "Lunarisia":
                        setCharacterAppearance(R.id.p7_img, R.id.p7_name, view);
                        break;
                }
            }
        }
    }

    //metodo per selezionare i personaggi da mostrare in base al tema
    void selectCharactersFromTheme(ArrayList<Integer> imgResourceId, ArrayList<Integer> textResourceId, View view){
        ImageButton img = null;
        TextView text;
        ArrayList<ImageButton> imgList = new ArrayList<>();
        ArrayList<String> textList = new ArrayList<>();
        for(int id : imgResourceId){
            img = view.findViewById(id);
            if(img != null) {
                img.setVisibility(View.VISIBLE);
                imgList.add(img);
            }
        }
        for(int id : textResourceId) {
            text = view.findViewById(id);
            String characterName = text.getText().toString();
            if (text != null) {
                text.setVisibility(View.VISIBLE);
                textList.add(characterName);
            }
        }

        //ciclo for per aggiungere listener ad ogni personaggio
        for(int i = 0; i < imgList.size(); i++){
            ImageButton imageOfList = imgList.get(i);
            String textOfList = textList.get(i);

            //leggo il costo del personaggio corrente
            FirebaseCharactersModel.getCost(textOfList, new FirebaseCharactersModel.OnCharacterCostListener() {
                @Override
                public void onCostRead(int cost) {
                    //mostro popup informativo del costo in coin del personaggio solo se questo
                    //non è ancora stato sbloccato
                    if(!Patient.getInstance().getCharacters_unlocked().contains(textOfList) &&
                            Patient.getInstance().getCoin() >= cost){
                        imageOfList.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MediaPlayer.create(getActivity(), R.raw.button_click_sound).start();
                                //mostro popup per sbloccare il personaggio
                                showPopupToUnlockCharacter(imageOfList, textOfList);
                            }
                        });
                    }else{
                        //personaggio bloccato non ancora sbloccabile
                        if(!Patient.getInstance().getCharacters_unlocked().contains(textOfList)){
                            imageOfList.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    MediaPlayer.create(getActivity(), R.raw.button_click_sound).start();
                                    //mostro popup informativo sul prezzo in termini di coin del personaggio bloccato
                                    showPopupLockedCharacter(imageOfList, textOfList);
                                }
                            });
                        }else{
                            FirebasePatientModel.getPatientPrincipalCharacters(new FirebasePatientModel.OnCharactersListener<HashMap<String, String>>() {
                                @Override
                                public void onCharactersRead(HashMap<String, String> charactersRead) {
                                    for(Map.Entry<String,String>  entry : charactersRead.entrySet()) {
                                        if (entry.getKey().equals(theme)) {
                                            principalCharacter = entry.getValue();
                                        }
                                            //personaggio sbloccato
                                            if (Patient.getInstance().getCharacters_unlocked().contains(textOfList)
                                                    && !textOfList.equals(principalCharacter)) {
                                                imageOfList.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        MediaPlayer.create(getActivity(), R.raw.button_click_sound).start();
                                                        //mostro popup per impostare come principale il personaggio corrente
                                                        showPopupSetPrincipalCharacter(imageOfList, textOfList);
                                                    }
                                                });
                                            }
                                    }
                                }
                                @Override
                                public void onCharactersReadFailure(HashMap<String, String> charactersRead) {}
                            });
                        }
                    }
                }

                @Override
                public void onCostReadFailed() {}
            });


        }



    }

    //metodo per mostrare popup per acquistare il personaggio
    private void showPopupSetPrincipalCharacter(View ancorView, String character) {
        View popupView = getLayoutInflater().inflate(R.layout.set_principal_character_popup, null);

        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        // Mostra il popup attaccato al pulsante
        popupWindow.showAsDropDown(ancorView, 0, -ancorView.getHeight());

        Button positiveButton = popupView.findViewById(R.id.custom_positive_button);
        Button negativeButton = popupView.findViewById(R.id.custom_negative_button);

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebasePatientModel.setPrincipalCharacter(character, new FirebasePatientModel.OnCharactersListener<Boolean>() {
                    @Override
                    public void onCharactersRead(Boolean charactersRead) {
                        //chiudo il popup
                        mCallback.refreshCharactersData();
                        popupWindow.dismiss();
                    }
                    @Override
                    public void onCharactersReadFailure(Boolean value) {}
                });

            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Azioni per il pulsante "No"
                // Chiudi la finestra di dialogo
                popupWindow.dismiss();
            }
        });

    }

    //metodo per mostrare popup per acquistare il personaggio
    private void showPopupToUnlockCharacter(View ancorView, String character) {
        View popupView = getLayoutInflater().inflate(R.layout.unlock_character_popup, null);

        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        // Mostra il popup attaccato al pulsante
        popupWindow.showAsDropDown(ancorView, 0, -ancorView.getHeight());

        Button positiveButton = popupView.findViewById(R.id.custom_positive_button);
        Button negativeButton = popupView.findViewById(R.id.custom_negative_button);

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mostro il fragment di spacchettamento del personaggio
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new NewCharacterFragment(character)).commit();
                //nascondo la bottom bar
                mCallback.hideBottomBar();
                popupWindow.dismiss();
            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Azioni per il pulsante "No"
                // Chiudi la finestra di dialogo
                popupWindow.dismiss();
            }
        });

    }

    //metodo per mostrare popup in cui viene mostrato il prezzo dei personaggi in termini di coin
    private void showPopupLockedCharacter(View ancorView, String character) {
        FirebaseCharactersModel.getCost(character, new FirebaseCharactersModel.OnCharacterCostListener() {
            @Override
            public void onCostRead(int cost) {
                View popupView = getLayoutInflater().inflate(R.layout.locked_character_popup, null);

                PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
                popupWindow.setTouchable(true);
                popupWindow.setFocusable(true);
                // Mostra il popup attaccato al pulsante
                popupWindow.showAsDropDown(ancorView, 0, -ancorView.getHeight());

                TextView coinTxt = popupView.findViewById(R.id.coin);
                coinTxt.setText(String.valueOf(cost));

                Button closeButton = popupView.findViewById(R.id.closeButton);
                closeButton.setBackgroundColor(getResources().getColor(R.color.childAccent));
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });
            }

            @Override
            public void onCostReadFailed() {}
        });
    }

    void setCharacterAppearance( int imgResourceId, int nameResourceId, View view) {
        ImageButton img;
        TextView name;
        img = view.findViewById(imgResourceId);
        if(img!=null) {
            img.setImageTintList(null);
            img.setBackgroundTintList(null);
        }
        name = view.findViewById(nameResourceId);
        if(name!=null)
            name.setTextColor(Color.WHITE);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new CharactersFragment(false, charactersUnlocked)).commit();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new CharactersFragment(true, charactersUnlocked)).commit();
        }

    }
}