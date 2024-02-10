package it.uniba.dib.sms232421.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface FCMService {
    @Headers({
            "Content-Type: application/json",
            "Authorization: key=AAAA6pLX2-s:APA91bHrKcn21aj6cYBJhNl7Ih06y9HCCN8GREaK1F_-glzTRDW4k6aaHjCnw5iEOsRqZlhATPT7dNnaxVxD7fCtFd5eUvoIb5ZU7Sw2ri6Hub0DE1XyVSzwLVefdGUC3FentS5s43n7" // Sostituire con il tuo server key
    })

    @POST("fcm/send")
    Call<FCMResponse> sendNotification(@Body FCMRequest body);
}
