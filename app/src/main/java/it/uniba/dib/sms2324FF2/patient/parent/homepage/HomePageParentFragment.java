package it.uniba.dib.sms2324FF2.patient.parent.homepage;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;
import java.time.LocalDate;
import java.util.ArrayList;
import it.uniba.dib.sms2324FF2.R;
import it.uniba.dib.sms2324FF2.appointments.Appointment;
import it.uniba.dib.sms2324FF2.appointments.FirebaseAppointmentsModel;

public class HomePageParentFragment extends Fragment {

    public static final String UPCOMING = "Upcoming";
    public static final String PAST = "Past";
    private boolean isPortrait = true;
    Context context;
    String appointmentCheck = UPCOMING;
    ArrayList<Appointment> appointments;
    private onHomePageListener mCallback;
    private Toolbar toolbar;


    public HomePageParentFragment(ArrayList<Appointment> appointments){
        this.appointments = appointments;
    }

    public HomePageParentFragment(boolean isPortrait, ArrayList<Appointment> appointments){
        this.isPortrait = isPortrait;
        this.appointments = appointments;
    }


    public interface onHomePageListener {
        void refreshHomePageData();
        void finishActivity();
        void hideBottomBar();
        void showBottomBar();
        void setToolbar(Context context, Toolbar toolbar);
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
        if(isPortrait){
            view = inflater.inflate(R.layout.fragment_home_page_parent, container, false);
            mCallback.showBottomBar();
        }else{
            view = inflater.inflate(R.layout.landscape_layout, container, false);
            mCallback.hideBottomBar();
        }

        toolbar = view.findViewById(R.id.toolbar);
        mCallback.setToolbar(context,toolbar);


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
                    mCallback.refreshHomePageData();
                }
            });

            MaterialButton past = view.findViewById(R.id.past);
            MaterialButton future = view.findViewById(R.id.future);

            //default
            //cambia colori
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
                    past.setEnabled(false);
                    future.setEnabled(true);

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

                }
            });
        }
    }

    //metodo per settare sullo schermo gli appuntamenti
    void setAppointments(View view, ArrayList<Appointment> appointments){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        LinearLayout linearLayout = view.findViewById(R.id.appointmentLinearLayout);
        linearLayout.removeAllViews();

        String therapistName, appointmentCity, appointmentAddress, appointmentDate, appointmentHour;
        for(Appointment appointment : appointments) {
            therapistName = appointment.getDoctorName();
            appointmentCity = appointment.getCity();
            appointmentAddress = appointment.getAddress();
            appointmentDate = String.valueOf(appointment.getDate());
            appointmentHour = String.valueOf(appointment.getHour());

            View frameLayoutView = inflater.inflate(R.layout.appointment_fragment, null);
            FrameLayout frameLayout = new FrameLayout(context);
            frameLayout.addView(frameLayoutView);

            // Imposta i margini superiori e inferiori per il FrameLayout
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(0, 20, 0, 20);
            frameLayout.setLayoutParams(layoutParams);

            linearLayout.addView(frameLayout);

            //info sugli appuntamenti
            TextView date = frameLayoutView.findViewById(R.id.appointmentDate);
            date.setText(appointmentDate);

            TextView hour = frameLayoutView.findViewById(R.id.appointmentHour);
            hour.setText(appointmentHour);

            TextView city = frameLayoutView.findViewById(R.id.appointmentCity);
            city.setText(appointmentCity);

            TextView address = frameLayoutView.findViewById(R.id.appointmentAddress);
            address.setText(appointmentAddress);

            TextView therapist = frameLayoutView.findViewById(R.id.name);
            therapist.setText(String.format("%s%s", getResources().getString(R.string.doctor), therapistName));

            frameLayoutView.setVisibility(View.GONE);

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
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new HomePageParentFragment(false, appointments)).commit();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new HomePageParentFragment(true, appointments)).commit();
        }

    }

}