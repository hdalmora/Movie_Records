package dalmora.henrique.movierecords;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dalmora.henrique.movierecords.Interfaces.OnItemClickListener;
import dalmora.henrique.movierecords.Interfaces.OnItemLongClickListener;
import dalmora.henrique.movierecords.Interfaces.MarkWatchedListener;
import dalmora.henrique.movierecords.Interfaces.WatchedListener;
import dalmora.henrique.movierecords.Util.Util;
import dalmora.henrique.movierecords.adapters.MoviesRecyclerAdapter;
import dalmora.henrique.movierecords.data.TableControllerMovie;

import static dalmora.henrique.movierecords.MainActivity.recyclerState;

/**
 * Created by Henrique Dal' Mora R. da Silva on 18/10/2016.
 */

public class SeriesListaFavoritosActivity extends AppCompatActivity implements View.OnClickListener{
    public static final String LOG_TAG = SeriesListaFavoritosActivity.class.getSimpleName();

    TextView tv_show_count;

    private String mSortStr = "DESC";

    private RecyclerView mRecyclerView;
    private StaggeredGridLayoutManager mStaggeredLayoutManager;
    private MoviesRecyclerAdapter mRecyclerAdapter;

    private List<MovieDataStructure> mListStructure = new ArrayList<>();

    private FloatingActionButton fab_go_to_top;


    private OnItemClickListener mItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(int position, View poster, View titleView) {
            //Toast.makeText(getApplicationContext(), "Posição no Adapter: " + position, Toast.LENGTH_SHORT).show();

            String title = "";
            String movieTitle = mListStructure.get(position).getmTitulo();
            if (movieTitle.contains(".) ")){
                String title_only = movieTitle.substring(3);
                if (title_only.contains(")")){
                    title = title_only.replace(")", "");
                } else {
                    title = title_only;
                }
            } else {
                title = movieTitle;
            }

            Log.d(LOG_TAG, "title_only: " + title);
            Intent intent = new Intent(getApplicationContext(), MovieDetailActivity.class);
            intent.putExtra("MOVIE_TITLE", title);
            intent.putExtra("MOVIE_ANO", mListStructure.get(position).getmAno());
            intent.putExtra("MOVIE_POSTER", mListStructure.get(position).getmPosterUrl());
            intent.putExtra("MOVIE_IMDBID", mListStructure.get(position).getmImdbID());
            intent.putExtra("MOVIE_TYPE", mListStructure.get(position).getmTipo());
            intent.putExtra("MOVIE_DESCRIPTION", mListStructure.get(position).getmContent());
            intent.putExtra("MOVIE_BG_URL", mListStructure.get(position).getmBGImageUrl());
            intent.putExtra("MOVIE_IMDB_RATING", mListStructure.get(position).getmImdbRating());
            intent.putExtra("MOVIE_TOMATOE_RATING", mListStructure.get(position).getmTomatoeRating());
            intent.putExtra("MOVIE_METASCORE_RATING", mListStructure.get(position).getmMetaScoreRating());
            intent.putExtra("MOVIE_DURATION", mListStructure.get(position).getmTempoFilme());
            intent.putExtra("MOVIE_GENERO", mListStructure.get(position).getmGeneroFilme());

            intent.putExtra("MOVIE_DIRETOR", mListStructure.get(position).getmDiretor());
            intent.putExtra("MOVIE_ESCRITOR", mListStructure.get(position).getmEscritor());
            intent.putExtra("MOVIE_ATORES", mListStructure.get(position).getmAtores());
            intent.putExtra("MOVIE_IDIOMA", mListStructure.get(position).getmIdioma());
            intent.putExtra("MOVIE_PAIS", mListStructure.get(position).getmPais());
            intent.putExtra("MOVIE_PREMIACOES", mListStructure.get(position).getmPremiacoes());

            intent.putExtra("MOVIE_POSITION", position);
            intent.putExtra("TOP100", false);
            intent.putExtra("FAVORITOS", true);
            startActivity(intent);
        }
    };

    private OnItemLongClickListener mItemLongClickListener = new OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(int position) {
            /*if (isGridViewActive) {
                Toast.makeText(getApplicationContext(), mListStructure.get(position).getmTitulo(), Toast.LENGTH_SHORT).show();
            }
            Log.d("AppInfo", "OnLongClick: CALLED");*/
            return true;
        }
    };

    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "AddedToFavorite" is broadcasted.
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            if (message.equalsIgnoreCase("adicionado")){
                mListStructure.clear();
                readRecords(mSortStr);
                tv_show_count.setText(countRecords() + " Series favoritos");
                Log.d("receiver", "readrecords()");
            }
            Log.d("receiver", "Got message: " + message);
        }
    };

    private String mThemeOptions = null;

    BroadcastReceiver mThemeMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");

            if (message.equals("themeChanged")){
                Log.d(LOG_TAG, "Message: Theme Changed");
                SeriesListaFavoritosActivity.this.recreate();
            }
        }
    };

    BroadcastReceiver mSortMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");

            if (message.equals("sortChangedToDESC")){
                Log.d(LOG_TAG, "Message: sortChangedToDESC");
                mListStructure.clear();
                mSortStr = "DESC";
                readRecords(mSortStr);

            } else if (message.equals("sortChangedToASC")){
                Log.d(LOG_TAG, "Message: sortChangedToASC");
                mListStructure.clear();
                mSortStr = "ASC";
                readRecords(mSortStr);

            } else if (message.equals("sortChangedToWatchedOnly")){
                Log.d(LOG_TAG, "Message: sortChangedToWhatchedOnly");
                mListStructure.clear();
                mSortStr = "WatchedOnly";
                readRecords(mSortStr);

            } else if (message.equals("sortChangedToNotWatchedOnly")){
                Log.d(LOG_TAG, "Message: sortChangedToNotWhatchedOnly");
                mListStructure.clear();
                mSortStr = "NotWatchedOnly";
                readRecords(mSortStr);

            } else if (message.equals("sortChangedToNenhum")){
                Log.d(LOG_TAG, "Message: sortChangedToNenhum");
                mListStructure.clear();
                mSortStr = "sortNenhum";
                readRecords(mSortStr);
            }
            SharedPreferences sortPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = sortPreferences.edit();
            editor.putString("sortState", mSortStr);
            Log.d(LOG_TAG, "mSortStr: " + mSortStr);
            editor.apply();
        }
    };

    private MarkWatchedListener mMarkWatchedListener = new MarkWatchedListener(){
        @Override
        public void onMarkWatched(int position) {
            showDialogWatched(position);
            Log.d(LOG_TAG, "isWatched: " + mListStructure.get(position).getmIsWatched());
        }
    };

    private WatchedListener mWatchedListener = new WatchedListener() {
        @Override
        public void onWatched(int position) {
            Log.d("Position in activity: ", String.valueOf(position));
            mRecyclerAdapter.markWatched(position);
            mListStructure.clear();
            mRecyclerAdapter.notifyDataSetChanged();

            readRecords(mSortStr);
        }
    };

    private void showDialogWatched(int position) {
        DialogWatched dialog = new DialogWatched();
        Bundle bundle = new Bundle();
        bundle.putInt("POSITION", position);
        dialog.setArguments(bundle);
        dialog.setCompleteListener(mWatchedListener);
        dialog.show(getSupportFragmentManager(), "Add a Watched Dialog");
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mThemeMessageReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mSortMessageReceiver);
        super.onDestroy();
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Setar o tema da activity Com base no tema escolhido nas configurações
        mThemeOptions = Util.getPreferredTheme(this);
        Util.setThemePreference(this, mThemeOptions);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_series_lista_favoritos);
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
        LocalBroadcastManager.getInstance(this).registerReceiver(mSortMessageReceiver,
                new IntentFilter("SortChanged"));

        fab_go_to_top = (FloatingActionButton) findViewById(R.id.fab_go_to_top_series_favoritos);
        fab_go_to_top.hide();
        fab_go_to_top.setOnClickListener(this);

        tv_show_count = (TextView) findViewById(R.id.favoritos_series_count_texview);
        tv_show_count.setText(countRecords() + " Series favoritas");


        mRecyclerView = (RecyclerView) findViewById(R.id.favorite_series_recycler_view);
        mRecyclerAdapter = new MoviesRecyclerAdapter(this, mListStructure, mItemClickListener, mItemLongClickListener, mMarkWatchedListener);
        mStaggeredLayoutManager = new StaggeredGridLayoutManager(recyclerState, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mStaggeredLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int pastVisibleItems = 0;

                int[] firstVisibleItemCount = null;
                firstVisibleItemCount = mStaggeredLayoutManager.findFirstVisibleItemPositions(firstVisibleItemCount);
                if (firstVisibleItemCount != null && firstVisibleItemCount.length > 0) {
                    pastVisibleItems = firstVisibleItemCount[0];
                }

                //Controlar a visibilidade do botao para ir para o topo do recycler view
                if (pastVisibleItems <= 3){
                    fab_go_to_top.hide();
                } else if (pastVisibleItems > 3){
                    fab_go_to_top.show();
                }
            }
        });

        SharedPreferences sortPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String sortState = sortPreferences.getString("sortState", "");
        if (!sortState.equalsIgnoreCase("")) {
            readRecords(sortState);
        } else {
            readRecords(mSortStr);
        }

        // Register to receive messages.
        // We are registering an observer (mMessageReceiver) to receive Intents
        // with actions named "AddedToFavorite".
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("AddedToFavorite"));
    }

    private void goBack() {
        super.finish();
        Log.d("APP_INFO", "Back Arrow Clicked");
    }

    public int countRecords() {
        return new TableControllerMovie(this).count("series");
    }

    public void readRecords(String sortStr) {

        List<MovieDataStructure> movies = new TableControllerMovie(this).read("series", sortStr);

        if (movies.size() > 0) {

            for (MovieDataStructure obj : movies) {

                int id = obj.getId();
                String movieTitle = obj.getmTitulo();
                String movieYearRelease = obj.getmAno();
                String movieType = obj.getmTipo();
                String moviePoster = obj.getmPosterUrl();
                String moviePlot = obj.getmContent();
                String movieBGUrl = obj.getmBGImageUrl();
                String movieImdbRating = obj.getmImdbRating();
                String movieTomatoeRating = obj.getmTomatoeRating();
                String movieMetaScoreRating = obj.getmMetaScoreRating();
                String movieDuration = obj.getmTempoFilme();
                String movieGenero = obj.getmGeneroFilme();
                String movieDiretor = obj.getmDiretor();
                String movieEscritor = obj.getmEscritor();
                String movieAtores = obj.getmAtores();
                String movieIdioma = obj.getmIdioma();
                String moviePais = obj.getmPais();
                String moviePremiacoes = obj.getmPremiacoes();
                String movieImdbId = obj.getmImdbID();
                String movieIsWatched = obj.getmIsWatched();
                long movieDate = obj.getData();
                long movieDateWatched = obj.getData_watched();

                mListStructure.add(new MovieDataStructure(null, movieTitle, movieYearRelease, movieType, moviePoster, movieImdbId, moviePlot, "",
                        1, movieBGUrl, movieImdbRating, movieTomatoeRating, movieMetaScoreRating, movieDuration, movieGenero, movieDiretor,
                        movieEscritor, movieAtores, movieIdioma, moviePais, moviePremiacoes, movieDate, movieIsWatched, movieDateWatched));
                mRecyclerAdapter.notifyDataSetChanged();
            }

        }

        else {
            mListStructure.clear();
            mRecyclerAdapter.notifyDataSetChanged();
        }

    }


    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch(id){
            case R.id.fab_go_to_top_series_favoritos:
                StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) mRecyclerView.getLayoutManager();
                layoutManager.scrollToPositionWithOffset(0, 0);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_favoritos, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_sort_data_crescente) {
            sendSortStringBroadcastMessage("DESC");
            Log.d(LOG_TAG, "Filtro Crescente");
        } else if (id == R.id.action_sort_data_decrescente){
            sendSortStringBroadcastMessage("ASC");
            Log.d(LOG_TAG, "Filtro Decrescente");
        } else if (id == R.id.action_sort_assistidos){
            sendSortStringBroadcastMessage("WatchedOnly");
            Log.d(LOG_TAG, "Filtro Assistidos");
        } else if (id == R.id.action_sort_nao_assistidos){
            sendSortStringBroadcastMessage("NotWatchedOnly");
            Log.d(LOG_TAG, "Filtro Não Assistidos");
        } else if (id == R.id.action_sort_nenhum){
            sendSortStringBroadcastMessage("sortNenhum");
        }

        return super.onOptionsItemSelected(item);
    }

    //TODO: Condição para não chamar o método readRecords caso não mude o estado de mSortStr (Clicar na mesma mais de 1 vez)
    private void sendSortStringBroadcastMessage(String sort) {
        Log.d("sender", "Broadcasting message");
        Intent intent = new Intent("SortChanged");

        if (sort.equals("DESC")){
            intent.putExtra("message", "sortChangedToDESC");
        } else if (sort.equals("ASC")){
            intent.putExtra("message", "sortChangedToASC");
        } else if (sort.equals("WatchedOnly")){
            intent.putExtra("message", "sortChangedToWatchedOnly");
        } else if (sort.equals("NotWatchedOnly")){
            intent.putExtra("message", "sortChangedToNotWatchedOnly");
        } else if (sort.equals("sortNenhum")){
            intent.putExtra("message", "sortChangedToNenhum");
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
