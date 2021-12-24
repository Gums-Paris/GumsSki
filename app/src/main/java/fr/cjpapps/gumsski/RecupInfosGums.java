package fr.cjpapps.gumsski;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;

public class RecupInfosGums implements Callable<String> {
    private String[] strings = new String[6];
    public RecupInfosGums(String[] strings) {this.strings = strings;}
    @Override
    public String call() throws Exception {
        HttpURLConnection conn = null;
        String resultat;
        StringBuilder result = new StringBuilder();
        if (Variables.isNetworkConnected) {
            try {
                if (BuildConfig.DEBUG){
                Log.i("SECUSERV", "recupInfosGums entre dans connect");}
                String fullURL = strings[0] + strings[1];
                if (BuildConfig.DEBUG){
                Log.i("SECUSERV", "URL " + fullURL);}
                URL urlObject = new URL(fullURL);
                conn = (HttpURLConnection) urlObject.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty(strings[2], strings[3]);
                conn.setRequestProperty(strings[4], strings[5]);

                if (BuildConfig.DEBUG){
                    Log.i("SECUSERV", "ouvre InputStream ");}
                InputStream in = new BufferedInputStream(conn.getInputStream());
                String line;
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                if (BuildConfig.DEBUG){
                Log.i("SECUSERV", "buffered reader ");}
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

            } catch (ConnectException e) {
                if (BuildConfig.DEBUG){
                Log.e("SECUSERV erreur connexion", e.getMessage());}
                result.append("netOUT");
            } catch (MalformedURLException e){
                if (BuildConfig.DEBUG){
                Log.e("SECUSERV erreur URL", e.getMessage());}
                result.append("netOUT");
            } catch (UnknownHostException e) {
                if (BuildConfig.DEBUG){
                Log.e("SECUSERV erreur url hôte", e.getMessage());}
                result.append("netOUT");
            } catch (IOException e) {
                e.printStackTrace();
                result.append("netOUT");
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }else{
            if (BuildConfig.DEBUG){
            Log.i("SECUSERV", "recupInfo netOUT ");}
            result.append("netOUT");
        }
        resultat = String.valueOf(result);
        if (BuildConfig.DEBUG){
        Log.i("SECUSERV", "reçu info "+resultat);}
        return resultat;
    }
}
