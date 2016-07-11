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
import android.widget.TextView;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import edu.uci.wmp.lineup.LevelManager;
import edu.uci.wmp.lineup.R;
import edu.uci.wmp.lineup.StimuliManager;
import edu.uci.wmp.lineup.Util;

public class Stage1 extends Fragment {

    FrameLayout flStimuliCircle;
    FrameLayout.LayoutParams circleLayoutParams;
    TextView tvTargetLureDebug;
    Random random;

    final double STIMULI_SIZE = 0.2; // 0.2 of height

    double sectorAngle;
    double cumulativeAngle;
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
        int circleStimuliSide = Double.valueOf(LevelManager.getInstance().screenHeight * STIMULI_SIZE).intValue();
        circleLayoutParams = new FrameLayout.LayoutParams(circleStimuliSide, circleStimuliSide);
        circleLayoutParams.gravity = Gravity.CENTER;
        random = new Random();
        sectorAngle = 360.00 / LevelManager.getInstance().setsize;
        cumulativeAngle = Double.valueOf(random.nextInt(360)).intValue(); // random starting point
        stageStartTime = SystemClock.uptimeMillis();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stage1, container, false);

        flStimuliCircle = (FrameLayout) view.findViewById(R.id.flStimuliCircle);
        tvTargetLureDebug = (TextView) view.findViewById(R.id.tvTargetLureDebug);

        for (int labeledStimulus : LevelManager.getInstance().stimuliSequence) // Adding images around circle
        {
//            Log.i("Stimulus", "" + labeledStimulus);
            try {
                flStimuliCircle.addView(createTargetImageView(labeledStimulus));
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            cumulativeAngle += sectorAngle; // rotate around center, adds up to 360
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

    private ImageView createTargetImageView(int labeledStimulus) throws IOException {
        ImageView ivStimuli = new ImageView(getActivity()); // create imageview object for each stimulus
        ivStimuli.setImageBitmap(StimuliManager.getInstance().getStimuli(labeledStimulus));
        ivStimuli.setLayoutParams(circleLayoutParams);

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

    private void toggleDebug(boolean onOff) {
        if (!onOff)
            return;
        String msg = "RP round: " + LevelManager.getInstance().rpRoundsOrder.get(LevelManager.getInstance().round - 1) +
		        "\nstimuliSequence: " + Util.iterableToString(LevelManager.getInstance().stimuliSequence) +
                "\nfirstPartTargetSets: " + Util.iterableToString(LevelManager.getInstance().firstPartTargetSets) +
                "\nfirstPartLureSets: " + Util.iterableToString(LevelManager.getInstance().firstPartLureSets) +
		        "\nsecondPartRPLures: " + Util.iterableToString(LevelManager.getInstance().secondPartRPLures) +
		        "\nrpRounds: " + Util.iterableToString(LevelManager.getInstance().rpRoundsOrder);
        tvTargetLureDebug.setText(msg);
    }
}
