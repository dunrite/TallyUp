package com.dunrite.tallyup.utility;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.Random;

/**
 * Utility class for various common methods
 */

public class Utils {

    /**
     * Empty Constructor
     */
    private Utils() {}

    /*****************************************************************************
     * Getters
     *****************************************************************************/

    /**
     * Returns whether or not this is the first time the app has been opened
     *
     * @param a current activity
     * @return if first launch or not
     */
    public static boolean isFirstLaunch(Activity a) {
        SharedPreferences sharedPref = a.getSharedPreferences("FIRST", Context.MODE_PRIVATE);
        return sharedPref.getBoolean("first", true);
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /*****************************************************************************
     * Setters
     *****************************************************************************/

    /**
     * App has been launched, set first to false
     */
    public static void appHasLaunched(Activity a) {
        SharedPreferences sharedPref = a.getSharedPreferences("FIRST", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("first", false);
        editor.apply();
    }


    /*****************************************************************************
     * Misc
     *****************************************************************************/

    /**
     * Use Glide to apply a drawable to an ImageView
     *
     * @param iv the ImageView to apply to
     * @param d  the drawable to apply
     */
    public static void applyImageToView(Context c, ImageView iv, Drawable d) {
        Glide.with(c)
                .load("") //load doesn't support drawables?
                .placeholder(d)
                .centerCrop()
                .crossFade()
                .into(iv);
    }

    /**
     * Use Glide to apply a drawable integer to an ImageView
     *
     * @param iv the ImageView to apply to
     * @param d  the drawable integer to apply
     */
    public static void applyImageToView(Context c, ImageView iv, int d) {
        Glide.with(c)
                .load(d)
                .fitCenter()
                .into(iv);
    }

    public static String generateSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 5) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }
}
