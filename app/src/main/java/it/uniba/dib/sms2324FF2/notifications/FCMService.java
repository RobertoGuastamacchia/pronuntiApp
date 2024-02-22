package it.uniba.dib.sms2324FF2.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface FCMService {
    @Headers({
            "Content-Type: application/json",
            "Authorization: key=AAAAz6RPts8:APA91bGi0oQOpha9HRQQjsr-fhurMDFrAtAJ1thIOPa-IOaWljH0DqHEZhbmQTNXZ7Ic9h-gwJ-gq0xNjK75fI1IKmwjxuQ_nB6tBmLJQDVFtck0S31uUjbMIQqNnjG8qHPq55fPHtWl" // Sostituire con il tuo server key
    })

    @POST("fcm/send")
    Call<FCMResponse> sendNotification(@Body FCMRequest body);
}
