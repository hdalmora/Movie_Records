package dalmora.henrique.movierecords.Interfaces;

import android.content.ContentValues;

import java.util.Vector;

/**
 * Created by Henrique Dal' Mora R. da Silva on 09/10/2016.
 */

public interface OnTaskCompleted {
    void onTaskCompleted(Vector<ContentValues> movieVector, String totalResults, String type);
}
