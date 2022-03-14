package fr.cjpapps.gumsski;

// pour vérifier la disponibilité du réseau et le surveiller
// https://gist.github.com/ExNDY/2c27b9c29b9642accbb0cd4f33ca9421
// modifiée pour fonctionner en singleton

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

public class NetworkConnectionMonitor extends LiveData<Boolean> {

    private static NetworkConnectionMonitor instance;
    Context mcontext;
    ConnectionNetworkCallback callback;

    // constructeur privé : seule la classe peut elle-même s'instancier
    private NetworkConnectionMonitor(@NonNull Context context) {
        mcontext = context.getApplicationContext();
    }

// création d'instance en passant un contexte ; doit être appelé une fois depuis un endroit ayant un context
// si il est nécessaire que getInstance soit threadsafe il faut "static synchronized NetWork..."
    static NetworkConnectionMonitor getInstance(@NonNull Context context) {
        if (instance == null) {
            instance = new NetworkConnectionMonitor(context);
        }
        return instance;
    }

    // fourniture d'instance dans les utilisations qui seront faites ailleurs dans l'appli
    static NetworkConnectionMonitor getInstance() {
        return instance;
    }

    public void unregisterDefaultNetworkCallback() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mcontext.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        connectivityManager.unregisterNetworkCallback(callback);
    }

    public void registerDefaultNetworkCallback() {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) mcontext.getSystemService(Context.CONNECTIVITY_SERVICE);
            postValue(checkConnection(connectivityManager));
//            assert connectivityManager != null;
            callback = new ConnectionNetworkCallback();
            connectivityManager.registerDefaultNetworkCallback(callback);
        } catch (Exception e) {
            Log.d("Connection: Exception in registerDefaultNetworkCallback", "xD");
            postValue(false);
        }
    }

    //Dans l'original, cette méthode est privée, mais j'ai besoin defaire une vérif dans onCreate avant que
// le network callback n'ait été enregistré dans onResume
    boolean checkConnection(@NonNull ConnectivityManager connectivityManager) {
        Network network = connectivityManager.getActiveNetwork();
        if (network == null) {
            return false;
        } else {
            NetworkCapabilities actNw = connectivityManager.getNetworkCapabilities(network);
            return actNw != null
                    && (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    || actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    || actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        }
    }

    private class ConnectionNetworkCallback extends ConnectivityManager.NetworkCallback {
        @Override
        public void onAvailable(@NonNull android.net.Network network) {
            super.onAvailable(network);
            postValue(true);
            Log.d("Connection:", "onAvailable");
        }

        @Override
        public void onLost(@NonNull android.net.Network network) {
            super.onLost(network);
            postValue(false);
            Log.d("Connection:", "onLost");
        }

        @Override
        public void onBlockedStatusChanged(@NonNull Network network, boolean blocked) {
            super.onBlockedStatusChanged(network, blocked);
            Log.d("Connection:", "onBlockedStatusChanged");
        }

        @Override
        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities);
            Log.d("Connection:", "onCapabilitiesChanged");
        }

        @Override
        public void onLinkPropertiesChanged(@NonNull Network network, @NonNull LinkProperties linkProperties) {
            super.onLinkPropertiesChanged(network, linkProperties);
            Log.d("Connection:", "onLinkPropertiesChanged");
        }

        @Override
        public void onLosing(@NonNull Network network, int maxMsToLive) {
            super.onLosing(network, maxMsToLive);
            Log.d("Connection:", "onLosing");
        }

        @Override
        public void onUnavailable() {
            super.onUnavailable();
            Log.d("Connection:", "onUnavailable");
        }
    }
}