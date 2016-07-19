package edu.uci.wmp.lineup.fragments;


import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.IOException;

import edu.uci.wmp.lineup.CSVWriter;
import edu.uci.wmp.lineup.LevelManager;
import edu.uci.wmp.lineup.R;
import edu.uci.wmp.lineup.StimuliManager;
import edu.uci.wmp.lineup.Util;

public class RoundFeedback extends Fragment {

    FrameLayout flRoundFeedback;
    ImageView ivRoundFeedbackNext;
    static final double FACE_WIDTH = 0.5;
    static final double FACE_HEIGHT = 0.5;
	static final double FACE_TOPMARGIN_PERCENTAGE = 0.10;
    final long BLANK_SCREEN = 500;
    long startBlankScreenTime;

    private Handler handler = new Handler();

    private Runnable blankScreen = new Runnable() {
        @Override
        public void run() {
            long timeInMills = SystemClock.uptimeMillis() - startBlankScreenTime;
            if (timeInMills >= BLANK_SCREEN) {
                LevelManager.getInstance().roundsPlayedThisSession++;
                if (LevelManager.getInstance().round == LevelManager.getInstance().repetitions) { // check if end of level
                    Log.wtf("round feedback", "round ==, end level");
	                if (LevelManager.getInstance().levelsPlayed == LevelManager.getInstance().sessionLevels - 1)
	                {
		                LevelManager.getInstance().levelsPlayed++;
		                calculateNextLevel(); // calculate next level to be saved here since player skips through LevelFeedback
		                if (LevelManager.getInstance().questions)
			                Util.loadFragment((AppCompatActivity) getActivity(), new ReflectionQuestion());
		                else
			                Util.loadFragment((AppCompatActivity) getActivity(), new MainScreen());
	                }
                    else
		                Util.loadFragment((AppCompatActivity) getActivity(), new LevelFeedback());
                }
                else {
                    Log.wtf("round feedback", "round ++, next round");
                    LevelManager.getInstance().round++;
                    Util.loadFragment((AppCompatActivity) getActivity(), new Stage1());
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	    LevelManager.getInstance().fillPotentialRPLures(); // setup potential rp lures before next round begins
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
	            LevelManager.getInstance().points += LevelManager.getInstance().level; // add points
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
	    CSVWriter.getInstance().collectData();
        return view;
    }

    /**
     * Returns round correct / incorrect
     */
    private boolean check() {
        int response = LevelManager.getInstance().response;
        if (response == Stage2.NOANSWER) // no response
            return false;
	    else if (response == Stage2.BLANK_BUTTON_TAG) // user chose blank button
        {
	        for (int otherChoice : LevelManager.getInstance().secondPartSequence)
		        if (LevelManager.getInstance().stimuliSequence.contains(otherChoice))
			        return false;
        }
	    else // user chose a stimulus
        {
	        if (!LevelManager.getInstance().stimuliSequence.contains(response))
		        return false;
        }
	    return true;
    }

	/**
	 * Set next level depending on number of rounds player got wrong
	 * Called upon finishing last level in session, no level feedback is given and directs player to questions/mainscreen.
	 */
	private void calculateNextLevel() {
		if (LevelManager.getInstance().wrongs <= LevelManager.getInstance().goup) {
			Log.wtf("LEVEL_UP", "+++");
			if (LevelManager.getInstance().level < LevelManager.MAX_LEVEL)
				LevelManager.getInstance().level++;
		}
		else if (LevelManager.getInstance().wrongs >= LevelManager.getInstance().godown) {
			Log.wtf("LEVEL_DOWN", "---");
			if (LevelManager.getInstance().level > LevelManager.MIN_LEVEL)
				LevelManager.getInstance().level--;
		}
	}

    private ImageView getFace(int correct) throws IOException {
        ImageView ivFace = new ImageView(getActivity());
        ivFace.setImageBitmap(StimuliManager.getInstance().getFeedbackAsset(getActivity(), correct));
        int faceWidth = Double.valueOf(LevelManager.getInstance().screenWidth * FACE_WIDTH).intValue();
        int faceHeight = Double.valueOf(LevelManager.getInstance().screenHeight * FACE_HEIGHT).intValue();
        FrameLayout.LayoutParams faceLayoutParams = new FrameLayout.LayoutParams(faceWidth, faceHeight);
        faceLayoutParams.gravity = Gravity.CENTER;
	    faceLayoutParams.topMargin = Double.valueOf(LevelManager.getInstance().screenHeight * FACE_TOPMARGIN_PERCENTAGE).intValue();
        ivFace.setLayoutParams(faceLayoutParams);
        return ivFace;
    }
}
