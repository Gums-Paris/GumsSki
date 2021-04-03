package fr.cjpapps.gumsski;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class DialogAlertes extends DialogFragment {

// cette classe doit être déclarée public
    /* copie de https://guides.codepath.com/android/using-dialogfragment
     * cette version du AlertDialog dans un DialogFragment a la particularité de pouvoir recevoir des valeurs
     * de paramètres lors de la création de l'instance. Ici c'est le message de l'alerte
     * */

    public DialogAlertes() {
        // Empty constructor required for DialogFragment
    }

    static DialogAlertes newInstance(String message) {
        DialogAlertes frag = new DialogAlertes();
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

        alertDialogBuilder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        return alertDialogBuilder.create();

    }
}