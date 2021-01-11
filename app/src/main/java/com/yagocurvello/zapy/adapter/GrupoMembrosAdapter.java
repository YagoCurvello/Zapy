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
import com.yagocurvello.zapy.model.Usuario;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GrupoMembrosAdapter extends RecyclerView.Adapter<GrupoMembrosAdapter.MyViewHolder> {

    private List<Usuario> listMembrosGrupo;
    private Context context;

    public GrupoMembrosAdapter(List<Usuario> listContatos, Context context) {
        this.listMembrosGrupo = listContatos;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent,int viewType) {

        View itemLista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_grupo_membros, parent, false);

        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder,int position) {

        Usuario contato = listMembrosGrupo.get(position);
        holder.nome.setText(contato.getName());

        if (contato.getFoto() != null){
            Uri uri = Uri.parse(contato.getFoto());
            Glide.with(context).load(uri).into(holder.foto);
        }else {
            holder.foto.setImageResource(R.drawable.padrao);
        }
    }

    @Override
    public int getItemCount() {
        return listMembrosGrupo.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView nome;
        CircleImageView foto;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            nome = itemView.findViewById(R.id.textViewNomeMembro);
            foto = itemView.findViewById(R.id.circleImageViewFotoMembros);
            //Só dá para usar o find depois de fazer o inflate
        }
    }
}

