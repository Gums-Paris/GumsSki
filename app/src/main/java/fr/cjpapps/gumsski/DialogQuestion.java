package fr.cjpapps.gumsski;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Objects;

public class DialogQuestion extends DialogFragment {

// cette classe doit être déclarée public
    /* copie de https://guides.codepath.com/android/using-dialogfragment
     * cette version du AlertDialog dans un DialogFragment a la particularité de pouvoir recevoir des valeurs
     * de paramètres lors de la création de l'instance. Ici c'est le message de l'alerte
     * */

// interface utilisée par Main pour être informée de ce qu'elle doit se flinguer en cas de réponse positive
// à la question
    public interface EndMainDialogListener {
        void onPositiveReply();
    }

    public DialogQuestion() {
        // Empty constructor required for DialogFragment
    }

    static DialogQuestion newInstance(String message) {
        DialogQuestion frag = new DialogQuestion();
        Bundle args = new Bundle();
        args.putString("message", message);
        frag.setArguments(args);
        return frag;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String message = "";
        if (getArguments() != null) {
            message = getArguments().getString("message", ""); }
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireActivity());
        alertDialogBuilder.setMessage(message);

        alertDialogBuilder.setNegativeButton("Non", (dialog, which) -> {
            if (BuildConfig.DEBUG){
            Log.i("SECUSERV", "dialog NON ");}
            dialog.dismiss();
        });

        alertDialogBuilder.setPositiveButton("Oui",  new DialogInterface.OnClickListener() {
            final SharedPreferences mesPrefs = MyHelper.getInstance().recupPrefs();
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (BuildConfig.DEBUG){
                Log.i("SECUSERV", "dialog OUI ");}
                final SharedPreferences.Editor editeur = mesPrefs.edit();
                editeur.putBoolean("authOK", false);
                editeur.putString("auth", "");
                editeur.apply();
// on termine StartActivity à travers un BroadcastReceiver parce qu'on a pas le droit (memory leak == caca)
// d'en conserver une référence dans MainActivity
                Intent intent = new Intent();
                intent.setAction("finish_activity");
                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent);
                dialog.dismiss();
// on termine MainActivity qui a créé ce dialogue
//                requireActivity().finish();  // au lieu de la tuer on lui demande de se suicider :
                EndMainDialogListener listener = (EndMainDialogListener) requireActivity();
                listener.onPositiveReply();
            }
        });

        return alertDialogBuilder.create();
    }
}
