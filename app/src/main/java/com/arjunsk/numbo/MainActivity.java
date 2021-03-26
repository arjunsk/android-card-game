package com.arjunsk.numbo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.arjunsk.numbo.Fragments.GameFragment;
import com.arjunsk.numbo.Fragments.HomeFragment;

import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity
        implements GameFragment.OnFragmentInteractionListener,
        HomeFragment.OnFragmentInteractionListener {


    private static final String PREFS_NAME = "APP_PREFS";
    SharedPreferences settings;

    public void calligraphy() {
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/fabiolo/FabioloSmallCap-regular.otf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        calligraphy();
        setContentView(R.layout.activity_main);

        //First Run Initialization
        settings = this.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        boolean firstRun = settings.getBoolean("first_time", true);
        if (firstRun) {
            settings.edit().putInt("highest_score", 0).apply();
            settings.edit().putInt("game_plays", 0).apply();

            settings.edit().putBoolean("state_vibration", true).apply();
            settings.edit().putBoolean("state_sound", true).apply();

            settings.edit().putBoolean("first_time", false).apply();
        }


        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, new HomeFragment()).commit();

    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }


    @Override
    public void onBackPressed() {

        @SuppressLint("RestrictedApi") List fragmentList = getSupportFragmentManager().getFragments();
        boolean handled = false;
        for (Object f : fragmentList) {
            if (f instanceof GameFragment) {
                handled = ((GameFragment) f).onBackPressed();
                if (handled) {
                    break;
                }
            }
        }

        if (!handled) {
            super.onBackPressed();
        }
    }
}
