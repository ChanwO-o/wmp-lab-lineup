package edu.uci.wmp.lineup;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import edu.uci.wmp.lineup.fragments.MainScreen;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setDisplaySettings();
        initializeManagers();

        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }

            MainScreen mainScreenFragment = new MainScreen();
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, mainScreenFragment).commit();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // punish user for aborting the test by saving decreased level by 1. Successful session closure is handled at SessionResults
        if (LevelManager.getInstance().testStarted && !LevelManager.getInstance().trainingmode.equals(LevelManager.TRAININGMODE_DEMO)) {
            LevelManager.getInstance().saveLevelToFile(false);
            Log.wtf("Activity onPause()", "You aborted the test!");
            finish(); // quit entire session, user must restart game
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Util.dimSystemBar(this);
    }

    public void setDisplaySettings() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
    }

    public void initializeManagers() {
        LevelManager.getInstance().reset();
        LevelManager.getInstance().setContext(this);
        LevelManager.getInstance().readSharedPreferences();
        LevelManager.getInstance().setScreenDimensions();
        StimuliManager.getInstance().setContext(this);
        Checks.getInstance().setContext(this);
        CSVWriter.getInstance().setContext(this);
    }
}
