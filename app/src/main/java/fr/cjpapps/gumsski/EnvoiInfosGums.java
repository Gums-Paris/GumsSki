package fr.cjpapps.gumsski;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

public class EnvoiInfosGums implements Callable<String> {

// pour exécuter des requêtes POST sur gumsparis

    private String[] strings = new String[6];
    public EnvoiInfosGums(String[] strings) {this.strings = strings;}

    @Override
    public String call() throws Exception {
        String resultat;
        StringBuilder result = new StringBuilder();
        HttpURLConnection conn = null;

        try {
//            String fullURL = strings[0];
            URL urlObject = new URL(strings[0]);
            conn = (HttpURLConnection) urlObject.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty(strings[2], strings[3]);
            if (!"".equals(strings[4])) {
                conn.setRequestProperty(strings[4], strings[5]);
            }
            conn.setRequestProperty("Accept-Charset", "UTF-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setChunkedStreamingMode(0);

            OutputStream out = new BufferedOutputStream(conn.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
            if (BuildConfig.DEBUG){
            Log.i("SECUSERV", " POST body "+strings[1]);}
            writer.write(strings[1]);
            writer.flush();
            writer.close();

            int code = conn.getResponseCode();
/*            if (code != 200){
                throw new IOException("Invalid response from server : "+code);
            }  */

            InputStream in = new BufferedInputStream(conn.getInputStream());
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            reader.close();
            if (BuildConfig.DEBUG){
            Log.i("SECUSERV", "retour de post "+result.toString());}

        }catch(IOException e){
            e.printStackTrace();
        }finally{
            if (conn != null) {
                conn.disconnect();
            }
        }
        resultat = String.valueOf(result);
        return resultat;
    }
}
