package dalmora.henrique.movierecords;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources.Theme;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import dalmora.henrique.movierecords.Interfaces.OnItemClickListener;
import dalmora.henrique.movierecords.Interfaces.OnItemLongClickListener;
import dalmora.henrique.movierecords.Interfaces.OnTaskCompleted;
import dalmora.henrique.movierecords.Interfaces.OnTaskPreExecute;
import dalmora.henrique.movierecords.Interfaces.MarkWatchedListener;
import dalmora.henrique.movierecords.Interfaces.WatchedListener;
import dalmora.henrique.movierecords.Util.Util;
import dalmora.henrique.movierecords.adapters.MoviesRecyclerAdapter;

import static dalmora.henrique.movierecords.MainActivity.recyclerState;

public class ListaTopMoviesActivity extends AppCompatActivity implements OnTaskCompleted, OnTaskPreExecute, View.OnClickListener {

    public static final String LOG_TAG = ListaTopMoviesActivity.class.getSimpleName();

    private String mGenreSelectedName = "";
    private String mItemSelectedNameSaved = "";
    int mItemSelectedPosition;

    private String mFileContents;
    private RecyclerView mRecyclerView;
    List<MovieDataStructure> mList = new ArrayList<>();

    private StaggeredGridLayoutManager mStaggeredLayoutManager;

    private MoviesRecyclerAdapter mRecyclerAdapter;

    private List<MovieDataStructure> mListStructure = new ArrayList<>();

    private int mRecyclerState = recyclerState;
    private boolean isGridViewActive = MainActivity.isGridViewActive;
    private Menu menu;

    private FloatingActionButton fab_go_to_top;

    private RelativeLayout mLoadingDataAnim;

    private OnItemClickListener mItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(int position, View poster, View titleView) {
            //Toast.makeText(getApplicationContext(), "Posição no Adapter: " + position, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), MovieDetailActivity.class);

            String movieTitle = mListStructure.get(position).getmTitulo();
            String title_only = movieTitle.substring(4);
            String title = "";
            if (title_only.contains(")")){
                title = title_only.replace(")", "").replace("(", "");
            } else {
                title = title_only;
            }
            Log.d(LOG_TAG, "title_only: " + title);
            intent.putExtra("MOVIE_TITLE", title);
            intent.putExtra("TOP100_TITLE", mListStructure.get(position).getmTituloSerie());
            intent.putExtra("MOVIE_ANO", mListStructure.get(position).getmAno());
            intent.putExtra("MOVIE_TYPE", mListStructure.get(position).getmTipo());
            intent.putExtra("MOVIE_POSTER", mListStructure.get(position).getmPosterUrl());
            intent.putExtra("MOVIE_IMDBID", mListStructure.get(position).getmImdbID());
            intent.putExtra("MOVIE_DESCRIPTION", mListStructure.get(position).getmContent());
            intent.putExtra("TOP100", true);
            intent.putExtra("TOP_SERIES", false);
            startActivity(intent);
        }
    };

    private OnItemLongClickListener mItemLongClickListener = new OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(int position) {
            Toast.makeText(getApplicationContext(), mListStructure.get(position).getmTitulo(), Toast.LENGTH_SHORT).show();
            return true;
        }
    };

    private String mThemeOptions = null;

    BroadcastReceiver mThemeMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");

            if (message.equals("themeChanged")){
                Log.d(LOG_TAG, "Message: Theme Changed");
                ListaTopMoviesActivity.this.recreate();
            }
        }
    };

    private MarkWatchedListener mMarkWatchedListener = new MarkWatchedListener(){
        @Override
        public void onMarkWatched(int position) {
            //showDialogWatched(position);
        }
    };

    private WatchedListener mWatchedListener = new WatchedListener() {
        @Override
        public void onWatched(int position) {
            Log.d("Position in activity: ", String.valueOf(position));
            mRecyclerAdapter.markWatched(position);
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
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mThemeMessageReceiver);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // super.onSaveInstanceState(outState);

        //outState.putBoolean("isGridViewActive", isGridViewActive);

        if (!mListStructure.isEmpty()) {
            outState.putParcelableArrayList("RecyclerState", (ArrayList<? extends Parcelable>) mListStructure);

        }
        outState.putString("ItemSelecteName", mGenreSelectedName);
        outState.putInt("ItemSelectedPosition", mItemSelectedPosition);
        Log.d("SPINNER", "onSaveInstanceState ItemName: " + mGenreSelectedName);
        Log.d("onSaveInstanceState", "CALLED");
        //Log.d("onSaveInstanceState", mListStructure.toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        //super.onRestoreInstanceState(savedInstanceState);

        Log.d("onViewStateRestored", "Called");

        if (savedInstanceState != null) {

            if (savedInstanceState.getParcelableArrayList("RecyclerState") != null) {
                mListStructure.clear();
                mListStructure = savedInstanceState.getParcelableArrayList("RecyclerState");
                mRecyclerAdapter = new MoviesRecyclerAdapter(getApplicationContext(), mListStructure, mItemClickListener, mItemLongClickListener, mMarkWatchedListener);
                mRecyclerView.setAdapter(mRecyclerAdapter);
                mRecyclerAdapter.notifyDataSetChanged();
                //Log.d("onViewStateRestored", "Not Null" + savedInstanceState.getParcelableArrayList("RecyclerState"));
            }

            mItemSelectedNameSaved = savedInstanceState.getString("ItemSelecteName");

            Log.d("SPINNER", "onRestoreInstanceState Item Name: " + mItemSelectedNameSaved);

        }


    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        //Setar o tema da activity Com base no tema escolhido nas configurações
        mThemeOptions = Util.getPreferredTheme(this);
        Util.setThemePreference(this, mThemeOptions);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_top_filmes);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back); // Icone Back Arrow
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Voltar para Main Activity
                goBack();
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(mThemeMessageReceiver,
                new IntentFilter("ThemeChanged"));

        fab_go_to_top = (FloatingActionButton) findViewById(R.id.fab_go_to_top_top_movies);
        fab_go_to_top.hide();
        fab_go_to_top.setOnClickListener(this);


        // Setup spinner
        Spinner spinner = (Spinner) findViewById(R.id.spinner_top_movies);
        spinner.setAdapter(new MyAdapter(
                toolbar.getContext(),
                new String[]{
                        "Action & Adventure",
                        "African",
                        "All",
                        "Anime",
                        "Bollywood",
                        "Classics",
                        "Comedy",
                        "Concert Films",
                        "Documentary",
                        "Drama",
                        "Foreign",
                        "Holiday",
                        "Horror",
                        "Independent",
                        "Japanese Cinema",
                        "Jidaigeki",
                        "Kids & Family",
                        "Korean Cinema",
                        "Made for TV",
                        "MiddleEastern",
                        "Music Documentaries",
                        "Music Feature Films",
                        "Musicals",
                        "Regional Indian",
                        "Romance",
                        "Russian",
                        "Sci-Fi & Fantasy",
                        "Short Films",
                        "Special Interest",
                        "Sports",
                        "Thriller",
                        "Tokusatsu",
                        "Turkish",
                        "Urban",
                        "Western",
                }));

        //Restaurar o estado do item selecionado no spinner quando a tela é rotacionada
        if (savedInstanceState != null) {
            //posição do item selecionado no spinner
            int position = savedInstanceState.getInt("ItemSelectedPosition");
            Log.d("SPINNER", "onRestoreInstanceState Item Position: " + position);
            spinner.setSelection(position);

            isGridViewActive = savedInstanceState.getBoolean("isGridViewActive");
            Log.d("onRestoreInstanceState", "isGridViewActive: " + isGridViewActive);

        }

        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // When the given dropdown item is selected, show its contents in the
                // container view.

                mGenreSelectedName = parent.getItemAtPosition(position).toString();

                mItemSelectedPosition = position;

                //mItemSelectedNameSaved = mGenreSelectedName;

                /*Log.d("SPINNER", "ITEM NAME: " + mGenreSelectedName);
                Log.d("SPINNER", "ITEM POSITION: " + mItemSelectedPosition);*/

                String genreUrl = loadMovieGenreUrl(mGenreSelectedName);
                /*Log.d("SPINNER Main", "URL: " + genreUrl);*/

                updateMovieGenreContent(genreUrl);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_top_movies);
        mStaggeredLayoutManager = new StaggeredGridLayoutManager(MainActivity.recyclerState, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mStaggeredLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerAdapter = new MoviesRecyclerAdapter(getApplicationContext(), mListStructure, mItemClickListener, mItemLongClickListener, mMarkWatchedListener);
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


        mLoadingDataAnim = (RelativeLayout) findViewById(R.id.loadingPanelTopListaMovies);
        mLoadingDataAnim.setVisibility(View.GONE);
    }

    private void updateMovieGenreContent(String url) {
        mListStructure.clear();
        mRecyclerAdapter.notifyDataSetChanged();
        DownloadTopMovieTask donwloadData = new DownloadTopMovieTask(getApplicationContext(), this, this);
        donwloadData.execute(url);
    }

    private String loadMovieGenreUrl(String itemName) {
        String url = "";
        String genre_id;
        switch (itemName) {
            case "Action & Adventure":
                url = "https://itunes.apple.com/us/rss/topmovies/limit=100/genre=4401/json";
                break;
            case "African":
                url = "https://itunes.apple.com/us/rss/topmovies/limit=100/genre=4434/json";
                break;
            case "All":
                url = "https://itunes.apple.com/us/rss/topmovies/limit=100/json";
                break;
            case "Anime":
                url = "https://itunes.apple.com/us/rss/topmovies/limit=100/genre=4402/json";
                break;
            case "Bollywood":
                url = "https://itunes.apple.com/us/rss/topmovies/limit=100/genre=4431/json";
                break;
            case "Classics":
                url = "https://itunes.apple.com/us/rss/topmovies/limit=100/genre=4403/json";
                break;
            case "Comedy":
                url = "https://itunes.apple.com/us/rss/topmovies/limit=100/genre=4404/json";
                break;
            case "Concert Films":
                url = "https://itunes.apple.com/us/rss/topmovies/limit=100/genre=4422/json";
                break;
            case "Documentary":
                url = "https://itunes.apple.com/us/rss/topmovies/limit=100/genre=4405/json";
                break;
            case "Drama":
                url = "https://itunes.apple.com/us/rss/topmovies/limit=100/genre=4406/json";
                break;
            case "Foreign":
                url = "https://itunes.apple.com/us/rss/topmovies/limit=100/genre=4407/json";
                break;
            case "Holiday":
                url = "https://itunes.apple.com/us/rss/topmovies/limit=100/genre=4420/json";
                break;
            case "Horror":
                url = "https://itunes.apple.com/us/rss/topmovies/limit=100/genre=4408/json";
                break;
            case "Independent":
                url = "https://itunes.apple.com/us/rss/topmovies/limit=100/genre=4409/json";
                break;
            case "Japanese Cinema":
                url = "https://itunes.apple.com/us/rss/topmovies/limit=100/genre=4425/json";
                break;
            case "Jidaigeki":
                url = "https://itunes.apple.com/us/rss/topmovies/limit=100/genre=4426/json";
                break;
            case "Kids & Family":
                url = "https://itunes.apple.com/us/rss/topmovies/limit=100/genre=4410/json";
                break;
            case "Korean Cinema":
                url = "https://itunes.apple.com/us/rss/topmovies/limit=100/genre=4428/json";
                break;
            case "Made for TV":
                url = "https://itunes.apple.com/us/rss/topmovies/limit=100/genre=4421/json";
                break;
            case "MiddleEastern":
                url = "https://itunes.apple.com/us/rss/topmovies/limit=100/genre=4433/json";
                break;
            case "Music Documentaries":
                url = "https://itunes.apple.com/us/rss/topmovies/limit=100/genre=4423/json";
                break;
            case "Music Feature Films":
                url = "https://itunes.apple.com/us/rss/topmovies/limit=100/genre=4424/json";
                break;
            case "Musicals":
                url = "https://itunes.apple.com/us/rss/topmovies/limit=100/genre=4411/json";
                break;
            case "Regional Indian":
                url = "https://itunes.apple.com/us/rss/topmovies/limit=100/genre=4432/json";
                break;
            case "Romance":
                url = "https://itunes.apple.com/us/rss/topmovies/limit=100/genre=4412/json";
                break;
            case "Russian":
                url = "https://itunes.apple.com/us/rss/topmovies/limit=100/genre=4429/json";
                break;
            case "Sci-Fi & Fantasy":
                url = "https://itunes.apple.com/us/rss/topmovies/limit=100/genre=4413/json";
                break;
            case "Short Films":
                url = "https://itunes.apple.com/us/rss/topmovies/limit=100/genre=4414/json";
                break;
            case "Special Interest":
                url = "https://itunes.apple.com/us/rss/topmovies/limit=100/genre=4415/json";
                break;
            case "Sports":
                url = "https://itunes.apple.com/us/rss/topmovies/limit=100/genre=4417/json";
                break;
            case "Thriller":
                url = "https://itunes.apple.com/us/rss/topmovies/limit=100/genre=4416/json";
                break;
            case "Tokusatsu":
                url = "https://itunes.apple.com/us/rss/topmovies/limit=100/genre=4427/json";
                break;
            case "Turkish":
                url = "https://itunes.apple.com/us/rss/topmovies/limit=100/genre=4430/json";
                break;
            case "Urban":
                url = "https://itunes.apple.com/us/rss/topmovies/limit=100/genre=4419/json";
                break;
            case "Western":
                url = "https://itunes.apple.com/us/rss/topmovies/limit=100/genre=4418/json";
                break;

            default:
                url = "https://itunes.apple.com/us/rss/topmovies/limit=100/genre=4401/json";
                break;
        }
        //updateTopFilmes(url);
        return url;
    }

    private void goBack() {
        super.finish();
        Log.d("APP_INFO", "Back Arrow Clicked");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_lista_top_filmes, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return true;
        //return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {

        int id = view.getId();

        switch(id){
            case R.id.fab_go_to_top_top_movies:
                StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) mRecyclerView.getLayoutManager();
                layoutManager.scrollToPositionWithOffset(0, 0);
                break;
        }
    }

    @Override
    public void onTaskPreExecute() {
        mLoadingDataAnim.setVisibility(View.VISIBLE);

    }

    @Override
    public void onTaskCompleted(Vector<ContentValues> movieVector, String totalResults, String type) {
        mLoadingDataAnim.setVisibility(View.GONE);

        String mTitle;
        String mReleaseDate;
        String mContentType;
        //String mSummary;
        String mPosterUrl;

        int contador = 0;

        if (movieVector != null) {

            for (ContentValues movieValues : movieVector) {

                contador += 1;
                mTitle = movieValues.get("im:name").toString();
                mReleaseDate = movieValues.get("im:releaseDate").toString();
                mContentType = movieValues.get("im:contentType").toString();
                //mSummary = movieValues.get("summary").toString();
                mPosterUrl = movieValues.get("im:image").toString();

                mListStructure.add(new MovieDataStructure(mTitle, contador + ".) " + mTitle, mReleaseDate, mContentType, mPosterUrl, "000", null,
                        "100", 1, null, null, null,null,null, null, null, null, null, null, null, null, 0, "0", 0));
                mRecyclerAdapter.notifyDataSetChanged();

                Log.d("TITULO: ", mTitle
                        + " Tipo: " + mContentType
                        + " Ano: " + mReleaseDate);
                //+ "PosterUrl: " + mPosterUrl
                //+ "Conteúdo: " + mSummary); Conteúdo Ok
                //+ "TotalResults" + mTotalRestults);
            }
        } else {
            Toast.makeText(getApplicationContext(), "Erro no download dos filmes...", Toast.LENGTH_SHORT).show();
        }

    }


    private static class MyAdapter extends ArrayAdapter<String> implements ThemedSpinnerAdapter {
        private final ThemedSpinnerAdapter.Helper mDropDownHelper;

        public MyAdapter(Context context, String[] objects) {
            super(context, android.R.layout.simple_list_item_1, objects);
            mDropDownHelper = new ThemedSpinnerAdapter.Helper(context);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                // Inflate the drop down using the helper's LayoutInflater
                LayoutInflater inflater = mDropDownHelper.getDropDownViewInflater();
                view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            } else {
                view = convertView;
            }

            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setText(getItem(position));

            return view;
        }

        @Override
        public Theme getDropDownViewTheme() {
            return mDropDownHelper.getDropDownViewTheme();
        }

        @Override
        public void setDropDownViewTheme(Theme theme) {
            mDropDownHelper.setDropDownViewTheme(theme);
        }
    }
}
