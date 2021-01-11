package com.yagocurvello.zapy.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.yagocurvello.zapy.R;
import com.yagocurvello.zapy.config.ConfigFirebase;
import com.yagocurvello.zapy.model.Usuario;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editEmail, editSenha;
    Usuario usuarioLogin = new Usuario();
    FirebaseAuth autenticacao = ConfigFirebase.getFirebaseAutenticacao();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editEmail = findViewById(R.id.editEmailLogin);
        editSenha = findViewById(R.id.editSenhaLogin);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        TextView textCadastro = findViewById(R.id.textCadastro);


        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usuarioLogin.setEmail(editEmail.getText().toString());
                usuarioLogin.setSenha(editSenha.getText().toString());

                if (validaTexto(usuarioLogin)){
                    logarUsuario(usuarioLogin);
                }
            }
        });

        textCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, CadastroActivity.class));
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (autenticacao.getCurrentUser() != null){
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

    public boolean validaTexto(Usuario usuarioValidacao){
        if (!usuarioValidacao.getEmail().isEmpty()){
            if (!usuarioValidacao.getSenha().isEmpty()){
                return true;
            }else {
                Toast.makeText(getApplicationContext(), "Digite um Senha", Toast.LENGTH_SHORT).show();
                return false;
            }
        }else {
            Toast.makeText(getApplicationContext(), "Digite um Email", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public void logarUsuario(Usuario usuarioDeslogado){

        autenticacao.signInWithEmailAndPassword(usuarioDeslogado.getEmail(), usuarioDeslogado.getSenha())
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        }else{
                            String error;
                            try {
                                throw task.getException();
                            }catch (FirebaseAuthInvalidUserException e ){
                                error = "E-mail e/ou Senha inválidos";
                            }catch (FirebaseAuthInvalidCredentialsException e ){
                                error = "Senha incorreta";
                            } catch (Exception e) {
                                error = "Erro: " + e.getMessage();
                                e.printStackTrace();
                            }
                            Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
                            Log.i("Erro login", error);
                        }
                    }
                });
    }
}