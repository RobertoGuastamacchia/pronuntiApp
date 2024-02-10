package it.uniba.dib.sms232421.patient.child.characters;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class FirebaseCharactersModel {
    static FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface OnCharacterCostListener {
        void onCostRead(int cost);
        void onCostReadFailed();
    }

    public static void getCost(String character, OnCharacterCostListener listener){
        db.collection("characters")
                .whereEqualTo("name", character)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                listener.onCostRead(document.getLong("coin").intValue());
                            }
                        }else{
                            listener.onCostReadFailed();
                        }
                    }
                });
    }


}
