package com.yagocurvello.zapy.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.yagocurvello.zapy.R;
import com.yagocurvello.zapy.model.Conversa;
import com.yagocurvello.zapy.model.Grupo;
import com.yagocurvello.zapy.model.Usuario;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversasAdapter extends RecyclerView.Adapter<ConversasAdapter.MyViewHolder> {

    private List<Conversa> conversasList;
    private Context context;

    public ConversasAdapter(List<Conversa> conversasList,Context context) {
        this.conversasList = conversasList;
        this.context = context;
    }

    public List<Conversa> getConversas(){
        return this.conversasList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent,int viewType) {

        View itemLista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_conversas, parent, false);

        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder,int position) {

        Conversa conversa = conversasList.get(position);
        holder.ultimaMsg.setText(conversa.getUltimaMsg());

        if (conversa.getIsGroup().equals("true")){
            Grupo grupo = conversa.getGrupo();
            holder.nome.setText(grupo.getName());

            if (grupo.getFoto() != null){
                Uri uri = Uri.parse(grupo.getFoto());
                Glide.with(context).load(uri).into(holder.foto);
            }else {
                holder.foto.setImageResource(R.drawable.padrao);
            }

        } else {
            Usuario usuario = conversa.getUsuarioExibicao();
            if (usuario != null){
                holder.nome.setText(usuario.getName());
                if (usuario.getFoto() != null){
                    Uri uri = Uri.parse(usuario.getFoto());
                    Glide.with(context).load(uri).into(holder.foto);
                }else {
                    holder.foto.setImageResource(R.drawable.padrao);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return conversasList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView nome, ultimaMsg;
        CircleImageView foto;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            nome = itemView.findViewById(R.id.textNomeConversa);
            ultimaMsg = itemView.findViewById(R.id.textUltimaMsg);
            foto = itemView.findViewById(R.id.circleImageViewFotoPerfilConversa);
        }
    }
}
