package com.example.klkv1;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MovieReceiver extends BroadcastReceiver {

    private static int maxRating = -1;
    private static String bestMovie = "";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent == null) {
            return;
        }

        if ("com.example.klkv1.MOVIE_ADDED".equals(intent.getAction())) {

            String name = intent.getStringExtra("name");
            int rating = intent.getIntExtra("rating", 0);

            if (name == null) {
                name = "";
            }


            if (rating > maxRating) {
                maxRating = rating;
                bestMovie = name;
            }

            Toast.makeText(
                    context,
                    "Film sa najvećom ocenom: "
                            + bestMovie
                            + " (" + maxRating + ")",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}