package com.yagocurvello.zapy.model;

import com.google.firebase.database.DatabaseReference;
import com.yagocurvello.zapy.config.ConfigFirebase;

public class Conversa {

    private String idEnvia;
    private String idRecebe;
    private String ultimaMsg;
    private Usuario usuarioExibicao;

    private String isGroup;
    private Grupo grupo;

    public Conversa() {
        this.setIsGroup("false");
    }

    public String getIdEnvia() {
        return idEnvia;
    }

    public void setIdEnvia(String idEnvia) {
        this.idEnvia = idEnvia;
    }

    public String getIdRecebe() {
        return idRecebe;
    }

    public void setIdRecebe(String idRecebe) {
        this.idRecebe = idRecebe;
    }

    public String getUltimaMsg() {
        return ultimaMsg;
    }

    public void setUltimaMsg(String ultimaMsg) {
        this.ultimaMsg = ultimaMsg;
    }

    public Usuario getUsuarioExibicao() {
        return usuarioExibicao;
    }

    public void setUsuarioExibicao(Usuario usuarioExibicao) {
        this.usuarioExibicao = usuarioExibicao;
    }

    public String getIsGroup() {
        return isGroup;
    }

    public void setIsGroup(String isGroup) {
        this.isGroup = isGroup;
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
    }

    public void salvar (){

        DatabaseReference conversasReference = ConfigFirebase.getFirebaseDatabase()
                .child("conversas").child(this.getIdEnvia()).child(this.getIdRecebe());

        conversasReference.setValue(this);

    }
}
