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

import dalmora.henrique.movierecords.Interfaces.OnBackgroundImageTaskCompleted;
import dalmora.henrique.movierecords.Interfaces.OnTaskCompleted;
import dalmora.henrique.movierecords.Interfaces.OnTaskPreExecute;

/**
 * Created by Henrique Dal' Mora R. da Silva on 20/10/2016.
 */

public class DownloadMovieBackgroundImageTask extends AsyncTask<String, Void, Vector<ContentValues>> {

    private final String LOG_TAG = DownloadMovieBackgroundImageTask.class.getSimpleName();

    private OnBackgroundImageTaskCompleted completeListener;
    private OnTaskPreExecute preExecuteListener;

    private final Context mContext;

    String mTotalResults;


    public DownloadMovieBackgroundImageTask(Context context, OnBackgroundImageTaskCompleted completeListener, OnTaskPreExecute preExecuteListener) {
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
        try {


            // ConstruiR a Url para realizar o query na API OMDB.com
            // Parâmetros possíveis disponíceis na pagina da API, em
            // http://www.omdbapi.com/

            String MOVIE_BASE_URL = "";
            if (params[1].equals("seriesTOP100")){
                MOVIE_BASE_URL = "http://webservice.fanart.tv/v3/tv/";
            } else {
                MOVIE_BASE_URL = "http://webservice.fanart.tv/v3/movies/";
            }
            final String API_QUERY_PARAM = "api_key";
            final String API_KEY = "API KEY";

            Uri builtUri_default = Uri.parse(MOVIE_BASE_URL + params[0] + "?" + API_QUERY_PARAM + "=" + API_KEY);

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

            return getMovieBackgroundImageFromJson(movieJsonStr, params[1]);

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
            completeListener.onBackgrounImageTaskCompleted(vector);
        } else {
            Log.d(LOG_TAG, "onPostExecute: VECTOR is Null");
            completeListener.onBackgrounImageTaskCompleted(null);
        }
    }

    private Vector<ContentValues> getMovieBackgroundImageFromJson(String movieJsonStr, String type)
            throws JSONException {
     

        String OWM_BACKGROUND_ARRAY = "";

        if (type.equals("seriesTOP100")){
            OWM_BACKGROUND_ARRAY = "showbackground";
        } else {
            OWM_BACKGROUND_ARRAY = "moviebackground";
        }

        final String OWM_URL = "url";

     
        try {
            Vector<ContentValues> cVVector = new Vector<>(); //= null;

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray arrayBackground = movieJson.getJSONArray(OWM_BACKGROUND_ARRAY);

            for (int i = 0; i < arrayBackground.length(); i++) {

                String background;
                String id;
                String url;

                ContentValues movieValues = new ContentValues();

                background = arrayBackground.getJSONObject(i).getString(OWM_URL);

            
                movieValues.put(OWM_URL + "_" + i, background);

                cVVector.add(movieValues);

            }

          
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
