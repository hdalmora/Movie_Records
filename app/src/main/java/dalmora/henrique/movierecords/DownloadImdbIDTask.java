package dalmora.henrique.movierecords;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import dalmora.henrique.movierecords.Interfaces.OnDownloadImdbIDTaskComplete;
import dalmora.henrique.movierecords.Interfaces.OnTaskCompleted;
import dalmora.henrique.movierecords.Interfaces.OnTaskPreExecute;

/**
 * Created by Henrique Dal' Mora R. da Silva on 21/10/2016.
 */

public class DownloadImdbIDTask extends AsyncTask<String, Void, Vector<ContentValues>> {

    private final String LOG_TAG = DownloadImdbIDTask.class.getSimpleName();

    private OnDownloadImdbIDTaskComplete completeListener;
    private OnTaskPreExecute preExecuteListener;

    private final Context mContext;

    String mTotalResults;

    /** progress dialog para mostrar ao usuário que o processo está em execução */
    /**
     * application context.
     */
   /* private ProgressDialog dialog;*/
    public DownloadImdbIDTask(Context context, OnDownloadImdbIDTaskComplete completeListener, OnTaskPreExecute preExecuteListener) {
        mContext = context;
        this.completeListener = completeListener;
        this.preExecuteListener = preExecuteListener;
        // ProgressDialog dialog = new ProgressDialog(mContext);
    }

    // Irá conter a resposta em JSON como string
    String movieJsonStr = null;


    @Override
    protected void onPreExecute() {
        preExecuteListener.onTaskPreExecute();

    }

    @Override
    protected Vector<ContentValues> doInBackground(String... params) {


        //Verificar comprimento dos params, pois se não houver nenhum dado, não há nada para retornar
        if (params.length == 0) {
            return null;
        }

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String movieJsonStr = null;
        String format = "json";
        String[] plot = {"short", "full"};
        try {


            // ConstruiR a Url para realizar o query na API OMDB.com
            // Parâmetros possíveis disponíceis na pagina da API, em
            // http://www.omdbapi.com/
            final String MOVIE_BASE_URL = "http://www.omdbapi.com/?";
            final String T_QUERY_PARAM = "t=";

            String titulo = params[0].replace(" ", "%20").replace("'", "%27");


            Uri builtUri_default = Uri.parse(MOVIE_BASE_URL + T_QUERY_PARAM + titulo);

            URL url = new URL(builtUri_default.toString());
            Log.d(LOG_TAG, "URL: " + url);

            // Cria a requisição à API e abre a conexão
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Le a stream de entrada em uma String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
          
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
				
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream estava vazia
                return null;
            }
            movieJsonStr = buffer.toString();

            return getMovieDataFromJson(movieJsonStr);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Vector<ContentValues> vector) {

        if (vector != null) {
            completeListener.onImdbIDTaskComplete(vector);
        } else {
            Log.d(LOG_TAG, "onPostExecute: VECTOR is Null");
            completeListener.onImdbIDTaskComplete(null);
        }
    }

    private Vector<ContentValues> getMovieDataFromJson(String movieJsonStr)
            throws JSONException {
       
        final String OWM_IMDBID = "imdbID";

        Log.d(LOG_TAG, "MOVIE STR: " + movieJsonStr);
        try {
            Vector<ContentValues> cVVector = new Vector<ContentValues>(); //= null;
            JSONObject movieJson = new JSONObject(movieJsonStr);

            String imdbId;

            ContentValues movieValues = new ContentValues();

            imdbId = movieJson.getString(OWM_IMDBID);
            movieValues.put(OWM_IMDBID, imdbId);



            cVVector.add(movieValues);

            // Criar uma instancia de ContentValues para armazenar cada valor
            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);

                return cVVector;
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error closing stream", e);
        }
        return null;
    }
}
