package fr.cjpapps.gumsski;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

public class SupprimeInfosGums implements Callable<String> {

    private String[] strings = new String[6];
    public SupprimeInfosGums(String[] strings) {this.strings = strings;}

    @Override
    public String call() throws Exception {
        HttpURLConnection conn = null;
        String resultat;
        int code = 0;
        StringBuilder result = new StringBuilder();

        try{
            URL urlObject = new URL(strings[0]);
            conn = (HttpURLConnection) urlObject.openConnection();
            Log.i("SECUSERV", "connexion ouverte ");
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer "+ strings[1]);
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
//        if (code == 200) {resultat = String.valueOf(result);}
        resultat = String.valueOf(result);
        Log.i("SECUSERV", "del item "+resultat);
        return resultat;
    }
}
