package edu.uci.wmp.lineup.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import edu.uci.wmp.lineup.LevelManager;
import edu.uci.wmp.lineup.R;
import edu.uci.wmp.lineup.StimuliManager;
import edu.uci.wmp.lineup.Util;

public class Stage1 extends Fragment {

    RelativeLayout rlDisplay;
    RelativeLayout.LayoutParams firstRowLayoutParams, secondRowLayoutParams, rlStimuliLayoutParams;
	LinearLayout llFirstRow, llSecondRow;
	LinearLayout.LayoutParams llStimuliLayoutParams;
    TextView tvTargetLureDebug;
    Random random;

	static final int MAX_SETSIZE_CIRCLE = 8; // display stimuli in circle until 8 stimuli. otherwise in rows
    static final double STIMULI_SIZE = 0.2; // 0.2 of height
	static final double ROW_TOP_MARGIN = 0.33; // position rows 1/3 & 2/3 of screen height
	static final double LEFT_RIGHT_MARGIN = 0.005; // gap between stimuli in rows

	boolean circle; // circle or row, depending on setsize
    double sectorAngle;
    double cumulativeAngle;
	int numStimuliPerRow;
    long stageStartTime;

    private Handler handler = new Handler();

    private Runnable nextStage = new Runnable() {
        @Override
        public void run() {
            long timeInMills = SystemClock.uptimeMillis() - stageStartTime;
//            int seconds = (int) timeInMills / 1000;

            if (timeInMills >= LevelManager.getInstance().presentationtimeperstimulus * LevelManager.getInstance().setsize) // consistent time given per stimulus
                Util.loadFragment((AppCompatActivity) getActivity(), new Stage2());
            else {
                handler.postDelayed(this, 0); // loop until button is visible
            }
        }
    };

    public Stage1() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LevelManager.getInstance().startRound();
        LevelManager.getInstance().generateStimuliFirstPart();

	    int stimuliSideLength = Double.valueOf(LevelManager.getInstance().screenHeight * STIMULI_SIZE).intValue();
	    circle = LevelManager.getInstance().setsize <= MAX_SETSIZE_CIRCLE;

	    if (circle) { // circle
		    rlStimuliLayoutParams = new RelativeLayout.LayoutParams(stimuliSideLength, stimuliSideLength); // layoutparams for circle stimuli
		    rlStimuliLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
		    rlStimuliLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
		    random = new Random();
		    sectorAngle = 360.00 / LevelManager.getInstance().setsize;
		    cumulativeAngle = Double.valueOf(random.nextInt(360)).intValue(); // random starting point
	    }
	    else { // 2 rows
		    llStimuliLayoutParams = new LinearLayout.LayoutParams(stimuliSideLength, stimuliSideLength); // layoutparams for row stimuli
		    int stimLeftRightMargin = Double.valueOf(LevelManager.getInstance().screenWidth * LEFT_RIGHT_MARGIN).intValue();
		    llStimuliLayoutParams.setMargins(stimLeftRightMargin, 0, stimLeftRightMargin, 0);
		    numStimuliPerRow = (LevelManager.getInstance().setsize + 1) / 2;
		    llFirstRow = new LinearLayout(getActivity());
		    llSecondRow = new LinearLayout(getActivity());
		    firstRowLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		    secondRowLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		    int topMargin = Double.valueOf(LevelManager.getInstance().screenHeight * ROW_TOP_MARGIN).intValue();
		    firstRowLayoutParams.setMargins(0, topMargin, 0, 0);
		    secondRowLayoutParams.setMargins(0, topMargin * 2, 0, 0);
		    firstRowLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
		    secondRowLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);

		    llFirstRow.setLayoutParams(firstRowLayoutParams);
		    llSecondRow.setLayoutParams(secondRowLayoutParams);
	    }
        stageStartTime = SystemClock.uptimeMillis();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stage1, container, false);

	    rlDisplay = (RelativeLayout) view.findViewById(R.id.rlStimuliDisplay);
        tvTargetLureDebug = (TextView) view.findViewById(R.id.tvTargetLureDebug);

	    // add stimuli to circle/rows
	    int sequenceLength = LevelManager.getInstance().stimuliSequence.size();
	    try {
		    for (int i = 1; i <= sequenceLength; ++i)
		    {
//              Log.i("Stimulus", "" + labeledStimulus);
			    int labeledStimulus = LevelManager.getInstance().stimuliSequence.get(i - 1);
			    if (circle) {
				    rlDisplay.addView(createTargetCircleImageView(labeledStimulus));
				    cumulativeAngle += sectorAngle; // rotate around center, adds up to 360
			    }
			    else {
				    if (i <= numStimuliPerRow)
					    llFirstRow.addView(createTargetRowImageView(labeledStimulus));
				    else
					    llSecondRow.addView(createTargetRowImageView(labeledStimulus));
			    }
		    }
	    } catch (IOException e) {
		    e.printStackTrace();
	    }

	    if (!circle) {
		    rlDisplay.addView(llFirstRow);
		    rlDisplay.addView(llSecondRow);
	    }
        toggleDebug(LevelManager.getInstance().debug);
        handler.postDelayed(nextStage, 0);
        return view;
    }

    @Override
    public void onPause() {
        handler.removeCallbacks(nextStage);
        super.onPause();
    }

	private ImageView createTargetCircleImageView(int labeledStimulus) throws IOException {
		ImageView ivStimuli = new ImageView(getActivity()); // create imageview object for each stimulus
		ivStimuli.setImageBitmap(StimuliManager.getInstance().getStimuli(labeledStimulus));
		ivStimuli.setLayoutParams(rlStimuliLayoutParams);

//        Log.i("Cumulative angle", "" + cumulativeAngle);
		double radians = cumulativeAngle * 0.0174533;
		float xTrans = 300 * (float) Math.sin(radians); // apply angle
		float yTrans = (-1) * 300 * (float) Math.cos(radians);
//        Log.i("X trans", "" + xTrans);
//        Log.i("Y trans", "" + yTrans);
		ivStimuli.setTranslationX(xTrans); // apply translation away from center
		ivStimuli.setTranslationY(yTrans);
		return ivStimuli;
	}

	private ImageView createTargetRowImageView(int labeledStimulus) throws IOException {
		ImageView ivStimuli = new ImageView(getActivity());
		ivStimuli.setImageBitmap(StimuliManager.getInstance().getStimuli(labeledStimulus));
		ivStimuli.setLayoutParams(llStimuliLayoutParams);
		return ivStimuli;
	}

    private void toggleDebug(boolean onOff) {
        if (!onOff)
            return;
        String msg = "stimuliSequence: " + Util.iterableToString(LevelManager.getInstance().stimuliSequence) +
                "\nfirstPartTargetSets: " + Util.iterableToString(LevelManager.getInstance().firstPartTargetSets) +
                "\nfirstPartLureSets: " + Util.iterableToString(LevelManager.getInstance().firstPartLureSets) +
		        "\nsecondPartRPLures: " + Util.iterableToString(LevelManager.getInstance().secondPartRPLures);
        tvTargetLureDebug.setText(msg);
    }
}
