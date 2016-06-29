package edu.uci.wmp.lineup.fragments;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import edu.uci.wmp.lineup.Checks;
import edu.uci.wmp.lineup.LevelManager;
import edu.uci.wmp.lineup.R;
import edu.uci.wmp.lineup.Util;


public class Settings extends Fragment {

    public static final String SUBJECT_KEY = "subject_key";
    public static final String SESSION_KEY = "session_key";
    public static final String QUESTIONS_KEY = "questions_key";
    public static final String DEBUG_KEY = "debug_key";
    public static final String TRAININGMODE_KEY = "trainingmode_key";
    public static final String ROUNDS_KEY = "rounds_key";
    public static final String SESSIONLENGTH_KEY = "sessionlength_key";

    LinearLayout llSettingsWidgets;
    EditText etSubject, etSession, etRoundsTime;
    Switch swQuestions, swDebug, swTrainingMode;
    TextView tvRTPrompt, tvRTUnit;
    Button bPerformChecks;
    Button bPopulate;
    Button bBack;

    public Settings() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        llSettingsWidgets = (LinearLayout) view.findViewById(R.id.llSettingsWidgets);
        etSubject = (EditText) view.findViewById(R.id.etSubject);
        etSession = (EditText) view.findViewById(R.id.etSession);
        swQuestions = (Switch) view.findViewById(R.id.swQuestions);
        swDebug = (Switch) view.findViewById(R.id.swDebug);
        swTrainingMode = (Switch) view.findViewById(R.id.swTrainingMode);
        tvRTPrompt = (TextView) view.findViewById(R.id.tvRTPrompt);
        etRoundsTime = (EditText) view.findViewById(R.id.etRoundsTime);
        tvRTUnit = (TextView) view.findViewById(R.id.tvRTUnit);
        bPerformChecks = (Button) view.findViewById(R.id.bPerformChecks);
        bPopulate = (Button) view.findViewById(R.id.bPopulate);
        bBack = (Button) view.findViewById(R.id.bSettingsBack);

        // initial setup
        swQuestions.setTextOn("On");
        swQuestions.setTextOff("Off");
        swDebug.setTextOn("On");
        swDebug.setTextOff("Off");
        swTrainingMode.setTextOn("Time");
        swTrainingMode.setTextOff("Rounds");
        etSubject.setText(String.valueOf(LevelManager.getInstance().subject));
        etSession.setText(String.valueOf(LevelManager.getInstance().session));

        // set default mode of switches
        swQuestions.setChecked(LevelManager.getInstance().questions);
        swDebug.setChecked(LevelManager.getInstance().debug);
        boolean trainingModeIsTime = LevelManager.getInstance().trainingmode.equals(LevelManager.TRAININGMODE_TIME);
        swTrainingMode.setChecked(trainingModeIsTime);

        // setup RoundsTime input
        if (trainingModeIsTime)
            setRTLayoutTime();
        else
            setRTLayoutLevels();

        swTrainingMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    LevelManager.getInstance().trainingmode = LevelManager.TRAININGMODE_TIME;
                    setRTLayoutTime();
                } else {
                    LevelManager.getInstance().trainingmode = LevelManager.TRAININGMODE_LEVELS;
                    setRTLayoutLevels();
                }
            }
        });

        bPerformChecks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Checks.getInstance().runAllChecks()) {
                    int imgResource = R.drawable.checkmark;
                    bPerformChecks.setCompoundDrawablesWithIntrinsicBounds(0, 0, imgResource, 0);
                }
                else {
                    int imgResource = R.drawable.crossmark;
                    bPerformChecks.setCompoundDrawablesWithIntrinsicBounds(0, 0, imgResource, 0);
                }
            }
        });

        bPopulate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Checks.getInstance().populateAssets())
                    bPopulate.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checkmark, 0);
                else
                    bPopulate.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.crossmark, 0);
            }
        });

        /**
         * Check valid inputs of subject and session numbers
         * Update these values in LevelManager, then return back to main screen
         */
        bBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // read inputs
                    LevelManager.getInstance().subject = Integer.valueOf(etSubject.getText().toString());
                    LevelManager.getInstance().session = Integer.valueOf(etSession.getText().toString());

                    // set trainingmode values
                    // TODO: check for input == 0, should not proceed
                    if (LevelManager.getInstance().trainingmode.equals(LevelManager.TRAININGMODE_LEVELS))
                        LevelManager.getInstance().sessionLevels = Integer.valueOf(etRoundsTime.getText().toString());
                    else if (LevelManager.getInstance().trainingmode.equals(LevelManager.TRAININGMODE_TIME))
                        LevelManager.getInstance().sessionLength = Integer.valueOf(etRoundsTime.getText().toString());

                    LevelManager.getInstance().questions = swQuestions.isChecked(); // set questions
                    LevelManager.getInstance().debug = swDebug.isChecked(); // set debug
                    LevelManager.getInstance().saveSharedPreferences(); // save settings variables to preferences
                    Util.loadFragment(getActivity(), new MainScreen());
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Invalid inputs: Subject & session numbers must be integers", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

    public void setRTLayoutLevels() {
        tvRTPrompt.setText("Rounds");
        etRoundsTime.setText(String.valueOf(LevelManager.getInstance().sessionLevels));
        tvRTUnit.setText("");
    }

    public void setRTLayoutTime() {
        tvRTPrompt.setText("Time");
        etRoundsTime.setText(String.valueOf(LevelManager.getInstance().sessionLength));
        tvRTUnit.setText("(s)");
    }
}
