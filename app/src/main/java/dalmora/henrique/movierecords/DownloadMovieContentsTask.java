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

import dalmora.henrique.movierecords.Interfaces.OnTaskCompleted;
import dalmora.henrique.movierecords.Interfaces.OnTaskPreExecute;

/**
 * Created by Henrique Dal' Mora R. da Silva on 09/10/2016.
 */

public class DownloadMovieContentsTask extends AsyncTask<String, Void, Vector<ContentValues>> {

    private final String LOG_TAG = DownloadMovieTask.class.getSimpleName();

    private OnTaskCompleted completeListener;
    private OnTaskPreExecute preExecuteListener;

    private final Context mContext;

    String mTotalResults;



    public DownloadMovieContentsTask(Context context, OnTaskCompleted completeListener, OnTaskPreExecute preExecuteListener) {
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
            final String I_QUERY_PARAM = "i";
            final String R_QUERY_PARAM = "r"; //formato -> json
            final String TOMATOES_PARAM = "tomatoes";
            final String PLOT_PARAM = "plot"; //short ou full

            Uri builtUri_default = Uri.parse(MOVIE_BASE_URL).buildUpon()
                    .appendQueryParameter(I_QUERY_PARAM, params[0])
                    .appendQueryParameter(PLOT_PARAM, plot[1])
                    .appendQueryParameter(R_QUERY_PARAM, format)
                    .appendQueryParameter(TOMATOES_PARAM, "true")
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

        if (vector != null) {
            completeListener.onTaskCompleted(vector, "1", "movieAPI");
        } else {
            Log.d(LOG_TAG, "onPostExecute: VECTOR is Null");
            completeListener.onTaskCompleted(null, "1", "movieAPI");
        }
    }

    private Vector<ContentValues> getMovieDataFromJson(String movieJsonStr)
            throws JSONException {

        final String OWM_PLOT = "Plot";
        final String OWM_RUNTIME = "Runtime";
        final String OWM_RELEASED = "Released";
        final String OWM_GENRE = "Genre";
        final String OWM_DIRECTOR = "Director";
        final String OWM_WRITER = "Writer";
        final String OWM_ACTORS = "Actors";
        final String OWM_METASCORE = "Metascore";
        final String OWM_LANGUAGE = "Language";
        final String OWM_COUNTRY = "Country";
        final String OWM_AWARDS = "Awards";
        final String OWM_IMDBRATING = "imdbRating";
        final String OWM_TOMATORATING = "tomatoRating";

        Log.d(LOG_TAG, "MOVIE STR: " + movieJsonStr);
        try {
            Vector<ContentValues> cVVector = new Vector<ContentValues>(); //= null;
            JSONObject movieJson = new JSONObject(movieJsonStr);

            String plot;
            String runTime;
            String released;
            String genre;
            String director;
            String writer;
            String actors;
            String language;
            String country;
            String awards;
            String metascore;
            String imdbRating;
            String tomatoRating;

            ContentValues movieValues = new ContentValues();

            plot = movieJson.getString(OWM_PLOT);
            runTime = movieJson.getString(OWM_RUNTIME);
            released = movieJson.getString(OWM_RELEASED);
            genre = movieJson.getString(OWM_GENRE);
            director = movieJson.getString(OWM_DIRECTOR);
            writer = movieJson.getString(OWM_WRITER);
            actors = movieJson.getString(OWM_ACTORS);
            language = movieJson.getString(OWM_LANGUAGE);
            country = movieJson.getString(OWM_COUNTRY);
            awards = movieJson.getString(OWM_AWARDS);
            metascore = movieJson.getString(OWM_METASCORE);
            imdbRating = movieJson.getString(OWM_IMDBRATING);
            tomatoRating = movieJson.getString(OWM_TOMATORATING);


            movieValues.put(OWM_PLOT, plot);
            movieValues.put(OWM_RUNTIME, runTime);
            movieValues.put(OWM_RELEASED, released);
            movieValues.put(OWM_GENRE, genre);
            movieValues.put(OWM_DIRECTOR, director);
            movieValues.put(OWM_WRITER, writer);
            movieValues.put(OWM_ACTORS, actors);
            movieValues.put(OWM_LANGUAGE, language);
            movieValues.put(OWM_COUNTRY, country);
            movieValues.put(OWM_AWARDS, awards);
            movieValues.put(OWM_METASCORE, metascore);
            movieValues.put(OWM_IMDBRATING, imdbRating);
            movieValues.put(OWM_TOMATORATING, tomatoRating);


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
