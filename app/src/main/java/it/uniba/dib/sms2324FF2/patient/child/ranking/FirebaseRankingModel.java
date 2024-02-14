package it.uniba.dib.sms2324FF2.patient.child.ranking;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import it.uniba.dib.sms2324FF2.patient.Patient;

public class FirebaseRankingModel {
    static FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface  RankingCallback{
        void onSuccess(ArrayList<Ranking> rankingList);
        void onFailure();
    }

    //metodo per ottenere la classifica da firebase dei bambini in cura presso uno stesso logopedista
    public static void getRankingList(RankingCallback callback) {
        ArrayList<Ranking> rankingList = new ArrayList<>();
        String userDoctorId = Patient.getInstance().getDoctorId();
        db.collection("users")
                .whereEqualTo("doctor_id", userDoctorId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            QuerySnapshot querySnapshot = task.getResult();
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                Ranking ranking = new Ranking();
                                ranking.setId(document.getString("id"));
                                ranking.setName(document.getString("child_name"));
                                ranking.setCoin(document.getLong("coin").intValue());
                                rankingList.add(ranking);
                            }
                            callback.onSuccess(rankingList);
                        }else{
                            callback.onFailure();
                        }
                    }
                });

    }
}
