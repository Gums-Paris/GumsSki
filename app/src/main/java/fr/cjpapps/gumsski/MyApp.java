package fr.cjpapps.gumsski;

import android.app.Application;
import android.os.StrictMode;

public class MyApp extends Application {

    public MyApp() {
        if(BuildConfig.DEBUG)
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build());
    }
}