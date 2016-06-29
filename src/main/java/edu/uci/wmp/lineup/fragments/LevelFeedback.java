package edu.uci.wmp.lineup.fragments;


import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

import edu.uci.wmp.lineup.LevelManager;
import edu.uci.wmp.lineup.R;
import edu.uci.wmp.lineup.StimuliManager;
import edu.uci.wmp.lineup.Util;
import edu.uci.wmp.lineup.fragments.GetReady;
import edu.uci.wmp.lineup.fragments.MainScreen;
import edu.uci.wmp.lineup.fragments.Stage2;


public class LevelFeedback extends Fragment implements View.OnClickListener {

    TextView tvFeedbackPhrase;
    ImageView ivLevelFeedbackNext;
    long levelFeedbackStartTime;

    final int LEVELFEEDBACK_SHOW_BUTTON_TIME = 1;
    final double FEEDBACK_PERCENTAGE = 0.25; // 25% of width
    final double FEEDBACK_TOPMARGIN_PERCENTAGE = 0.10; // top margin of height
    final int LEVEL_UP = 1;
    final int LEVEL_SAME = 0;
    final int LEVEL_DOWN = -1;

    private Handler handler = new Handler();

    private Runnable showButton = new Runnable() {
        @Override
        public void run() {
            long timeInMills = SystemClock.uptimeMillis() - levelFeedbackStartTime;
            int seconds = (int) timeInMills / 1000;

            if (seconds >= LEVELFEEDBACK_SHOW_BUTTON_TIME) {
                ivLevelFeedbackNext.setVisibility(View.VISIBLE);
            } else {
                handler.postDelayed(this, 0); // loop until button is visible
            }
        }
    };

    public LevelFeedback() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        levelFeedbackStartTime = SystemClock.uptimeMillis();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_level_feedback, container, false);


        return view;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivLevelFeedbackNext:
//                goToNextLevel();
                break;

            case R.id.ivDemoQuit:
                Util.loadFragment(getActivity(), new MainScreen());
                break;
        }
    }

}
