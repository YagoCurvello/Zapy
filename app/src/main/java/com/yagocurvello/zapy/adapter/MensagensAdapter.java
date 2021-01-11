package com.yagocurvello.zapy.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.yagocurvello.zapy.R;
import com.yagocurvello.zapy.helper.UsuarioFirebase;
import com.yagocurvello.zapy.model.Mensagem;

import java.util.List;


public class MensagensAdapter extends RecyclerView.Adapter<MensagensAdapter.MyViewHolder> {

    private List<Mensagem> listMensagens;
    Context context;

    private static final int TIPO_ENVIA = 0;
    private static final int TIPO_RECEBE = 1;


    public MensagensAdapter(List<Mensagem> listMensagens, Context context) {
        this.listMensagens = listMensagens;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent,int viewType) {

        View itemLista = null;
        if (viewType == TIPO_ENVIA){
            itemLista = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.adapter_mensagens_envia, parent, false);
        } else if ((viewType == TIPO_RECEBE)){
            itemLista = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.adapter_mensagens_recebe, parent, false);
        }
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder,int position) {
        Mensagem mensagem = listMensagens.get(position);
        String msg = mensagem.getMensagem();
        String imagem = mensagem.getImagem();

        if (imagem!=null){
            Uri url = Uri.parse(imagem);
            Glide.with(context).load(url).into(holder.imagem);
            holder.mensagem.setVisibility(View.GONE);

            String nome = mensagem.getNome();
            if (!nome.isEmpty()){
                holder.nome.setText(nome);
            } else {
                holder.nome.setVisibility(View.GONE);
            }
        } else {
            holder.mensagem.setText(msg);
            holder.imagem.setVisibility(View.GONE);
            String nome = mensagem.getNome();
            if (!nome.isEmpty()){
                holder.nome.setText(nome);
            } else {
                holder.nome.setVisibility(View.GONE);
            }
        }

    }

    @Override
    public int getItemCount() {
        return listMensagens.size();
    }

    @Override
    public int getItemViewType(int position) {
        Mensagem mensagem = listMensagens.get(position);
        String idUsuario = UsuarioFirebase.getIdUsuario();
        if (idUsuario.equals(mensagem.getIdUsuario())){
            return TIPO_ENVIA;
        } else {
            return TIPO_RECEBE;
        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView mensagem, nome;
        ImageView imagem;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            mensagem = itemView.findViewById(R.id.textViewMensagem);
            imagem = itemView.findViewById(R.id.imageMensagemFoto);
            nome = itemView.findViewById(R.id.textNomeExibicao);
            //Só dá para usar o find depois de fazer o inflate
        }
    }
}
