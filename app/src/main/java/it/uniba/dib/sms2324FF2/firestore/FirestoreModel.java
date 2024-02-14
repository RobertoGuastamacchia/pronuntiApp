package it.uniba.dib.sms2324FF2.firestore;

import android.content.Context;
import android.net.Uri;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class FirestoreModel {

    public interface DownloadImageCallback {
        void onImageDownloaded(Uri imageUri);

        void onDownloadFailure();
    }
    public interface DownloadAudioCallback {
        void onAudioDownloaded(String audioPath);

        void onDownloadFailure();
    }
    public interface UploadCallback {
        void onUploadSuccess();

        void onUploadFailure();
    }
    public static void downloadImage(StorageReference imgRef, Context context, DownloadImageCallback callback) {
        // Crea un file locale per salvare il file scaricato
        try {
            File localFile;
            localFile = File.createTempFile("image", "jpeg");

            // Scarica il file immagine in locale
            imgRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                // Imposta il file come immagine
                Uri imageUri = Uri.fromFile(localFile);

                if (callback != null) {
                    callback.onImageDownloaded(imageUri);
                }
            }).addOnFailureListener(exception -> {
                if (callback != null) {
                    callback.onDownloadFailure();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();

            if (callback != null) {
                callback.onDownloadFailure();
            }
        }
    }
    public static void uploadImage(String ref,Uri imageUri, UploadCallback callback){
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        // Creare un riferimento al percorso nel Firebase Storage
        StorageReference wallpaperRef = storageReference.child(ref);

        try {
            // Caricare il wallpaper su Firebase Storage utilizzando putFile()

            wallpaperRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Il caricamento è avvenuto con successo
                        if (callback != null) {
                            callback.onUploadSuccess();
                        }
                    })
                    .addOnFailureListener(exception -> {
                        // Gestire eventuali errori durante il caricamento
                        if (callback != null) {
                            callback.onUploadFailure();
                        }
                    });
        } catch (Exception e) {
            if (callback != null) {
                callback.onUploadFailure();
            }
        }
    }
    public static void downloadAudio(StorageReference audioRef, Context context, DownloadAudioCallback callback) {
        // Crea un file locale per salvare il file scaricato
        File localFile;
        try {
            localFile = File.createTempFile("audio", "mpeg");

            // Scarica il file audio in locale
            audioRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                // Il file è stato scaricato con successo
                String audioPath = localFile.getAbsolutePath();

                if (callback != null) {
                    callback.onAudioDownloaded(audioPath);
                }
            }).addOnFailureListener(exception -> {
                if (callback != null) {
                    callback.onDownloadFailure();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();

            if (callback != null) {
                callback.onDownloadFailure();
            }
        }
    }
    public static void uploadAudio(String pathRef, String audioPath, UploadCallback callback) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        // Creare un riferimento al percorso nel Firebase Storage
        StorageReference audioRef = storageReference.child("Audio/" + pathRef + ".mp3");

        try {
            // Caricare il file audio su Firebase Storage utilizzando putFile()
            InputStream stream = Files.newInputStream(new File(audioPath).toPath());
            audioRef.putStream(stream)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Il caricamento è avvenuto con successo
                        if (callback != null) {
                            callback.onUploadSuccess();
                        }
                    })
                    .addOnFailureListener(exception -> {
                        // Gestire eventuali errori durante il caricamento
                        if (callback != null) {
                            callback.onUploadFailure();
                        }
                    });
        } catch (Exception e) {
            if (callback != null) {
                callback.onUploadFailure();
            }
        }
    }
}
