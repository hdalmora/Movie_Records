package dalmora.henrique.movierecords;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import dalmora.henrique.movierecords.Interfaces.OnBackgroundImageTaskCompleted;
import dalmora.henrique.movierecords.Interfaces.OnDownloadTvDbIDTaskComplete;
import dalmora.henrique.movierecords.Interfaces.OnTaskPreExecute;

/**
 * Created by Henrique Dal' Mora R. da Silva on 22/10/2016.
 */

public class DownloadSerieTvDbIdTask extends AsyncTask<String, Void, String> {
    private String mFileContents;

    private OnDownloadTvDbIDTaskComplete theTvDbCompleteListener;
    private OnTaskPreExecute onTaskPreExecute;
    private Context mContext;

    public DownloadSerieTvDbIdTask(Context context, OnDownloadTvDbIDTaskComplete theTvDbCompleteListener, OnTaskPreExecute onTaskPreExecute) {
        mContext = context;
        this.theTvDbCompleteListener = theTvDbCompleteListener;
        this.onTaskPreExecute = onTaskPreExecute;
        // ProgressDialog dialog = new ProgressDialog(mContext);
    }

    @Override
    protected String doInBackground(String... params) {

        mFileContents = donwloadXMLFile(params[0]);
        if (mFileContents == null) {
            Log.d("DonwloadData", "Erro no donwload");

        } else {
            //Log.d("DonwloadData", mFileContents);
        }
        return parseXmlFile(mFileContents);
    }

    @Override
    protected void onPostExecute(String s) {
        if (s != null){
            Log.d("AppInfo", "ID PostExecute: " + s);
            theTvDbCompleteListener.onTheTvDbIDTaskComplete(s);
        } else {
            theTvDbCompleteListener.onTheTvDbIDTaskComplete(null);
            Log.d("AppInfo", "ID PostExecute: null");
        }
    }

    @Override
    protected void onPreExecute() {
        onTaskPreExecute.onTaskPreExecute();
    }

    private String donwloadXMLFile(String nomeSerie) {
        try {

            final String MOVIE_BASE_URL = "http://thetvdb.com/api/GetSeries.php?seriesname=";
            String serie_uri = nomeSerie.replace(" ", "%20").replace("'", "");

            Uri builtUri_default = Uri.parse(MOVIE_BASE_URL + serie_uri);

            URL myURL = new URL(builtUri_default.toString());
            //Log.d("AppInfo", "URL: " + myURL);
            HttpURLConnection myconnection = (HttpURLConnection) myURL.openConnection();
            int response = myconnection.getResponseCode();
            //Log.d("DonwloadData", "The response code was " + response);
            InputStream data = myconnection.getInputStream();
            InputStreamReader caracteres = new InputStreamReader(data);
            int numCharRead;
            char[] inputBuffer = new char[500];
            StringBuilder tempBuffer = new StringBuilder();
            while (true) {
                numCharRead = caracteres.read(inputBuffer);
                if (numCharRead <= 0) {
                    break;
                }

                tempBuffer.append(String.copyValueOf(inputBuffer, 0, numCharRead));
            }

            return tempBuffer.toString();

        } catch (Exception e) {
            Log.d("DownloadData", "Exception reading data: " + e.getMessage());
        }
        return null;
    }

    private String parseXmlFile (String fileContents) {
        boolean inEntry = false;
        String textValue ="";
        String theTvDbID = "";

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(fileContents));

            int counter = 0;
            int evenType = xpp.getEventType();

            //contador < 10 pois eh o numero de tags dentro da tag 'Series'
            //faço isso pois quero o resultado da primeira tag apenas
            while (evenType != XmlPullParser.END_DOCUMENT && counter < 10) {
                String tagName = xpp.getName();
                switch (evenType) {
                    case XmlPullParser.START_TAG:
                        Log.d("ParseApplication", "Start tag for " + tagName);
                        if (tagName.equals("Series")) {
                            inEntry = true;
                        }
                        break;
                    case XmlPullParser.TEXT:
                        textValue = xpp.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        Log.d("ParseApplication", "Ending tag for " + tagName);
                        if (inEntry) {
                            if (tagName.equals("Series")) {
                                inEntry = false;
                            } else if (tagName.equalsIgnoreCase("seriesid")) {
                                //pegar o id da série
                                theTvDbID = textValue;
                                Log.d("AppInfo", "ID: " + textValue);
                            }
                        }
                        break;
                    default:
                }
                counter += 1;
                evenType=xpp.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return theTvDbID ;
    }
}


