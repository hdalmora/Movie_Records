package dalmora.henrique.movierecords;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Henrique Dal' Mora R. da Silva on 09/10/2016.
 */

public class MovieDataStructure implements Parcelable{

    private String mTitulo = "";
    private String mAno = "";
    private String mTipo = "";
    private String mPosterUrl = "";
    private String mBGImageUrl = "";
    private String mImdbID = "";
    private String mContent = "";
    private String mTotalResults = "";
    private String mTituloSerie = "";
    private String mImdbRating = "";
    private String mTomatoeRating = "";
    private String mMetaScoreRating = "";
    private String mTempoFilme = "";
    private String mGeneroFilme = "";
    private String mDiretor = "";
    private String mEscritor = "";
    private String mAtores = "";
    private String mIdioma = "";
    private String mPais = "";
    private String mPremiacoes = "";
    private String mIsWatched = "0";
    private long data;
    private long data_watched;

    private int id;

    public MovieDataStructure(){

    }

    public MovieDataStructure(String mTituloSerie, String mTitulo, String mAno, String mTipo, String mPosterUrl, String mImdbID,
                              String content, String totalResults, int id, String mBGImageUrl, String mImdbRating,
                              String mTomatoeRating, String mMetaScoreRating, String mTempoFilme, String mGeneroFilme,
                              String mDiretor, String mEscritor, String mAtores, String mIdioma, String mPais, String mPremiacoes, long data,
                              String mIsWatched, long dataWatched) {
        this.mTitulo = mTitulo;
        this.mAno = mAno;
        this.mTipo = mTipo;
        this.mPosterUrl = mPosterUrl;
        this.mBGImageUrl = mBGImageUrl;
        this.mImdbID = mImdbID;
        this.mContent = content;
        this.mTotalResults = totalResults;
        this.mTituloSerie = mTituloSerie;
        this.mImdbRating = mImdbRating;
        this.mTomatoeRating = mTomatoeRating;
        this.mMetaScoreRating = mMetaScoreRating;
        this.mTempoFilme = mTempoFilme;
        this.mGeneroFilme = mGeneroFilme;
        this.mDiretor = mDiretor;
        this.mEscritor = mEscritor;
        this.mAtores = mAtores;
        this.mIdioma = mIdioma;
        this.mPais = mPais;
        this.mPremiacoes = mPremiacoes;
        this.data = data;
        this.data_watched = dataWatched;
        this.id = id;
        this.mIsWatched = mIsWatched;
    }

    public long getData_watched() {
        return data_watched;
    }

    public void setData_watched(long data_watched) {
        this.data_watched = data_watched;
    }

    public String getmIsWatched() {
        return mIsWatched;
    }

    public void setmIsWatched(String mIsWatched) {
        this.mIsWatched = mIsWatched;
    }

    public long getData() {
        return data;
    }

    public void setData(long data) {
        this.data = data;
    }

    public String getmDiretor() {
        return mDiretor;
    }

    public void setmDiretor(String mDiretor) {
        this.mDiretor = mDiretor;
    }

    public String getmEscritor() {
        return mEscritor;
    }

    public void setmEscritor(String mEscritor) {
        this.mEscritor = mEscritor;
    }

    public String getmAtores() {
        return mAtores;
    }

    public void setmAtores(String mAtores) {
        this.mAtores = mAtores;
    }

    public String getmIdioma() {
        return mIdioma;
    }

    public void setmIdioma(String mIdioma) {
        this.mIdioma = mIdioma;
    }

    public String getmPais() {
        return mPais;
    }

    public void setmPais(String mPais) {
        this.mPais = mPais;
    }

    public String getmPremiacoes() {
        return mPremiacoes;
    }

    public void setmPremiacoes(String mPremiacoes) {
        this.mPremiacoes = mPremiacoes;
    }

    public String getmImdbRating() {
        return mImdbRating;
    }

    public void setmImdbRating(String mImdbRating) {
        this.mImdbRating = mImdbRating;
    }

    public String getmTomatoeRating() {
        return mTomatoeRating;
    }

    public void setmTomatoeRating(String mTomatoeRating) {
        this.mTomatoeRating = mTomatoeRating;
    }

    public String getmMetaScoreRating() {
        return mMetaScoreRating;
    }

    public void setmMetaScoreRating(String mMetaScoreRating) {
        this.mMetaScoreRating = mMetaScoreRating;
    }

    public String getmTempoFilme() {
        return mTempoFilme;
    }

    public void setmTempoFilme(String mTempoFilme) {
        this.mTempoFilme = mTempoFilme;
    }

    public String getmGeneroFilme() {
        return mGeneroFilme;
    }

    public void setmGeneroFilme(String mGeneroFilme) {
        this.mGeneroFilme = mGeneroFilme;
    }

    public String getmBGImageUrl() {
        return mBGImageUrl;
    }

    public void setmBGImageUrl(String mBGImageUrl) {
        this.mBGImageUrl = mBGImageUrl;
    }

    public String getmTituloSerie() {
        return mTituloSerie;
    }

    public void setmTituloSerie(String mTituloSerie) {
        this.mTituloSerie = mTituloSerie;
    }

    public String getmTotalResults() {
        return mTotalResults;
    }

    public void setmTotalResults(String mTotalResults) {
        this.mTotalResults = mTotalResults;
    }

    public String getmContent() {
        return mContent;
    }

    public void setmContent(String mContent) {
        this.mContent = mContent;
    }

    public String getmImdbID() {
        return mImdbID;
    }

    public void setmImdbID(String mImdbID) {
        this.mImdbID = mImdbID;
    }

    public String getmPosterUrl() {
        return mPosterUrl;
    }

    public void setmPosterUrl(String mPosterUrl) {
        this.mPosterUrl = mPosterUrl;
    }

    public String getmTitulo() {
        return mTitulo;
    }

    public void setmTitulo(String mTitulo) {
        this.mTitulo = mTitulo;
    }

    public String getmAno() {
        return mAno;
    }

    public void setmAno(String mAno) {
        this.mAno = mAno;
    }

    public String getmTipo() {
        return mTipo;
    }

    public void setmTipo(String mTipo) {
        this.mTipo = mTipo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    @Override
    public String toString() {
        return mTitulo +"\n";
    }




    //---------------- Estrutura do Parcel ------------------//
    protected MovieDataStructure(Parcel in) {
        mTitulo = in.readString();
        mAno = in.readString();
        mTipo = in.readString();
        mPosterUrl = in.readString();
        mImdbID = in.readString();
        id = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTitulo);
        dest.writeString(mAno);
        dest.writeString(mTipo);
        dest.writeString(mPosterUrl);
        dest.writeString(mImdbID);
        dest.writeInt(id);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<MovieDataStructure> CREATOR = new Parcelable.Creator<MovieDataStructure>() {
        @Override
        public MovieDataStructure createFromParcel(Parcel in) {
            return new MovieDataStructure(in);
        }

        @Override
        public MovieDataStructure[] newArray(int size) {
            return new MovieDataStructure[size];
        }
    };
}
