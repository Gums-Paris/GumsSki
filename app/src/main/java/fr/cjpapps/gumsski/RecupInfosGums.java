package fr.cjpapps.gumsski;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

public class RecupInfosGums implements Callable<String> {
    private String[] strings = new String[6];
    public RecupInfosGums(String[] strings) {this.strings = strings;}
    @Override
    public String call() throws Exception {
        HttpURLConnection conn = null;
        String resultat;
        StringBuilder result = new StringBuilder();

        try{
            String fullURL = strings[0]+strings[1];
            Log.i("SECUSERV", "URL "+fullURL);
            URL urlObject = new URL(fullURL);
            conn = (HttpURLConnection) urlObject.openConnection();
            Log.i("SECUSERV", "connexion ouverte ");
            conn.setRequestMethod("GET");
            conn.setRequestProperty(strings[2], strings[3]);
            conn.setRequestProperty(strings[4], strings[5]);

            InputStream in = new BufferedInputStream(conn.getInputStream());
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            reader.close();

        }catch (Exception e) {
            e.printStackTrace();
        }finally{
            if (conn != null) {
                conn.disconnect();
            }
        }
        resultat = String.valueOf(result);
        Log.i("SECUSERV", "reçu info "+resultat);
        return resultat;
    }
}
