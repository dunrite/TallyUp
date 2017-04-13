package com.dunrite.tallyup.utility;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.dunrite.tallyup.activities.PollActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static android.content.ContentValues.TAG;

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
        SharedPreferences sharedPref = a.getSharedPreferences("first", Context.MODE_PRIVATE);
        return sharedPref.getBoolean("first", true);
    }

    public static boolean isComingFromIntroSignIn(Activity a) {
        SharedPreferences sharedPref = a.getSharedPreferences("introSignIn", Context.MODE_PRIVATE);
        return sharedPref.getBoolean("introSignIn", false);
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

    public static void cameFromIntroSignIn(Activity a, boolean b) {
        SharedPreferences sharedPref = a.getSharedPreferences("introSignIn", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("introSignIn", b);
        editor.apply();
    }

    /**
     * App has been launched, set first to false
     */
    public static void appHasLaunched(Activity a) {
        SharedPreferences sharedPref = a.getSharedPreferences("first", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("first", false);
        editor.apply();
    }

    /*****************************************************************************
     * Misc
     *****************************************************************************/


    public static String generateSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 5) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        return salt.toString();
    }

    public static String replaceLast(String string, String toReplace, String replacement) {
        int pos = string.lastIndexOf(toReplace);
        if (pos > -1) {
            return string.substring(0, pos)
                    + replacement
                    + string.substring(pos + toReplace.length(), string.length());
        } else {
            return string;
        }
    }


    public static JSONObject buildJSONBody(String pollID) {
        JSONObject jsonObject = null;

        String body = "{\"dynamicLinkInfo\":" +
                            "{\"dynamicLinkDomain\":\"wb975.app.goo.gl\"," +
                            "\"link\": \"https://dunriteapps.com/tallyup/poll/"+ pollID+"\"," +
                            "\"androidInfo\":{" +
                                "\"androidPackageName\": \"com.dunrite.tallyup\"}," +
                            "\"socialMetaTagInfo\":{" +
                                "\"socialTitle\": \"Take My TallyUp Poll\"," +
                                "\"socialDescription\": \"This is a description\"," +
                                "\"socialImageLink\": \"http://i.imgur.com/j7mMh71.png\"}}," +
                        "\"suffix\":{" +
                            "\"option\":\"SHORT\"}" +
                      "}";
        //Log.d("BODY", body);
        try {
            jsonObject = new JSONObject(body);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static void buildDeepLink(final PollActivity a, String pollID) {
        String url = "https://firebasedynamiclinks.googleapis.com/v1/shortLinks?key=" + Constants.FIREBASE_API_KEY;

        JsonObjectRequest postRequest = new JsonObjectRequest(
                Request.Method.POST, url, buildJSONBody(pollID),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        try {
                            a.launchShareIntent(response.get("shortLink").toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }
        }) {
            /**
             * Passing some request headers
             */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };
        Volley.newRequestQueue(a).add(postRequest);
    }
}
