package edu.uci.wmp.lineup.fragments;


import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.IOException;

import edu.uci.wmp.lineup.LevelManager;
import edu.uci.wmp.lineup.R;
import edu.uci.wmp.lineup.StimuliManager;
import edu.uci.wmp.lineup.Util;

public class RoundFeedback extends Fragment {

    FrameLayout flRoundFeedback;
    ImageView ivRoundFeedbackNext;
    final double FACE_WIDTH = 0.5;
    final double FACE_HEIGHT = 0.5;
    final long BLANK_SCREEN = 500;
    long startBlankScreenTime;

    private Handler handler = new Handler();

    private Runnable blankScreen = new Runnable() {
        @Override
        public void run() {
            long timeInMills = SystemClock.uptimeMillis() - startBlankScreenTime;
            if (timeInMills >= BLANK_SCREEN) {
                LevelManager.getInstance().roundsPlayed++;
                if (LevelManager.getInstance().round == LevelManager.getInstance().repetitions) { // check if end of level
                    Log.wtf("round feedback", "round ==, end level");
                    Util.loadFragment(getActivity(), new LevelFeedback());
                }
                else {
                    Log.wtf("round feedback", "round ++, next round");
                    LevelManager.getInstance().round++;
                    Util.loadFragment(getActivity(), new Stage1());
                }
            }
            else {
                flRoundFeedback.setVisibility(View.INVISIBLE);
                handler.postDelayed(this, 0);
            }
        }
    };

    public RoundFeedback() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_round_feedback, container, false);

        flRoundFeedback = (FrameLayout) view.findViewById(R.id.flRoundFeedback);
        ivRoundFeedbackNext = (ImageView) view.findViewById(R.id.ivRoundFeedbackNext);

        ivRoundFeedbackNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startBlankScreenTime = SystemClock.uptimeMillis();
                handler.postDelayed(blankScreen, 0);
            }
        });

        try {
            if (check()) {
                flRoundFeedback.addView(getFace(StimuliManager.CORRECT));
                LevelManager.getInstance().accuracy = StimuliManager.CORRECT;
            }
            else {
                flRoundFeedback.addView(getFace(StimuliManager.INCORRECT));
                LevelManager.getInstance().accuracy = StimuliManager.INCORRECT;
                LevelManager.getInstance().wrongs++;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return view;
    }

    /**
     * Returns round correct / incorrect
     */
    private boolean check() {
        int response = LevelManager.getInstance().response;
        if (response == Stage2.NOANSWER)
            return false;
        // check if response was in stimulisequence
        boolean inSequence = false;
        for (int stimFromFirst : LevelManager.getInstance().stimuliSequence) {
            if (LevelManager.getInstance().secondPartSequence.contains(stimFromFirst)) {
                inSequence = true;
                break;
            }
        }
        return (response == Stage2.BLANK_BUTTON_TAG && !inSequence) || (response != Stage2.BLANK_BUTTON_TAG && inSequence); // TODO: what if player clicks a lure? wrong.
    }

    private ImageView getFace(int correct) throws IOException {
        ImageView ivFace = new ImageView(getActivity());
        ivFace.setImageBitmap(StimuliManager.getInstance().getFeedbackAsset(getActivity(), correct));
        int faceWidth = Double.valueOf(LevelManager.getInstance().screenWidth * FACE_WIDTH).intValue();
        int faceHeight = Double.valueOf(LevelManager.getInstance().screenHeight * FACE_HEIGHT).intValue();
        FrameLayout.LayoutParams faceLayoutParams = new FrameLayout.LayoutParams(faceWidth, faceHeight);
        faceLayoutParams.gravity = Gravity.CENTER;
        ivFace.setLayoutParams(faceLayoutParams);
        return ivFace;
    }
}
