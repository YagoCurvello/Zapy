package com.yagocurvello.zapy.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.yagocurvello.zapy.R;
import com.yagocurvello.zapy.activity.ChatActivity;
import com.yagocurvello.zapy.activity.GrupoActivity;
import com.yagocurvello.zapy.adapter.ContatosAdapter;
import com.yagocurvello.zapy.config.ConfigFirebase;
import com.yagocurvello.zapy.helper.RecyclerItemClickListener;
import com.yagocurvello.zapy.helper.UsuarioFirebase;
import com.yagocurvello.zapy.model.Usuario;

import java.util.ArrayList;
import java.util.List;


public class ContatosFragment extends Fragment {

    private RecyclerView recyclerViewContato;
    private ContatosAdapter adapterContatos;
    private ArrayList<Usuario> listaContatos = new ArrayList<>();
    private DatabaseReference databaseReference;
    private ValueEventListener eventListenerContatos;
    private FirebaseUser usuarioAtual;

    public ContatosFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_contatos,container,false);

        databaseReference = ConfigFirebase.getFirebaseDatabase().child("usuarios");
        recyclerViewContato = view.findViewById(R.id.recyclerViewContatos);
        usuarioAtual = UsuarioFirebase.getUsuarioAtual();


        //Configurar Adapter
        adapterContatos = new ContatosAdapter(listaContatos, getActivity());
        recyclerViewContato.setAdapter(adapterContatos);

        //Configurar RecycleView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerViewContato.setLayoutManager(layoutManager);
        recyclerViewContato.setHasFixedSize(true);

        //Evento de click do RecyclerView
        recyclerViewContato.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(),
                        recyclerViewContato,
                        new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view,int position) {

                        List<Usuario> usuarioListAtualizada = adapterContatos.getListContatos();
                        Usuario usuarioSelecionado = usuarioListAtualizada.get(position);

                        boolean cabecalho = usuarioSelecionado.getEmail().isEmpty();
                        if (cabecalho){
                            Intent intent = new Intent(getActivity(),GrupoActivity.class);
                            startActivity(intent);
                        }else {
                            Intent intent = new Intent(getActivity(),ChatActivity.class);
                            intent.putExtra("chatContato", usuarioSelecionado);
                            startActivity(intent);
                        }
                    }
                    @Override
                    public void onLongItemClick(View view,int position) {
                    }
                    @Override
                    public void onItemClick(AdapterView<?> adapterView,View view,int i,long l) {
                    }
                })
        );

        adicionarMenuNovoGrupo();

        return view;
    }

    public void recuperarContatos(){

        eventListenerContatos = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                limparListaContatos();

                for (DataSnapshot data : snapshot.getChildren()){
                    Usuario usuario = data.getValue(Usuario.class);
                    if (!usuarioAtual.getEmail().equals(usuario.getEmail())){ //Para não adicionar o proprio contato
                        listaContatos.add(usuario);
                    }
                }
                adapterContatos.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarContatos();
    }

    @Override
    public void onStop() {
        super.onStop();
        databaseReference.removeEventListener(eventListenerContatos);
    }

    public void  limparListaContatos(){
        listaContatos.clear();
        adicionarMenuNovoGrupo();
    }

    public void adicionarMenuNovoGrupo(){
        Usuario itemGrupo = new Usuario();
        itemGrupo.setName("Novo Grupo");
        itemGrupo.setEmail("");
        listaContatos.add(itemGrupo);
    }

    public void pesquisarContatos (String texto){

        List<Usuario> contatosListPesquisa = new ArrayList<>();

        for ( Usuario usuario : listaContatos) {
            if (usuario.getName().toLowerCase().contains(texto.toLowerCase())) {
                contatosListPesquisa.add(usuario);
            }
        }

        adapterContatos = new ContatosAdapter(contatosListPesquisa, getActivity());
        recyclerViewContato.setAdapter(adapterContatos);
        adapterContatos.notifyDataSetChanged();
    }

    public void recarregarContatos(){
        adapterContatos = new ContatosAdapter(listaContatos, getActivity());
        recyclerViewContato.setAdapter(adapterContatos);
        adapterContatos.notifyDataSetChanged();
    }
}