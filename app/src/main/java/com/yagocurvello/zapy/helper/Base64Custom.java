package com.yagocurvello.zapy.helper;

import android.util.Base64;

/* Classe para Codificar/Decodificar textos */
public class Base64Custom {

    // Metodo de codificação
    public static String codificarBase64(String texto){
        return Base64.encodeToString(texto.getBytes(),Base64.DEFAULT).replaceAll("(\\n|\\r)", "");
    }

    //Metodo de decodificação
    public static String decodificarBase64(String texto){
        return new String(Base64.decode(texto, Base64.DEFAULT));
    }
}

