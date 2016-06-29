package edu.uci.wmp.lineup.fragments;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import edu.uci.wmp.lineup.LevelManager;
import edu.uci.wmp.lineup.R;
import edu.uci.wmp.lineup.StimuliManager;
import edu.uci.wmp.lineup.Util;


public class Stage2 extends Fragment {

    LinearLayout llChoiceButtons;
    TextView tvSecondDebug;

    public static final int BLANK_BUTTON_TAG = -99;
    final int BUTTONS_PER_ROW = 4;
    final double CHOICE_BUTTON_WIDTH = 0.17;
    final double CHOICE_BUTTON_HEIGHT = 0.2;
    final double CHOICE_BUTTON_MARGIN = 0.05;
    final double CHOICE_BUTTON_PADDING = 0.01;

    public Stage2() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LevelManager.getInstance().generateStimuliSecondPart();
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
        return view;
    }

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

    private ImageButton createImageButton(int imageLabel, LinearLayout.LayoutParams layoutParams) throws IOException {
        ImageButton choiceButton = new ImageButton(getActivity());
        choiceButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
        choiceButton.setAdjustViewBounds(true);
        if (imageLabel != BLANK_BUTTON_TAG)
            choiceButton.setImageBitmap(StimuliManager.getInstance().getStimuli(imageLabel));
        choiceButton.setLayoutParams(layoutParams);
        choiceButton.setTag(imageLabel);
        choiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LevelManager.getInstance().responses.add((int) view.getTag()); // submit answer
                Util.loadFragment(getActivity(), new RoundFeedback());
            }
        });
        return choiceButton;
    }

    private void toggleDebug(boolean onOff) {
        if (!onOff)
            return;
        String msg = "stimuliSequence: " + Util.iterableToString(LevelManager.getInstance().stimuliSequence) +
                "\nfirstPartTargetSets: " + Util.iterableToString(LevelManager.getInstance().firstPartTargetSets) +
                "\nfirstPartLureSets: " + Util.iterableToString(LevelManager.getInstance().firstPartLureSets) +
                "\nsecondPartSequence: " + Util.iterableToString(LevelManager.getInstance().secondPartSequence) +
                "\nsecondPartPotentialTargets: " + Util.iterableToString(LevelManager.getInstance().secondPartPotentialTargets) +
                "\nsecondPartPotentialLures: " + Util.iterableToString(LevelManager.getInstance().secondPartPotentialLures) +
                "\nsecondPartPotentialDistractors: " + Util.iterableToString(LevelManager.getInstance().secondPartPotentialDistractors);
        tvSecondDebug.setText(msg);
    }
}
