package edu.uci.wmp.lineup.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Arrays;

import edu.uci.wmp.lineup.CSVWriter;
import edu.uci.wmp.lineup.LevelManager;
import edu.uci.wmp.lineup.R;
import edu.uci.wmp.lineup.StimuliManager;
import edu.uci.wmp.lineup.Util;


public class Stage2 extends Fragment {

    LinearLayout llChoiceButtons;
    TextView tvSecondDebug;

    public static final int BLANK_BUTTON_TAG = -99;
	public static final int NOANSWER = -1;
    final int BUTTONS_PER_ROW = 4;
    final double CHOICE_BUTTON_WIDTH = 0.17;
    final double CHOICE_BUTTON_HEIGHT = 0.2;
	final double CHOICE_BUTTON_MARGIN = 0.05;
	final double CHOICE_BUTTON_PADDING = 0.02;
    final long BLANK_SCREEN = 500;
    long stageStartTime;
    long startBlankScreenTime;
	boolean responded;

    private Handler handler = new Handler();

    private Runnable presentationLimit = new Runnable() {
        @Override
        public void run() {
            long timeInMills = SystemClock.uptimeMillis() - stageStartTime;
//            int seconds = (int) timeInMills / 1000;

            if (timeInMills >= LevelManager.getInstance().choicetimelimit) // TIME UP!
            {
                LevelManager.getInstance().response = NOANSWER; // submit no answer
                LevelManager.getInstance().reactionTime = (long) NOANSWER;
                Util.loadFragment((AppCompatActivity) getActivity(), new RoundFeedback());
            } else {
                handler.postDelayed(this, 0); // loop until button is visible
            }
        }
    };

    private Runnable blankScreen = new Runnable() {
        @Override
        public void run() {
            long timeInMills = SystemClock.uptimeMillis() - startBlankScreenTime;
            if (timeInMills >= BLANK_SCREEN)
                Util.loadFragment((AppCompatActivity) getActivity(), new RoundFeedback());
            else {
                llChoiceButtons.setVisibility(View.INVISIBLE);
                handler.postDelayed(this, 0);
            }
        }
    };

    public Stage2() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//	    LevelManager.getInstance().part = LevelManager.STAGE2;
        LevelManager.getInstance().generateStimuliSecondPart();
        stageStartTime = SystemClock.uptimeMillis();
	    responded = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stage2, container, false);

        llChoiceButtons = (LinearLayout) view.findViewById(R.id.llChoiceButtons);
        tvSecondDebug = (TextView) view.findViewById(R.id.tvSecondDebug);

        try {
            fillButtons();
        }
        catch (IOException e) {
            Toast.makeText(getActivity(), "Failed to load buttons", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        toggleDebug(LevelManager.getInstance().debug);
        handler.postDelayed(presentationLimit, 0);
        return view;
    }

    @Override
    public void onPause() {
        handler.removeCallbacks(presentationLimit);
        super.onPause();
    }

    /**
     * Fills center layout with rows of buttons, using correct number of rows given number of stimuli for the round.
     * Given constant BUTTONS_PER_ROW, the remainder is calculated which tells the number of buttons in the last row.
     */
    private void fillButtons() throws IOException
    {
        int numberOfButtons = LevelManager.getInstance().secondPartSequence.size() + 1;
        int numberOfRows = numberOfButtons / BUTTONS_PER_ROW;
        int remainder = numberOfButtons % BUTTONS_PER_ROW; // check remainder for break case when there's few images
        if (remainder != 0)
            numberOfRows++;

        int buttonWidth = Double.valueOf(LevelManager.getInstance().screenWidth * CHOICE_BUTTON_WIDTH).intValue();
        int buttonHeight = Double.valueOf(LevelManager.getInstance().screenHeight * CHOICE_BUTTON_HEIGHT).intValue();
        LinearLayout.LayoutParams choiceButtonLayoutParams = new LinearLayout.LayoutParams(buttonWidth, buttonHeight);
        int choiceButtonMargin = Double.valueOf(LevelManager.getInstance().screenHeight * CHOICE_BUTTON_MARGIN).intValue();
        choiceButtonLayoutParams.setMargins(choiceButtonMargin, choiceButtonMargin, choiceButtonMargin, choiceButtonMargin);

        for (int row = 0; row < numberOfRows; ++row) // for each row
        {
            LinearLayout buttonRow = new LinearLayout(getActivity());
            buttonRow.setOrientation(LinearLayout.HORIZONTAL);

            for (int but = 0; but < BUTTONS_PER_ROW; ++but) // for each button
            {
                int indexInList = BUTTONS_PER_ROW * row + but;
                if (indexInList == LevelManager.getInstance().secondPartSequence.size()) { // last index should be the blank button
                    buttonRow.addView(createImageButton(BLANK_BUTTON_TAG, choiceButtonLayoutParams));
                    break;
                }
                // all non-blank buttons
                int currentImageLabel = LevelManager.getInstance().secondPartSequence.get(indexInList);
                buttonRow.addView(createImageButton(currentImageLabel, choiceButtonLayoutParams));
            }
            llChoiceButtons.addView(buttonRow);
        }
    }

    /**
     * Return ImageButton with corresponding stimulus.
     * Unless imageLabel == BLANK_BUTTON_TAG, cuz that obviously leaves the button blank.
     */
    private ImageButton createImageButton(int imageLabel, LinearLayout.LayoutParams layoutParams) throws IOException {
        final ImageButton choiceButton = new ImageButton(getActivity());
        choiceButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
        choiceButton.setAdjustViewBounds(true);
        if (imageLabel != BLANK_BUTTON_TAG)
            choiceButton.setImageBitmap(StimuliManager.getInstance().getStimuli(imageLabel));
        choiceButton.setLayoutParams(layoutParams);
	    choiceButton.setBackgroundResource(R.drawable.responsebuttonshape);
	    int buttonPadding = Double.valueOf(LevelManager.getInstance().screenHeight * CHOICE_BUTTON_PADDING).intValue();
	    choiceButton.setPadding(buttonPadding,buttonPadding,buttonPadding,buttonPadding);
        choiceButton.setTag(imageLabel);
        choiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
	            if (!responded) {
		            responded = true;
		            LevelManager.getInstance().response = (int) choiceButton.getTag(); // submit answer
		            LevelManager.getInstance().reactionTime = SystemClock.uptimeMillis() - stageStartTime;
		            startBlankScreenTime = SystemClock.uptimeMillis();
		            handler.postDelayed(blankScreen, 0);
	            }
            }
        });
        return choiceButton;
    }

    private void toggleDebug(boolean onOff) {
        if (!onOff)
            return;
        String msg = "stimuliSequence: " + Util.iterableToString(LevelManager.getInstance().stimuliSequence) +
                "\nsecondPartSequence: " + Util.iterableToString(LevelManager.getInstance().secondPartSequence) +
//                "\nsecondPartPotentialTargets: " + Util.iterableToString(LevelManager.getInstance().secondPartPotentialTargets) +
//                "\nsecondPartPotentialLures: " + Util.iterableToString(LevelManager.getInstance().secondPartPotentialLures) +
//                "\nsecondPartPotentialDistractors: " + Util.iterableToString(LevelManager.getInstance().secondPartPotentialDistractors) +
		        "\nsecondPartRPLures: " + Util.iterableToString(LevelManager.getInstance().secondPartRPLures) +
		        "\nresponsechoice: " + Arrays.toString(LevelManager.getInstance().responsechoice.get(LevelManager.getInstance().round - 1));

	    tvSecondDebug.setText(msg);
    }
}
