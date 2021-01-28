package fr.cjpapps.gumski;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ParticipantsAdapter extends RecyclerView.Adapter<ParticipantsAdapter.MyViewHolder> {

//modifié par rapport à RecyclerViewGenericAdapter : on rajoute les boutons avec clic phone et emeil dans le ViewHolder

    final ArrayList<String> mesData;
    final LayoutInflater mInflater;
    final RecyclerViewClickListener listener;

    ParticipantsAdapter(Context context, ArrayList<String> maList, RecyclerViewClickListener mlistener) {
        mInflater = LayoutInflater.from(context);
        this.mesData = maList;
        this.listener = mlistener;
     }

    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        final TextView listItemView;
        final Button phoneItemView;
        final Button emailItemView;
        final ParticipantsAdapter mAdapter;
        final RecyclerViewClickListener mListener;
        MyViewHolder(View itemView, ParticipantsAdapter adapter, RecyclerViewClickListener listener) {
            super(itemView);
            listItemView = itemView.findViewById(R.id.un_participant);
            phoneItemView = itemView.findViewById(R.id.phone_button);
            emailItemView = itemView.findViewById(R.id.email_button);
            this.mAdapter = adapter;
            mListener = listener;
            listItemView.setOnClickListener(this);
            phoneItemView.setOnClickListener(this);
            emailItemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int mPosition = getLayoutPosition();
// ceci renvoie au listener dans l'activité ou le fragment où on va faire le travail :
            mListener.onClick(v, mPosition);
        }
    }

    @NonNull
    @Override
    public ParticipantsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // fournit le ViewHolder avec son layout
        View mItemView = mInflater.inflate(R.layout.participants, parent, false);
        return new MyViewHolder(mItemView, this, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ParticipantsAdapter.MyViewHolder holder, int position) {
        // connecte les données au ViewHolder
        String mCurrent = mesData.get(position);
        holder.listItemView.setText(mCurrent);
    }

    @Override
    public int getItemCount() {
        return mesData.size();
    }
}
