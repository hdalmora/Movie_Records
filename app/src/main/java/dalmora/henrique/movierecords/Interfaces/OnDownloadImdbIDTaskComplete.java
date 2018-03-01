package dalmora.henrique.movierecords.Interfaces;

import android.content.ContentValues;

import java.util.Vector;

/**
 * Created by Henrique Dal' Mora R. da Silva on 21/10/2016.
 */

public interface OnDownloadImdbIDTaskComplete {
    void onImdbIDTaskComplete(Vector<ContentValues> movieVector);

}
