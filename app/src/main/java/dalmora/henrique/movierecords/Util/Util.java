package dalmora.henrique.movierecords.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import dalmora.henrique.movierecords.R;

/**
 * Created by Henrique Dal' Mora R. da Silva on 09/10/2016.
 */

public class Util {


    //retorna true se a internet esta disponível
    static public boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    public static String getPreferredTheme(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_theme_key),
                context.getString(R.string.pref_theme_default));
    }

    public static String getPreferredBgImage(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_bg_image_key),
                context.getString(R.string.pref_bg_image_default));
    }

    public static void setThemePreference(Context context, String themeOptions){
        if (themeOptions.equals("Azul")) {
            context.setTheme(R.style.ThemeBlue);
        } else if (themeOptions.equals("Vermelho")){
            context.setTheme(R.style.ThemeRed);
        } else if (themeOptions.equals("Verde")){
            context.setTheme(R.style.ThemeGreen);
        } else if (themeOptions.equals("Roxo")){
            context.setTheme(R.style.ThemePurple);
        } else if (themeOptions.equals("Amarelo")){
            context.setTheme(R.style.ThemeYellow);
        } else if(themeOptions.equals("Laranja")){
            context.setTheme(R.style.ThemeOranje);
        } else if(themeOptions.equals("Rosa")){
            context.setTheme(R.style.ThemePink);
        } else {
            context.setTheme(R.style.AppTheme);
        }
    }

    public static void setBgImagePreference (Context context, String bgImageOptions, ImageView imageView){
        if (bgImageOptions.equals("Flat Beautiful Mountain")){
            Picasso.with(context).load(R.drawable.bg_flat_beautiful_mountain).fit().centerCrop().into(imageView);
        } else if(bgImageOptions.equals("Jungle Landscape")){
            Picasso.with(context).load(R.drawable.bg_jungle_landscape).fit().centerCrop().into(imageView);
        } else if(bgImageOptions.equals("Landscape With Mountains")){
            Picasso.with(context).load(R.drawable.bg_landscape_with_mountains).fit().centerCrop().into(imageView);
        } else if(bgImageOptions.equals("Lighthouse")){
            Picasso.with(context).load(R.drawable.bg_lighthouse).fit().centerCrop().into(imageView);
        } else if(bgImageOptions.equals("Shiny Stars")){
            Picasso.with(context).load(R.drawable.bg_shiny_stars).fit().centerCrop().into(imageView);
        } else if(bgImageOptions.equals("Winter Landscape")){
            Picasso.with(context).load(R.drawable.bg_winterlandscape).fit().centerCrop().into(imageView);
        }
    }


}
