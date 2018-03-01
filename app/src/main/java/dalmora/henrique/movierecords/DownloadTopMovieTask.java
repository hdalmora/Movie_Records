package dalmora.henrique.movierecords;

import android.content.ContentValues;
import android.content.Context;
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
 * Created by Henrique Dal' Mora R. da Silva on 13/10/2016.
 */

public class DownloadTopMovieTask extends AsyncTask<String, Void, Vector<ContentValues>> {

    private final String LOG_TAG = DownloadTopMovieTask.class.getSimpleName();

    private OnTaskCompleted completeListener;
    private OnTaskPreExecute preExecuteListener;

    private final Context mContext;

    private String mFileContents;


    public DownloadTopMovieTask(Context context, OnTaskCompleted completeListener, OnTaskPreExecute preExecuteListener){
        mContext = context;
        this.completeListener = completeListener;
        this.preExecuteListener = preExecuteListener;
    }


    @Override
    protected Vector<ContentValues> doInBackground(String... params) {
        /*//Verificar comprimento dos params, pois se não houver nenhum dado, não há nada para retornar
        mFileContents = downloadXmlFile(params[0]);
        if (mFileContents == null) {
            Log.d("DonwloadData", "Erro no donwload");

        }
        return mFileContents;*/

        //Verificar comprimento dos params, pois se não houver nenhum dado, não há nada para retornar
        if (params.length == 0) {
            return null;
        }

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String movieJsonStr = null;

        try{
            URL myURL = new URL(params[0]);
            Log.d(LOG_TAG, "URL: " + myURL);

            urlConnection = (HttpURLConnection) myURL.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {

                buffer.append(line + "\n");
            }
            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            movieJsonStr = buffer.toString();

            return downloadMovieJsonFile(movieJsonStr);

        }catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
        }catch (JSONException e){
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

    private Vector<ContentValues> downloadMovieJsonFile(String movieJsonStr)
            throws JSONException {

        // Tags do arquivo Json que serão extraídas
        final String OWM_FEED = "feed";
        final String OWM_ENTRY = "entry";
        final String OWM_NAME = "im:name";
        final String OWM_IMAGE = "im:image";
        final String OWM_CONTENT_TYPE = "im:contentType";
        final String OWM_RELEASE_DATE= "im:releaseDate";

        final String OWM_LABEL = "label";

        Vector<ContentValues> cVVector = null;

        try{

            JSONObject movieJson = new JSONObject(movieJsonStr);

            JSONObject feedJsonObject = movieJson.getJSONObject(OWM_FEED);
            JSONArray movieArray = feedJsonObject.getJSONArray(OWM_ENTRY);

            cVVector = new Vector<ContentValues>(movieArray.length());

            for (int i = 0; i < movieArray.length(); i++) {
                String title;
                String imageUrl;
                String releaseDate;

                ContentValues movieValues = new ContentValues();

                title = movieArray.getJSONObject(i).getJSONObject(OWM_NAME).getString(OWM_LABEL);
                imageUrl =  movieArray.getJSONObject(i).getJSONArray(OWM_IMAGE).getJSONObject(2).getString("label");
                releaseDate = movieArray.getJSONObject(i).getJSONObject(OWM_RELEASE_DATE).getJSONObject("attributes").getString(OWM_LABEL);

                movieValues.put(OWM_NAME, title);
                movieValues.put(OWM_IMAGE, imageUrl);
                movieValues.put(OWM_CONTENT_TYPE, "movie");
                movieValues.put(OWM_RELEASE_DATE, releaseDate);

                cVVector.add(movieValues);
            }
            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);

                return cVVector;
            }
        }catch(JSONException e) {
            Log.e(LOG_TAG, "Error closing stream", e);
        }
        return null;

    }


    @Override
    protected void onPreExecute() {
        preExecuteListener.onTaskPreExecute();
    }

    @Override
    protected void onPostExecute(Vector<ContentValues> vector) {

        if (vector != null){
            completeListener.onTaskCompleted(vector, "0", "movieTOP100");
        } else {
            Log.d(LOG_TAG, "onPostExecute: VECTOR is Null");
            completeListener.onTaskCompleted(null, null, "movieTOP100");
        }
    }
}
