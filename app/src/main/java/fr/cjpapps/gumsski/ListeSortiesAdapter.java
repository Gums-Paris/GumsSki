package fr.cjpapps.gumsski;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ListeSortiesAdapter extends RecyclerView.Adapter<ListeSortiesAdapter.MyViewHolder> {

    /*  adapter transformé pour devenir générique grâce au custom clicklistener qui permet de sortir le travail à faire
    lors du clic
    Exemple d'utilisation, la  recyclerView ayant été déjà créée :
                        RecyclerViewClickListener listener = new RecyclerViewClickListener() {
                            @Override
                            public void onClick(View view, int position) {
                        //        du code ici ...pour faire des trucs et machins
                            }
                        };
                        monAdapter = new RecyclerViewGenericAdapter(recyclerView.getContext(), nomsItems, listener);
                        recyclerView.setAdapter(monAdapter);
   */

    final ArrayList<String> mesData;
    final LayoutInflater mInflater;
    final RecyclerViewClickListener listener;

/*  C'est l'utilisation du RecyclerViewClickListener (portant la position de l'item) passé en paramètre à l'adapteur
    et par lui au ViewHolder  qui permet de sortir de l'adapteur tout le travail à faire sur le click et ainsi
    d'avoir un adapteur générique */

    //  Constructeur de l'adapter. On lui passe un context, les données et le custom clicklistener.
    ListeSortiesAdapter(Context context, ArrayList<String> maList, RecyclerViewClickListener mlistener) {
        mInflater = LayoutInflater.from(context);
        this.mesData = maList;
        this.listener = mlistener;
    }

// les trois méthodes de l'adapteur : onCreateViewHolder, onBindViewHolder, getItemCount

    @NonNull
    @Override
    public ListeSortiesAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // fournit le ViewHolder avec son layout
        View mItemView = mInflater.inflate(R.layout.item_liste, parent, false);
        return new ListeSortiesAdapter.MyViewHolder(mItemView, this, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ListeSortiesAdapter.MyViewHolder holder, int position) {
        // connecte les données au ViewHolder
        String mCurrent = mesData.get(position);
        holder.listItemView.setText(mCurrent);
    }

    @Override
    public int getItemCount() {
        return mesData.size();
    }

    /* Et voilà le ViewHolder qui sert de présentoir pour chaque item de données dans une vue spécifique de
    la RecyclerView. C'est cette vue qui va recevoir le clic */
    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        final TextView listItemView;
        final ListeSortiesAdapter mAdapter;
        final RecyclerViewClickListener mListener;
        MyViewHolder(View itemView, ListeSortiesAdapter adapter, RecyclerViewClickListener listener) {
            super(itemView);
            listItemView = itemView.findViewById(R.id.un_item);
            listItemView.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.item_liste_back));
            this.mAdapter = adapter;
            mListener = listener;
            listItemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int mPosition = getLayoutPosition();
// ceci renvoie au listener dans l'activité ou le fragment où on va faire le travail :
            mListener.onClick(v, mPosition);
        }
    }
}
