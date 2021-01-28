package fr.cjpapps.gumski;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
//import androidx.appcompat.widget.PopupMenu;
//import androidx.appcompat.widget.PopupMenu.OnMenuItemClickListener;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class FirstFragment extends DialogFragment {

    private static ModelListeItems model = null;
//    private ArrayList<String> lesData;
    private ArrayList<String> lesParticipants;
    private ArrayList<HashMap<String,String>> lesItems;
    private Aux methodesAux;
    private RecyclerView mRecyclerView;
    private TextView affichage;


    public FirstFragment(){}

    static FirstFragment newInstance(String titre, String numG) {
        FirstFragment frag =  new FirstFragment();
        Bundle args = new Bundle();
        args.putString("titre",titre);
        args.putString("numG", numG);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        methodesAux = new Aux();
        model = new ViewModelProvider(requireActivity()).get(ModelListeItems.class);
        View v = inflater.inflate(R.layout.fragment_first, container, false);
        affichage = v.findViewById(R.id.nom_groupe);
        mRecyclerView = v.findViewById(R.id.listeparticipants);
        return v;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        assert getArguments() != null;
        String title = getArguments().getString("titre", "Nom groupe");
        affichage.setText(title);
        final String numGroupe = getArguments().getString("numG", "1");

        final Observer<ArrayList<HashMap<String,String>>> participObserver = new Observer<ArrayList<HashMap<String,String>>>() {
            @Override
            public void onChanged(ArrayList<HashMap<String,String>> items) {
                if (items != null){
                    final ArrayList<String> lesParticipants = new ArrayList<>();
                    final ArrayList<String[]> groupe = new ArrayList<>();
                    for (HashMap<String,String> temp :items) {
                        if (numGroupe.equals(temp.get("groupe"))) {
                            try {
                                lesParticipants.add(temp.get("name"));
                                String[] participant = {temp.get("name"), temp.get("tel"), temp.get("email")};
                                groupe.add(participant);
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    Log.i("SECUSERV frag 1", "récup de la liste");
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                    RecyclerViewClickListener listener = new RecyclerViewClickListener() {
                        // pas confondre "position" de l'élément dans la liste et Position dans updatePosition qui serait plutôt "location"
                        @Override
                        public void onClick(View view, final int position) {
                            String element = lesParticipants.get(position);
                            String[] unP = groupe.get(position);
                            if (view.getId() == R.id.phone_button) {
                                Log.i("SECUSERV frag 1 onclick", unP[1]);
                            }
                            if (view.getId() == R.id.email_button) {
                                Log.i("SECUSERV frag 1 onclick", unP[2]);
                            }
//                            dismiss();
                        }
                    };
                    ParticipantsAdapter mAdapter = new ParticipantsAdapter(getActivity(), lesParticipants, listener);
                    mRecyclerView.setAdapter(mAdapter);
                }
            }
        };
        model.getListeDesItems().observe(getViewLifecycleOwner(),participObserver);

    }
}