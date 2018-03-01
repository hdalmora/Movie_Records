package dalmora.henrique.movierecords.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import dalmora.henrique.movierecords.MovieDataStructure;

/**
 * Created by Henrique Dal' Mora R. da Silva on 17/10/2016.
 */

public class TableControllerMovie extends MovieDbHelper {
    public static final String LOG_TAG = TableControllerMovie.class.getSimpleName();

    public TableControllerMovie(Context context) {
        super(context);
    }

    public boolean markWatched (String titulo, boolean isWatched){
        ContentValues value = new ContentValues();

        if(isWatched) {
            value.put(MovieEntry.COLUMN_MOVIE_IS_WATCHED, "1");
        } else {
            value.put(MovieEntry.COLUMN_MOVIE_IS_WATCHED, "0");
        }

        //value.put(MovieEntry.COLUMN_MOVIE_IS_WATCHED, 1);

        SQLiteDatabase db = this.getWritableDatabase();

        Log.d(LOG_TAG, "Titulo: " + titulo);

        boolean markWatchedSuccessful = db.update(MovieEntry.TABLE_NAME, value, String.format("%s = ?", MovieEntry.COLUMN_MOVIE_NAME)
                , new String[]{titulo}) > 0;
        db.close();

        return markWatchedSuccessful;
    }

    public boolean updateDateWatched (String titulo, long time){
        ContentValues values = new ContentValues();

        if (time != 0){
            values.put(MovieEntry.COLUMN_MOVIE_DATE_WATCHED, time);

            SQLiteDatabase db = this.getWritableDatabase();

            Log.d(LOG_TAG, "Titulo: " + titulo);

            boolean updateDateWatchedSuccessful = db.update(MovieEntry.TABLE_NAME, values, String.format("%s = ?", MovieEntry.COLUMN_MOVIE_NAME)
                    , new String[]{titulo}) > 0;
            db.close();

            return updateDateWatchedSuccessful;

        }  else {
            Log.d(LOG_TAG, "Tempo inválido (Deve ser != 0");
            return false;
        }
    }

    public boolean create(MovieDataStructure movieDataStructure) {

        ContentValues values = new ContentValues();

        values.put(MovieEntry.COLUMN_MOVIE_NAME, movieDataStructure.getmTitulo());
        values.put(MovieEntry.COLUMN_MOVIE_IMDB_ID, movieDataStructure.getmImdbID());
        values.put(MovieEntry.COLUMN_MOVIE_TYPE, movieDataStructure.getmTipo());
        values.put(MovieEntry.COLUMN_MOVIE_PLOT, movieDataStructure.getmContent());
        values.put(MovieEntry.COLUMN_MOVIE_YEAR_RELEASE, movieDataStructure.getmAno());
        values.put(MovieEntry.COLUMN_MOVIE_POSTER_URL, movieDataStructure.getmPosterUrl());
        values.put(MovieEntry.COLUMN_MOVIE_BG_URL, movieDataStructure.getmBGImageUrl());
        values.put(MovieEntry.COLUMN_MOVIE_IMDB_RATING, movieDataStructure.getmImdbRating());
        values.put(MovieEntry.COLUMN_MOVIE_TOMATOE_RATING, movieDataStructure.getmTomatoeRating());
        values.put(MovieEntry.COLUMN_MOVIE_METASCORE_RATING, movieDataStructure.getmMetaScoreRating());
        values.put(MovieEntry.COLUMN_MOVIE_DURATION, movieDataStructure.getmTempoFilme());
        values.put(MovieEntry.COLUMN_MOVIE_GENERO, movieDataStructure.getmGeneroFilme());
        values.put(MovieEntry.COLUMN_MOVIE_DIRECTOR, movieDataStructure.getmDiretor());
        values.put(MovieEntry.COLUMN_MOVIE_WRITER, movieDataStructure.getmEscritor());
        values.put(MovieEntry.COLUMN_MOVIE_ACTORS, movieDataStructure.getmAtores());
        values.put(MovieEntry.COLUMN_MOVIE_LANGUAGE, movieDataStructure.getmIdioma());
        values.put(MovieEntry.COLUMN_MOVIE_COUNTRY, movieDataStructure.getmPais());
        values.put(MovieEntry.COLUMN_MOVIE_AWARDS, movieDataStructure.getmPremiacoes());
        values.put(MovieEntry.COLUMN_MOVIE_DATE, movieDataStructure.getData());
        values.put(MovieEntry.COLUMN_MOVIE_IS_WATCHED, movieDataStructure.getmIsWatched());
        values.put(MovieEntry.COLUMN_MOVIE_DATE_WATCHED, movieDataStructure.getData_watched());

        SQLiteDatabase db = this.getWritableDatabase();

        boolean createSuccessful = db.insert(MovieEntry.TABLE_NAME, null, values) > 0;
        db.close();

        return createSuccessful;
    }

    public int count(String type) {

        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "SELECT * FROM " + MovieEntry.TABLE_NAME + " WHERE " + MovieEntry.COLUMN_MOVIE_TYPE + " =?";

        int recordCount = db.rawQuery(sql, new String[]{type}).getCount();
        db.close();

        return recordCount;

    }

    public List<MovieDataStructure> read(String type, String ordem) {

        SQLiteDatabase db = this.getWritableDatabase();

        List<MovieDataStructure> recordsList = new ArrayList<MovieDataStructure>();
        String sql;
        Cursor cursor = null;

        if (ordem.equals("DESC") || ordem.equals("ASC")){
            if (type.equals("movies")) {
                //Ler apenas o tipo dos filmes
                sql = "SELECT * FROM " + MovieEntry.TABLE_NAME + " WHERE " + MovieEntry.COLUMN_MOVIE_TYPE + "=? OR " + MovieEntry.COLUMN_MOVIE_TYPE + "=? ORDER BY " + MovieEntry.COLUMN_MOVIE_DATE +" " + ordem;
                cursor = db.rawQuery(sql, new String[]{"movie", "Movie"});
            } else if (type.equals("series")) {
                //Ler apenas o tipo das Series
                sql = "SELECT * FROM " + MovieEntry.TABLE_NAME + " WHERE " + MovieEntry.COLUMN_MOVIE_TYPE + "=? OR " + MovieEntry.COLUMN_MOVIE_TYPE + "=? ORDER BY " + MovieEntry.COLUMN_MOVIE_DATE +" " + ordem;
                cursor = db.rawQuery(sql, new String[]{"series", "Series"});
            } else if (type.equals("game")){
                sql = "SELECT * FROM " + MovieEntry.TABLE_NAME + " WHERE " + MovieEntry.COLUMN_MOVIE_TYPE + "=? OR " + MovieEntry.COLUMN_MOVIE_TYPE + "=? ORDER BY " + MovieEntry.COLUMN_MOVIE_DATE +" " + ordem;
                cursor = db.rawQuery(sql, new String[]{"game"});
            } else {
                //Ler todos os tipos
                sql = "SELECT * FROM " + MovieEntry.TABLE_NAME + " ORDER BY _id DESC";
                cursor = db.rawQuery(sql, null);
            }
        } else {

            if (type.equals("movies")){
                if (ordem.equals("WatchedOnly")) {
                    //Ler apenas filmes marcados como "assistidos"
                    sql = "SELECT * FROM " + MovieEntry.TABLE_NAME + " WHERE " + MovieEntry.COLUMN_MOVIE_IS_WATCHED + " =? AND " + MovieEntry.COLUMN_MOVIE_TYPE + "=? ";
                    cursor = db.rawQuery(sql, new String[]{"1", "movie"});
                } else if (ordem.equals("NotWatchedOnly")) {
                    sql = "SELECT * FROM " + MovieEntry.TABLE_NAME + " WHERE " + MovieEntry.COLUMN_MOVIE_IS_WATCHED + " =? AND " + MovieEntry.COLUMN_MOVIE_TYPE + "=? ";
                    cursor = db.rawQuery(sql, new String[]{"0", "movie"});
                } else if (ordem.equals("sortNenhum")) {
                    sql = "SELECT * FROM " + MovieEntry.TABLE_NAME + " WHERE " + MovieEntry.COLUMN_MOVIE_TYPE + " =? ";
                    cursor = db.rawQuery(sql, new String[]{"movie"});
                }
            } else if (type.equals("series")){
                if (ordem.equals("WatchedOnly")) {
                    //Ler apenas Series marcadas como "assistidas"
                    sql = "SELECT * FROM " + MovieEntry.TABLE_NAME + " WHERE " + MovieEntry.COLUMN_MOVIE_IS_WATCHED + " =? AND " + MovieEntry.COLUMN_MOVIE_TYPE + "=? ";
                    cursor = db.rawQuery(sql, new String[]{"1", "series"});
                } else if (ordem.equals("NotWatchedOnly")) {
                    sql = "SELECT * FROM " + MovieEntry.TABLE_NAME + " WHERE " + MovieEntry.COLUMN_MOVIE_IS_WATCHED + " =? AND " + MovieEntry.COLUMN_MOVIE_TYPE + "=? ";
                    cursor = db.rawQuery(sql, new String[]{"0", "series"});
                } else if (ordem.equals("sortNenhum")) {
                    sql = "SELECT * FROM " + MovieEntry.TABLE_NAME + " WHERE " + MovieEntry.COLUMN_MOVIE_TYPE + " =? ";
                    cursor = db.rawQuery(sql, new String[]{"series"});
                }
            } else if (type.equals("game")) {
                if (ordem.equals("WatchedOnly")) {
                    //Ler apenas Games marcados como "Jogados"
                    sql = "SELECT * FROM " + MovieEntry.TABLE_NAME + " WHERE " + MovieEntry.COLUMN_MOVIE_IS_WATCHED + " =? AND " + MovieEntry.COLUMN_MOVIE_TYPE + "=? ";
                    cursor = db.rawQuery(sql, new String[]{"1", "game"});
                } else if (ordem.equals("NotWatchedOnly")) {
                    sql = "SELECT * FROM " + MovieEntry.TABLE_NAME + " WHERE " + MovieEntry.COLUMN_MOVIE_IS_WATCHED + " =? AND " + MovieEntry.COLUMN_MOVIE_TYPE + "=? ";
                    cursor = db.rawQuery(sql, new String[]{"0", "game"});
                } else if (ordem.equals("sortNenhum")) {
                    sql = "SELECT * FROM " + MovieEntry.TABLE_NAME + " WHERE " + MovieEntry.COLUMN_MOVIE_TYPE + " =? ";
                    cursor = db.rawQuery(sql, new String[]{"game"});
                }

            }

        }

        if (cursor.moveToFirst()) {
            do {

                int id = Integer.parseInt(cursor.getString(cursor.getColumnIndex("_id")));
                String movieTitle = cursor.getString(cursor.getColumnIndex("movie_name"));
                String movieImdbId = cursor.getString(cursor.getColumnIndex("movie_imdb_id"));
                String movieType = cursor.getString(cursor.getColumnIndex("movie_type"));
                String moviePlot = cursor.getString(cursor.getColumnIndex("movie_plot"));
                String movieYearRelease = cursor.getString(cursor.getColumnIndex("movie_year_release"));
                String moviePosterUrl = cursor.getString(cursor.getColumnIndex("movie_poster_url"));
                String movieBGUrl = cursor.getString(cursor.getColumnIndex("movie_bg_url"));
                String movieImdbRating = cursor.getString(cursor.getColumnIndex("movie_imdb_rating"));
                String movieTomatoeRating = cursor.getString(cursor.getColumnIndex("movie_tomatoe_rating"));
                String movieMetaScoreRating = cursor.getString(cursor.getColumnIndex("movie_metascore_rating"));
                String movieDuration = cursor.getString(cursor.getColumnIndex("movie_duration"));
                String movieGenero = cursor.getString(cursor.getColumnIndex("movie_genero"));
                String movieDiretor = cursor.getString(cursor.getColumnIndex("movie_director"));
                String movieEscritor = cursor.getString(cursor.getColumnIndex("movie_writer"));
                String movieAtores = cursor.getString(cursor.getColumnIndex("movie_actors"));
                String movieIdioma = cursor.getString(cursor.getColumnIndex("movie_language"));
                String moviePais = cursor.getString(cursor.getColumnIndex("movie_country"));
                String moviePremiacoes = cursor.getString(cursor.getColumnIndex("movie_awards"));
                long movieData = cursor.getLong(cursor.getColumnIndex("movie_date"));
                long movieDataWatched = cursor.getLong(cursor.getColumnIndex("movie_date_watched"));
                String movieIsWatched = cursor.getString(cursor.getColumnIndex("is_watched"));


                MovieDataStructure movieDataStructure = new MovieDataStructure(null, movieTitle, movieYearRelease, movieType, moviePosterUrl, movieImdbId,
                        moviePlot, "1", id, movieBGUrl, movieImdbRating, movieTomatoeRating, movieMetaScoreRating, movieDuration, movieGenero,
                        movieDiretor, movieEscritor, movieAtores, movieIdioma, moviePais, moviePremiacoes, movieData, movieIsWatched, movieDataWatched);

                recordsList.add(movieDataStructure);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return recordsList;
    }


    public boolean hasObject(String movieTitle) {
        SQLiteDatabase db = getWritableDatabase();
        String selectString = "SELECT * FROM " + MovieEntry.TABLE_NAME + " WHERE " + MovieEntry.COLUMN_MOVIE_NAME + " =?";

        // Adicionar a String que esta sendo Pesquisada aqui
        // Colocar em um array para evitar "Unrecognized token Error"
        Cursor cursor = db.rawQuery(selectString, new String[]{movieTitle});

        boolean hasObject = false;
        if (cursor.moveToFirst()) {
            hasObject = true;

            

            int count = 0;
            while (cursor.moveToNext()) {
                count++;
            }
          
            Log.d(LOG_TAG, String.format("%d records found", count));



        }

        cursor.close();          // Fechar o cursor
        db.close();              // Fechar a Database
        return hasObject;
    }

	// Função DELETE
    public boolean delete(String movieTitle) {
        boolean deleteSuccessful = false;

        SQLiteDatabase db = this.getWritableDatabase();

        if (movieTitle.contains("'")){
            movieTitle = movieTitle.replaceAll("'","''");
        }

        deleteSuccessful = db.delete(MovieEntry.TABLE_NAME, MovieEntry.COLUMN_MOVIE_NAME +" ='" + movieTitle + "'", null) > 0;

        db.close();

        return deleteSuccessful;

    }
}
