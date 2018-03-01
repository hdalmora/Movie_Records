package dalmora.henrique.movierecords.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by Henrique Dal' Mora R. da Silva on 17/10/2016.
 */

public class MovieDbHelper extends SQLiteOpenHelper {

    // Ao mudar o esquema da DataBase, deve se incrementar a versão da database
    private static final int DATABASE_VERSION = 14;

    public static final String DATABASE_NAME = "movierecords.db";


    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {


        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MovieEntry.COLUMN_MOVIE_NAME + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_MOVIE_IMDB_ID + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_MOVIE_TOMATOE_RATING + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_MOVIE_METASCORE_RATING + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_MOVIE_TYPE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_MOVIE_PLOT + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_MOVIE_IMDB_RATING + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_MOVIE_YEAR_RELEASE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_MOVIE_GENERO + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_MOVIE_DIRECTOR + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_MOVIE_WRITER + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_MOVIE_ACTORS + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_MOVIE_LANGUAGE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_MOVIE_COUNTRY + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_MOVIE_AWARDS + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_MOVIE_DURATION + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_MOVIE_BG_URL + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_MOVIE_DATE + " LONG NOT NULL, " +
                MovieEntry.COLUMN_MOVIE_DATE_WATCHED + " LONG NOT NULL, " +
                MovieEntry.COLUMN_MOVIE_POSTER_URL + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_MOVIE_IS_WATCHED + " TEXT NOT NULL" +
                " )";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }


    /* Classe Interna que define as tabelas da database movierecords.db */
    public static final class MovieEntry implements BaseColumns {

        public static final String TABLE_NAME = "movie";

        public static final String COLUMN_MOVIE_NAME = "movie_name";

        public static final String COLUMN_MOVIE_IMDB_ID = "movie_imdb_id";

        public static final String COLUMN_MOVIE_TYPE = "movie_type";

        public static final String COLUMN_MOVIE_PLOT = "movie_plot";

        public static final String COLUMN_MOVIE_IMDB_RATING = "movie_imdb_rating";

        public static final String COLUMN_MOVIE_TOMATOE_RATING = "movie_tomatoe_rating";

        public static final String COLUMN_MOVIE_METASCORE_RATING = "movie_metascore_rating";

        public static final String COLUMN_MOVIE_YEAR_RELEASE = "movie_year_release";

        public static final String COLUMN_MOVIE_DURATION = "movie_duration";

        public static final String COLUMN_MOVIE_GENERO = "movie_genero";

        public static final String COLUMN_MOVIE_DIRECTOR = "movie_director";

        public static final String COLUMN_MOVIE_WRITER = "movie_writer";

        public static final String COLUMN_MOVIE_ACTORS = "movie_actors";

        public static final String COLUMN_MOVIE_LANGUAGE = "movie_language";

        public static final String COLUMN_MOVIE_COUNTRY = "movie_country";

        public static final String COLUMN_MOVIE_AWARDS = "movie_awards";

        public static final String COLUMN_MOVIE_BG_URL = "movie_bg_url";

        public static final String COLUMN_MOVIE_POSTER_URL = "movie_poster_url";

        public static final String COLUMN_MOVIE_DATE = "movie_date";

        public static final String COLUMN_MOVIE_DATE_WATCHED = "movie_date_watched";

        public static final String COLUMN_MOVIE_IS_WATCHED = "is_watched";

    }
}
