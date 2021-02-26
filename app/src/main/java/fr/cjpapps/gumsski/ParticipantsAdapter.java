package fr.cjpapps.gumsski;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ParticipantsAdapter extends RecyclerView.Adapter<ParticipantsAdapter.MyViewHolder> {

//modifié par rapport à RecyclerViewGenericAdapter : on rajoute les boutons avec clic phone et emeil dans le ViewHolder

//    final ArrayList<String> mesData;
    final ArrayList<MembreGroupe> mesData;
    final LayoutInflater mInflater;
    final RecyclerViewClickListener listener;
    Resources res;

//    ParticipantsAdapter(Context context, ArrayList<String> maList, RecyclerViewClickListener mlistener) {
    ParticipantsAdapter(Context context, ArrayList<MembreGroupe> maList, RecyclerViewClickListener mlistener) {
        mInflater = LayoutInflater.from(context);
        this.mesData = maList;
        this.listener = mlistener;
     }

    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        final TextView listItemView, detailItemView;
        final ImageButton phoneItemView;
        final ImageButton emailItemView;
        final ImageButton smsItemView;
        final ParticipantsAdapter mAdapter;
        final RecyclerViewClickListener mListener;
        MyViewHolder(View itemView, ParticipantsAdapter adapter, RecyclerViewClickListener listener) {
            super(itemView);
            listItemView = itemView.findViewById(R.id.un_participant);
            detailItemView = itemView.findViewById(R.id.detail);
            phoneItemView = itemView.findViewById(R.id.phone_button);
            emailItemView = itemView.findViewById(R.id.email_button);
            smsItemView = itemView.findViewById(R.id.sms_button);
            this.mAdapter = adapter;
            mListener = listener;
            listItemView.setOnClickListener(this);
            phoneItemView.setOnClickListener(this);
            emailItemView.setOnClickListener(this);
            smsItemView.setOnClickListener(this);
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
//        String mCurrent = mesData.get(position);
        String autonome = ""; String peage = "";
        MembreGroupe mCurrent = mesData.get(position);
        holder.listItemView.setText(mCurrent.getName());
        if ("1".equals(mCurrent.getAutonome())) {autonome = "A";}
        if ("1".equals(mCurrent.getPeage())) {peage = "P";}
        res = MyHelper.getInstance().recupResources();
        holder.detailItemView.setText(res.getString(R.string.details, autonome, peage));
    }

    @Override
    public int getItemCount() {
        return mesData.size();
    }
}
