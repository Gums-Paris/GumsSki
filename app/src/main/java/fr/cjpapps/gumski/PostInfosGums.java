package fr.cjpapps.gumski;

import android.os.AsyncTask;
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

public class PostInfosGums extends AsyncTask<String,Void,String> {

/*  générique pour l'envoi d'une requête POST
*   en entrée un array de 6 chaînes strings[6] :
*       string[0] = l'url d'accueil finissant par /
*       string[1] = la chaine de requête à ajouter comme corps de la requête commence par index.php?
*       string[2] et string[3] = la RequestProperty à mettre dans l'en-tête pour auth et [4] et [5] en plus
*           pour les POSTs d'item
*   en sortie le json renvoyé suite à la demande*/

    @Override
    protected String doInBackground(String... strings) {

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
            Log.i("SECUSERV", " POST body "+strings[1]);
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
            Log.i("SECUSERV", "retour de post "+result.toString());

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

    protected void onPostExecute (String resultat) {
// le traitement dépend de l'url accédée
    }
}
