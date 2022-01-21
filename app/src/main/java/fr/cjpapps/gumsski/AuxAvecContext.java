package fr.cjpapps.gumsski;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class AuxAvecContext {
    Context context;
    AuxAvecContext (Context c) {
        this.context = c;
    }

    ArrayList<String[]> buildForm(LinearLayout parentLayout) {
/*  Pour construire le formulaire utilisé pour créer ou éditer un item dans un LinearLayout avec pour
*   chaque champ un titre (Nom du champ) et un "hint" dans la fenêtre d'édition s'il s'agit du création,
*    ou les valeurs actuelles s'il s'agit d'une édition.*/

        ArrayList<String[]> fieldParams = new ArrayList<>();
// id dans les 100 pour les textviews, 200 pour les edittext et 300 pour les boutons.
        int idT = 100;
        int idE = 200;

        for (Attributs attr : Attributs.values()) {
            if ("id".equals(attr.getChamp())){ continue;}
            if ("checked_out".equals(attr.getChamp())){ continue;}
            if ("checked_out_time".equals(attr.getChamp())){ continue;}
            if ("verrou".equals(attr.getChamp())){ continue;}
            if ("meteo".equals(attr.getChamp())){ continue;}
            if ("secours".equals(attr.getChamp())){ continue;}
            if ("canedit".equals(attr.getChamp())){ continue;}

            idT++;
            idE++;
            String[] params = new String[5];
            params[0] = attr.getChamp();
            params[1] = attr.getNomChamp();
            params[2] = attr.getHint();
            params[3] = String.valueOf(idT);
            params[4] = String.valueOf(idE);
            fieldParams.add(params);

            TextView textView = new TextView(context);
            textView.setId(idT);
            textView.setText(params[1]);
            textView.setTextColor(context.getColor(R.color.colorTitreChamp));
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setTypeface(null, Typeface.BOLD);
            parentLayout.addView(textView);

            EditText editText = new EditText(context);
            editText.setId(idE);
            editText.setHint(params[2]);
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
//            editText.setLines(5);
            editText.setMinLines(2);
            editText.setGravity(Gravity.TOP | Gravity.START);
            parentLayout.addView(editText);

        }

        return fieldParams;
    }


}
