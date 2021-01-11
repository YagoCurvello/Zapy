package com.yagocurvello.zapy.helper;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.yagocurvello.zapy.config.ConfigFirebase;
import com.yagocurvello.zapy.model.Usuario;

import java.util.HashMap;

public class UsuarioFirebase {

    //Metodo que retorna o id do usuario
    public static String getIdUsuario(){
        FirebaseAuth usuario = ConfigFirebase.getFirebaseAutenticacao();
        if (usuario.getCurrentUser() != null){
            return Base64Custom.codificarBase64(usuario.getCurrentUser().getEmail());
        }
        return null;
    }

    //Metodo que retorna o usuarioFirebase
    public static FirebaseUser getUsuarioAtual(){
        FirebaseAuth usuario = ConfigFirebase.getFirebaseAutenticacao();
        return usuario.getCurrentUser();
    }



    //Metodo que atualiza a foto do usuario
    public static boolean atualizarFotoUsuarioFb(Uri url){

        try {
            FirebaseUser user = getUsuarioAtual();
            UserProfileChangeRequest upcr = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(url).build();

            user.updateProfile(upcr)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful()){
                                Log.d("Perfil", "Erro ao atualizar foto de Perfil do UsuarioFirebase");
                            }
                        }
                    });
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    //Metodo que atualiza o nome do usuario
    public static boolean atualizarNomeUsuarioFb(final String name){

        try {
            FirebaseUser user = getUsuarioAtual();


            UserProfileChangeRequest upcr = new UserProfileChangeRequest.Builder()
                    .setDisplayName(name).build();

            user.updateProfile(upcr)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful()){
                                Log.d("Perfil", "Erro ao atualizar nome de Perfil do UsuarioFirebase");
                            }
                        }
                    });
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static boolean salvarUsuario(Usuario usuario){
        if (usuario.getIdUsuario() != null){
            DatabaseReference reference = ConfigFirebase.getFirebaseDatabase();
            reference.child("usuarios").child(usuario.getIdUsuario()).setValue(usuario);
            return true;
        }else {
            return false;
        }
    }


    public static void atualizarUsuario (Usuario usuario){
        String id = getIdUsuario();
        DatabaseReference firebaseDatabaseRef = ConfigFirebase.getFirebaseDatabase();
        DatabaseReference usuarioDatabaseRef = firebaseDatabaseRef.child("usuarios").child(id);

        HashMap<String, Object> usuarioMap = new HashMap<>();
        usuarioMap.put("email", usuario.getEmail());
        usuarioMap.put("name", usuario.getName());
        usuarioMap.put("foto", usuario.getFoto());

        usuarioDatabaseRef.updateChildren(usuarioMap);

    }

    public static Usuario recuperarUsuarioLogado(){
        Usuario usuario = new Usuario();
        FirebaseUser firebaseUser = getUsuarioAtual();

        usuario.setEmail(firebaseUser.getEmail());
        usuario.setName(firebaseUser.getDisplayName());

        if (firebaseUser.getPhotoUrl() == null){
            usuario.setFoto("");
        }else {
            usuario.setFoto(firebaseUser.getPhotoUrl().toString());
        }

        return usuario;
    }



}
