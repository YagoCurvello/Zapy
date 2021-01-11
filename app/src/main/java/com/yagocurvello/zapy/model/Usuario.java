package com.yagocurvello.zapy.model;

import com.google.firebase.database.Exclude;

import java.io.Serializable;


public class Usuario implements Serializable {

    private String name;
    private String email;
    private String senha;
    private String IdUsuario;
    private String foto;

    public Usuario() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Exclude
    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    @Exclude
    public String getIdUsuario() {
        return IdUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        IdUsuario = idUsuario;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

}

