package com.yagocurvello.zapy.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ConfigFirebase {

    private static FirebaseAuth autenticacao;
    private static DatabaseReference databaseReference;
    private static StorageReference firebaseStorage;

    //Metodo que retorna a instancia de autenticação de usuarios do Firebase
    public static FirebaseAuth getFirebaseAutenticacao (){

        if (autenticacao == null){
            autenticacao = FirebaseAuth.getInstance();
        }
        return autenticacao;
    }

    //Metodo que retorna uma referencia Do banco de dados do Firebase para salvar/recuperar dados
    public static DatabaseReference getFirebaseDatabase(){
        if(databaseReference == null){
            databaseReference = FirebaseDatabase.getInstance().getReference();
        }
        return databaseReference;
    }

    public static StorageReference getFirebaseStorage(){
        if (firebaseStorage == null){
            firebaseStorage = FirebaseStorage.getInstance().getReference();
        }
        return firebaseStorage;
    }
}
