package edu.uci.wmp.lineup.fragments;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.Fragment;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import edu.uci.wmp.lineup.CSVWriter;
import edu.uci.wmp.lineup.LevelManager;
import edu.uci.wmp.lineup.R;
import edu.uci.wmp.lineup.Util;

public class EffortQuestion extends Fragment {

    int questionNum = 1;
    TextView tvQuestion, tvSeekBarFirst, tvSeekBarSecond, tvSeekBarThird;
    SeekBar seekBar;
    RelativeLayout rlSeekBarLabels;
    LinearLayout llEffortImages;
    ImageView ivEffortFirst, ivEffortSecond, ivEffortThird, ivNext;
    View[] hiddenViews;

    final double IMAGE_WIDTH_PERCENTAGE = 0.50;
    final double IMAGE_HEIGHT_PERCENTAGE = 0.50;

    // second question setup
    final String SECONDQUESTION = "How hard did you try to do your best at the task?";
    final String[] SECONDQUESTIONLABELS = new String[] {"I did not try very hard", "", "I tried my best"};

    boolean responded;
    long responseStartTime;
    final int HIDE_TIME = 500; // hide all views
    final int HIDE_FLICKER_TIME = 2000; // short hide time for screen flickering
    private Handler handler = new Handler();

    private Runnable response = new Runnable() {
        @Override
        public void run() {
            long current = SystemClock.uptimeMillis() - responseStartTime;
            if (current < HIDE_TIME) {
	            setViewsVisible(View.INVISIBLE);
                handler.postDelayed(this, 0);
            } else {
	            handler.removeCallbacks(this);
	            setViewsVisible(View.VISIBLE);
            }
        }
    };

    /**
     * Hides the flicker from views adjusting and instantly being displayed on screen
     */
    private Runnable hideFlicker = new Runnable() {
        @Override
        public void run() {
            long current = SystemClock.uptimeMillis();
            if (!responded && current < HIDE_FLICKER_TIME) {
                handler.postDelayed(this, 0);
            } else {
	            handler.removeCallbacks(this);
	            setViewsVisible(View.VISIBLE);
            }
        }
    };

    public EffortQuestion() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_effort_question, container, false);

        tvQuestion = (TextView) view.findViewById(R.id.tvEffortQuestion);
        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        rlSeekBarLabels = (RelativeLayout) view.findViewById(R.id.rlSeekBarLabels);
        tvSeekBarFirst = (TextView) rlSeekBarLabels.findViewById(R.id.tvSeekBarFirst);
        tvSeekBarSecond = (TextView) rlSeekBarLabels.findViewById(R.id.tvSeekBarSecond);
        tvSeekBarThird = (TextView) rlSeekBarLabels.findViewById(R.id.tvSeekBarThird);
        ivNext = (ImageView) view.findViewById(R.id.ivEffortQuestionsDone);

        llEffortImages = (LinearLayout) view.findViewById(R.id.llEffortImages);
        ivEffortFirst = (ImageView) view.findViewById(R.id.ivEffortFirst);
        ivEffortSecond = (ImageView) view.findViewById(R.id.ivEffortSecond);
        ivEffortThird = (ImageView) view.findViewById(R.id.ivEffortThird);

        responded = false;
        hiddenViews = new View[10];
        fillHiddenViews();
        setViewsVisible(View.INVISIBLE); // remove flicker

//        seekBar.bringToFront();
//        seekBar.invalidate(); // for drawing seekbar above labels
        seekBar.getThumb().setAlpha(200); // set transparency value 0-255

        // scale images
        int imageWidth = Double.valueOf(LevelManager.getInstance().screenHeight * IMAGE_WIDTH_PERCENTAGE).intValue();
        int imageHeight = Double.valueOf(LevelManager.getInstance().screenHeight * IMAGE_HEIGHT_PERCENTAGE).intValue();
        LinearLayout.LayoutParams imageLayoutParams = new LinearLayout.LayoutParams(imageWidth, imageHeight);

        ivEffortFirst.setLayoutParams(imageLayoutParams);
        ivEffortSecond.setLayoutParams(imageLayoutParams);
        ivEffortThird.setLayoutParams(imageLayoutParams);

        ivNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!responded) {
                    responseStartTime = SystemClock.uptimeMillis();
                    responded = true;

                    CSVWriter.getInstance().collectQuestionResponse(tvQuestion.getText().toString(), seekBar.getProgress());
                    CSVWriter.getInstance().writeQuestionResponse();

                    if (questionNum == 1) {
                        handler.postDelayed(response, 0);
                        setUpNextQuestion();
                    }
                    else
                        Util.loadFragment((AppCompatActivity) getActivity(), new SessionResults());
                }
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        adjustLabelsLayout();
        handler.postDelayed(hideFlicker, 0);
    }

    /**
     * Setup for second effort question
     */
    public void setUpNextQuestion() {
        tvQuestion.setText(SECONDQUESTION);
        ivEffortFirst.setImageResource(R.drawable.tryhard1);
        ivEffortThird.setImageResource(R.drawable.tryhard2);

        for (int i = 0; i < SECONDQUESTIONLABELS.length; i++)
            ((TextView) rlSeekBarLabels.getChildAt(i)).setText(SECONDQUESTIONLABELS[i]);
//        rlSeekBarLabels.getChildAt(1).setVisibility(View.GONE); // hide seekbar stop on second label (drawableTop)
        seekBar.setProgress(50);
        questionNum++;
        responded = false;
        adjustLabelsLayout();
    }

    /**
     * Fills array with views to hide/show after each question
     */
    public void fillHiddenViews() {
        hiddenViews[0] = tvQuestion;
        hiddenViews[1] = ivEffortFirst;
        hiddenViews[2] = ivEffortSecond;
        hiddenViews[3] = ivEffortThird;
        hiddenViews[4] = rlSeekBarLabels;
        hiddenViews[5] = tvSeekBarFirst;
        hiddenViews[6] = tvSeekBarSecond;
        hiddenViews[7] = tvSeekBarThird;
        hiddenViews[8] = seekBar;
        hiddenViews[9] = ivNext;
    }

    /**
     * Hides/shows all views between questions
     */
    public void setViewsVisible(int visibility) {
        for (View v : hiddenViews)
            v.setVisibility(visibility);
        if (questionNum == 2)
            ivEffortSecond.setVisibility(View.INVISIBLE);
    }

    /**
     * Extend relativelayout to position labels and stops precisely on seekbar
     * Calculates new width using textview lengths so that labels are centered at seekbar stops
     */
    public void adjustLabelsLayout() {

        rlSeekBarLabels.post(new Runnable() {
            @Override
            public void run() {
//                Log.i("rl width", "" + rlSeekBarLabels.getWidth());
                View v0 = rlSeekBarLabels.getChildAt(0);
                View v2 = rlSeekBarLabels.getChildAt(2);
//                Log.i("rl child0 width", "" + v0.getWidth());
//                Log.i("rl child2 width", "" + v2.getWidth());

                int seekBarWidthWithoutPadding = seekBar.getWidth() - 2 * (int) getResources().getDimension(R.dimen.seekbar_padding_width);
                int newRlWidth = seekBarWidthWithoutPadding + v0.getWidth() / 2 + v2.getWidth() / 2;
//                Log.i("new rl width", "" + newRlWidth);
                RelativeLayout.LayoutParams newRlLayoutParams = new RelativeLayout.LayoutParams(newRlWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
                newRlLayoutParams.addRule(RelativeLayout.ALIGN_LEFT, seekBar.getId());
                newRlLayoutParams.addRule(RelativeLayout.BELOW, tvQuestion.getId());
                int theTinyGapBetweenWhiteLabelBarAndSeekbar = Double.valueOf(v0.getHeight() * 0.25).intValue();
                newRlLayoutParams.topMargin = (int) getResources().getDimension(R.dimen.gap_huge) - theTinyGapBetweenWhiteLabelBarAndSeekbar;
                newRlLayoutParams.leftMargin = (int) getResources().getDimension(R.dimen.seekbar_padding_width) - v0.getWidth() / 2;
                rlSeekBarLabels.setLayoutParams(newRlLayoutParams);

                if (questionNum == 2) { // set second label to center of seekbar
                    RelativeLayout.LayoutParams invisibleSecondTextViewLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    invisibleSecondTextViewLayoutParams.topMargin = (int) getResources().getDimension(R.dimen.seekbar_label_margin_height);
                    invisibleSecondTextViewLayoutParams.leftMargin = seekBarWidthWithoutPadding / 2 + v0.getWidth() / 2;
                    tvSeekBarSecond.setLayoutParams(invisibleSecondTextViewLayoutParams);
                }
            }
        });
    }

}
