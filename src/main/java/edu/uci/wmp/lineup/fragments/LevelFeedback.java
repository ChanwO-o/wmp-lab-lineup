package edu.uci.wmp.lineup.fragments;


import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
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

    FrameLayout flLevelFeedback;
    TextView tvFeedbackPhrase;
    ImageView ivDemoQuit;
    ImageView ivLevelFeedbackNext;
    TextView tvLevelFeedbackDebug;

    final int LEVELFEEDBACK_SHOW_BUTTON_TIME = 1;
    final double FACE_WIDTH = 0.5;
    final double FACE_HEIGHT = 0.5;
    public static final int LEVEL_UP = 777;
    public static final int LEVEL_SAME = 555;
    public static final int LEVEL_DOWN = 666;

    long levelFeedbackStartTime;

    private Handler handler = new Handler();

    private Runnable showButton = new Runnable() {
        @Override
        public void run() {
            long timeInMills = SystemClock.uptimeMillis() - levelFeedbackStartTime;
            int seconds = (int) timeInMills / 1000;

            if (seconds >= LEVELFEEDBACK_SHOW_BUTTON_TIME)
                ivLevelFeedbackNext.setVisibility(View.VISIBLE);
            else
                handler.postDelayed(this, 0); // loop until button is visible
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

        flLevelFeedback = (FrameLayout) view.findViewById(R.id.flLevelFeedback);
        tvFeedbackPhrase = (TextView) view.findViewById(R.id.tvFeedbackPhrase);
        ivDemoQuit = (ImageView) view.findViewById(R.id.ivDemoQuit);
        ivLevelFeedbackNext = (ImageView) view.findViewById(R.id.ivLevelFeedbackNext);
        tvLevelFeedbackDebug = (TextView) view.findViewById(R.id.tvLevelFeedbackDebug);

        if (!LevelManager.getInstance().trainingmode.equals(LevelManager.TRAININGMODE_DEMO))
            ivDemoQuit.setVisibility(View.GONE);
        ivDemoQuit.setOnClickListener(this);
        ivLevelFeedbackNext.setOnClickListener(this);


        try {
            calculateNextLevel();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LevelManager.getInstance().levelsPlayed++;
        handler.postDelayed(showButton, 0);
        toggleDebug(LevelManager.getInstance().debug);
        return view;
    }

    private ImageView getFace(int result) throws IOException {
        ImageView ivFace = new ImageView(getActivity());
        ivFace.setImageBitmap(StimuliManager.getInstance().getFeedbackAsset(getActivity(), result));
        int faceWidth = Double.valueOf(LevelManager.getInstance().screenWidth * FACE_WIDTH).intValue();
        int faceHeight = Double.valueOf(LevelManager.getInstance().screenHeight * FACE_HEIGHT).intValue();
        FrameLayout.LayoutParams faceLayoutParams = new FrameLayout.LayoutParams(faceWidth, faceHeight);
        faceLayoutParams.gravity = Gravity.CENTER;
        ivFace.setLayoutParams(faceLayoutParams);
        return ivFace;
    }

    /**
     * Set next level depending on number of rounds player got wrong
     * Display correct feedback face
     */
    private void calculateNextLevel() throws IOException {
        if (LevelManager.getInstance().wrongs <= LevelManager.getInstance().goup) {
            Log.wtf("LEVEL_UP", "++");
            LevelManager.getInstance().level++; //TODO: SETUP LIMITS
            flLevelFeedback.addView(getFace(LEVEL_UP));
            tvFeedbackPhrase.setText("Level up");
        }
        else if (LevelManager.getInstance().wrongs < LevelManager.getInstance().godown) {
            Log.wtf("LEVEL_SAME", "==");
            flLevelFeedback.addView(getFace(LEVEL_SAME));
            tvFeedbackPhrase.setText("Level same");
        }
        else {
            Log.wtf("LEVEL_DOWN", "--");
            LevelManager.getInstance().level--; //TODO: SETUP LIMITS
            flLevelFeedback.addView(getFace(LEVEL_DOWN));
            tvFeedbackPhrase.setText("Level down");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivLevelFeedbackNext:
                if (LevelManager.getInstance().levelsPlayed == LevelManager.getInstance().sessionLevels) { // end session
                    if (LevelManager.getInstance().questions)
                        Util.loadFragment(getActivity(), new ReflectionQuestion());
                    else
                        Util.loadFragment(getActivity(), new MainScreen());
                }
                else if (LevelManager.getInstance().levelsPlayed < LevelManager.getInstance().sessionLevels) // next level
                    Util.loadFragment(getActivity(), new GetReady());
                break;

            case R.id.ivDemoQuit:
                Util.loadFragment(getActivity(), new MainScreen());
                break;
        }
    }

    private void toggleDebug(boolean onOff) {
        if (!onOff)
            return;
        String msg = "repetitions: " + LevelManager.getInstance().repetitions +
                "\nrounds wrong: " + LevelManager.getInstance().wrongs +
                "\ngoup: " + LevelManager.getInstance().goup +
                "\ngodown: " + LevelManager.getInstance().godown;
        tvLevelFeedbackDebug.setText(msg);
    }
}
