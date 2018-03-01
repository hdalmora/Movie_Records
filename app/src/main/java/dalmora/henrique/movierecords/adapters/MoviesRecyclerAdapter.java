package dalmora.henrique.movierecords.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import dalmora.henrique.movierecords.Interfaces.OnItemClickListener;
import dalmora.henrique.movierecords.Interfaces.OnItemLongClickListener;
import dalmora.henrique.movierecords.Interfaces.MarkWatchedListener;
import dalmora.henrique.movierecords.MainActivity;
import dalmora.henrique.movierecords.MovieDataStructure;
import dalmora.henrique.movierecords.R;
import dalmora.henrique.movierecords.Util.Util;
import dalmora.henrique.movierecords.data.TableControllerMovie;

/**
 * Created by Henrique Dal' Mora R. da Silva on 10/10/2016.
 */

public class MoviesRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String LOG_TAG = MoviesRecyclerAdapter.class.getSimpleName();

    LayoutInflater mInflater;

    public static final int LIST_VIEW = 0;
    public static final int GRID_VIEW = 1;
    public static final int NO_ITEMS = 2;

    private Context mContext;
    private List<MovieDataStructure> mListStructure;

    private OnItemClickListener mItemClickListener;
    private OnItemLongClickListener mItemLongClickListener;
    private MarkWatchedListener mWatchListener;

    //Construtor
    public MoviesRecyclerAdapter(Context context, List<MovieDataStructure> mListStructure, OnItemClickListener clickListener, OnItemLongClickListener itemLongClickListener,
                                 MarkWatchedListener watchListener) {
        mContext = context;
        this.mListStructure = mListStructure;
        this.mItemClickListener = clickListener;
        this.mItemLongClickListener = itemLongClickListener;
        mWatchListener = watchListener;
    }


    @Override
    public int getItemViewType(int position) {
        if (MainActivity.getListLayoutState() == GRID_VIEW) {
            return GRID_VIEW;
        } else if (MainActivity.getListLayoutState() == LIST_VIEW) {
            return LIST_VIEW;
        } else if (mListStructure.size() == 0) {
            return NO_ITEMS;
        }
        return LIST_VIEW;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater mInflater = LayoutInflater.from(parent.getContext());

        //Log.d("AppInfo", "OnCreateViewHolder: ViewType: " + viewType);

        if (viewType == LIST_VIEW) {
            View view = mInflater.inflate(R.layout.row_list_movies, parent, false);
            return new MovieListHolder(view, mItemClickListener, mItemLongClickListener, mWatchListener);
        } else if (viewType == GRID_VIEW) {
            View view = mInflater.inflate(R.layout.row_grid_movies, parent, false);
            return new GridViewHolder(view, mItemClickListener, mItemLongClickListener, mWatchListener);
        } else {
            View view = mInflater.inflate(R.layout.no_movies_layout, parent, false);
            return new NoMoviesViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof MovieListHolder) {
            MovieListHolder movieHolder = (MovieListHolder) holder;
            movieHolder.setMovieTitle(mListStructure.get(position).getmTitulo());
            movieHolder.setMovieYear(mListStructure.get(position).getmAno());
            movieHolder.setPoster(mListStructure.get(position).getmPosterUrl());
            movieHolder.setType(mListStructure.get(position).getmTipo());
            movieHolder.setDate(mListStructure.get(position).getData());
            movieHolder.setBackground(mListStructure.get(position).getmIsWatched());
            movieHolder.setDateWatched(mListStructure.get(position).getData_watched(), mListStructure.get(position).getmIsWatched());


        } else if (holder instanceof GridViewHolder) {
            GridViewHolder movieHolder = (GridViewHolder) holder;
            movieHolder.setMovieTitle(mListStructure.get(position).getmTitulo());
            movieHolder.setPoster(mListStructure.get(position).getmPosterUrl());
            movieHolder.setBackground(mListStructure.get(position).getmIsWatched());
        }
        //Log.d("AppInfo", "onBindViewHolder: " + position);
    }


    @Override
    public int getItemCount() {
        return (mListStructure == null) ? 0 : mListStructure.size();
    }

    public void markWatched(int position) {
        if (position < mListStructure.size()) {
            String titulo = mListStructure.get(position).getmTitulo();
            boolean markWatchedSuccessful = new TableControllerMovie(mContext).markWatched(titulo, true);

            if (markWatchedSuccessful) {
                Toast.makeText(mContext, "Marcado como assistido !", Toast.LENGTH_SHORT).show();
                notifyItemChanged(position);

                long currentTime = System.currentTimeMillis();
                mListStructure.get(position).setData_watched(currentTime);
                boolean updateDateWatchedSuccessful = new TableControllerMovie(mContext).updateDateWatched(mListStructure.get(position).getmTitulo(), currentTime);
                if (updateDateWatchedSuccessful){
                    Log.d(LOG_TAG, "updateDateWatched: SUCESSO !");
                } else {
                    Log.d(LOG_TAG, "updateDateWatched: ERRO !");
                }
            } else {
                Toast.makeText(mContext, "Erro ao marcar como assistido !", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public static class MovieListHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        OnItemClickListener mItemClickListener;
        OnItemLongClickListener mItemLongClickListener;

        TextView mTextTitle;
        TextView mTextAno;
        TextView mTextTipo;
        TextView mTextData;
        TextView mTextDataWatched;
        ImageView mPoster;
        ImageView mCheckMarkWatched;
        Context mContext;
        View mItemView;

        CardView card_view;

        MarkWatchedListener mWatchListener;

        public MovieListHolder(View itemView, OnItemClickListener itemClickListener, OnItemLongClickListener itemLongClickListener,
                               MarkWatchedListener markWatchedListener) {
            super(itemView);

            mWatchListener = markWatchedListener;
            this.mItemClickListener = itemClickListener;
            this.mItemLongClickListener = itemLongClickListener;
            mItemView = itemView;
            mContext = itemView.getContext();
            itemView.setOnLongClickListener(this);
            itemView.setOnClickListener(this);
            mTextTitle = (TextView) itemView.findViewById(R.id.list_movie_title_textview);
            mPoster = (ImageView) itemView.findViewById(R.id.list_movie_poster);
            mTextAno = (TextView) itemView.findViewById(R.id.list_movie_year_textview);
            mTextTipo = (TextView) itemView.findViewById(R.id.list_movie_tipo_textview);
            mTextData = (TextView) itemView.findViewById(R.id.list_movie_data_textview);
            mTextDataWatched = (TextView) itemView.findViewById(R.id.list_movie_data_watched_textview);
            mCheckMarkWatched = (ImageView) itemView.findViewById(R.id.list_movie_checkmark_watched);
            card_view = (CardView) itemView.findViewById(R.id.card_view);
        }

        @Override
        public void onClick(View view) {
            final int adapterPosition = MoviesRecyclerAdapter.MovieListHolder.this.getAdapterPosition();
            mItemClickListener.onItemClick(adapterPosition, mPoster, mTextTitle);

        }

        @Override
        public boolean onLongClick(View view) {
            final int adapterPosition = MoviesRecyclerAdapter.MovieListHolder.this.getAdapterPosition();
            mItemLongClickListener.onItemLongClick(adapterPosition);
            mWatchListener.onMarkWatched(adapterPosition);
            return true;
        }

        public void setBackground(String watched) {
            if (watched.equals("1")) {
                card_view.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.bg_movie_watched));
            } else {
                card_view.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.bg_movie_not_watched));
            }
        }

        public void setMovieTitle(String title) {
            if (!title.isEmpty()) {
                mTextTitle.setText(title);
            }
        }

        public void setMovieYear(String year) {
            if (!year.isEmpty()) {
                mTextAno.setText("(" + year + ")");
            }
        }

        public void setType(String type) {
            if (!type.isEmpty()) {
                mTextTipo.setText(type);
            }
        }

        public void setDate(long date) {
            if (date != 0) {
                mTextData.setText(DateFormat.getDateInstance().format(new Date(date)));
            } else {
                mTextData.setText("");
            }
        }

        public void setDateWatched(long dateWatched, String watched) {
            if (watched.equals("1")) {
                if (dateWatched != 0) {
                    Date date = new Date(dateWatched);
                    SimpleDateFormat formatter = new SimpleDateFormat("dd'/'MM'/'y");
                    String today = formatter.format(date);
                    mTextDataWatched.setText(today);
                    mCheckMarkWatched.setVisibility(View.VISIBLE);
                } else {
                    mTextDataWatched.setText("");
                    mCheckMarkWatched.setVisibility(View.INVISIBLE);
                }
            } else {
                mTextDataWatched.setText("");
                mCheckMarkWatched.setVisibility(View.INVISIBLE);
            }
        }

        public void setPoster(String posterUrl) {
            if (!posterUrl.isEmpty()) {
                if (!posterUrl.equals("N/A")) {
                    Picasso.with(mContext).load(posterUrl).error(R.drawable.ic_error_outline).placeholder(R.drawable.image_loading_progress_animation).fit().centerCrop().into(mPoster);
                } else {
                    Picasso.with(mContext).load(R.drawable.no_image_available).fit().centerCrop().into(mPoster);
                }
            }
        }
    }

    //View.OnLongClickListener
    public static class GridViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        OnItemClickListener mItemClickListener;
        OnItemLongClickListener mItemLongClickListener;
        TextView mTextTitle;
        CardView card_view;
        ImageView mPoster;
        Context mContext;
        View mItemView;

        MarkWatchedListener mWatchListener;

        public GridViewHolder(View itemView, OnItemClickListener clickListener, OnItemLongClickListener itemLongClickListener,
                              MarkWatchedListener markWatchedListener) {
            super(itemView);

            mWatchListener = markWatchedListener;
            this.mItemLongClickListener = itemLongClickListener;
            this.mItemClickListener = clickListener;
            mItemView = itemView;
            mContext = itemView.getContext();
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            mTextTitle = (TextView) itemView.findViewById(R.id.gridview_titulo_textview);
            mPoster = (ImageView) itemView.findViewById(R.id.grid_movie_poster);
            card_view = (CardView) itemView.findViewById(R.id.card_view);
        }

        @Override
        public void onClick(View view) {
            final int adapterPosition = MoviesRecyclerAdapter.GridViewHolder.this.getAdapterPosition();
            mItemClickListener.onItemClick(adapterPosition, mPoster, mTextTitle);
        }

        @Override
        public boolean onLongClick(View view) {
            final int adapterPosition = MoviesRecyclerAdapter.GridViewHolder.this.getAdapterPosition();
            mItemLongClickListener.onItemLongClick(adapterPosition);
            mWatchListener.onMarkWatched(adapterPosition);
            return true;
        }

        public void setMovieTitle(String title) {
            mTextTitle.setText(title);
        }

        public void setBackground(String watched) {
            if (watched.equals("1")) {
                card_view.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.bg_movie_watched));
            } else {
                card_view.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.bg_movie_not_watched));
            }
        }

        public void setPoster(String posterUrl) {
            if (!posterUrl.equals("N/A")) {
                Picasso.with(mContext).load(posterUrl).error(R.drawable.ic_error_outline).placeholder(R.drawable.image_loading_progress_animation).fit().centerCrop().into(mPoster);
            } else {
                Picasso.with(mContext).load(R.drawable.no_image_available).fit().centerCrop().into(mPoster);
            }
        }


    }


    public static class NoMoviesViewHolder extends RecyclerView.ViewHolder {

        public NoMoviesViewHolder(View itemView) {
            super(itemView);
        }
    }
}