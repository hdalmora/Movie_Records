package dalmora.henrique.movierecords;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
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

/**
 * Created by Henrique Dal' Mora R. da Silva on 18/10/2016.
 */

public class ListaTopSeriesActivity extends AppCompatActivity implements OnTaskCompleted, OnTaskPreExecute, View.OnClickListener {

    public static final String LOG_TAG = ListaTopSeriesActivity.class.getSimpleName();

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

    String[] title_parts;
    String serie_title_only;
    String serie_title_season;

    private OnItemClickListener mItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(int position, View poster, View titleView) {
            //Toast.makeText(getApplicationContext(), "Posição no Adapter: " + position, Toast.LENGTH_SHORT).show();

            String movieTitle = mListStructure.get(position).getmTitulo();
            String title_only = movieTitle.substring(4);
            String title = "";
            if (title_only.contains(")")){
                title = title_only.replace(")", "").replace("(", "");
            } else {
                title = title_only;
            }
            Log.d(LOG_TAG, "title_only: " + title);

            Intent intent = new Intent(getApplicationContext(), MovieDetailActivity.class);
            intent.putExtra("MOVIE_TITLE", title);
            intent.putExtra("TOP100_TITLE", mListStructure.get(position).getmTituloSerie());
            intent.putExtra("MOVIE_ANO", mListStructure.get(position).getmAno());
            intent.putExtra("MOVIE_TYPE", mListStructure.get(position).getmTipo());
            intent.putExtra("MOVIE_POSTER", mListStructure.get(position).getmPosterUrl());
            intent.putExtra("MOVIE_IMDBID", mListStructure.get(position).getmImdbID());
            intent.putExtra("MOVIE_DESCRIPTION", mListStructure.get(position).getmContent());
            intent.putExtra("TOP100", true);
            intent.putExtra("TOP_SERIES", true);
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
                ListaTopSeriesActivity.this.recreate();
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
        setContentView(R.layout.activity_lista_top_series);

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

        fab_go_to_top = (FloatingActionButton) findViewById(R.id.fab_go_to_top_top_series);
        fab_go_to_top.hide();
        fab_go_to_top.setOnClickListener(this);

        // Setup spinner
        Spinner spinner = (Spinner) findViewById(R.id.spinner_top_series);
        spinner.setAdapter(new ListaTopSeriesActivity.MyAdapter(
                toolbar.getContext(),
                new String[]{
                        "Action & Adventure",
                        "All",
                        "Animation",
                        "Classic",
                        "Comedy",
                        "Drama",
                        "Kids",
                        "Latino TV",
                        "Nonfiction",
                        "Reality TV",
                        "Sci-Fi & Fantasy",
                        "Sports",
                        "Teens",
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

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // When the given dropdown item is selected, show its contents in the
                // container view.

                mGenreSelectedName = parent.getItemAtPosition(position).toString();

                mItemSelectedPosition = position;

                //mItemSelectedNameSaved = mGenreSelectedName;

                Log.d("SPINNER", "ITEM NAME: " + mGenreSelectedName);
                Log.d("SPINNER", "ITEM POSITION: " + mItemSelectedPosition);

                String genreUrl = loadMovieGenreUrl(mGenreSelectedName);
                /*Log.d("SPINNER Main", "URL: " + genreUrl);*/

                updateSerieGenreContent(genreUrl);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_top_series);
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


        mLoadingDataAnim = (RelativeLayout) findViewById(R.id.loadingPanelTopListaSeries);
        mLoadingDataAnim.setVisibility(View.GONE);
    }

    private void goBack() {
        super.finish();
        Log.d("APP_INFO", "Back Arrow Clicked");
    }

    private void updateSerieGenreContent(String url) {
        mListStructure.clear();
        mRecyclerAdapter.notifyDataSetChanged();
        DownloadTopSeriesTask donwloadData = new DownloadTopSeriesTask(getApplicationContext(), this, this);
        donwloadData.execute(url);
    }


    private String loadMovieGenreUrl(String itemName) {
        String url = "";
        String genre_id;
        switch (itemName) {
            case "Action & Adventure":
                url = "https://itunes.apple.com/us/rss/toptvseasons/limit=100/genre=4003/json";
                break;
            case "All":
                url = "https://itunes.apple.com/us/rss/toptvseasons/limit=100/json";
                break;
            case "Animation":
                url = "https://itunes.apple.com/us/rss/toptvseasons/limit=100/genre=4002/json";
                break;
            case "Classic":
                url = "https://itunes.apple.com/us/rss/toptvseasons/limit=100/genre=4004/json";
                break;
            case "Comedy":
                url = "https://itunes.apple.com/us/rss/toptvseasons/limit=100/genre=4000/json";
                break;
            case "Drama":
                url = "https://itunes.apple.com/us/rss/toptvseasons/limit=100/genre=4001/json";
                break;
            case "Kids":
                url = "https://itunes.apple.com/us/rss/toptvseasons/limit=100/genre=4005/json";
                break;
            case "Latino TV":
                url = "https://itunes.apple.com/us/rss/toptvseasons/limit=100/genre=4011/json";
                break;
            case "Nonfiction":
                url = "https://itunes.apple.com/us/rss/toptvseasons/limit=100/genre=4006/json";
                break;
            case "Reality TV":
                url = "https://itunes.apple.com/us/rss/toptvseasons/limit=100/genre=4007/json";
                break;
            case "Sci-Fi & Fantasy":
                url = "https://itunes.apple.com/us/rss/toptvseasons/limit=100/genre=4008/json";
                break;
            case "Sports":
                url = "https://itunes.apple.com/us/rss/toptvseasons/limit=100/genre=4009/json";
                break;
            case "Teens":
                url = "https://itunes.apple.com/us/rss/toptvseasons/limit=100/genre=4010/json";
                break;
            default:
                url = "https://itunes.apple.com/us/rss/toptvseasons/limit=100/genre=4003/json";
                break;
        }
        return url;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_lista_top_series, menu);

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
            case R.id.fab_go_to_top_top_series:
                StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) mRecyclerView.getLayoutManager();
                layoutManager.scrollToPositionWithOffset(0, 0);
                break;
        }
    }

    @Override
    public void onTaskCompleted(Vector<ContentValues> movieVector, String totalResults, String Type) {

        mLoadingDataAnim.setVisibility(View.GONE);

        String mTitle;
        String mReleaseDate;
        String mContentType;
        String mPosterUrl;

        int contador = 0;

        if (movieVector != null) {

            for (ContentValues movieValues : movieVector) {

                contador += 1;
                mTitle = movieValues.get("title").toString();
                mReleaseDate = movieValues.get("im:releaseDate").toString();
                mContentType = movieValues.get("im:contentType").toString();
                mPosterUrl = movieValues.get("im:image").toString();

                title_parts = mTitle.split(" - ");
                serie_title_only = title_parts[1];
                serie_title_season = title_parts[0];

                mListStructure.add(new MovieDataStructure(serie_title_only, contador + ".) " + serie_title_season, mReleaseDate, mContentType, mPosterUrl, "000", "N/A", "100",
                        1, null, null,null, null, null, null, null, null, null, null, null, null, 0, "0", 0));
                mRecyclerAdapter.notifyDataSetChanged();

                Log.d("TITULO: ", mTitle
                        + " Tipo: " + mContentType
                        + " Ano: " + mReleaseDate);
                //+ "PosterUrl: " + mPosterUrl
                //+ "Conteúdo: " + mSummary); Conteúdo Ok
                //+ "TotalResults" + mTotalRestults);
            }
        } else {
            Toast.makeText(getApplicationContext(), "Erro no download das Series...", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onTaskPreExecute() {
        mLoadingDataAnim.setVisibility(View.VISIBLE);
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
        public Resources.Theme getDropDownViewTheme() {
            return mDropDownHelper.getDropDownViewTheme();
        }

        @Override
        public void setDropDownViewTheme(Resources.Theme theme) {
            mDropDownHelper.setDropDownViewTheme(theme);
        }
    }


}
