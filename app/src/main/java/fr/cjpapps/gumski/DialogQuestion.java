package fr.cjpapps.gumski;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.Objects;

public class DialogQuestion extends DialogFragment {

// cette classe doit être déclarée public
    /* copie de https://guides.codepath.com/android/using-dialogfragment
     * cette version du AlertDialog dans un DialogFragment a la particularité de pouvoir recevoir des valeurs
     * de paramètres lors de la création de l'instance. Ici c'est le message de l'alerte
     * */

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

        alertDialogBuilder.setNegativeButton("Non",  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialogBuilder.setPositiveButton("Oui",  new DialogInterface.OnClickListener() {
            final SharedPreferences mesPrefs = MyHelper.getInstance().recupPrefs();
            final SharedPreferences.Editor editeur = mesPrefs.edit();
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                getViewModelStore().clear();
                editeur.putBoolean("authOK", false);
                editeur.putString("auth", "");
                editeur.apply();
                requireActivity().finish();
                dialog.dismiss();
            }
        });

        return alertDialogBuilder.create();

    }
}
