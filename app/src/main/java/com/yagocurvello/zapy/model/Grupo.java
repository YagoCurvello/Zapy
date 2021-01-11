package com.yagocurvello.zapy.model;

import com.google.firebase.database.DatabaseReference;
import com.yagocurvello.zapy.config.ConfigFirebase;
import com.yagocurvello.zapy.helper.Base64Custom;

import java.io.Serializable;
import java.util.List;

public class Grupo implements Serializable {

    private String id;
    private String name;
    private String foto;
    private List<Usuario> membros;


    public Grupo() {
        DatabaseReference databaseReference = ConfigFirebase.getFirebaseDatabase();
        DatabaseReference grupoRef = databaseReference.child("grupos");

        String idGrupo = grupoRef.push().getKey();
        setId(idGrupo);

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Usuario> getMembros() {
        return membros;
    }

    public void setMembros(List<Usuario> membros) {
        this.membros = membros;
    }

    public void salvar(){
        //Salvar grupo
        DatabaseReference databaseReference = ConfigFirebase.getFirebaseDatabase();
        DatabaseReference grupoRef = databaseReference.child("grupos");
        grupoRef.child(getId()).setValue(this);

        //Criar e Salvar conversa para todos os membros
        for (Usuario usuario : getMembros()){

            String idEnvia = Base64Custom.codificarBase64(usuario.getEmail());
            String idRecebe = getId();

            Conversa conversa = new Conversa();
            conversa.setIdEnvia(idEnvia);
            conversa.setIdRecebe(idRecebe);
            conversa.setUltimaMsg("");
            conversa.setIsGroup("true");
            conversa.setGrupo(this);

            conversa.salvar();
        }

    }
}
