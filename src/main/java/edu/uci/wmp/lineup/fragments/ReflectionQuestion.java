package edu.uci.wmp.lineup.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.os.SystemClock;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import edu.uci.wmp.lineup.CSVWriter;
import edu.uci.wmp.lineup.LevelManager;
import edu.uci.wmp.lineup.R;
import edu.uci.wmp.lineup.Util;

public class ReflectionQuestion extends Fragment {


    int questionNum = 1;
    TextView tvQuestion;
    LinearLayout llFaces;
    TextView tvTapFace;
    View vLine;
    View[] hiddenViews;

    final double FACESIZE = 0.25;
    final double FACEGAP = 0.02;
    final String SECONDQUESTION = "I think I did pretty well on this task.";
    final int GREEN_TIME = 1000; // face turns green for 1 second
    final int HIDE_TIME = 250; // hide all views

    boolean responded;
    long responseStartTime;
    int faceClickedID;
    private Handler handler = new Handler();

    private Runnable response = new Runnable() {
        @Override
        public void run() {
            long current = SystemClock.uptimeMillis() - responseStartTime;

            if (current <= GREEN_TIME) { // keep showing green
                handler.postDelayed(this, 0);
            } else if (GREEN_TIME < current && current <= GREEN_TIME + HIDE_TIME) { // hide
                setViewsVisible(View.GONE);
                handler.postDelayed(this, 0);
            } else { // setup for second reflection question or proceed to effort questions
                if (questionNum == 1)
                    setUpNextQuestion();
                else
                    Util.loadFragment((AppCompatActivity) getActivity(), new EffortQuestion());
            }
        }
    };

    public ReflectionQuestion() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reflection_question, container, false);

        tvQuestion = (TextView) view.findViewById(R.id.tvReflQuestion);
        llFaces = (LinearLayout) view.findViewById(R.id.llFaces);
        tvTapFace = (TextView) view.findViewById(R.id.tvTapFace);
        vLine = view.findViewById(R.id.vLine);
        hiddenViews = new View[4];

        fillFaces();
        fillHiddenViews();
        responded = false;
        return view;
    }

    @Override
    public void onPause() {
        handler.removeCallbacks(response);
        super.onPause();
    }

    public void fillFaces() {
        for (int i = 1; i <= 5; i++)
            llFaces.addView(createFace(i));
    }

    /**
     * Fills array with views to hide/show after each question
     */
    public void fillHiddenViews() {
        hiddenViews[0] = tvQuestion;
        hiddenViews[1] = llFaces;
        hiddenViews[2] = tvTapFace;
        hiddenViews[3] = vLine;
    }

    public ImageView createFace(final int id) {
        final ImageView ivFace = new ImageView(getActivity());
        setFace(ivFace, id);

        int faceDim = Double.valueOf(LevelManager.getInstance().screenHeight * FACESIZE).intValue();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(faceDim, faceDim);
        int margin = Double.valueOf(LevelManager.getInstance().screenHeight * FACEGAP).intValue(); // margin between each image in grid
        layoutParams.setMargins(margin, margin, margin, margin);
        ivFace.setLayoutParams(layoutParams);
        ivFace.setTag(id); // set tag for click listener

        ivFace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!responded) {
                    responseStartTime = SystemClock.uptimeMillis();

                    int greenLabel = id + 10 * id % 100; // set face green
                    setFace((ImageView) v, greenLabel);

                    CSVWriter.getInstance().collectQuestionResponse(tvQuestion.getText().toString(), (int) v.getTag() % 100);
                    CSVWriter.getInstance().writeQuestionResponse();

                    responded = true;
                    faceClickedID = (int) v.getTag() - 1;// - StimuliManager.FACE_LABEL - 1;
                    handler.postDelayed(response, 0);
                }
            }
        });
        return ivFace;
    }

    private void setFace(ImageView ivFace, int id) {
        switch (id) {
            case 1:
                ivFace.setImageResource(R.drawable.face1);
                break;
            case 2:
                ivFace.setImageResource(R.drawable.face2);
                break;
            case 3:
                ivFace.setImageResource(R.drawable.face3);
                break;
            case 4:
                ivFace.setImageResource(R.drawable.face4);
                break;
            case 5:
                ivFace.setImageResource(R.drawable.face5);
                break;
            case 11:
                ivFace.setImageResource(R.drawable.face1_g);
                break;
            case 22:
                ivFace.setImageResource(R.drawable.face2_g);
                break;
            case 33:
                ivFace.setImageResource(R.drawable.face3_g);
                break;
            case 44:
                ivFace.setImageResource(R.drawable.face4_g);
                break;
            case 55:
                ivFace.setImageResource(R.drawable.face5_g);
                break;
        }
    }

    /**
     * Setup for second reflection question
     */
    public void setUpNextQuestion() {
        // restore face color
        ImageView face = (ImageView) llFaces.getChildAt(faceClickedID);
        setFace(face, (int) face.getTag());

        tvQuestion.setText(SECONDQUESTION);
        questionNum++;
        responded = false;
        setViewsVisible(View.VISIBLE);
    }

    /**
     * Hides/shows all views between questions
     */
    public void setViewsVisible(int visibility) {
        for (View v : hiddenViews)
            v.setVisibility(visibility);
    }

}
