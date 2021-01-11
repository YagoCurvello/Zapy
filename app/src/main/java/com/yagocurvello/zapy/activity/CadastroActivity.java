package com.yagocurvello.zapy.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.yagocurvello.zapy.R;
import com.yagocurvello.zapy.config.ConfigFirebase;
import com.yagocurvello.zapy.helper.Base64Custom;
import com.yagocurvello.zapy.helper.UsuarioFirebase;
import com.yagocurvello.zapy.model.Usuario;

public class CadastroActivity extends AppCompatActivity {

    private TextInputEditText editNome, editEmail, editSenha;
    Usuario usuarioCadastro = new Usuario();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        editEmail = findViewById(R.id.editEmailCadastro);
        editNome = findViewById(R.id.editNomeCadastro);
        editSenha = findViewById(R.id.editSenhaCadastro);
        Button buttonCadastro = findViewById(R.id.buttonCadastro);
        TextView textLogin = findViewById(R.id.textLogin);

        buttonCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usuarioCadastro.setSenha(editSenha.getText().toString());
                usuarioCadastro.setEmail(editEmail.getText().toString());
                usuarioCadastro.setName(editNome.getText().toString());

                if (validaTexto(usuarioCadastro)){
                    cadastroUsuario(usuarioCadastro);
                }
            }
        });


        textLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CadastroActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    public boolean validaTexto(Usuario usuarioValidacao){
        if (!usuarioValidacao.getName().isEmpty()){
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
        }else {
            Toast.makeText(getApplicationContext(), "Digite uma Usuario", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public void cadastroUsuario(final Usuario usuarioCadastrado){
        FirebaseAuth autenticacao = ConfigFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
                usuarioCadastrado.getEmail(), usuarioCadastrado.getSenha()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    String IdUsuario = Base64Custom.codificarBase64(usuarioCadastrado.getEmail());
                    usuarioCadastrado.setIdUsuario(IdUsuario);
                    usuarioCadastrado.setFoto("");
                    if (UsuarioFirebase.salvarUsuario(usuarioCadastrado)){
                        UsuarioFirebase.atualizarNomeUsuarioFb(usuarioCadastrado.getName());
                        startActivity(new Intent(CadastroActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(CadastroActivity.this, "Erro ao salvar Usuario no banco de dados",
                                Toast.LENGTH_LONG).show();
                    }

                }else {
                    String error;
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthWeakPasswordException e) {
                        error = "Senha fraca";
                    } catch (FirebaseAuthInvalidCredentialsException e){
                        error = "email inválido";
                    } catch (FirebaseAuthUserCollisionException e){
                        error = "email já cadastrado";
                    }catch (Exception e){
                        error = "Erro: " + e.getMessage();
                        e.printStackTrace();
                    }
                    Toast.makeText(CadastroActivity.this, error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

