package fr.cjpapps.gumsski;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class FirstFragment extends DialogFragment {

/* Ce fragment sert à l'affichage des membres d'un goupe plus leur tph et email
*  Il partage le modèle ModelListeItems avec MainActivity ce qui lui donne accès à la liste des participants
*  à partir de laquelle il nourrit sa recyclerView */

    private static ModelListeItems model = null;
    private ArrayList<HashMap<String,String>> lesItems;
    private RecyclerView mRecyclerView;
    private TextView affichage;
    private Button smsGroupe;
    private Button emailGroupe;
    private Boolean okPhone = false;
    private final ArrayList<String> groupeEmail = new ArrayList<>();
    private String listeAdresses = "";

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
        StringBuilder addressList = new StringBuilder();

        final Observer<ArrayList<HashMap<String,String>>> participObserver = items -> {
            if (items != null) {
                final ArrayList<MembreGroupe> membresGroupe = new ArrayList<>();
                for (HashMap<String,String> temp :items) {
                    if (Aux.egaliteChaines(numGroupe, temp.get("groupe"))) {
                        try {
                            MembreGroupe unMembre = new MembreGroupe();
                            unMembre.setName(temp.get("name"));
                            String numTel = Aux.numInter(temp.get("tel"));
// pour les essais
//                                numTel = "+33688998191";
                            unMembre.setTel(numTel);
                            unMembre.setEmail(temp.get("email"));
// pour les essais
//                                unMembre.setEmail("claude_pastre@yahoo.fr");
                            unMembre.setAutonome(temp.get("autonome"));
                            unMembre.setPeage(temp.get("peage"));
                            membresGroupe.add(unMembre);
                            groupeEmail.add(temp.get("email"));
                            addressList.append(temp.get("email"));
                            addressList.append(",");
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                }
                listeAdresses = addressList.deleteCharAt(addressList.length()-1).toString();
                if (BuildConfig.DEBUG){
                Log.i("SECUSERV frag 1", "récup de la liste");}
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

                RecyclerViewClickListener listener = (view1, position) -> {
                    MembreGroupe unP = membresGroupe.get(position);
                    if (view1.getId() == R.id.phone_button) {
// on vérifie la permission de téléphoner ; si on l'a pas on demande
                        if (ContextCompat.checkSelfPermission(
                                requireActivity(), Manifest.permission.CALL_PHONE) ==
                                PackageManager.PERMISSION_GRANTED) {
                            Aux.phoneCall(unP);
                        } else {
                            // You can directly ask for the permission.
                            // The registered ActivityResultCallback gets the result of this request.
                            requestPermissionLauncher.launch(
                                    Manifest.permission.CALL_PHONE);
                            if (okPhone) {
                                Aux.phoneCall(unP);
                            }
                        }
                    }
                    if (view1.getId() == R.id.email_button) {
/*  composeEmail utilise le schéma GoogleMail de String[] en extra pour passer les adresses tandis que sendEmail
    utilise le schéma du mailto de HTML mailto:+String, la chaîne contenant les adresses séparées par une virgule.
    MailOrange n'accepte que ce dernier.  Avec ce schéma GMail n'accepte pas de sujet ni de texte en extra */
/*                        String[] adresses = {unP.getEmail()};
                        Aux.composeEmail(adresses, subject, texte); */
                        String subject = "";
                        String text = "";
                        String adresse = unP.getEmail();
                        Aux.sendEmail(adresse, subject, text);
                    }
                    if (view1.getId() == R.id.sms_button) {
                        Aux.envoiSMS(unP);
                    }
                };
                ParticipantsAdapter mAdapter = new ParticipantsAdapter(getActivity(), membresGroupe, listener);
                mRecyclerView.setAdapter(mAdapter);
            }
        };
        model.getListeDesItems().observe(getViewLifecycleOwner(),participObserver);

        emailGroupe.setOnClickListener(view12 -> {
            if (BuildConfig.DEBUG){
            Log.i("SECUSERV frag 1 onclick emailGroupe", groupeEmail.toString());
            Log.i("SECUSERV frag 1 onclick liste adresses", listeAdresses);};
            String[] adresses = new String[groupeEmail.size()];
            adresses = groupeEmail.toArray(adresses);
            String subject = "";
            String texte = "";
/*  composeEmail utilise le schéma GoogleMail de String[] en extra pour passer les adresses tandis que sendEmail
    utilise le schéma du mailto de HTML mailto:+String, la chaîne contenant les adresses séparées par une virgule.
    MailOrange n'accepte que ce dernier. Avec ce schéma GMail n'accepte pas de sujet ni de texte en extra */
//            Aux.composeEmail(adresses, subject, texte);
            Aux.sendEmail(listeAdresses, subject, texte);
        });

// pas d'envoi sms au groupe ; ce bouton ouvre Signal
        smsGroupe.setOnClickListener(view13 -> {
            Intent launchIntent = requireContext().getPackageManager().getLaunchIntentForPackage("org.thoughtcrime.securesms");
            if (launchIntent != null) {
                startActivity(launchIntent);
            } else {
                Toast.makeText(requireActivity(), "L'appli Signal n'est pas disponible", Toast.LENGTH_LONG).show();
            }
/*             Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_VIEW);
            sendIntent.setData(Uri.parse("sgnl://signal.group?group_id=dqRJDMbLnldYWhKD3d9pxlHVhn3QkCk2P62xCCmYcPs="));
            sendIntent.setPackage("org.thoughtcrime.securesms");
            if (sendIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(sendIntent);
            } else {
                Toast.makeText(getActivity(), "Appli Signal pas disponible", Toast.LENGTH_LONG).show();
            } */
        });
    }

}