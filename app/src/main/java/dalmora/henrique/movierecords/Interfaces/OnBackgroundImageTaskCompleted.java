package dalmora.henrique.movierecords.Interfaces;

import android.content.ContentValues;

import java.util.Vector;

/**
 * Created by Henrique Dal' Mora R. da Silva on 20/10/2016.
 */

public interface OnBackgroundImageTaskCompleted {
    void onBackgrounImageTaskCompleted(Vector<ContentValues> movieVector);
}
