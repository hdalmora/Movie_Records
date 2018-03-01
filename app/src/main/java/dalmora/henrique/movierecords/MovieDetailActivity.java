package dalmora.henrique.movierecords;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Vector;

import dalmora.henrique.movierecords.Interfaces.OnBackgroundImageTaskCompleted;
import dalmora.henrique.movierecords.Interfaces.OnDownloadImdbIDTaskComplete;
import dalmora.henrique.movierecords.Interfaces.OnDownloadTvDbIDTaskComplete;
import dalmora.henrique.movierecords.Interfaces.OnTaskCompleted;
import dalmora.henrique.movierecords.Interfaces.OnTaskPreExecute;
import dalmora.henrique.movierecords.Util.Util;
import dalmora.henrique.movierecords.data.TableControllerMovie;

/**
 * Created by Henrique Dal' Mora R. da Silva on 09/10/2016.
 */

public class MovieDetailActivity extends AppCompatActivity implements OnDownloadTvDbIDTaskComplete, OnDownloadImdbIDTaskComplete, OnBackgroundImageTaskCompleted, OnTaskPreExecute, OnTaskCompleted, View.OnClickListener {

    private static final String LOG_TAG = MovieDetailActivity.class.getSimpleName();

    private static final String MOVIE_SHARE_HASHTAG = " #MovieRecordsApp";

    MovieDataStructure mMovieDataStructure = new MovieDataStructure();

    ArrayList<String> bgUrlArray = new ArrayList<>();
    int countClickNext = 0; //Flag para o contador da seta para ir para a proxima imagem de background
    int countClickPrevious = 0;

    String mMovieTitle;
    String mMovieAno;
    String mMoviePosterUrl;
    String mImdbId;
    String mContent;
    String mMovieType;
    String mContentType;
    String mTop100TitleForSearch;
    String mBackgroundImageUrl;
    int mMoviePosition;

    boolean isTop100 = false;
    boolean isTopSeries = false;
    boolean isFavorite = false;
    boolean alreadyDatabaseFavorite;

    private RelativeLayout mLoadingDataAnim;

    ImageView mMoviePosterImageView;
    ImageView mMovieBackgroundImageView;
    ImageView mMovieImdbLogo;
    ImageView mMovieTomatoLogo;
    TextView mPlotTextView;
    TextView mMovieTitleTextView;
    TextView mMovieAnoTextView;
    TextView mMovieGenreTextView;
    TextView mMovieRuntimeTextView;
    TextView mMovieImdbRatingTextView;
    TextView mMovieTomatoRatingTextView;
    TextView mMovieMetaScoreRatingTextView;
    TextView mMovieDirectorTextView;
    TextView mMovieWriterTextView;
    TextView mMovieActorsTextView;
    TextView mMovieLanguageTextView;
    TextView mMovieCountryTextView;
    TextView mMovieAwardsTextView;
    ImageButton mMovieNextBgImageBtn;
    ImageButton mMoviePreviousBgImageBtn;

    Button mAddToFavoriteBtn;

    Menu menu;

    private String mThemeOptions = null;

    BroadcastReceiver mThemeMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");

            if (message.equals("themeChanged")) {
                Log.d(LOG_TAG, "Message: Theme Changed");
                MovieDetailActivity.this.recreate();
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mThemeMessageReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Setar o tema da activity Com base no tema escolhido nas configurações
        mThemeOptions = Util.getPreferredTheme(this);
        Util.setThemePreference(this, mThemeOptions);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back); // Icone Back Arrow
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Voltar para Main Activity
                goBack();
            }
        });


        LocalBroadcastManager.getInstance(this).registerReceiver(mThemeMessageReceiver,
                new IntentFilter("ThemeChanged"));

        //------ TextView e ImageViews Contidas na activity MovieDetailActivity ------//
        mPlotTextView = (TextView) findViewById(R.id.detail_plot_textview);
        mMovieTitleTextView = (TextView) findViewById(R.id.detail_title_textview);
        mMovieAnoTextView = (TextView) findViewById(R.id.detail_ano_textView);
        mMovieGenreTextView = (TextView) findViewById(R.id.detail_genre_textView);
        mMoviePosterImageView = (ImageView) findViewById(R.id.detail_poster_imageview);
        mMovieBackgroundImageView = (ImageView) findViewById(R.id.detail_background_image);
        mMovieRuntimeTextView = (TextView) findViewById(R.id.detail_duration_textView);
        mMovieImdbRatingTextView = (TextView) findViewById(R.id.detail_imdb_rating_textView);
        mMovieTomatoRatingTextView = (TextView) findViewById(R.id.detail_tomatoes_rating_textView);
        mMovieMetaScoreRatingTextView = (TextView) findViewById(R.id.detail_metascore_rating_textView);
        mMovieDirectorTextView = (TextView) findViewById(R.id.detail_diretor_textview);
        mMovieWriterTextView = (TextView) findViewById(R.id.detail_escritor_textview);
        mMovieActorsTextView = (TextView) findViewById(R.id.detail_atores_textview);
        mMovieLanguageTextView = (TextView) findViewById(R.id.detail_idioma_textview);
        mMovieCountryTextView = (TextView) findViewById(R.id.detail_pais_textview);
        mMovieAwardsTextView = (TextView) findViewById(R.id.detail_premiacoes_textview);
        mAddToFavoriteBtn = (Button) findViewById(R.id.detail_btn_add_favorite);
        mAddToFavoriteBtn.setOnClickListener(this);
        mAddToFavoriteBtn.setClickable(false);
        mMovieImdbLogo = (ImageView) findViewById(R.id.detail_imdb_logo_imageview);
        mMovieTomatoLogo = (ImageView) findViewById(R.id.detail_tomatoes_logo_imageview);
        mMovieNextBgImageBtn = (ImageButton) findViewById(R.id.detail_arrow_right);
        mMoviePreviousBgImageBtn = (ImageButton) findViewById(R.id.detail_arrow_left);

        mLoadingDataAnim = (RelativeLayout) findViewById(R.id.loadingPanelDetail);
        mLoadingDataAnim.setVisibility(View.GONE);


        Intent i = this.getIntent();
        Bundle b = i.getExtras();
        if (b != null) {
            mMovieTitle = b.getString("MOVIE_TITLE");
            mMovieAno = b.getString("MOVIE_ANO");
            mMovieType = b.getString("MOVIE_TYPE");
            mMoviePosterUrl = b.getString("MOVIE_POSTER");
            mImdbId = b.getString("MOVIE_IMDBID");
            mMoviePosition = b.getInt("MOVIE_POSITION");
            mTop100TitleForSearch = b.getString("TOP100_TITLE");


            if (mImdbId != null && !mImdbId.isEmpty() && !mImdbId.equals("000")) {
                mMovieImdbLogo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String url = "http://www.imdb.com/title/" + mImdbId + "/";
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    }
                });
            }

            isFavorite = b.getBoolean("FAVORITOS");
            String bgImageUrlFavorite = b.getString("MOVIE_BG_URL");
            String imdbRatingFavorite = b.getString("MOVIE_IMDB_RATING");
            String tomatoeRatingFavorite = b.getString("MOVIE_TOMATOE_RATING");
            String metaScoreRatingFavorite = b.getString("MOVIE_METASCORE_RATING");
            String durationFavorite = b.getString("MOVIE_DURATION");
            String generoFavorite = b.getString("MOVIE_GENERO");
            String diretorFavorite = b.getString("MOVIE_DIRETOR");
            String escritorFavorite = b.getString("MOVIE_ESCRITOR");
            String atoresFavorite = b.getString("MOVIE_ATORES");
            String idiomaFavorite = b.getString("MOVIE_IDIOMA");
            String paisFavorite = b.getString("MOVIE_PAIS");
            String premiacoesFavorite = b.getString("MOVIE_PREMIACOES");

            mContent = b.getString("MOVIE_DESCRIPTION"); //descrição das medias top 100
            mContentType = b.getString("MOVIE_TYPE");
            isTopSeries = b.getBoolean("TOP_SERIES");
            isTop100 = b.getBoolean("TOP100"); //Se é top 100 ou filme retornado por pesquisa na api

            if (mMovieTitle != null) {
                mMovieTitleTextView.setText(mMovieTitle);
                alreadyDatabaseFavorite = new TableControllerMovie((getApplicationContext())).hasObject(mMovieTitle);

                mMovieTomatoLogo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String title = "";
                        if (mMovieTitle.contains(", Season")) {
                            title = mMovieTitle.split(", Season")[0];
                            Log.d(LOG_TAG, "title_serie: " + title);
                        } else {
                            title = mMovieTitle;
                        }

                        String url_tomatoes = "https://www.rottentomatoes.com/search/?search=" + title;
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url_tomatoes));
                        startActivity(intent);
                    }
                });


                if (alreadyDatabaseFavorite) {
                    mAddToFavoriteBtn.setText(R.string.str_remover_favoritos);
                    mAddToFavoriteBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_btn_remove_favorite));
                }

            }
            if (mMovieAno != null) {
                mMovieAnoTextView.setText("(" + mMovieAno + ")");
            }

            if (!isFavorite) {

                //Media acessada pelas listas top 100
                if (isTop100) {
                    Log.d(LOG_TAG, "Media is Top 100");
                    //get movie ImdbId
                    DownloadImdbIDTask downloadImdbIDTask = new DownloadImdbIDTask(this, this, this);
                    if (mTop100TitleForSearch.contains(" (")) {
                        String[] titulo_split = mTop100TitleForSearch.split(" \\(");
                        String titulo = titulo_split[0];
                        Log.d(LOG_TAG, titulo_split[0]);
                        downloadImdbIDTask.execute(titulo);
                    } else {
                        downloadImdbIDTask.execute(mTop100TitleForSearch);
                    }
                    if (isTopSeries) {
                        if (mTop100TitleForSearch != null) {
                            DownloadSerieTvDbIdTask downloadSerieTvDbIdTask = new DownloadSerieTvDbIdTask(this, this, this);
                            downloadSerieTvDbIdTask.execute(mTop100TitleForSearch);
                            updateTopSeriesContent(mTop100TitleForSearch);
                        }
                        Log.d(LOG_TAG, "Media is Top 100 Series");
                    } else {
                        Log.d(LOG_TAG, "Media is Top 100 Movies");
                    }
                } else {
                    Log.d(LOG_TAG, "Media is from OMDB API");

                    if (mMovieType.equals("series")) {
                        //Download das informações que faltam paraos filmes pesquisados na barra de pesquisa(plot, genero, etc...)
                        Log.d(LOG_TAG, "Movie type equals series");
                        Log.d(LOG_TAG, "Serie Title: " + mMovieTitle);
                        downloadBackgroundImage(mMovieTitle, true);
                    } else {
                        Log.d(LOG_TAG, "Movie type equals movie");
                        downloadBackgroundImage(mImdbId, false);
                    }
                    updateMovieContent(mImdbId);
                }
            } else {
                //FAVORITOS
                //Toast.makeText(getApplicationContext(), "FAVORITO", Toast.LENGTH_SHORT).show();
                mAddToFavoriteBtn.setClickable(true);
                if (mContent != null) {
                    mPlotTextView.setText(mContent);
                }
                if (bgImageUrlFavorite != null && !bgImageUrlFavorite.equals("no_bg")) {
                    Picasso.with(this).load(bgImageUrlFavorite).error(R.drawable.ic_error_outline).fit().centerCrop().into(mMovieBackgroundImageView);
                } else {
                    Picasso.with(this).load(R.drawable.placeholder_bg_no_image).error(R.drawable.ic_error_outline).placeholder(R.drawable.image_loading_progress_animation).fit().centerCrop().into(mMovieBackgroundImageView);
                }
                mMovieImdbRatingTextView.setText(imdbRatingFavorite);
                mMovieTomatoRatingTextView.setText(tomatoeRatingFavorite);
                mMovieMetaScoreRatingTextView.setText(metaScoreRatingFavorite);
                mMovieGenreTextView.setText(generoFavorite);
                mMovieRuntimeTextView.setText(durationFavorite);
                mMovieDirectorTextView.setText(diretorFavorite);
                mMovieWriterTextView.setText(escritorFavorite);
                mMovieActorsTextView.setText(atoresFavorite);
                mMovieLanguageTextView.setText(idiomaFavorite);
                mMovieCountryTextView.setText(paisFavorite);
                mMovieAwardsTextView.setText(premiacoesFavorite);

                long currentTime = System.currentTimeMillis();

                mMovieDataStructure.setmTituloSerie(null);
                mMovieDataStructure.setmTitulo(mMovieTitle);
                mMovieDataStructure.setmAno(mMovieAno);
                mMovieDataStructure.setmTipo(mMovieType);
                mMovieDataStructure.setmPosterUrl(mMoviePosterUrl);
                //mMovieDataStructure.setmImdbID(mImdbId);
                mMovieDataStructure.setmImdbID(mImdbId);
                mMovieDataStructure.setmContent(mContent);
                mMovieDataStructure.setmTotalResults("1");
                mMovieDataStructure.setId(0);
                mMovieDataStructure.setmBGImageUrl(bgImageUrlFavorite);
                mMovieDataStructure.setmImdbRating(imdbRatingFavorite);
                mMovieDataStructure.setmTomatoeRating(tomatoeRatingFavorite);
                mMovieDataStructure.setmMetaScoreRating(metaScoreRatingFavorite);
                mMovieDataStructure.setmTempoFilme(durationFavorite);
                mMovieDataStructure.setmGeneroFilme(generoFavorite);
                mMovieDataStructure.setmDiretor(diretorFavorite);
                mMovieDataStructure.setmEscritor(escritorFavorite);
                mMovieDataStructure.setmAtores(atoresFavorite);
                mMovieDataStructure.setmIdioma(idiomaFavorite);
                mMovieDataStructure.setmPais(paisFavorite);
                mMovieDataStructure.setmPremiacoes(premiacoesFavorite);
                mMovieDataStructure.setData(currentTime);


                mMovieImdbLogo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(LOG_TAG, "FAVORITOS IMDBID" + mImdbId);
                        String url = "http://www.imdb.com/title/" + mImdbId + "/";
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    }
                });
            }

            Log.d(LOG_TAG, "ID: " + mImdbId);
            if (!mMoviePosterUrl.equals("N/A")) {
                Picasso.with(this).load(mMoviePosterUrl).error(R.drawable.ic_error_outline).fit().centerCrop().into(mMoviePosterImageView);
            } else {
                Picasso.with(this).load(R.drawable.no_image_available).error(R.drawable.ic_error_outline).fit().centerCrop().into(mMoviePosterImageView);
            }
        }
    }

    private void updateTopSeriesContent(String serieName) {
        DownloadTopSeriesContentTask downloadTopSeriesContentTask = new DownloadTopSeriesContentTask(this, this, this);
        downloadTopSeriesContentTask.execute(serieName);
    }


    private void downloadBackgroundImage(String imdbId, boolean isTopSeries) {
        if (isTopSeries) {
            //Se for serie, deve pegar o id na api theTvDB para depois pesquisar na fanart.tv
            DownloadSerieTvDbIdTask downloadSerieTvDbIdTask = new DownloadSerieTvDbIdTask(this, this, this);
            downloadSerieTvDbIdTask.execute(imdbId);
        } else {
            DownloadMovieBackgroundImageTask movieBackgroundImageTask = new DownloadMovieBackgroundImageTask(this, this, this);
            movieBackgroundImageTask.execute(imdbId, "moviesTOP100");
        }
    }

    private void updateMovieContent(String imdbId) {
        DownloadMovieContentsTask downloadMovieContentTask = new DownloadMovieContentsTask(this, this, this);
        downloadMovieContentTask.execute(imdbId);
    }


    private void actionShare() {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, "Movie Records App");
        i.putExtra(Intent.EXTRA_TEXT, mMovieTitle + MOVIE_SHARE_HASHTAG);
        startActivity(Intent.createChooser(i, getResources().getText(R.string.action_share)));
    }

    private void goBack() {
        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            //supportFinishAfterTransition();
        } else {
            super.finish();
        }*/
        super.finish();
        Log.d(LOG_TAG, "Back Arrow Clicked");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_movie_detail, menu);

        alreadyDatabaseFavorite = new TableControllerMovie((getApplicationContext())).hasObject(mMovieTitle);
        //TODO: QUando adiciona ou remove da database alterar avisibility dinamicamente
        MenuItem item = menu.findItem(R.id.action_favorito);
        if (alreadyDatabaseFavorite) {
            item.setVisible(true);
        } else {
            item.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            /*Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);*/
        } else if (id == R.id.action_share) {
            actionShare();
        } else if (id == R.id.home) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                supportFinishAfterTransition();
            } else {
                super.finish();
            }
        } else if (id == R.id.action_favorito) {
            //Funçãojá assisti
            Toast.makeText(this, "Favorito", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTaskCompleted(Vector<ContentValues> movieVector, String totalResults, String type) {

        mLoadingDataAnim.setVisibility(View.GONE);
        mAddToFavoriteBtn.setClickable(true);

        String mPlotStr = "";
        String mGenreStr = "";
        String mRuntimeStr = "";
        String mReleasedStr = "";
        String mDirectorStr = "";
        String mWriterStr = "";
        String mActorsStr = "";
        String mMetaScoreStr = "";
        String mLanguageStr = "";
        String mCountryStr = "";
        String mAwardsStr = "";
        String mImdbRatingStr = "";
        String mTomatoRatingStr = "";
        String mImdbIdSerieStr = null;

        if (movieVector != null) {
            for (ContentValues movieValues : movieVector) {
                mPlotStr = movieValues.get("Plot").toString();
                mGenreStr = movieValues.get("Genre").toString();
                mRuntimeStr = movieValues.get("Runtime").toString();
                mReleasedStr = movieValues.get("Released").toString();
                mDirectorStr = movieValues.get("Director").toString();
                mWriterStr = movieValues.get("Writer").toString();
                mActorsStr = movieValues.get("Actors").toString();
                mMetaScoreStr = movieValues.get("Metascore").toString();
                mLanguageStr = movieValues.get("Language").toString();
                mCountryStr = movieValues.get("Country").toString();
                mAwardsStr = movieValues.get("Awards").toString();
                mImdbRatingStr = movieValues.get("imdbRating").toString();
                mTomatoRatingStr = movieValues.get("tomatoRating").toString();

            }

            Log.d(LOG_TAG, "Type: " + type);
            mPlotTextView.setText(mPlotStr);
            mMovieGenreTextView.setText(mGenreStr);
            mMovieRuntimeTextView.setText(mRuntimeStr);
            mMovieImdbRatingTextView.setText(mImdbRatingStr + "/10");
            mMovieTomatoRatingTextView.setText(mTomatoRatingStr + "/10");
            mMovieMetaScoreRatingTextView.setText(mMetaScoreStr);
            mMovieDirectorTextView.setText(mDirectorStr);
            mMovieWriterTextView.setText(mWriterStr);
            mMovieActorsTextView.setText(mActorsStr);
            mMovieLanguageTextView.setText(mLanguageStr);
            mMovieCountryTextView.setText(mCountryStr);
            mMovieAwardsTextView.setText(mAwardsStr);

            Log.d(LOG_TAG, "MOVIE DIRECTOR: " + mDirectorStr);

            long currentTime = System.currentTimeMillis();

            Log.d(LOG_TAG, "BG URL: " + mBackgroundImageUrl);
            mMovieDataStructure.setmTituloSerie(null);
            mMovieDataStructure.setmTitulo(mMovieTitle);
            mMovieDataStructure.setmAno(mMovieAno);
            mMovieDataStructure.setmTipo(mMovieType);
            mMovieDataStructure.setmPosterUrl(mMoviePosterUrl);
            mMovieDataStructure.setmImdbID(mImdbId);
            mMovieDataStructure.setmContent(mPlotStr);
            mMovieDataStructure.setmTotalResults("1");
            mMovieDataStructure.setId(1);
            mMovieDataStructure.setmBGImageUrl(mBackgroundImageUrl);
            mMovieDataStructure.setmImdbRating(mImdbRatingStr);
            mMovieDataStructure.setmTomatoeRating(mTomatoRatingStr);
            mMovieDataStructure.setmMetaScoreRating(mMetaScoreStr);
            mMovieDataStructure.setmTempoFilme(mRuntimeStr);
            mMovieDataStructure.setmGeneroFilme(mGenreStr);
            mMovieDataStructure.setmDiretor(mDirectorStr);
            mMovieDataStructure.setmEscritor(mWriterStr);
            mMovieDataStructure.setmAtores(mActorsStr);
            mMovieDataStructure.setmIdioma(mLanguageStr);
            mMovieDataStructure.setmPais(mCountryStr);
            mMovieDataStructure.setmPremiacoes(mAwardsStr);
            mMovieDataStructure.setData(currentTime);

        } else {
            Toast.makeText(getApplicationContext(), R.string.str_nenhuma_informacao_exibir, Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onImdbIDTaskComplete(Vector<ContentValues> movieVector) {

        mLoadingDataAnim.setVisibility(View.GONE);

        String mTop100ImdbId = null;

        if (movieVector != null) {
            ContentValues values = movieVector.get(0);
            mTop100ImdbId = values.get("imdbID").toString();
            mImdbId = mTop100ImdbId;

            mMovieDataStructure.setmImdbID(mImdbId);
            Log.d(LOG_TAG, "IMDBID TASK COMPLETE TOP100: " + mImdbId);
            mMovieImdbLogo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = "http://www.imdb.com/title/" + mImdbId + "/";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }
            });


            if (mTop100ImdbId != null) {
                if (!isTopSeries) {
                    downloadBackgroundImage(mTop100ImdbId, false);
                }
                updateMovieContent(mTop100ImdbId);
            }
        } else {
            mBackgroundImageUrl = "no_bg";
            Picasso.with(this).load(R.drawable.placeholder_bg_no_image).error(R.drawable.ic_error_outline).placeholder(R.drawable.image_loading_progress_animation).fit().centerCrop().into(mMovieBackgroundImageView);
        }

    }

    @Override
    public void onBackgrounImageTaskCompleted(Vector<ContentValues> movieVector) {
        //mLoadingDataAnim.setVisibility(View.GONE);


        if (movieVector != null) {

            int count = 0;
            for (ContentValues valuesArray : movieVector){
                //Log.d(LOG_TAG,  count + " " + valuesArray.toString());
                bgUrlArray.add(count, valuesArray.get("url_" + count).toString());
                count++;
            }

            //TODO: Terminar a funcionalidade de selecionar os diferentes backgrounds quando disponíveis na api.
            if (bgUrlArray.size() > 0){
                mBackgroundImageUrl = bgUrlArray.get(0);

                if (bgUrlArray.size() > 1){
                    //Setar as setas para selecionar as imagens visíveis
                    mMovieNextBgImageBtn.setVisibility(View.VISIBLE);
                    mMoviePreviousBgImageBtn.setVisibility(View.VISIBLE);


                    //TODO: Salvar o Estado das Var. countClickNext e CountClickPrevious para tratar caso seja rotacionada a tela
                    mMovieNextBgImageBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            /******* TESTE ********/
                            String url;
                            if (countClickNext < bgUrlArray.size() - 1){
                                Log.d(LOG_TAG, "bgUrlArray Size: " + bgUrlArray.size());
                                countClickNext += 1;
                                url = bgUrlArray.get(countClickNext);
                                Picasso.with(getApplicationContext()).load(url).error(R.drawable.ic_error_outline).placeholder(R.drawable.image_loading_progress_animation).fit().centerCrop().into(mMovieBackgroundImageView);
                                Log.d(LOG_TAG, "Count Click Next: " + countClickNext);
                                Log.d(LOG_TAG, "Url: " + url);
                            }
                        }
                    });

                    mMoviePreviousBgImageBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            /******* TESTE ********/
                            String url;
                            if (countClickPrevious >= 0){
                                if (countClickNext > 0){
                                    countClickNext = countClickNext - 1;
                                }
                                countClickPrevious = countClickNext;
                                url = bgUrlArray.get(countClickPrevious);
                                Picasso.with(getApplicationContext()).load(url).error(R.drawable.ic_error_outline).placeholder(R.drawable.image_loading_progress_animation).fit().centerCrop().into(mMovieBackgroundImageView);
                                Log.d(LOG_TAG, "Count Click Previous: " + countClickPrevious);
                                Log.d(LOG_TAG, "Url: " + url);
                            }

                        }
                    });

                } else {
                    //Como só existe apenas uma imagem, retirar as setas
                    mMovieNextBgImageBtn.setVisibility(View.GONE);
                    mMoviePreviousBgImageBtn.setVisibility(View.GONE);
                }
            } else {
                //Não existe nenhuma url de background para o filme selecionado
                mBackgroundImageUrl = "no_bg";
                mMovieNextBgImageBtn.setVisibility(View.GONE);
                mMoviePreviousBgImageBtn.setVisibility(View.GONE);
            }

            Log.d(LOG_TAG, "URL: " + mBackgroundImageUrl);
            if (mBackgroundImageUrl != null) {
                Picasso.with(this).load(mBackgroundImageUrl).error(R.drawable.ic_error_outline).placeholder(R.drawable.image_loading_progress_animation).fit().centerCrop().into(mMovieBackgroundImageView);
                if (mMovieType.equals("series")) {
                    mMovieDataStructure.setmBGImageUrl(mBackgroundImageUrl);
                }
            }

            Log.d(LOG_TAG, "BG_VECTOR_SIZE: " + movieVector.size());
            Log.d(LOG_TAG, "BG_VECTOR: " + movieVector.toString());
        } else {
            if (mMovieType.equals("series")) {
                mMovieDataStructure.setmBGImageUrl("no_bg");
            } else {
                mBackgroundImageUrl = "no_bg";
            }
            Picasso.with(this).load(R.drawable.placeholder_bg_no_image).error(R.drawable.ic_error_outline).placeholder(R.drawable.image_loading_progress_animation).fit().centerCrop().into(mMovieBackgroundImageView);

        }
    }

    @Override
    public void onTheTvDbIDTaskComplete(String theTvDbID) {
        mLoadingDataAnim.setVisibility(View.GONE);

        if (theTvDbID != null) {
            Log.d(LOG_TAG, "ID RECUPERADO: " + theTvDbID);
            DownloadMovieBackgroundImageTask movieBackgroundImageTask = new DownloadMovieBackgroundImageTask(this, this, this);
            movieBackgroundImageTask.execute(theTvDbID, "seriesTOP100");
        } else {
            Log.d(LOG_TAG, "ID RECUPERADO: " + "null");
            mBackgroundImageUrl = "no_bg";
        }

    }

    @Override
    public void onTaskPreExecute() {
        mLoadingDataAnim.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.detail_btn_add_favorite:
                //Salvar dados na database
                alreadyDatabaseFavorite = new TableControllerMovie((getApplicationContext())).hasObject(mMovieTitle);

                if (alreadyDatabaseFavorite) {
                    //Toast.makeText(getApplicationContext(), "Já pertence aos favoritos", Toast.LENGTH_SHORT).show();

                    boolean deleteSuccessful = new TableControllerMovie(getApplicationContext()).delete(mMovieTitle);

                    if (deleteSuccessful) {
                        invalidateOptionsMenu();
                        Toast.makeText(getApplicationContext(), R.string.str_removido_favoritos, Toast.LENGTH_SHORT).show();
                        sendAddedOrRemovedFromFavoriteMessage(true);
                        mAddToFavoriteBtn.setText(R.string.str_adicionar_favoritos);
                        mAddToFavoriteBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_btn_add_favorite));
                    } else {
                        sendAddedOrRemovedFromFavoriteMessage(false);
                        Toast.makeText(getApplicationContext(), R.string.str_erro_ao_remover_favoritos, Toast.LENGTH_SHORT).show();
                    }

                } else {
                    boolean createSuccessful = new TableControllerMovie(getApplicationContext()).create(mMovieDataStructure);

                    //Toda vez que um filme for adicionada na databse, eu quero notificar essa ação através de broadcast receiver
                    if (createSuccessful) {
                        invalidateOptionsMenu();
                        sendAddedOrRemovedFromFavoriteMessage(true); //Enviar a notificação
                        Toast.makeText(getApplicationContext(), R.string.str_adicionado_favoritos, Toast.LENGTH_SHORT).show();
                        mAddToFavoriteBtn.setText(R.string.str_remover_favoritos);
                        mAddToFavoriteBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_btn_remove_favorite));
                    } else {
                        sendAddedOrRemovedFromFavoriteMessage(false);
                        Toast.makeText(getApplicationContext(), R.string.str_erro_salvar_informacoes, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    //Enviar um Intent com a ação "AddedToFavorite", que será usado para notificar
    //quando um filme é adicionado ou removido da database para as outras activities (Que receberão essa ação)
    private void sendAddedOrRemovedFromFavoriteMessage(boolean createSuccessfull) {
        Log.d("sender", "Broadcasting message");
        Intent intent = new Intent("AddedToFavorite");

        if (createSuccessfull) {
            intent.putExtra("message", "adicionado");
        } else {
            intent.putExtra("message", "erro ao adicionar");
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}
