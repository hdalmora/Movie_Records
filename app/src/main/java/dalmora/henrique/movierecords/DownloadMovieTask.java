package dalmora.henrique.movierecords;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import dalmora.henrique.movierecords.Interfaces.OnTaskCompleted;
import dalmora.henrique.movierecords.Interfaces.OnTaskPreExecute;

/**
 * Created by Henrique Dal' Mora R. da Silva on 09/10/2016.
 */

public class DownloadMovieTask extends AsyncTask<String, Void, Vector<ContentValues>> {

    private final String LOG_TAG = DownloadMovieTask.class.getSimpleName();

    private OnTaskCompleted completeListener;
    private OnTaskPreExecute preExecuteListener;

    private final Context mContext;

    String mTotalResults;



    public DownloadMovieTask(Context context, OnTaskCompleted completeListener, OnTaskPreExecute preExecuteListener) {
        mContext = context;
        this.completeListener = completeListener;
        this.preExecuteListener = preExecuteListener;
        // ProgressDialog dialog = new ProgressDialog(mContext);
    }


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
            final String T_QUERY_PARAM = "t";
            final String S_QUERY_PARAM = "s";
            final String R_QUERY_PARAM = "r"; //formato -> json
            final String TOMATOES_PARAM = "tomatoes";
            final String PLOT_PARAM = "plot"; //short ou full
            final String Y_PARAM = "y"; //ano de lançamento
            final String PAGE_PARAM = "page"; //numero da pagina

            Uri builtUri_default = Uri.parse(MOVIE_BASE_URL).buildUpon()
                    .appendQueryParameter(S_QUERY_PARAM, params[0])
                    .appendQueryParameter(Y_PARAM, "")
                    .appendQueryParameter(PLOT_PARAM, plot[1])
                    .appendQueryParameter(R_QUERY_PARAM, format)
                    .appendQueryParameter(TOMATOES_PARAM, "true")
                    .appendQueryParameter(PAGE_PARAM, params[1])
                    .build();

            URL url = new URL(builtUri_default.toString());
            Log.d(LOG_TAG, "URL: " + url);

         
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

         
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

        if (vector != null){
            completeListener.onTaskCompleted(vector, mTotalResults, "movieAPI");
        } else {
            Log.d(LOG_TAG, "onPostExecute: VECTOR is Null");
            completeListener.onTaskCompleted(null, null, null);
        }
    }

    private Vector<ContentValues> getMovieDataFromJson(String movieJsonStr)
            throws JSONException {

        final String OWM_SEARCH = "Search";
        final String OWM_TITLE = "Title";
        final String OWM_YEAR = "Year";
        final String OWM_TYPE = "Type";
        final String OWM_POSTER = "Poster";
        final String OWM_IMDBID = "imdbID";
        final String OWM_PAGE = "page";
        final String OWM_TOTAL_RESULTS = "totalResults";

        Vector<ContentValues> cVVector = null;


        try {
            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(OWM_SEARCH);


            cVVector = new Vector<ContentValues>(movieArray.length());

            mTotalResults = movieJson.getString(OWM_TOTAL_RESULTS);

            for (int i = 0; i < movieArray.length(); i++) {
                String titulo;
                String ano_lancamento;
                String tipo;
                String imdbID;
                String poster_url;

                ContentValues movieValues = new ContentValues();
                titulo = movieArray.getJSONObject(i).getString(OWM_TITLE);
                ano_lancamento = movieArray.getJSONObject(i).getString(OWM_YEAR);
                tipo = movieArray.getJSONObject(i).getString(OWM_TYPE);
                imdbID = movieArray.getJSONObject(i).getString(OWM_IMDBID);
                poster_url = movieArray.getJSONObject(i).getString(OWM_POSTER);

                movieValues.put(OWM_TITLE, titulo);
                movieValues.put(OWM_YEAR, ano_lancamento);
                movieValues.put(OWM_TYPE, tipo);
                movieValues.put(OWM_POSTER, poster_url);
                movieValues.put(OWM_IMDBID, imdbID);

                cVVector.add(movieValues);
            }
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