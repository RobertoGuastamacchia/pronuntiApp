package it.uniba.dib.sms232421.notifications;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FCMClient {
    private static final String BASE_URL = "https://fcm.googleapis.com/";
    private final FCMService fcmService;

    public FCMClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        fcmService = retrofit.create(FCMService.class);
    }

    public void sendNotification(FCMRequest fcmRequest) {
        Call<FCMResponse> call = fcmService.sendNotification(fcmRequest);
        call.enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if (response.isSuccessful()) {
                    // Successo, la notifica è stata inviata con successo
                    FCMResponse fcmResponse = response.body();
                } else {
                    // Errore durante l'invio della notifica
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {
                // Gestire eventuali errori di connessione
            }
        });
    }
}

