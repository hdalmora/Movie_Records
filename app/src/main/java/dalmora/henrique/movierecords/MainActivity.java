package dalmora.henrique.movierecords;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Pair;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import dalmora.henrique.movierecords.Util.Util;
import dalmora.henrique.movierecords.adapters.MoviesRecyclerAdapter;
import dalmora.henrique.movierecords.data.TableControllerMovie;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, OnTaskCompleted, OnTaskPreExecute {

    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    public static final int RECYCLER_GRID_NUM_COLUMS = 3; //3 colunas no layout manager
    public static final int RECYCLER_LIST_NUM_COLUMS = 1; //1 coluna no layout manager


    private CollapsingToolbarLayout mRevealView;
    private InputMethodManager mInputManager; //Controlar o input na caixa de pesquisa
    private boolean isSearchBoxVisible; //Flag para controlar a visualização da caixa de pesquisa

    private List<MovieDataStructure> mListStructure = new ArrayList<>(); //Lista contendo dados dos filmes

    public static boolean isGridViewActive = false; //Flag para controlar o estado da GridView

    private FloatingActionButton fab_search; //Botao de pesquisa
    private FloatingActionButton fab_move_to_top; //Botao para mover para o início da lista

    private EditText mSearchEntry; //Entrada do testo para pesquisa do filme
    private TextView mTextEmptyList; //Texto para mostrar quando não há filmes na lista
    private TextView mTotalResultsTextView;

    TextView mMoviesCountTextView; //TextView com o Numero de filmes na database
    TextView mSeriesCountTextView; //TextView com o Numero de series na database
    TextView mGamesCountTexView; //TextView com o Numero de jogos na database

    private Menu menu;

    private ImageView navDrawerBgImageView;
    private RelativeLayout mLoadingDataAnim;
    private ImageButton mEditNameBtn;
    private TextView mUserNameTextView;

    private String mTotalRestults; //Total de resultados retornado pela asyncTask DownloadMovieTask
    private String mTituloFilme; //Titulo do filme inserido na barra de pesquisa

    int pageCounter = 1; //Controlar a paginação na url que irá ser passada para a asyncTask
    boolean allItemsShown = false; //Flag para interromper o download de dados quando atingir a ultima pagina


    public static int recyclerState = 1; //Estado do Adaptador do recyclerView (Lista ou Grid)

    private RecyclerView mRecyclerView;
    private StaggeredGridLayoutManager mStaggeredLayoutManager;
    private MoviesRecyclerAdapter mRecyclerAdapter;

    private String mThemeOptions = null;
    private String mBgImageOptions = null;

    private OnItemClickListener mItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(int position, View posterView, View titleView) {
            Intent intent = new Intent(getApplicationContext(), MovieDetailActivity.class);

            //Passar as informações do filme clicado por intent para a activity de detalhes
            intent.putExtra("MOVIE_TITLE", mListStructure.get(position).getmTitulo());
            intent.putExtra("MOVIE_ANO", mListStructure.get(position).getmAno());
            intent.putExtra("MOVIE_POSTER", mListStructure.get(position).getmPosterUrl());
            intent.putExtra("MOVIE_IMDBID", mListStructure.get(position).getmImdbID());
            intent.putExtra("MOVIE_TYPE", mListStructure.get(position).getmTipo());
            intent.putExtra("MOVIE_POSITION", position);

            //Indicar se é top serie ou filme para controlar quais dados estão faltando na activity de detalhes
            if (mListStructure.get(position).getmTipo().equals("series")) {
                intent.putExtra("TOP_SERIES", true);
            } else {
                intent.putExtra("TOP_MOVIES", false);
            }
            intent.putExtra("TOP100", false);

            startActivity(intent);

        }
    };

    private OnItemLongClickListener mItemLongClickListener = new OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(int position) {
            if (isGridViewActive) {
                //Mostrar nome do filme quando esta no modo grid
                Toast.makeText(getApplicationContext(), mListStructure.get(position).getmTitulo(), Toast.LENGTH_SHORT).show();
            }
            Log.d("AppInfo", "OnLongClick: CALLED");
            return true;
        }
    };

    public static int getListLayoutState() {
        //Método estático para indicar o estado da lista (Grid ou Lista vertical)
        if (isGridViewActive) {
            //Grid (3 colunas)
            return 1;
        } else {
            //Lista vertical (1 coluna)
            return 0;
        }
    }

    boolean isLoading = false; //Flag para indicar quando dados estão sendo carregados pela asynctask


    //Handler para receber intents (ações) com a tag "AddedToFavorite"
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            if (message.equalsIgnoreCase("adicionado")) {
                //Atualizar as textviews com o numero de filmes, series e jogos na navDrawer
                //Todas vez que uma media é adicionada ou deletada
                initializeCountDrawer();
            }
            Log.d("receiver", "Got message: " + message);
        }
    };

    //Handler para controlar quando a imagem de fundo da navDrawer é alterada na activity settings
    private BroadcastReceiver mMessageBgImageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");

            if (message.equals("bgImageChanged")) {
                Log.d(LOG_TAG, "Message: BG Image Changed");
                //Processar a tag da imagem
                mBgImageOptions = Util.getPreferredBgImage(MainActivity.this);

                //Armazernar a tag da imagem em shared preferences
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("BgImageOptionsName", mBgImageOptions);
                editor.apply();

                //setar a imagem de fundo com base na tag selecionada na activity settings
                Util.setBgImagePreference(MainActivity.this, mBgImageOptions, navDrawerBgImageView);
            }
        }
    };

    private BroadcastReceiver mMessageThemeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");

            if (message.equals("themeChanged")) {
                Log.d(LOG_TAG, "Message: Theme Changed");
                MainActivity.this.recreate();
            }
        }
    };

    private MarkWatchedListener mMarkWatchedListener = new MarkWatchedListener() {
        @Override
        public void onMarkWatched(int position) {
            //showDialogWatched(position);
            //Quando o filmes é pesquisado na mainactivity não quero possibilitar que seja marcado
            //apenas quando esta nas listas de favoritos
        }
    };

    @Override
    protected void onDestroy() {
        // Retirar os broadCast Receiver, já que a activity será fechada
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageThemeReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageBgImageReceiver);
        super.onDestroy();

        Log.d(LOG_TAG, "onDestroy: Called");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Salvar aqui todos os estádos de variáveis ou arrays que devem ser mantidos quando a activity for destruida
        //e posterioemente recriada (e.g : quando rotaciona a tela do celular)

        outState.putString("TotalResults", mTotalRestults); //quantidade de filmes retornados pela APi na pesquisa
        outState.putString("TituloFilme", mTituloFilme); //Titulo do filme

        //salvar o estado da lista utilizando parcel (Possibilita escrever (salvar) e ler (recuperar) referências
        //de objetos em um Parcel)
        outState.putParcelableArrayList("RecyclerState", (ArrayList<? extends Parcelable>) mListStructure);

        //Salvar o estado do layout manager (GridView ativo ou não)
        outState.putBoolean("isGridViewActive", isGridViewActive);
        Log.d("onSaveInstanceState", "CALLED");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //Restaurar os dados salvos

        if (savedInstanceState.getParcelableArrayList("RecyclerState") != null) {
            mTotalRestults = savedInstanceState.getString("TotalResults");
            mTituloFilme = savedInstanceState.getString("TituloFilme");
            mTotalResultsTextView.setText(mTotalRestults + " resultados para " + "\"" + mTituloFilme + "\"");
            if (mTotalRestults != null || mTituloFilme != null) {
                mTotalResultsTextView.setVisibility(View.VISIBLE);
            }

            //Restaurar o estado da lista e jogar no adptador do recycler view
            mListStructure = savedInstanceState.getParcelableArrayList("RecyclerState");
            mRecyclerAdapter = new MoviesRecyclerAdapter(this, mListStructure, mItemClickListener, mItemLongClickListener, mMarkWatchedListener);
            mRecyclerView.setAdapter(mRecyclerAdapter);
            mRecyclerAdapter.notifyDataSetChanged();

            //Controlar a visibilidade da textView da main activity
            if (mListStructure.size() == 0) {
                //Mostrar quando a lista esta vazia
                mTextEmptyList.setVisibility(View.VISIBLE);
            } else {
                //Esconder quando a lista possui filmes
                mTextEmptyList.setVisibility(View.GONE);
            }
        }
        Log.d("onRestoreInstanceState", "CALLED: ");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Setar o tema da activity Com base no tema escolhido nas configurações
        mThemeOptions = Util.getPreferredTheme(this);
        Util.setThemePreference(this, mThemeOptions);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Definição dos BroadCastReceiver para quando um tema ou Bg é alterado nas configurações
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageThemeReceiver,
                new IntentFilter("ThemeChanged"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageBgImageReceiver,
                new IntentFilter("BgImageChange"));

        mTotalResultsTextView = (TextView) findViewById(R.id.totalResults_textview);
        mTotalResultsTextView.setVisibility(View.GONE);
        mRevealView = (CollapsingToolbarLayout) findViewById(R.id.rl_search_background);
        mInputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mRevealView.setVisibility(View.INVISIBLE);
        isSearchBoxVisible = false;

        mSearchEntry = (EditText) findViewById(R.id.search_movie_editText);
        mTextEmptyList = (TextView) findViewById(R.id.main_search_movies_empty_textview);

        if (mListStructure.size() == 0) {
            mTextEmptyList.setVisibility(View.VISIBLE);
        }


        Log.d("AppInfo", "Oncreate: CALLED");
        if (savedInstanceState != null) {
            isGridViewActive = savedInstanceState.getBoolean("isGridViewActive");
            Log.d("onRestoreInstanceState", "isGridViewActive: " + isGridViewActive);

            if (isGridViewActive) {
                recyclerState = RECYCLER_GRID_NUM_COLUMS;
            } else {
                recyclerState = RECYCLER_LIST_NUM_COLUMS;
            }
        }


        //*********** RecyclerView ********************************************************//
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_movies);
        mRecyclerAdapter = new MoviesRecyclerAdapter(this, mListStructure, mItemClickListener, mItemLongClickListener, mMarkWatchedListener);
        mStaggeredLayoutManager = new StaggeredGridLayoutManager(recyclerState, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mStaggeredLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int pastVisibleItems = 0;

                int visibleItemCount = mStaggeredLayoutManager.getChildCount();
                int total = mStaggeredLayoutManager.getItemCount();
                int[] firstVisibleItemCount = null;
                firstVisibleItemCount = mStaggeredLayoutManager.findFirstVisibleItemPositions(firstVisibleItemCount);
                if (firstVisibleItemCount != null && firstVisibleItemCount.length > 0) {
                    pastVisibleItems = firstVisibleItemCount[0];
                }

                //Controlar a visibilidade do botao para ir para o topo do recyclerview
                if (pastVisibleItems <= 3) {
                    fab_move_to_top.hide();
                } else if (pastVisibleItems > 3) {
                    fab_move_to_top.show();
                }



                /*Log.d("OnScrolled: ", "total: " + total);
                Log.d("OnScrolled: ", "visibleItemCount: " + visibleItemCount);
                Log.d("OnScrolled: ", "pastVisibleItems: " + pastVisibleItems);
                Log.d("OnScrolled: ", "lastItem: " + lastItem);*/

                //(Condição para não carregar items indefinitivamente quando na lista há menos que 10 filmes
                if ((mListStructure.size() >= 10)) {
                    //Funcionalidades e condições para download das outras páginas ao longo do scroll
                    if (!isLoading && !allItemsShown) {
                        if ((visibleItemCount + pastVisibleItems) >= total) {
                            isLoading = true;
                            /*Log.d("tag", "LOAD NEXT MOVIES");*/
                            if (pageCounter < getNumberOfPages()) {
                                pageCounter++;
                                if (pageCounter == getNumberOfPages()) {
                                    allItemsShown = true;
                                }
                            }
                            /*Log.d("SCROLL", "Page: " + pageCounter);*/
                            //Download da próxima página
                            updateMovies(mTituloFilme, String.valueOf(pageCounter));
                            //TODO: Quando sem internet: Após a mensagem de conexão, ao voltar a ativa-la, o metodo updateMovies() não é mais chamado
                        }
                    }
                }
            }
        });
        //*********************************************************************************//


        //********** Floating Action Buttons ***************************************//
        fab_search = (FloatingActionButton) findViewById(R.id.fab_search);
        fab_search.setOnClickListener(this);

        fab_move_to_top = (FloatingActionButton) findViewById(R.id.fab_go_to_top);
        fab_move_to_top.setOnClickListener(this);
        fab_move_to_top.hide();
        //*******************************************************************************//


        //*********************** Drawer Layout ***************************************//
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        mMoviesCountTextView = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().
                findItem(R.id.nav_listas_filmes));
        mSeriesCountTextView = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().
                findItem(R.id.nav_listas_series));
        mGamesCountTexView = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().
                findItem(R.id.nav_listas_jogos));

        //Inicializar os valores atuais da quantidade de filmes, seriese games na database nos textViews do Drawer Layout
        initializeCountDrawer();

        // Registrar Broadcast Receiver para receber mensagens.
        // Registrando um observador (mMessageReceiver) para receber intents
        // com ações nomeadas "AddedToFavorite".
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("AddedToFavorite"));

        //Inicializando as views do Drawer Layout
        View headerView = LayoutInflater.from(this).inflate(R.layout.nav_header_main, navigationView, false);
        navigationView.addHeaderView(headerView);
        navDrawerBgImageView = (ImageView) headerView.findViewById(R.id.nav_drawer_bg_imageview);
        mUserNameTextView = (TextView) headerView.findViewById(R.id.nav_drawer_user_name);

        //Recuperar o nome do usuario por shared preferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String userName = preferences.getString("UserName", "");
        if (!userName.equalsIgnoreCase("")) {
            mUserNameTextView.setText(userName);
        }
        mEditNameBtn = (ImageButton) headerView.findViewById(R.id.nav_drawer_edit_user_name);
        mEditNameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText changeNameEditText = new EditText(getApplicationContext());
                changeNameEditText.setTextColor(Color.BLACK);
                int maxLength = 17;
                changeNameEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.str_alterar_nome)
                        .setMessage(R.string.str_dig_nome_abaixo)
                        .setView(changeNameEditText)
                        .setPositiveButton(R.string.str_alterar, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if (changeNameEditText.getText().toString().length() >= 3) {
                                    String nome = changeNameEditText.getText().toString();
                                    Toast.makeText(getApplicationContext(), R.string.str_nome_alterado, Toast.LENGTH_SHORT).show();
                                    mUserNameTextView.setText(nome);

                                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putString("UserName", nome);
                                    editor.apply();
                                } else {
                                    Toast.makeText(getApplicationContext(), R.string.str_minimo_tres_caracteres, Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        })
                        .show();
            }
        });


        String bgImageName = preferences.getString("BgImageOptionsName", "");
        if (!bgImageName.isEmpty()) {
            Util.setBgImagePreference(MainActivity.this, bgImageName, navDrawerBgImageView);
        } else {
            Util.setBgImagePreference(MainActivity.this, "Flat Beautiful Mountain", navDrawerBgImageView);
        }
        navigationView.setNavigationItemSelectedListener(this);
        //***************************************************************************//


        mLoadingDataAnim = (RelativeLayout) findViewById(R.id.loadingPanel);
        mLoadingDataAnim.setVisibility(View.GONE);
    }

    private void initializeCountDrawer() {
        //Favorite Movies
        mMoviesCountTextView.setGravity(Gravity.CENTER_VERTICAL);
        mMoviesCountTextView.setTypeface(null, Typeface.BOLD);
        mMoviesCountTextView.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
        int countFavoriteMovies = new TableControllerMovie(this).count("movie");
        mMoviesCountTextView.setText(String.valueOf(countFavoriteMovies));

        //Favorite Series
        mSeriesCountTextView.setGravity(Gravity.CENTER_VERTICAL);
        mSeriesCountTextView.setTypeface(null, Typeface.BOLD);
        mSeriesCountTextView.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
        int countFavoriteSeries = new TableControllerMovie(this).count("series");
        mSeriesCountTextView.setText(String.valueOf(countFavoriteSeries));

        //Favorite Games
        mGamesCountTexView.setGravity(Gravity.CENTER_VERTICAL);
        mGamesCountTexView.setTypeface(null, Typeface.BOLD);
        mGamesCountTexView.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
        int countFavoriteGames = new TableControllerMovie(this).count("game");
        mGamesCountTexView.setText(String.valueOf(countFavoriteGames));
    }

    //Calcular o número total de paginas com base na quantidade de filmes retornada pela API
    private int getNumberOfPages() {
        int totalResults = Integer.parseInt(mTotalRestults);
        //10 filmes por página
        //Arredondar para o valor máximo inteiro mais proximo
        return (int) Math.ceil(totalResults / 10.0);
    }

    private void updateMovies(String titulo, String pagina) {
        if (Util.isNetworkAvailable(getApplicationContext())) {
            DownloadMovieTask downloadMovieTask = new DownloadMovieTask(this, this, this);
            downloadMovieTask.execute(titulo, pagina);
        } else {
            final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
            Snackbar snackbar = Snackbar.make(coordinatorLayout, R.string.str_sem_conexao_internet,
                    Snackbar.LENGTH_LONG);
            // Mudar cor da Mensagem
            snackbar.setActionTextColor(Color.RED);

            // Mudar com do botão de ação
            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(Color.rgb(255, 255, 153));
            snackbar.show();
        }

    }

    private void revealSearchEditText(CollapsingToolbarLayout view) {
        //posição da CollapsingToolBarLayout
        int cx = view.getRight() - 85;
        int cy = view.getBottom() - 40;

        int finalRadius = Math.max(view.getWidth(), view.getHeight());
        Animator anim = null;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);
        }
        view.setVisibility(View.VISIBLE);
        isSearchBoxVisible = true;
        anim.start();
    }

    private void hideSearchEditText(final CollapsingToolbarLayout view) {
        //posição da CollapsingToolBarLayout
        int cx = view.getRight() - 85;
        int cy = view.getBottom() - 40;
        int initialRadius = view.getWidth();
        Animator anim = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, initialRadius, 0);
        }
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(View.INVISIBLE);
            }
        });
        isSearchBoxVisible = false;
        anim.start();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        /*
        * Se estiver no modo grid quando rotacionar a tela, seta o icone da Toolbar para o correto,
        * com base no estado da variável booleana 'isGridViewActive' recuperada pelo saveInstanceState
         */
        if (isGridViewActive) {
            MenuItem item = menu.findItem(R.id.action_grid_view);
            item.setIcon(R.drawable.ic_storage);
            item.setTitle(R.string.str_mostrar_como_lista);
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
        if (id == R.id.action_grid_view) {
            listviewToGridView();
            return true;
        } else if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

    private void listviewToGridView() {
        MenuItem item = menu.findItem(R.id.action_grid_view);
        if (!isGridViewActive && recyclerState == RECYCLER_LIST_NUM_COLUMS) {
            recyclerState = RECYCLER_GRID_NUM_COLUMS;
            mStaggeredLayoutManager.setSpanCount(recyclerState);
            item.setIcon(R.drawable.ic_storage);
            item.setTitle(R.string.str_mostrar_como_lista);
            isGridViewActive = true;
        } else if (isGridViewActive && recyclerState == RECYCLER_GRID_NUM_COLUMS) {
            recyclerState = RECYCLER_LIST_NUM_COLUMS;
            mStaggeredLayoutManager.setSpanCount(recyclerState);
            item.setIcon(R.drawable.ic_apps);
            item.setTitle(R.string.str_mostrar_como_grid);
            isGridViewActive = false;
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


        if (id == R.id.nav_top_100_movies) {
            Intent intent = new Intent(MainActivity.this, ListaTopMoviesActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_top_100_series) {
            Intent intent = new Intent(MainActivity.this, ListaTopSeriesActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_listas_filmes) {
            Intent intent = new Intent(this, MoviesListaFavoritosActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_listas_series) {
            Intent intent = new Intent(this, SeriesListaFavoritosActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_listas_jogos) {
            Intent intent = new Intent(this, GamesListaFavoritosActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_backup_restore) {
            Intent intent = new Intent(this, DriveSyncActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_contatar) {
            Intent intent = new Intent(this, ContactDeveloperActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_configuracoes) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_sobre) {
            //TODO: Abrir dialog com as informações (sobre)
            DialogAbout dialog = new DialogAbout();
            dialog.show(getFragmentManager(), "About Dialog");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.fab_search:
                if (!isSearchBoxVisible) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        revealSearchEditText(mRevealView);
                    } else {
                        mRevealView.setVisibility(View.VISIBLE);
                        isSearchBoxVisible = true;
                    }
                    mSearchEntry.requestFocus();
                    mInputManager.showSoftInput(mSearchEntry, InputMethodManager.SHOW_IMPLICIT);
                } else {
                    mSearchEntry.clearFocus();
                    mInputManager.hideSoftInputFromWindow(mSearchEntry.getWindowToken(), 0);
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        hideSearchEditText(mRevealView);
                    } else {
                        mRevealView.setVisibility(View.INVISIBLE);
                        isSearchBoxVisible = false;
                    }
                    if (mSearchEntry.getText() != null) {
                        pageCounter = 1; //Resetar o contador para as páginas
                        mTituloFilme = mSearchEntry.getText().toString();
                        Log.d("AppInfo", "Titulo Search: " + mTituloFilme);

                        mListStructure.clear(); //Esvaziar a lista para nova pesquisa
                        updateMovies(mTituloFilme, "1");
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.str_entre_nome_de_um_filme, Toast.LENGTH_SHORT).show();
                    }
                    allItemsShown = false;
                }
                break;
            case R.id.fab_go_to_top:
                StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) mRecyclerView.getLayoutManager();
                layoutManager.scrollToPositionWithOffset(0, 0);
                break;
        }
    }

    @Override
    public void onTaskCompleted(Vector<ContentValues> movieVector, String totalResults, String type) {
        mLoadingDataAnim.setVisibility(View.GONE);

        isLoading = false;

        String mTitulo;
        String mAnoLancamento;
        String mTipo;
        String mImdbID;
        String mPosterUrl;

        if (totalResults != null) {
            mTotalRestults = totalResults;
            mTotalResultsTextView.setVisibility(View.VISIBLE);
            String resultados_para = getString(R.string.str_resultados_para);
            mTotalResultsTextView.setText(mTotalRestults + " " + resultados_para + " " + "\"" + mTituloFilme + "\"");
        }

        if (movieVector != null) {
            //mListStructure.clear();

            mTextEmptyList.setVisibility(View.GONE);
            int movieCounter = 0;

            for (ContentValues movieValues : movieVector) {
                mTitulo = movieValues.get("Title").toString();
                mAnoLancamento = movieValues.get("Year").toString();
                mTipo = movieValues.get("Type").toString();
                mImdbID = movieValues.get("imdbID").toString();
                mPosterUrl = movieValues.get("Poster").toString();
                mListStructure.add(new MovieDataStructure(null, mTitulo, mAnoLancamento, mTipo, mPosterUrl, mImdbID, null, mTotalRestults,
                        1, null, null, null, null, null, null, null, null, null, null, null, null, 0, "0", 0));
                mRecyclerAdapter.notifyDataSetChanged();

                Log.d("TITULO: ", mTitulo
                        + " Tipo: " + mTipo
                        + " Ano: " + mAnoLancamento
                        + "PosterUrl: " + mPosterUrl
                        + "ImdbID: " + mImdbID);
                //+ "TotalResults" + mTotalRestults);

                movieCounter += 1;
                Log.d("MovieVector", "MovieCounter: " + String.valueOf(movieCounter));
            }
            Log.d("TOTAL RESULTSS: ", mTotalRestults);

        } else {
            mListStructure.clear();
            mRecyclerAdapter.notifyDataSetChanged();
            mTotalResultsTextView.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(), R.string.str_nenhum_filme_encontrado, Toast.LENGTH_SHORT).show();
            mTextEmptyList.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onTaskPreExecute() {
        mLoadingDataAnim.setVisibility(View.VISIBLE);
    }
}
