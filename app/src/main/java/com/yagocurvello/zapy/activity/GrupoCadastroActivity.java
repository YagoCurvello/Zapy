package com.yagocurvello.zapy.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yagocurvello.zapy.R;
import com.yagocurvello.zapy.adapter.GrupoMembrosAdapter;
import com.yagocurvello.zapy.config.ConfigFirebase;
import com.yagocurvello.zapy.helper.Permissao;
import com.yagocurvello.zapy.helper.UsuarioFirebase;
import com.yagocurvello.zapy.model.Grupo;
import com.yagocurvello.zapy.model.Usuario;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class GrupoCadastroActivity extends AppCompatActivity {

    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };

    private List<Usuario> membros = new ArrayList<>();
    private RecyclerView recyclerViewGrupo;
    private GrupoMembrosAdapter adapterMembrosGrupo;
    private EditText editTextNomeGrupo;
    private CircleImageView circleImageViewFotoGrupo;
    private TextView textViewNumeroMembros;
    private Grupo grupo;

    StorageReference storageReference;

    private final static int SELECAO_GALERIA = 200;
    private final static int PERMISSOES = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grupo_cadastro);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Novo Grupo");
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //Configuração de elementos visuais
        recyclerViewGrupo = findViewById(R.id.recyclerViewGrupo);
        editTextNomeGrupo = findViewById(R.id.editTextNomeGrupo);
        circleImageViewFotoGrupo = findViewById(R.id.circleImageViewFotoGrupo);
        textViewNumeroMembros = findViewById(R.id.textViewNumeroMembros);

        storageReference = ConfigFirebase.getFirebaseStorage();

        grupo = new Grupo();

        //Validar permissões
        Permissao.ValidarPermissoes(permissoesNecessarias, this, PERMISSOES);

        //Recuperar lista de membros escolhidos
        if (getIntent().getExtras() != null){
            List<Usuario> listMembros = (List<Usuario>) getIntent().getExtras().getSerializable("listaMembros");
            membros.addAll(listMembros);
        }
        textViewNumeroMembros.setText("Participantes: " + (membros.size()+1));

        //RecyclerView para os contatosSelecionados
        //Configurar Adapter
        adapterMembrosGrupo = new GrupoMembrosAdapter(membros,getApplicationContext());
        recyclerViewGrupo.setAdapter(adapterMembrosGrupo);

        //Configurar RecycleView
        RecyclerView.LayoutManager layoutManagerGrupo = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewGrupo.setLayoutManager(layoutManagerGrupo);
        recyclerViewGrupo.setHasFixedSize(true);

        //Trocar imagem
        circleImageViewFotoGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (intent.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(intent, SELECAO_GALERIA);
                }
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String nomeGrupo = editTextNomeGrupo.getText().toString();
                membros.add(UsuarioFirebase.recuperarUsuarioLogado());
                grupo.setMembros(membros);
                grupo.setName(nomeGrupo);

                if (grupo.getName().isEmpty() || grupo.getName() == null){
                    Toast.makeText(GrupoCadastroActivity.this, "Digite um nome válido para o grupo", Toast.LENGTH_SHORT).show();
                }else {
                    grupo.salvar();
                    Intent intent = new Intent(GrupoCadastroActivity.this, ChatActivity.class);
                    intent.putExtra("chatGrupo", grupo);
                    startActivity(intent);
                    finish();
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
            Bitmap imagemGrupo = null;

            //Seta a imagem
            try {
                Uri localImagemSelecionada = data.getData();
                imagemGrupo = MediaStore.Images.Media.getBitmap(getContentResolver(),localImagemSelecionada);

                if (imagemGrupo != null){
                    //Setar a imagem recuperada como a foto do perfil (apenas interface)
                    circleImageViewFotoGrupo.setImageBitmap(imagemGrupo);

                    //Preparar dados da imagem para o firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagemGrupo.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    //Cria referencia de onde será salva a imagem
                    final StorageReference imagemRef = storageReference
                            .child("imagens").child("grupos").child(grupo.getId() + ".jpeg")    ;

                    //Salva a imagem na referencia acima
                    UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(GrupoCadastroActivity.this,
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
                                    String url = task.getResult().toString();
                                    grupo.setFoto(url);
                                }
                            });

                            Toast.makeText(GrupoCadastroActivity.this,
                                    "Grupo criado com sucesso",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }catch (Exception e){
                e.printStackTrace();
            }
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