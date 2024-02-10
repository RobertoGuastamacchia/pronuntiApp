package it.uniba.dib.sms232421.therapist.homepage;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;

import it.uniba.dib.sms232421.R;
import it.uniba.dib.sms232421.appointments.Appointment;
import it.uniba.dib.sms232421.appointments.FirebaseAppointmentsModel;
import it.uniba.dib.sms232421.login.FirebaseAuthenticationModel;
import it.uniba.dib.sms232421.notifications.FCMClient;
import it.uniba.dib.sms232421.notifications.FCMNotification;
import it.uniba.dib.sms232421.notifications.FCMRequest;
import it.uniba.dib.sms232421.patient.FirebasePatientModel;
import it.uniba.dib.sms232421.user.FirebaseUserModel;
import it.uniba.dib.sms232421.login.LoginActivity;
import it.uniba.dib.sms232421.user.User;

//Dove ci sono gli esercizi
public class HomePageTherapistFragment extends Fragment {
    private onHomePageListener mCallback;
    private String patientName = null, patientId = null;
    private Context context;
    private Toolbar toolbar;
    private AlertDialog dialog;
    private ArrayList<Appointment> appointments;
    private ArrayList<String> patients, patientsId;
    private boolean isPortrait = true;
    public static final String UPCOMING = "Upcoming";
    public static final String PAST = "Past";
    private String appointmentCheck = UPCOMING;

    //empty constructor
    public HomePageTherapistFragment(boolean isPortrait, ArrayList<Appointment> appointments, ArrayList<String> patients, ArrayList<String> patientsId) {
        this.appointments = appointments;
        this.patients = patients;
        this.patientsId = patientsId;
        this.isPortrait = isPortrait;
    }

    public HomePageTherapistFragment() {

    }

    public interface onHomePageListener {
        void refreshHomePageData();

        void setToolbar(Context context, Toolbar toolbar);

        void showBottomBar();

        void hideBottomBar();
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

        View view;
        if (isPortrait) {
            view = inflater.inflate(R.layout.fragment_homepage_therapist, container, false);
            mCallback.showBottomBar();
        } else {
            view = inflater.inflate(R.layout.landscape_layout, container, false);
            mCallback.hideBottomBar();
        }

        toolbar = view.findViewById(R.id.toolbar);
        mCallback.setToolbar(context, toolbar);
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (isPortrait) {
            //Gestisco il refresh
            SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    mCallback.refreshHomePageData();
                }
            });

            ImageButton createAppointmentButton = view.findViewById(R.id.addAppointment);

            MaterialButton past = view.findViewById(R.id.past);
            MaterialButton future = view.findViewById(R.id.future);

            //default
            //cambia colori in base a se vengono selezionati appuntamenti passati o futuri
            past.setBackgroundColor(getActivity().getColor(R.color.switchOffColor));
            past.setTextColor(getActivity().getColor(android.R.color.white));

            future.setBackgroundColor(getActivity().getColor(R.color.primary));
            future.setTextColor(getActivity().getColor(android.R.color.white));

            appointmentCheck = UPCOMING;
            future.setEnabled(false);
            past.setEnabled(true);
            //di default mostro gli appuntamenti futuri
            if (appointments != null)
                setAppointments(view, appointments);
            else {
                FirebaseAppointmentsModel.getPatientAppointments(new FirebaseAppointmentsModel.OnReadAppointmentsListener() {
                    @Override
                    public void onAppointmentsRead(ArrayList<Appointment> appointments) {
                        setAppointments(view, appointments);
                    }

                    @Override
                    public void onAppointmentsReadFailed() {
                    }
                });
            }


            //gestisco l'evento di click sullo spostamento nella sezione appuntamenti passati
            past.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //cambia colori
                    future.setBackgroundColor(getActivity().getColor(R.color.switchOffColor));
                    future.setTextColor(getActivity().getColor(android.R.color.white));

                    past.setBackgroundColor(getActivity().getColor(R.color.primary));
                    past.setTextColor(getActivity().getColor(android.R.color.white));

                    appointmentCheck = PAST;
                    past.setEnabled(false); //disabilito per indicare che è selezionato
                    future.setEnabled(true);

                    if (appointments != null)//Se la lista degli appuntamenti non è stata ancora recuperata chiama il metodo getPatientAppointments
                        setAppointments(view, appointments);
                    else {
                        FirebaseAppointmentsModel.getDoctorAppointments(new FirebaseAppointmentsModel.OnReadAppointmentsListener() {
                            @Override //Se la lettura dei dati ha successo li visualizzo
                            public void onAppointmentsRead(ArrayList<Appointment> appointments) {
                                setAppointments(view, appointments);
                            }

                            @Override//caso in cui la lettura dei dati non dovesse avere successo
                            public void onAppointmentsReadFailed() {
                            }
                        });
                    }

                }
            });

            //gestisco l'evento di click sullo spostamento nella sezione appuntamenti futuri
            future.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //cambia colori
                    past.setBackgroundColor(getActivity().getColor(R.color.switchOffColor));
                    past.setTextColor(getActivity().getColor(android.R.color.white));

                    future.setBackgroundColor(getActivity().getColor(R.color.primary));
                    future.setTextColor(getActivity().getColor(android.R.color.white));

                    appointmentCheck = UPCOMING;
                    future.setEnabled(false);
                    past.setEnabled(true);

                    if (appointments != null)//Se gli appuntamenti sono stati recuperati chiamo setAppointments per visualizzarli
                        setAppointments(view, appointments);
                    else { //altrimenti appointments è nullo chiamo il metodo getPatientAppointments da Firebase
                        FirebaseAppointmentsModel.getDoctorAppointments(new FirebaseAppointmentsModel.OnReadAppointmentsListener() {
                            @Override
                            public void onAppointmentsRead(ArrayList<Appointment> appointments) {
                                setAppointments(view, appointments);
                            }

                            @Override
                            public void onAppointmentsReadFailed() {
                            }
                        });
                    }

                }
            });


            createAppointmentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseUserModel.getPatientsListforDoctor(new FirebaseUserModel.OnPatientsListListener() {
                        @Override
                        public void onPatientsListRead(ArrayList<String> nameList, ArrayList<String> idList,ArrayList<String> emailList) {
                            // Ottieni l'oggetto LayoutInflater
                            LayoutInflater inflater = LayoutInflater.from(getContext());

                            // Carica il layout del tuo dialog personalizzato
                            View dialogView = inflater.inflate(R.layout.add_appointment_dialog, null);

                            // Crea un AlertDialog
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                            alertDialogBuilder.setView(dialogView);


                            // Ottieni le referenze agli elementi UI all'interno del layout del dialog
                            TimePicker timePicker = dialogView.findViewById(R.id.timePicker);
                            DatePicker datePicker = dialogView.findViewById(R.id.datePicker);
                            Spinner patientNameSpinner = dialogView.findViewById(R.id.patientNameSpinner);


                            // Definire un array di opzioni senza il primo elemento vuoto
                            String[] options = new String[nameList.size()];
                            String[] optionsid = new String[idList.size()];
                            int i = 0;
                            for (String element : nameList) {
                                options[i] = element;
                                i++;
                            }
                            int j = 0;
                            for (String elementid : idList) {
                                optionsid[j] = elementid;
                                j++;
                            }

                            // Creo un adapter personalizzato per lo spinner utilizzando CustomArrayAdapter
                            CustomArrayAdapter adapter = new CustomArrayAdapter(context, R.layout.spinner_dropdown_item, options);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            patientNameSpinner.setAdapter(adapter);


                            // Definisco i listener per gestire la selezione dell'utente
                            patientNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                                    // Gestire l'opzione selezionata
                                    patientName = options[position];
                                    patientId = optionsid[position];
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parentView) {
                                    // Quando nessuna opzione è selezionata
                                }
                            });

                            // Mostra il tuo AlertDialog
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.setCanceledOnTouchOutside(false);
                            // Blocca l'orientamento in portrait quando la finestra di dialogo è aperta
                            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                            alertDialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.dialog_rounded_corner_gray));
                            alertDialog.show();

                            // Ottieni le referenze agli elementi UI all'interno del layout del dialog
                            Button addButton = dialogView.findViewById(R.id.btnAdd);
                            Button cancelButton = dialogView.findViewById(R.id.btnCancel);

                            // Imposta gli onClickListener per i pulsanti del dialog
                            addButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    // Controlla che tutti i campi siano pieni
                                    if (patientNameSpinner.getSelectedItemPosition() == AdapterView.INVALID_POSITION) {
                                        // Se uno qualsiasi dei campi di testo è vuoto, mostra un messaggio di errore
                                        Toast.makeText(context, getString(R.string.inserisci_il_paziente), Toast.LENGTH_SHORT).show();
                                        return; // Esci dalla funzione se i campi di testo non sono completi
                                    }

                                    // Controlla che la data e l'ora siano selezionate
                                    if (datePicker == null || timePicker == null) {
                                        Toast.makeText(context, getString(R.string.seleziona_data_e_ora), Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    int year = datePicker.getYear();
                                    int month = datePicker.getMonth(); // Mese inizia da 0
                                    int dayOfMonth = datePicker.getDayOfMonth();

                                    int hour = timePicker.getHour();
                                    int minute = timePicker.getMinute();

                                    // Creo un oggetto Calendar e imposto la data e l'ora
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.set(year, month, dayOfMonth, hour, minute);

                                    // Crea un oggetto Appointment con i dati ottenuti
                                    if (calendar.compareTo(Calendar.getInstance()) < 0) {
                                        Toast.makeText(context, getString(R.string.creazione_dell_appuntamento_fallita_riprova), Toast.LENGTH_LONG).show();
                                    } else {
                                        Appointment newAppointment = new Appointment(User.getInstance().getCity(), User.getInstance().getAddress(), calendar, patientName, patientId);
                                        // Chiamata alla funzione in FirebaseAppointmentsModel per aggiungere l'appuntamento
                                        FirebaseAppointmentsModel.addAppointment(newAppointment, new FirebaseAppointmentsModel.OnAddAppointmentsListener() {
                                            @Override
                                            public void onAppointmentsAddSuccess() {
                                                // Chiudi il dialog con successo
                                                sendNotification(patientId, getString(R.string.un_nuovo_appuntamento_stato_fissato));
                                                alertDialog.dismiss();
                                                // Ripristina l'orientamento predefinito
                                                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                                            }

                                            @Override
                                            public void onAppointmentsAddFailure() {
                                                // Chiudi il dialog con errore
                                                Toast.makeText(context, getString(R.string.creazione_dell_appuntamento_fallita_riprova), Toast.LENGTH_LONG).show();
                                                alertDialog.dismiss();
                                                // Ripristina l'orientamento predefinito
                                                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                                            }
                                        });
                                    }

                                }
                            });

                            cancelButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // Chiudi il dialog senza fare alcuna operazione
                                    alertDialog.dismiss();
                                    // Ripristina l'orientamento predefinito
                                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                                }
                            });

                        }

                        @Override
                        public void onPatientsListReadFailure() {

                        }
                    });

                }
            });
        }

    }


    //metodo per settare sullo schermo gli appuntamenti
    void setAppointments(View view, ArrayList<Appointment> appointments) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); //Genero la vista per manipolarla

        LinearLayout linearLayout = view.findViewById(R.id.appointmentLinearLayout);
        linearLayout.removeAllViews();

        String appointmentAddress, appointmentDate, appointmentHour, appointmentCity, appointmentName;
        for (Appointment appointment : appointments) {

            appointmentAddress = appointment.getAddress();
            appointmentDate = String.valueOf(appointment.getDate());
            appointmentHour = String.valueOf(appointment.getHour());
            appointmentName = String.valueOf(appointment.getPatientName());
            appointmentCity = String.valueOf(appointment.getCity());

            View frameLayoutView = inflater.inflate(R.layout.appointment_fragment, null);
            FrameLayout frameLayout = new FrameLayout(context);
            frameLayout.addView(frameLayoutView);
            linearLayout.addView(frameLayout);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            layoutParams.topMargin = 10;
            frameLayout.setLayoutParams(layoutParams);

            //info sugli appuntamenti
            TextView date = frameLayoutView.findViewById(R.id.appointmentDate);
            date.setText(appointmentDate);

            TextView hour = frameLayoutView.findViewById(R.id.appointmentHour);
            hour.setText(appointmentHour);

            TextView address = frameLayoutView.findViewById(R.id.appointmentAddress);
            address.setText(appointmentAddress);

            TextView city = frameLayoutView.findViewById(R.id.appointmentCity);
            city.setText(appointmentCity);

            TextView patientName = frameLayoutView.findViewById(R.id.name);
            patientName.setText(getActivity().getResources().getString(R.string.patient) + ": " + appointmentName);

            frameLayoutView.setVisibility(View.GONE);

            MaterialButton delete = frameLayoutView.findViewById(R.id.delete);

            if (LocalDate.now().isBefore(LocalDate.parse(appointmentDate)) ||
                    LocalDate.now().isEqual(LocalDate.parse(appointmentDate)))
                delete.setVisibility(View.VISIBLE);

            switch (appointmentCheck) {
                case PAST:
                    //settaggio solo se la data è passata
                    if (LocalDate.now().isAfter(LocalDate.parse(appointmentDate))) {
                        frameLayoutView.setVisibility(View.VISIBLE);
                    }
                    break;

                case UPCOMING:
                    //settaggio solo se la data è prossima
                    if (LocalDate.now().isBefore(LocalDate.parse(appointmentDate)) || LocalDate.now().isEqual(LocalDate.parse(appointmentDate))) {
                        frameLayoutView.setVisibility(View.VISIBLE);
                    }
                    break;
            }

            //gestione eliminazione paziente
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //se delete è visibile
                    if (delete.getVisibility() == View.VISIBLE) {
                        //mostro finestra di dialogo
                        showDeletePatientDialog(appointment);
                    }
                }
            });
        }

    }

    //mostro una finestra di dialogo per l'eliminazione di un appuntamento
    void showDeletePatientDialog(Appointment appointment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View customView = inflater.inflate(R.layout.delete_appointment_dialog, null);

        builder.setView(customView);
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        // Blocca l'orientamento in portrait quando la finestra di dialogo è aperta
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        dialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.dialog_rounded_corner_gray));

        Button positiveButton = customView.findViewById(R.id.custom_positive_button);
        Button negativeButton = customView.findViewById(R.id.custom_negative_button);

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Azioni per il pulsante "Sì"
                FirebaseAppointmentsModel.deleteAppointment(appointment, new FirebaseAppointmentsModel.OnDeleteListener() {
                    @Override
                    public void onAppointmentDeleted() {
                        Toast.makeText(context, "Appuntamento eliminato", Toast.LENGTH_SHORT).show();
                        sendNotification(appointment.getPatientId(), getString(R.string.un_appuntamento_stato_annullato));
                        dialog.dismiss();
                        // Ripristina l'orientamento predefinito
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    }

                    @Override
                    public void onAppointmentDeleteFailed() {
                        Toast.makeText(context, "Errore nell'eliminazione dell'appuntamento", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new HomePageTherapistFragment(false, appointments, patients, patientsId)).commit();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new HomePageTherapistFragment(true, appointments, patients, patientsId)).commit();
        }

    }

    public void sendNotification(String patientId, String body){
        FirebasePatientModel.getParentToken(patientId, new FirebasePatientModel.TokenListener() {
            @Override
            public void onTokenReceived(String token) {
                FCMClient fcmClient = new FCMClient();
                FCMNotification notification = new FCMNotification(getString(R.string.Notification),body);
                FCMRequest fcmRequest = new FCMRequest(token, notification);
                fcmClient.sendNotification(fcmRequest);
            }

            @Override
            public void onTokenReceiveFailed() {

            }
        });
    }

}
