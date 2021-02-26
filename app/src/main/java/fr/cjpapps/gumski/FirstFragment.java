package fr.cjpapps.gumski;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
//import androidx.appcompat.widget.PopupMenu;
//import androidx.appcompat.widget.PopupMenu.OnMenuItemClickListener;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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

/* Ce fragment sert à l'affichage des membres d'un goupe plus leur tph et email
*  Il partage le modèle ModelListeItems avec MainActivity ce qui lui donne accès à la liste des participants à partir
*  de laquelle il nourrit sa recyclerView */

    private static ModelListeItems model = null;
    private ArrayList<HashMap<String,String>> lesItems;
    private RecyclerView mRecyclerView;
    private TextView affichage;
    private Button smsGroupe;
    private Button emailGroupe;
    private Boolean okPhone = false;
    private final ArrayList<String> groupeTel = new ArrayList<>();
    private final ArrayList<String> groupeEmail = new ArrayList<>();

    public FirstFragment(){}

    final private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your app
                    okPhone = true;
                } else {
                    // Explain to the user that the feature is unavailable
                    okPhone = false;
                }
            });

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
        model = new ViewModelProvider(requireActivity()).get(ModelListeItems.class);
        View v = inflater.inflate(R.layout.fragment_first, container, false);
        affichage = v.findViewById(R.id.nom_groupe);
        emailGroupe = v.findViewById(R.id.groupe_mail_button);
        smsGroupe = v.findViewById(R.id.groupe_sms_button);
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
                    final ArrayList<MembreGroupe> membresGroupe = new ArrayList<>();
                    for (HashMap<String,String> temp :items) {
                        if (numGroupe.equals(temp.get("groupe"))) {
                            try {
                                MembreGroupe unMembre = new MembreGroupe();
                                unMembre.setName(temp.get("name"));
                                String numTel = Aux.numInter(temp.get("tel"));
//                                unMembre.setTel(temp.get("tel"));
                                unMembre.setTel(numTel);
                                unMembre.setEmail(temp.get("email"));
                                unMembre.setAutonome(temp.get("autonome"));
                                unMembre.setPeage(temp.get("peage"));
                                membresGroupe.add(unMembre);
//                                groupeTel.add(numTel);
                                groupeTel.add("+33612345678");
//                                groupeEmail.add(temp.get("email"));
                                groupeEmail.add("claude_pastre@yahoo.fr");
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    Log.i("SECUSERV frag 1", "récup de la liste");
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

                    RecyclerViewClickListener listener = new RecyclerViewClickListener() {
                        @Override
                        public void onClick(View view, final int position) {
                            MembreGroupe unP = membresGroupe.get(position);
                            if (view.getId() == R.id.phone_button) {
// on vérifie la permission de téléphoner ; si on l'a pas on demande
                                if (ContextCompat.checkSelfPermission(
                                        requireActivity(), Manifest.permission.CALL_PHONE) ==
                                        PackageManager.PERMISSION_GRANTED) {
                                    phoneCall(unP);
                                } else {
                                    // You can directly ask for the permission.
                                    // The registered ActivityResultCallback gets the result of this request.
                                    requestPermissionLauncher.launch(
                                            Manifest.permission.CALL_PHONE);
                                    if (okPhone) {
                                        phoneCall(unP);
                                    }
                                }
                            }
                            if (view.getId() == R.id.email_button) {
                                Log.i("SECUSERV frag 1 onclick", unP.getEmail());
//                                String[] adresses = {unP.getEmail()};
                                String[] adresses = {"claude_pastre@yahoo.fr"};
                                String subject = "J'te cause";
                                String texte = "Je sais pas quoi te dire";
                                composeEmail(adresses, subject, texte);
                            }
                            if (view.getId() == R.id.sms_button) {
                                envoiSMS(unP);
                            }
//                            dismiss(); // finalement on garde le fragment ouvert ; il faudra l'éliminer avec le backbutton
                        }
                    };

                    ParticipantsAdapter mAdapter = new ParticipantsAdapter(getActivity(), membresGroupe, listener);
                    mRecyclerView.setAdapter(mAdapter);
                }
            }
        };
        model.getListeDesItems().observe(getViewLifecycleOwner(),participObserver);

        emailGroupe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("SECUSERV frag 1 onclick emailGroupe", groupeEmail.toString());
                String[] adresses = new String[groupeEmail.size()];
                adresses = groupeEmail.toArray(adresses);
                String subject = "J'te cause";
                String texte = "Je sais pas quoi te dire";
                composeEmail(adresses, subject, texte);
            }
        });

        smsGroupe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
// pour un envoi multiple faut faire une boucle
                Toast.makeText(getActivity(), "Le bouton SMS GROUPE est factice", Toast.LENGTH_LONG).show();
            }
        });
    }

    void phoneCall(MembreGroupe unP){
//        String num1 = unP.getTel().replaceAll("\\s", "");
//        num1 = num1.startsWith("0") ? num1.substring(1) : num1;
        String numInt = unP.getTel();
        Log.i("SECUSERV frag 1 onclick", numInt);
        Intent phone = new Intent(Intent.ACTION_CALL);
//                                phone.setData(Uri.parse("tel:"+numInt));
// pour le développement on remplace les num de téléphone et les emails par des valeurs bidon
        phone.setData(Uri.parse("tel:" + "+33612345678"));
        if (phone.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(phone);
        } else {
            Toast.makeText(getActivity(), "Appli de téléphone non disponible", Toast.LENGTH_LONG).show();
        }
    }

    void composeEmail(String[] addresses, String subject, String texte) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
       intent.putExtra(Intent.EXTRA_TEXT, texte);
       if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(getActivity(), "Appli d'email non disponible", Toast.LENGTH_LONG).show();
        }
    }
    void envoiSMS(MembreGroupe unP){
//        String num1 = unP.getTel().replaceAll("\\s", "");
//        num1 = num1.startsWith("0") ? num1.substring(1) : num1;
        String numInt = unP.getTel();
        Intent sms = new Intent(Intent.ACTION_SENDTO)    ;
//                                phone.setData(Uri.parse("smsto:"+num1));
        sms.setData(Uri.parse("smsto:" + "+33688998191"));
        sms.putExtra("sms_body", "salut mon pote");
        if(sms.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(sms);
        } else {
            Toast.makeText(getActivity(), "Appli de messagerie non disponible", Toast.LENGTH_LONG).show();
        }
    }

}