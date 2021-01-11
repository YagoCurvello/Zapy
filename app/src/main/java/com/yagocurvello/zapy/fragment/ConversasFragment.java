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
import com.yagocurvello.zapy.adapter.ConversasAdapter;
import com.yagocurvello.zapy.config.ConfigFirebase;
import com.yagocurvello.zapy.helper.RecyclerItemClickListener;
import com.yagocurvello.zapy.helper.UsuarioFirebase;
import com.yagocurvello.zapy.model.Conversa;

import java.util.ArrayList;
import java.util.List;

public class ConversasFragment extends Fragment {

    private RecyclerView recyclerViewConversas;
    private ConversasAdapter adapterConversas;
    private List<Conversa> listConversas = new ArrayList<>();
    private DatabaseReference databaseReferenceConversas;
    private ValueEventListener eventListenerConversa;


    public ConversasFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,
                             Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.fragment_conversas,container,false);

        databaseReferenceConversas = ConfigFirebase.getFirebaseDatabase().child("conversas");
        recyclerViewConversas = view.findViewById(R.id.recyclerViewConversas);


        //Configurar Adapter
        adapterConversas = new ConversasAdapter(listConversas, getActivity());
        recyclerViewConversas.setAdapter(adapterConversas);

        //Configurar RecycleView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerViewConversas.setLayoutManager(layoutManager);
        recyclerViewConversas.setHasFixedSize(true);

        //Evento de click do RecyclerView
        recyclerViewConversas.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(),
                        recyclerViewConversas,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view,int position) {

                                List<Conversa> conversaListAtualizada = adapterConversas.getConversas();
                                Conversa conversaSelecionada = conversaListAtualizada.get(position);

                                if (conversaSelecionada.getIsGroup().equals("true")){
                                    Intent intent = new Intent(getActivity(),ChatActivity.class);
                                    intent.putExtra("chatGrupo", conversaSelecionada.getGrupo());
                                    startActivity(intent);
                                } else {
                                    Intent intent = new Intent(getActivity(),ChatActivity.class);
                                    intent.putExtra("chatContato", conversaSelecionada.getUsuarioExibicao());
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

       return view;
    }

    public void pesquisarConversas (String texto){

        List<Conversa> conversaListPesquisa = new ArrayList<>();

        for (Conversa conversa : listConversas) {
            if (conversa.getUsuarioExibicao() != null) {
                if (conversa.getUsuarioExibicao().getName().toLowerCase().contains(texto.toLowerCase())) {
                    conversaListPesquisa.add(conversa);
                }
            } else {
                if (conversa.getGrupo().getName().toLowerCase().contains(texto.toLowerCase())) {
                    conversaListPesquisa.add(conversa);
                }
            }
        }

        adapterConversas = new ConversasAdapter(conversaListPesquisa, getActivity());
        recyclerViewConversas.setAdapter(adapterConversas);
        adapterConversas.notifyDataSetChanged();
    }

    public void recarregarConversas(){
        adapterConversas = new ConversasAdapter(listConversas, getActivity());
        recyclerViewConversas.setAdapter(adapterConversas);
        adapterConversas.notifyDataSetChanged();
    }

    public void recuperarConversas(){

        listConversas.clear();

        final String idUsuario = UsuarioFirebase.getIdUsuario();

        eventListenerConversa = databaseReferenceConversas.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.child(idUsuario).getChildren()){
                    Conversa conversa = data.getValue(Conversa.class);
                    listConversas.add(conversa);
                }
                adapterConversas.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarConversas();
    }

    @Override
    public void onStop() {
        super.onStop();
        databaseReferenceConversas.removeEventListener(eventListenerConversa);
    }

}