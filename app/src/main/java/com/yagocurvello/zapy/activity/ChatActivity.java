package com.yagocurvello.zapy.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yagocurvello.zapy.R;
import com.yagocurvello.zapy.adapter.MensagensAdapter;
import com.yagocurvello.zapy.config.ConfigFirebase;
import com.yagocurvello.zapy.helper.Base64Custom;
import com.yagocurvello.zapy.helper.UsuarioFirebase;
import com.yagocurvello.zapy.model.Conversa;
import com.yagocurvello.zapy.model.Grupo;
import com.yagocurvello.zapy.model.Mensagem;
import com.yagocurvello.zapy.model.Usuario;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private TextView textViewNome;
    private CircleImageView circleImageViewFoto;
    private Usuario usuarioDestinatario;
    private Grupo grupo;
    private EditText editMensagem;
    private ImageView imageCameraChat;
    private String idUsuarioEnvia, idUsuarioRecebe;
    private RecyclerView recyclerMensagens;
    private List<Mensagem> listaMensagens = new ArrayList<>();
    private DatabaseReference databaseReference;
    private DatabaseReference mensagensReference;
    private StorageReference storageReference;
    private ChildEventListener childEventListenerMensagens;
    private MensagensAdapter adapterMensagens;
    private Usuario usuarioLogado;
    private final static int SELECAO_CAMERA = 100;
    private final static int SELECAO_GALERIA = 200;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Configuração toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Configurações iniciais
        textViewNome = findViewById(R.id.textViewNomeChat);
        circleImageViewFoto = findViewById(R.id.circleImageFotoChat);
        editMensagem = findViewById(R.id.editMensagem);
        imageCameraChat = findViewById(R.id.imageViewCameraChat);
        recyclerMensagens = findViewById(R.id.recycleMensagens);

        usuarioLogado = UsuarioFirebase.recuperarUsuarioLogado(); //Usuario que envia a mensagem

        //Recuperando dados do contato
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){

            if (bundle.containsKey("chatGrupo")){

                grupo = (Grupo) bundle.getSerializable("chatGrupo");
                textViewNome.setText(grupo.getName());
                idUsuarioRecebe = grupo.getId();
                if (grupo.getFoto() != null){                                 //Seta a imagem perfil caso o Grupo tenha uma foto
                    Uri url = Uri.parse(grupo.getFoto());
                    Glide.with(ChatActivity.this).load(url).into(circleImageViewFoto);
                }else {
                    circleImageViewFoto.setImageResource(R.drawable.padrao);
                }

            }else {
                usuarioDestinatario = (Usuario) bundle.getSerializable("chatContato");  //Recupera objeto Usuario
                textViewNome.setText(usuarioDestinatario.getName());                        //Seta o campo nome
                //Recuperar idusuario
                idUsuarioRecebe = Base64Custom
                        .codificarBase64(usuarioDestinatario.getEmail());
                if (usuarioDestinatario.getFoto() != null){                                 //Seta a imagem perfil caso o Usuario tenha uma foto
                    Uri url = Uri.parse(usuarioDestinatario.getFoto());
                    Glide.with(ChatActivity.this).load(url).into(circleImageViewFoto);
                }else {
                    circleImageViewFoto.setImageResource(R.drawable.padrao);
                }
            }
            }

        idUsuarioEnvia = UsuarioFirebase.getIdUsuario();

        //Definir referencias de banco de dados
        databaseReference = ConfigFirebase.getFirebaseDatabase();
        mensagensReference = databaseReference.child("mensagens").child(idUsuarioEnvia).child(idUsuarioRecebe);
        storageReference = ConfigFirebase.getFirebaseStorage();

        //Configurar Adapter
        adapterMensagens = new MensagensAdapter(listaMensagens, this);
        recyclerMensagens.setAdapter(adapterMensagens);

        //Configurar RecycleView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerMensagens.setLayoutManager(layoutManager);
        recyclerMensagens.smoothScrollToPosition(13);

        //Abrir camera
        imageCameraChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enviarFoto();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,@Nullable Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        if (resultCode == RESULT_OK) {
            Bitmap imagem = null;

            //Seta a imagem de acordo com a 'fonte' dela (camera ou galeria) pelo RequestCode
            try {
                switch (requestCode) {
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

                    //Recuperar dados da imagem para o firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    //Cria um nome para cada imagem
                    String nomeImagem = UUID.randomUUID().toString();

                    //Cria referencia de onde será salva a imagem
                    final StorageReference imagemMensagemRef = storageReference
                            .child("imagens").child("fotos").child(idUsuarioEnvia).child(nomeImagem);

                    //Salva a imagem na referencia acima
                    UploadTask uploadTask = imagemMensagemRef.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("Erro", "Erro ao fazer upload da imagem");
                            Toast.makeText(ChatActivity.this,
                                    "Falha ao fazer upload da imagem",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imagemMensagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    String downloadUrl = task.getResult().toString();

                                    if (usuarioDestinatario != null){ //Mensagem normal
                                        Mensagem mensagem = new Mensagem();
                                        mensagem.setIdUsuario(idUsuarioEnvia);
                                        mensagem.setMensagem("imagem.jpeg");
                                        mensagem.setImagem(downloadUrl);

                                        salvarMensagem(mensagem, idUsuarioRecebe, idUsuarioEnvia);
                                    }else{ //Mensagem grupo
                                        //Cria uma mensagem e uma conversa para cada integrante do grupo
                                        for (Usuario membro : grupo.getMembros()) {

                                            String idMembroGrupo = Base64Custom.codificarBase64(membro.getEmail());
                                            String idUsuarioLogadoGrupo = UsuarioFirebase.getIdUsuario();

                                            Mensagem mensagem = new Mensagem();
                                            mensagem.setIdUsuario(idUsuarioLogadoGrupo);
                                            mensagem.setMensagem("imagem.jpeg");
                                            mensagem.setImagem(downloadUrl);
                                            mensagem.setNome(usuarioLogado.getName());

                                            //Salvar mensagem
                                            salvarMensagem(mensagem,idMembroGrupo,idUsuarioRecebe); //idUsuarioRecebe = grupo.getId (configurado na criação)
                                            //Salvar conversa do grupo (não precisa salvar para quem recebe pois o grupo não é um usuario, então não precisa acessar as mensagens)
                                            salvarConversa(mensagem, idMembroGrupo, idUsuarioRecebe, usuarioDestinatario,true);
                                        }
                                    }
                                }
                            });
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Recupera lista de mensagens
        recuperaListaMensagens();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mensagensReference.removeEventListener(childEventListenerMensagens);
    }

    public void enviarMensagem (View view){
        String textoMensagem = editMensagem.getText().toString();

        //Se possui um UsuarioExibicao (Usuario Destinatario) logo, é uma conversa comum
        if (usuarioDestinatario != null){
            if (!textoMensagem.isEmpty()){

                Mensagem mensagem = new Mensagem();
                mensagem.setIdUsuario(idUsuarioEnvia);
                mensagem.setMensagem(textoMensagem);

                //Salvar mensagem
                salvarMensagem(mensagem, idUsuarioEnvia, idUsuarioRecebe);

                //Salvar conversa para quem envia
                salvarConversa(mensagem, idUsuarioEnvia, idUsuarioRecebe, usuarioDestinatario,false);

                //Salvar conversa para quem recebe
                salvarConversa(mensagem, idUsuarioRecebe, idUsuarioEnvia, usuarioLogado,false);
            }
        } else {
            //Cria uma mensagem e uma conversa para cada integrante do grupo
            for (Usuario membro : grupo.getMembros()){

                String idMembroGrupo = Base64Custom.codificarBase64(membro.getEmail());
                String idUsuarioLogadoGrupo = UsuarioFirebase.getIdUsuario();

                Mensagem mensagem = new Mensagem();
                mensagem.setIdUsuario(idUsuarioLogadoGrupo);
                mensagem.setMensagem(textoMensagem);
                mensagem.setNome(usuarioLogado.getName());

                //Salvar mensagem
                salvarMensagem(mensagem, idMembroGrupo, idUsuarioRecebe); //idUsuarioRecebe = grupo.getId (configurado na criação)

                //Salvar conversa do grupo (não precisa salvar para quem recebe pois o grupo não é um usuario, então não precisa acessar as mensagens)
                salvarConversa(mensagem, idMembroGrupo, idUsuarioRecebe, usuarioDestinatario,true);
            }
        }
        recyclerMensagens.smoothScrollToPosition(recyclerMensagens.getAdapter().getItemCount());
    }

    public void salvarConversa(Mensagem mensagem, String idUsuarioEnvia, String idUsuarioRecebe, Usuario usuarioExibicao, boolean isGroup){
        //Salva a conversa para quem envia
        Conversa conversaEnvia = new Conversa();
        conversaEnvia.setIdEnvia(idUsuarioEnvia);
        conversaEnvia.setIdRecebe(idUsuarioRecebe);
        conversaEnvia.setUltimaMsg(mensagem.getMensagem());

        if (isGroup){
            conversaEnvia.setIsGroup("true");
            conversaEnvia.setGrupo(grupo);

        }else {
            conversaEnvia.setIsGroup("false");
            conversaEnvia.setUsuarioExibicao(usuarioExibicao);
        }
        conversaEnvia.salvar();
    }

    public void salvarMensagem(Mensagem mensagem, String idUsuarioEnvia, String idUsuarioRecebe){

            mensagensReference = databaseReference.child("mensagens").child(idUsuarioEnvia).child(idUsuarioRecebe);
            mensagensReference.push().setValue(mensagem);

            mensagensReference = databaseReference.child("mensagens").child(idUsuarioRecebe).child(idUsuarioEnvia);
            mensagensReference.push().setValue(mensagem);
            editMensagem.setText("");
    }

    private void recuperaListaMensagens(){

        listaMensagens.clear();

        childEventListenerMensagens = mensagensReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot,@Nullable String previousChildName) {
                Mensagem mensagem = snapshot.getValue(Mensagem.class);
                listaMensagens.add(mensagem);
                //Coloca o scroll na ultima mensagem
                recyclerMensagens.smoothScrollToPosition(recyclerMensagens.getAdapter().getItemCount());
                adapterMensagens.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot,@Nullable String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot,@Nullable String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public void enviarFoto(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, SELECAO_CAMERA);
        }
    }
}