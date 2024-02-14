package it.uniba.dib.sms2324FF2.therapist.patients;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Calendar;

import it.uniba.dib.sms2324FF2.R;
import it.uniba.dib.sms2324FF2.appointments.Appointment;
import it.uniba.dib.sms2324FF2.login.FirebaseAuthenticationModel;
import it.uniba.dib.sms2324FF2.patient.FirebasePatientModel;
import it.uniba.dib.sms2324FF2.therapist.patients.exercises.AddAudioToExerciseFragment;
import it.uniba.dib.sms2324FF2.therapist.patients.exercises.ImageDenominationFragment;
import it.uniba.dib.sms2324FF2.therapist.patients.exercises.MinimalPairsRecognitionFragment;

public class PatientsManagementFragment extends Fragment {

    Context context;
    AlertDialog dialog;
    FrameLayout frameLayout;
    ArrayList<String> patients, patientsId;

    ArrayList<Appointment> appointments;
    boolean isPortrait = true;

    private onPatientsListener mCallback;
    //empty constructor


    public PatientsManagementFragment(boolean isPortrait, ArrayList<String> patients, ArrayList<String> patientsId) {
        this.patients = patients;
        this.patientsId = patientsId;
        this.isPortrait = isPortrait;
    }

    public interface onPatientsListener {
        void refreshPatientsData();
        void activityFinish();
        void showBottomBar();
        void hideBottomBar();
    }

    /*Questo metodo collega il fragment all'activity contenitore e assicura che l'activity implementi
    l'interfaccia onHomePageListener, consentendo al fragment di chiamare i metodi definiti in tale interfaccia
    per comunicare con l'activity.*/
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof onPatientsListener) {
            mCallback = (onPatientsListener) context;
        } else {
            throw new RuntimeException(context + " must implement onPatientsListener");
        }
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override //Il Fragment Crea la sua interfaccia
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view;
        if(isPortrait){
            view = inflater.inflate(R.layout.fragment_patients_management, container, false);
            mCallback.showBottomBar();
        }else{
            view = inflater.inflate(R.layout.landscape_layout, container, false);
            mCallback.hideBottomBar();
        }

        // Inflate the layout for this fragment
        return view;
    }


    @SuppressLint("MissingInflatedId")
    @Override //Interagisco con gli elementi dell'interfaccia
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(isPortrait) {
            //mi assicuro che la bottom bar sia visibile
            mCallback.showBottomBar();
            //Gestisco il refresh
            SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    mCallback.refreshPatientsData();
                }
            });

            setPatients(view);

            //Gestisco l'apertura dia un nuovo dialog per registrare il paziente
            // Trova il bottone nel layout
            ImageButton newPatientButton = view.findViewById(R.id.btnRegisterPatient);

            // Imposta l'OnClickListener per il bottone
            newPatientButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                    LayoutInflater inflater = requireActivity().getLayoutInflater();
                    View customView = inflater.inflate(R.layout.new_patient_dialog, null);

                    builder.setView(customView);
                    AlertDialog newPatientDialog = builder.create();
                    newPatientDialog.setCanceledOnTouchOutside(false);
                    // Blocca l'orientamento in portrait quando la finestra di dialogo è aperta
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

                    // mostra dialog
                    newPatientDialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.dialog_rounded_corner_gray));

                    // Recupera i widget dalla vista
                    EditText parent = customView.findViewById(R.id.parentRegister);
                    EditText child = customView.findViewById(R.id.kidRegister);
                    EditText email = customView.findViewById(R.id.emailRegister);
                    EditText password = customView.findViewById(R.id.password);


                    MaterialButton save = customView.findViewById(R.id.btnRegister);
                    // Gestisci la registrazione del paziente
                    save.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //se l'email è vuota
                            if (TextUtils.isEmpty(String.valueOf(email.getText()))) {
                                //mostro messaggio d'errore
                                Toast.makeText(context, getString(R.string.saveErrorData), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            //se il nome del bambino è vuoto
                            if (TextUtils.isEmpty(String.valueOf(child.getText()))) {
                                //mostro messaggio d'errore
                                Toast.makeText(context, getString(R.string.saveErrorData), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            //se il nome del genitore è vuoto
                            if (TextUtils.isEmpty(String.valueOf(parent.getText()))) {
                                //mostro messaggio d'errore
                                Toast.makeText(context, getString(R.string.saveErrorData), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            FirebaseAuthenticationModel.addPatient(email.getText().toString(), new FirebaseAuthenticationModel.OnPatientAddedListener() {
                                @Override
                                public void onSuccess(String uid) {
                                    FirebasePatientModel.addPatientData(email.getText().toString(), child.getText().toString(), parent.getText().toString(), uid, new FirebasePatientModel.OnPatientCreatedListener() {
                                        @Override
                                        public void onSuccess() {
                                            //Paziente creato con successo
                                            Toast.makeText(getContext(), getString(R.string.paziente_creato_correttamente), Toast.LENGTH_SHORT).show();
                                            newPatientDialog.dismiss();
                                            // Ripristina l'orientamento predefinito
                                            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                                        }

                                        @Override
                                        public void onFailure() {
                                            Toast.makeText(context, getString(R.string.saveErrorData), Toast.LENGTH_SHORT).show();

                                        }
                                    });
                                }

                                @Override
                                public void onFailure() {
                                    Toast.makeText(context, getString(R.string.saveErrorData), Toast.LENGTH_SHORT).show();

                                }
                            });
                        }
                    });

                    MaterialButton cancel = customView.findViewById(R.id.btnCancel);
                    // Gestisci il pulsante "Annulla"
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Torna al fragment dei pazienti
                            newPatientDialog.dismiss(); // Chiudi la finestra di dialogo quando si preme "Annulla"
                            // Ripristina l'orientamento predefinito
                            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                        }
                    });

                    newPatientDialog.show();
                }

            });
        }

    }

    void setPatients(View view){

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout patientLayout = view.findViewById(R.id.patientsList);

        for (int i = 0; i < patients.size(); i++) {
            String name = patients.get(i);

            // Inflate il layout per ogni paziente
            View frameLayoutView = inflater.inflate(R.layout.patient_line_layout, null);
            FrameLayout frameLayout = new FrameLayout(context);

            // Imposta il margine superiore
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );

            layoutParams.topMargin = 30;

            frameLayout.setLayoutParams(layoutParams);

            frameLayout.addView(frameLayoutView);
            patientLayout.addView(frameLayout);

            // Ottieni i riferimenti
            TextView nomePaziente = frameLayoutView.findViewById(R.id.nomePaziente);
            MaterialButton addButton = frameLayoutView.findViewById(R.id.aggiungiEsercizio);

            if (name.length() < 15)
                nomePaziente.setText(name);
            else
                nomePaziente.setText(name.substring(0, 12) + "...");

            int finalI = i;
            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDialogChooseExerciseType(patientsId.get(finalI));
                }
            });

            // Aggiungi un separatore, tranne per l'ultimo elemento
            if (i < patients.size() - 1) {
                View separator = new View(context);
                separator.setBackgroundColor(getActivity().getResources().getColor(R.color.white));
                int separatorHeight = 4;
                LinearLayout.LayoutParams separatorParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, separatorHeight);
                patientLayout.addView(separator, separatorParams);
            }


            ImageButton delete = frameLayoutView.findViewById(R.id.delete);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //mostra una finestra di dialogo di conferma
                    showDeletePatientDialog(patientsId.get(finalI));
                }
            });
        }



    }


    //mostrare finestra di dialogo per scegliere quale dei tre esercizi voler creare
    private void showDialogChooseExerciseType(String id) {
        // Infla il layout personalizzato
        View customView = getLayoutInflater().inflate(R.layout.chooser_exercise_type_dialog, null);

        // Trova i pulsanti nel layout personalizzato
        MaterialButton imageDenomination = customView.findViewById(R.id.imageDenomination);
        MaterialButton wordsSequencesRepetition = customView.findViewById(R.id.wordsSequencesRepetition);
        MaterialButton minimalPairsRecognition = customView.findViewById(R.id.minimalPairsRecognition);
        MaterialButton cancel = customView.findViewById(R.id.cancelButton);

        // Costruisci l'AlertDialog utilizzando il layout personalizzato
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(customView);
        AlertDialog chooseExerciseType = builder.create();
        // Blocca l'orientamento in portrait quando la finestra di dialogo è aperta
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        chooseExerciseType.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.dialog_rounded_corner_gray));
        chooseExerciseType.setCanceledOnTouchOutside(false);
        //gestione listener
        imageDenomination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateAndCoinDialog(1, id);
                chooseExerciseType.dismiss();
                // Ripristina l'orientamento predefinito
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        });


         wordsSequencesRepetition.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 showDateAndCoinDialog(2, id);
                 chooseExerciseType.dismiss();
                 // Ripristina l'orientamento predefinito
                 getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
             }
         });


         minimalPairsRecognition.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 showDateAndCoinDialog(3, id);
                 chooseExerciseType.dismiss();
                 // Ripristina l'orientamento predefinito
                 getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
             }
         });
         cancel.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // Annulla
                 chooseExerciseType.dismiss();
                 // Ripristina l'orientamento predefinito
                 getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
             }
         });

         // Mostriamo il dialog
         chooseExerciseType.show();
    }

    //finestra di dialogo per scegliere la data dell'esercizio e i coin stabiliti
    void showDateAndCoinDialog(int esType, String id){
        // Ottieni l'oggetto LayoutInflater
        LayoutInflater inflater = LayoutInflater.from(getContext());

        // Carica il layout del tuo dialog personalizzato
        View dialogView = inflater.inflate(R.layout.date_and_coin_dialog, null);

        // Crea un AlertDialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setView(dialogView);

        DatePicker datePicker = dialogView.findViewById(R.id.datePicker);

        // Mostra il tuo AlertDialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // Blocca l'orientamento in portrait quando la finestra di dialogo è aperta
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.dialog_rounded_corner_gray));

        alertDialog.show();

        @SuppressLint("MissingInflatedId") EditText coinTxtView = dialogView.findViewById(R.id.coin);


        // Ottieni le referenze agli elementi UI all'interno del layout del dialog
        Button addButton = dialogView.findViewById(R.id.continueButton);
        Button cancelButton = dialogView.findViewById(R.id.cancel);

        //gestione evento annulla operazione
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chiudi il dialog senza fare alcuna operazione
                alertDialog.dismiss();
                // Ripristina l'orientamento predefinito
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        });


        // Imposta gli onClickListener per i pulsanti del dialog
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int year = datePicker.getYear();
                int month = datePicker.getMonth(); // Mese inizia da 0
                int dayOfMonth = datePicker.getDayOfMonth();

                // Creo un oggetto Calendar e imposto la data e l'ora
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth, 0, 0, 0);
                com.google.firebase.Timestamp date = new Timestamp(calendar.getTime());

                // Controllo sulla data
                if (calendar.compareTo(Calendar.getInstance()) < 0) {
                    Toast.makeText(context, getString(R.string.selectAValidDate), Toast.LENGTH_SHORT).show();
                    return;
                }

                if(coinTxtView.getText().toString().isEmpty() || coinTxtView.getText().toString() == null){
                    Toast.makeText(context, getString(R.string.selectValidCoin), Toast.LENGTH_SHORT).show();
                    return;
                }

                int coin = Integer.valueOf(coinTxtView.getText().toString());

                //controllo sui coin
                if(coin <= 0) {
                    Toast.makeText(context, getString(R.string.selectValidCoin), Toast.LENGTH_SHORT).show();
                    return;
                }


                //controlli superati -> faccio partire l'esercizio corretto
                switch(esType){
                    case 1:
                        //nascondi la barra
                        mCallback.hideBottomBar();
                        alertDialog.dismiss();
                        // Ripristina l'orientamento predefinito
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                        //fai partire esercizio
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ImageDenominationFragment(true, id, coin, date)).commit();
                        break;
                    case 2:
                        //nascondi la barra
                        mCallback.hideBottomBar();
                        alertDialog.dismiss();
                        // Ripristina l'orientamento predefinito
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                        //fai partire esercizio
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new AddAudioToExerciseFragment(true, id, coin, date)).commit();
                        break;
                    case 3:
                        //nascondi la barra
                        mCallback.hideBottomBar();
                        alertDialog.dismiss();
                        // Ripristina l'orientamento predefinito
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                        //fai partire esercizio
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new MinimalPairsRecognitionFragment(true, id, coin, date)).commit();
                        break;
                }

            }
        });


    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new PatientsManagementFragment(false, patients, patientsId)).commit();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new PatientsManagementFragment(true, patients, patientsId)).commit();
        }

    }

    void showDeletePatientDialog(String patientId){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View customView = inflater.inflate(R.layout.delete_patient_dialog, null);

        builder.setView(customView);
        dialog = builder.create();
        // Blocca l'orientamento in portrait quando la finestra di dialogo è aperta
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.dialog_rounded_corner_gray));

        Button positiveButton = customView.findViewById(R.id.custom_positive_button);
        Button negativeButton = customView.findViewById(R.id.custom_negative_button);

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Azioni per il pulsante "Sì"
                //Elimino il paziente e tutti i suoi appuntamenti
                FirebasePatientModel.deletePatient(patientId, new FirebasePatientModel.OnDeleteListener() {
                    @Override
                    public void onPatientDeleted() {
                        Toast.makeText(context, "Paziente eliminato", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        // Ripristina l'orientamento predefinito
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    }

                    @Override
                    public void onPatientDeleteFailed() {
                        Toast.makeText(context, "Paziente non eliminato", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        // Ripristina l'orientamento predefinito
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    }
                });
            }
        });

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


}
