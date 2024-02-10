package it.uniba.dib.sms232421.therapist.settings;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;

import it.uniba.dib.sms232421.R;
import it.uniba.dib.sms232421.patient.parent.settings.SettingsFragment;
import it.uniba.dib.sms232421.user.FirebaseUserModel;
import it.uniba.dib.sms232421.user.User;

public class SettingsTherapist extends Fragment {
    Context context;
    private String email;
    private EditText AddressTherapist;
    private EditText CityTherapist;
    private ImageButton EditAddress;
    private ImageButton EditCity;
    private MaterialButton saveProfileTherapist;
    private EditText emailEditTextTherapist;
    private String AddressDB;
    private String CityDB;
    private MaterialButton resetPasswordTherapist;
    private boolean isPortrait = true;
    private onSettingsListener mCallback;
    SwipeRefreshLayout swipeRefreshLayout;

    public SettingsTherapist(boolean isPortrait){
        this.isPortrait = isPortrait;
    }
    public SettingsTherapist(){
    }

    public interface onSettingsListener {
        void refreshSettingsData();

        void hideBottomBar();

        void showBottomBar();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof onSettingsListener) {
            mCallback = (onSettingsListener) context;
        } else {
            throw new RuntimeException(context + " must implement onSettingsListener");
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

        View view;
        if (isPortrait) {
            view = inflater.inflate(R.layout.settings_therapist, container, false);
            mCallback.showBottomBar();
        } else {
            view = inflater.inflate(R.layout.landscape_layout, container, false);
            mCallback.hideBottomBar();
        }

        // Inflate the layout for this fragment
        return view;
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(isPortrait) {
            mCallback.showBottomBar();
            //Gestisco il refresh
            swipeRefreshLayout = view.findViewById(R.id.swipeSettingsTherapist);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    mCallback.refreshSettingsData();
                }
            });


            emailEditTextTherapist = view.findViewById(R.id.emailEditTextTherapist);
            resetPasswordTherapist = view.findViewById(R.id.resetPasswordTherapist);

            //sezione profilo
            AddressTherapist = view.findViewById(R.id.AddressTherapist);
            CityTherapist = view.findViewById(R.id.CityTherapist);
            EditAddress = view.findViewById(R.id.EditAddress);
            EditCity = view.findViewById(R.id.EditCity);
            saveProfileTherapist = view.findViewById(R.id.saveProfileTherapist);
            //prendo i dati attualmente impostati
            setInitialData();

            //chiamo i metodi per la gestione delle 3 sezioni delle impostazioni
            manageCredentialsSection();
            manageProfileSection();
        }else{
            mCallback.hideBottomBar();
        }
    }

    private void setInitialData() {
        email = User.getInstance().getEmail();
        AddressDB = User.getInstance().getAddress();
        CityDB = User.getInstance().getCity();

        //dopo aver recuperato i dati, setto i valori iniziali nei campi
        emailEditTextTherapist.setText(email);
        AddressTherapist.setText(AddressDB);
        CityTherapist.setText(CityDB);

    }
    void manageProfileSection(){

        //click sull'edit del nome del bambino
        EditAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddressTherapist.setEnabled(true);
            }
        });

        AddressTherapist.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Disabilita l'EditText dopo aver premuto Invio sulla tastiera
                    AddressTherapist.setEnabled(false);

                    // Puoi anche nascondere la tastiera in modo che l'utente non possa più inserire testo
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(AddressTherapist.getWindowToken(), 0);

                    return true; // Consuma l'evento
                }
                return false; // Non consuma l'evento
            }
        });


        //listener utile per vedere se ci sono state delle modifiche al dato
        //ed, eventualmente, mostrare il button di salvataggio
        AddressTherapist.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    if(!AddressDB.equals(AddressTherapist.getText().toString())) {
                        AddressTherapist.setEnabled(false);
                        saveProfileTherapist.setVisibility(View.VISIBLE);
                    }else{
                        saveProfileTherapist.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });


        //click sull'edit del nome del bambino
        EditCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CityTherapist.setEnabled(true);
            }
        });

        //listener utile per vedere se ci sono state delle modifiche al dato
        //ed, eventualmente, mostrare il button di salvataggio
        CityTherapist.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    if(!CityDB.equals(CityTherapist.getText().toString())) {
                        CityTherapist.setEnabled(false);
                        saveProfileTherapist.setVisibility(View.VISIBLE);
                    }else{
                        saveProfileTherapist.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });


        CityTherapist.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Disabilita l'EditText dopo aver premuto Invio sulla tastiera
                    CityTherapist.setEnabled(false);

                    // Puoi anche nascondere la tastiera in modo che l'utente non possa più inserire testo
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(CityTherapist.getWindowToken(), 0);

                    return true; // Consuma l'evento
                }
                return false; // Non consuma l'evento
            }
        });

        //gestione salvataggio nuovi dati
        saveProfileTherapist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //è cambiato il nome del bambino, cambiare
                if(!AddressDB.equals(AddressTherapist.getText().toString())){
                    FirebaseUserModel.changeUserData("address",AddressTherapist.getText().toString(), new FirebaseUserModel.OnUserUpdateListener() {
                        @Override
                        public void onUserUpdate(boolean result) {
                            if(result) {
                                saveProfileTherapist.setVisibility(View.INVISIBLE);
                                AddressDB = AddressTherapist.getText().toString();
                                Toast.makeText(context, getString(R.string.addressError), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(context, getString(R.string.impossibile_effettuare_la_modifica), Toast.LENGTH_LONG).show();

                            }
                        }
                    });
                }else{
                    //se è cambiato il nome del genitore, cambiare
                    if(!CityDB.equals(CityTherapist.getText().toString())){
                        FirebaseUserModel.changeUserData("city",CityTherapist.getText().toString(), new FirebaseUserModel.OnUserUpdateListener() {
                            @Override
                            public void onUserUpdate(boolean result) {
                                if(result) {
                                    saveProfileTherapist.setVisibility(View.INVISIBLE);
                                    CityDB = CityTherapist.getText().toString();
                                    Toast.makeText(context, getString(R.string.cityError), Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(context, getString(R.string.impossibile_effettuare_la_modifica), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }
            }
        });
    }
    void manageCredentialsSection(){
        resetPasswordTherapist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseUserModel.sendEmailforChangePassword(email, new FirebaseUserModel.OnSendEmailListener() {
                    @Override
                    public void onEmailSent() {
                        Toast.makeText(context, getString(R.string.email_inviata), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure() {
                        Toast.makeText(context, getString(R.string.errore_durante_l_invio), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new SettingsTherapist(false)).commit();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new SettingsTherapist(true)).commit();
        }

    }
}
