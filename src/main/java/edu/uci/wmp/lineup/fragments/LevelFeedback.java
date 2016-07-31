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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;

import edu.uci.wmp.lineup.Checks;
import edu.uci.wmp.lineup.LevelManager;
import edu.uci.wmp.lineup.R;
import edu.uci.wmp.lineup.StimuliManager;
import edu.uci.wmp.lineup.Util;


public class LevelFeedback extends Fragment implements View.OnClickListener {

    FrameLayout flLevelFeedback;
    TextView tvFeedbackPhrase;
    ImageView ivDemoQuit;
    ImageView ivLevelFeedbackNext;
    TextView tvLevelFeedbackDebug;

    final int LEVELFEEDBACK_SHOW_BUTTON_TIME = 1;
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
        LevelManager.getInstance().levelsPlayed++;
        try {
            calculateNextLevel();
        } catch (IOException e) {
            e.printStackTrace();
        }
        handler.postDelayed(showButton, 0);
        toggleDebug(LevelManager.getInstance().debug);
        return view;
    }

    private ImageView getFace(int result) throws IOException {
        ImageView ivFace = new ImageView(getActivity());
        ivFace.setImageBitmap(StimuliManager.getInstance().getFeedbackAsset(getActivity(), result));
        int faceWidth = Double.valueOf(LevelManager.getInstance().screenWidth * RoundFeedback.FACE_WIDTH).intValue();
        int faceHeight = Double.valueOf(LevelManager.getInstance().screenHeight * RoundFeedback.FACE_HEIGHT).intValue();
        FrameLayout.LayoutParams faceLayoutParams = new FrameLayout.LayoutParams(faceWidth, faceHeight);
        faceLayoutParams.gravity = Gravity.CENTER;
	    faceLayoutParams.topMargin = Double.valueOf(LevelManager.getInstance().screenHeight * RoundFeedback.FACE_TOPMARGIN_PERCENTAGE).intValue();
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
            if (LevelManager.getInstance().level < LevelManager.MAX_LEVEL)
                LevelManager.getInstance().level++;
            if (LevelManager.getInstance().trainingmode.equals(LevelManager.TRAININGMODE_LEVELS) && //TODO: also check for timedmode
                    LevelManager.getInstance().levelsPlayed != LevelManager.getInstance().sessionLevels) // don't display feedback phrase on last trial
                tvFeedbackPhrase.setText(getFeedbackPhrase(LEVEL_UP));
            flLevelFeedback.addView(getFace(LEVEL_UP));
        }
        else if (LevelManager.getInstance().wrongs < LevelManager.getInstance().godown) {
            Log.wtf("LEVEL_SAME", "==");
            if (LevelManager.getInstance().trainingmode.equals(LevelManager.TRAININGMODE_LEVELS) && //TODO: also check for timedmode
                    LevelManager.getInstance().levelsPlayed != LevelManager.getInstance().sessionLevels) // don't display feedback phrase on last trial
                tvFeedbackPhrase.setText(getFeedbackPhrase(LEVEL_SAME));
            flLevelFeedback.addView(getFace(LEVEL_SAME));
        }
        else {
            Log.wtf("LEVEL_DOWN", "--");
            if (LevelManager.getInstance().level > LevelManager.MIN_LEVEL)
                LevelManager.getInstance().level--;
            if (LevelManager.getInstance().trainingmode.equals(LevelManager.TRAININGMODE_LEVELS) && //TODO: also check for timedmode
                    LevelManager.getInstance().levelsPlayed != LevelManager.getInstance().sessionLevels)
                tvFeedbackPhrase.setText(getFeedbackPhrase(LEVEL_DOWN));
            flLevelFeedback.addView(getFace(LEVEL_DOWN));
        }
    }

    /**
     * Return random phrase from feedback phrase files with removed quotation marks, separated into lines
     * Iterate down random number of lines in file
     */
    public String getFeedbackPhrase(int next) {

	    String folderPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + Checks.FEEDBACKFOLDER_PATH;
	    String filePath = "";
	    if (next == LEVEL_UP)
		    filePath = folderPath + "roundfeedback_up.txt";
	    else if (next == LEVEL_DOWN)
		    filePath = folderPath + "roundfeedback_down.txt";
	    else if (next == LEVEL_SAME)
		    filePath = folderPath + "roundfeedback_same.txt";
	    File feedbackFile = new File(filePath);

        String line = "";
        try
        {
	        InputStream inputStream = new FileInputStream(feedbackFile);
	        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//            reader.mark(0); // when reset() is called, reader will return to line 0 after reaching last line in file
            int r = new Random().nextInt(200);
            for (int i = 0; i < r; ++i) {
                if ((line = reader.readLine()) == null) { // reached end of phrases file
	                inputStream = new FileInputStream(feedbackFile); // reset inputstream and reader
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    line = reader.readLine();
                }
//                Log.d("getFeedbackPhrase()", "Reading line " + line);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        // replace punctuation with newline characters
        String[] punc = new String[] {". ", "! ", "? "}; // keep one space after punctuation for effectiveness
        for (String p : punc)
            if (line != null)
                line = line.replace(p, p + "\n");
        return line;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivLevelFeedbackNext:
                if (LevelManager.getInstance().levelsPlayed == LevelManager.getInstance().sessionLevels) { // end session
                    if (LevelManager.getInstance().questions)
                        Util.loadFragment((AppCompatActivity) getActivity(), new ReflectionQuestion());
                    else
                        Util.loadFragment((AppCompatActivity) getActivity(), new MainScreen());
                }
                else if (LevelManager.getInstance().levelsPlayed < LevelManager.getInstance().sessionLevels) // next level
                    Util.loadFragment((AppCompatActivity) getActivity(), new GetReady());
                break;

            case R.id.ivDemoQuit:
                Util.loadFragment((AppCompatActivity) getActivity(), new MainScreen());
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
