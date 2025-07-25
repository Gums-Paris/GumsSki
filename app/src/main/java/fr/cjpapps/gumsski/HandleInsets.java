package fr.cjpapps.gumsski;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public final class HandleInsets {
    // HandleInsets sert à mettre des protections dans une fonctionnement EdgeToEdge pour que les commandes
    // de la barre d'appli ne soient pas masquées par la barre d'état

    static Insets myInsets;
    private HandleInsets() {};
    public static void placeInsets(Activity activity, View conteneur) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Window window = activity.getWindow();
            window.setDecorFitsSystemWindows(false);
            ViewCompat.setOnApplyWindowInsetsListener(conteneur, new OnApplyWindowInsetsListener() {
                @NonNull
                @Override
                public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                    myInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());
               /*   Je n'arrive pas à changer la couleur de statusBar
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    window.setStatusBarColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));*/
                    v.setPadding(
                            myInsets.left,
                            myInsets.top,
                            myInsets.right,
                            myInsets.bottom);
                    return (insets);
                }

            });
        }
        ViewCompat.requestApplyInsets(conteneur);
    }
}

