package edu.uci.wmp.lineup.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import edu.uci.wmp.lineup.LevelManager;
import edu.uci.wmp.lineup.R;
import edu.uci.wmp.lineup.Util;

public class GetReady extends Fragment implements View.OnClickListener {

    TextView tvGetReady;
    TextView tvLevelRoundDebug;
    ImageView ivReadyNext;
    final int READY_SHOW_BUTTON_TIME = 1; // when to show next button in seconds
    final long BLANK_SCREEN = 500; // showing blank screen in milliseconds
    long readyStartTime;
    long startBlankScreenTime;

    private Handler handler = new Handler();

    private Runnable showButton = new Runnable() {
        @Override
        public void run() {
            long timeInMills = SystemClock.uptimeMillis() - readyStartTime;
            int seconds = (int) timeInMills / 1000;

            if (seconds >= READY_SHOW_BUTTON_TIME) {
                ivReadyNext.setVisibility(View.VISIBLE);
            } else {
                handler.postDelayed(this, 0); // loop until button is visible
            }
        }
    };

    /**
     * Once player clicks Next, display a blank screen for BLANK_SCREEN milliseconds
     */
    private Runnable blankScreen = new Runnable() {
        @Override
        public void run() {
            long timeInMills = SystemClock.uptimeMillis() - startBlankScreenTime;
            if (timeInMills >= BLANK_SCREEN)
                Util.loadFragment(getActivity(), new Stage1());
            else {
                tvGetReady.setVisibility(View.INVISIBLE);
                ivReadyNext.setVisibility(View.INVISIBLE);
                handler.postDelayed(this, 0);
            }
        }
    };

    public GetReady() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (LevelManager.getInstance() != null) {
            LevelManager.getInstance().startLevel();
            LevelManager.getInstance().logVariables();
        }
        else
            Log.e("Stage 1 lm", "level manager is null!");
        readyStartTime = SystemClock.uptimeMillis();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_get_ready, container, false);
        tvGetReady = (TextView) view.findViewById(R.id.tvGetReady);
        tvLevelRoundDebug = (TextView) view.findViewById(R.id.tvLevelRoundDebug);
        ivReadyNext = (ImageView) view.findViewById(R.id.ivReadyGo);

        String readyPrompt = String.format("Get ready for level %d!", LevelManager.getInstance().level);
        tvGetReady.setText(readyPrompt);

        ivReadyNext.setVisibility(View.GONE); // hide button for 1 second
        ivReadyNext.setOnClickListener(this);

        toggleDebug(LevelManager.getInstance().debug);
        handler.postDelayed(showButton, 0);

        return view;
    }

    private void toggleDebug(boolean onOff) {
        if (!onOff)
            return;
        String msg = "sessionLevels: " + LevelManager.getInstance().sessionLevels +
                "\nrepetitions: " + LevelManager.getInstance().repetitions +
                "\ncurrent round: " + LevelManager.getInstance().round +
                "\nrounds left: " + (LevelManager.getInstance().repetitions - LevelManager.getInstance().round);
        tvLevelRoundDebug.setText(msg);
//        Log.d("sessionLevels", "" + LevelManager.getInstance().sessionLevels);
//        Log.d("repetitions", "" + LevelManager.getInstance().repetitions);
//        Log.d("current round", "" + LevelManager.getInstance().round);
    }

    @Override
    public void onClick(View v) {
        startBlankScreenTime = SystemClock.uptimeMillis();
        handler.postDelayed(blankScreen, 0);
    }

}
