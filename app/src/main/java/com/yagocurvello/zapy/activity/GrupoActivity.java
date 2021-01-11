package com.yagocurvello.zapy.activity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.yagocurvello.zapy.R;
import com.yagocurvello.zapy.adapter.ContatosAdapter;
import com.yagocurvello.zapy.adapter.GrupoMembrosAdapter;
import com.yagocurvello.zapy.config.ConfigFirebase;
import com.yagocurvello.zapy.helper.RecyclerItemClickListener;
import com.yagocurvello.zapy.helper.UsuarioFirebase;
import com.yagocurvello.zapy.model.Usuario;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GrupoActivity extends AppCompatActivity {

    private RecyclerView recyclerSelecionados, recyclerContatosGrupo;
    private ContatosAdapter adapterGrupo;
    private GrupoMembrosAdapter adapterMembros;
    private List<Usuario> listaMembros = new ArrayList<>();
    private List<Usuario> listaMembrosGrupo = new ArrayList<>();
    private ValueEventListener eventListenerGrupo;
    private DatabaseReference usuarioReference;
    private FirebaseUser usuarioAtual;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grupo);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Novo Grupo");
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        recyclerContatosGrupo = findViewById(R.id.recyclerContatosGrupo);
        recyclerSelecionados = findViewById(R.id.recyclerSelecionados);

        usuarioReference = ConfigFirebase.getFirebaseDatabase().child("usuarios");
        usuarioAtual = UsuarioFirebase.getUsuarioAtual();



        //RecyclerView para os contatos
        //Configurar Adapter
        adapterGrupo = new ContatosAdapter(listaMembros,getApplicationContext());
        recyclerContatosGrupo.setAdapter(adapterGrupo);

        //Configurar RecycleView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerContatosGrupo.setLayoutManager(layoutManager);
        recyclerContatosGrupo.setHasFixedSize(true);

        //Evento de click do RecyclerView
        recyclerContatosGrupo.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getApplicationContext(),
                        recyclerContatosGrupo,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view,int position) {

                                Usuario usuarioSelecionado = listaMembros.get(position);



                                //Remover usuario selecionado da lista
                                listaMembros.remove(usuarioSelecionado);
                                adapterGrupo.notifyDataSetChanged();

                                //Adicionar na outra lista
                                listaMembrosGrupo.add(usuarioSelecionado);
                                adapterMembros.notifyDataSetChanged();
                                atualizarMembrosToolbar();
                            }

                            @Override
                            public void onLongItemClick(View view,int position) {
                            }

                            @Override
                            public void onItemClick(AdapterView<?> adapterView,View view,int i,long l) {
                            }
                        })
        );

        //RecyclerView para os contatosSelecionados
        //Configurar Adapter
        adapterMembros = new GrupoMembrosAdapter(listaMembrosGrupo,getApplicationContext());
        recyclerSelecionados.setAdapter(adapterMembros);

        //Configurar RecycleView
        RecyclerView.LayoutManager layoutManagerGrupo = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerSelecionados.setLayoutManager(layoutManagerGrupo);
        recyclerSelecionados.setHasFixedSize(true);

        //Evento de click do RecyclerView
        recyclerSelecionados.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getApplicationContext(),
                        recyclerSelecionados,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view,int position) {
                                Usuario usuarioSelecionadoGrupo = listaMembrosGrupo.get(position);

                                //Remover usuario selecionado da lista
                                listaMembros.add(usuarioSelecionadoGrupo);
                                adapterGrupo.notifyDataSetChanged();

                                //Adicionar na outra lista
                                listaMembrosGrupo.remove(usuarioSelecionadoGrupo);
                                adapterMembros.notifyDataSetChanged();
                                atualizarMembrosToolbar();
                            }

                            @Override
                            public void onLongItemClick(View view,int position) {
                            }

                            @Override
                            public void onItemClick(AdapterView<?> adapterView,View view,int i,long l) {
                            }
                        })
        );

        FloatingActionButton fab = findViewById(R.id.button_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GrupoActivity.this, GrupoCadastroActivity.class);
                intent.putExtra("listaMembros",(Serializable) listaMembrosGrupo);
                startActivity(intent);
            }
        });
    }

        public void recuperarContatos(){

            eventListenerGrupo = usuarioReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot data : snapshot.getChildren()){
                        Usuario usuario = data.getValue(Usuario.class);
                        if (!usuarioAtual.getEmail().equals(usuario.getEmail())){ //Para não adicionar o proprio contato
                            listaMembros.add(usuario);
                        }
                    }
                    adapterGrupo.notifyDataSetChanged();
                    atualizarMembrosToolbar();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }

        public void atualizarMembrosToolbar(){

            int totalSelecionados = listaMembrosGrupo.size();
            int total = listaMembros.size() + listaMembrosGrupo.size();

            toolbar.setSubtitle(totalSelecionados + " de " + total + " Selecionados");
        }

        @Override
        public void onStart() {
            super.onStart();
            recuperarContatos();
        }

        @Override
        public void onStop() {
            super.onStop();
            usuarioReference.removeEventListener(eventListenerGrupo);
        }

}