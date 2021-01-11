package com.yagocurvello.zapy.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yagocurvello.zapy.R;
import com.yagocurvello.zapy.config.ConfigFirebase;
import com.yagocurvello.zapy.helper.Permissao;
import com.yagocurvello.zapy.helper.UsuarioFirebase;
import com.yagocurvello.zapy.model.Usuario;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConfiguracoesActivity extends AppCompatActivity {

    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    //RequestCodes para permissoes
    private static final int SELECAO_CAMERA = 100;
    private static final int SELECAO_GALERIA = 200;
    private static final int PERMISSOES = 300;

    private CircleImageView fotoPerfil;
    private Button buttonGaleria, buttonCamera;
    private EditText editName;
    private ImageView editarNomeView;

    private StorageReference storageReference;
    private String IdUsuario;

    private Usuario usuarioLogado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes);

        //Referenciamento de elementos visuais
        buttonGaleria = findViewById(R.id.buttonGaleria);
        buttonCamera = findViewById(R.id.buttonCamera);
        fotoPerfil = findViewById(R.id.circleImageViewFotoPerfil);
        editName = findViewById(R.id.editTextName);
        editarNomeView = findViewById(R.id.editarNomeView);

        usuarioLogado = UsuarioFirebase.recuperarUsuarioLogado();

        //Configurações iniciais
        storageReference = ConfigFirebase.getFirebaseStorage();
        IdUsuario = UsuarioFirebase.getIdUsuario();

        //Validar permissões
        Permissao.ValidarPermissoes(permissoesNecessarias, this, PERMISSOES);

        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Configurações");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Recuperar dados do UsuarioFirebase
        FirebaseUser usuarioFirebase = UsuarioFirebase.getUsuarioAtual();

        //Recuperar foto do UsuarioFirebase e abrir na imagem
        Uri url = usuarioFirebase.getPhotoUrl();
        if (url != null){
            Glide.with(ConfiguracoesActivity.this).load(url).into(fotoPerfil);
        }else{
            fotoPerfil.setImageResource(R.drawable.padrao);
        }

        //Recuperar nome do UsuarioFirebase
        editName.setText(usuarioFirebase.getDisplayName());

        //Botao para ligar camera
        buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(intent, SELECAO_CAMERA);
                }

            }
        });

        //Botao para abrir galeria
        buttonGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (intent.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(intent, SELECAO_GALERIA);
                }
            }
        });

        //Botao para salvar nome
        editarNomeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (UsuarioFirebase.atualizarNomeUsuarioFb(editName.getText().toString())){
                    usuarioLogado.setName(editName.getText().toString());
                    UsuarioFirebase.atualizarUsuario(usuarioLogado);
                    Toast.makeText(ConfiguracoesActivity.this,
                            "Nome atualizado com sucesso", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    //Metodo chamado para recuperar o resultado de uma Intent
    @Override
    protected void onActivityResult(int requestCode,int resultCode,@Nullable Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        //Se existir um resultado, cria uma imagem vazia
        if (resultCode == RESULT_OK){
            Bitmap imagem = null;

            //Seta a imagem de acordo com a 'fonte' dela (camera ou galeria) de acordo com o RequestCode
            try {
                switch (requestCode){
                    case SELECAO_CAMERA:
                        imagem = (Bitmap) data.getExtras().get("data");
                        break;

                    case SELECAO_GALERIA:
                        Uri localImagemSelecionada = data.getData();
                        imagem = MediaStore.Images.Media.getBitmap(getContentResolver(),
                                localImagemSelecionada);
                        break;
                }

                if (imagem != null){
                    //Setar a imagem recuperada como a foto do perfil (apenas interface)
                    fotoPerfil.setImageBitmap(imagem);

                    //Recuperar dados da imagem para o firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    //Cria referencia de onde será salva a imagem
                    final StorageReference imagemRef = storageReference
                            .child("imagens").child("perfil").child(IdUsuario + ".jpeg")    ;

                    //Salva a imagem na referencia acima
                    UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ConfiguracoesActivity.this,
                                    "Falha ao fazer upload da imagem",
                                 Toast.LENGTH_LONG).show();
                        }
                    });
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    Uri url = task.getResult();
                                    atualizaFotoUsuario(url);
                                }
                            });

                            Toast.makeText(ConfiguracoesActivity.this,
                                    "Upload realizado com sucesso",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void atualizaFotoUsuario(Uri url){
        if (UsuarioFirebase.atualizarFotoUsuarioFb(url)){
            usuarioLogado.setFoto(url.toString());
            UsuarioFirebase.atualizarUsuario(usuarioLogado);
            Toast.makeText(ConfiguracoesActivity.this, "Sua foto foi alterada",
                    Toast.LENGTH_SHORT).show();;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions,@NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);

        for (int permissaoResultado : grantResults){
            if (permissaoResultado == PackageManager.PERMISSION_DENIED){
                alertaValidacaoPermissao();
            }
        }
    }

    private void alertaValidacaoPermissao(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissoes negadas");
        builder.setMessage("Para utilizar esse app, é necessario que sejam concedidas todas as permissões pedidas");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface,int i) {
                finish();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}