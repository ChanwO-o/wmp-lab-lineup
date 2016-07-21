package edu.uci.wmp.lineup.fragments;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.Fragment;
import android.util.Log;
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
	public static final String CHANGETHEME_KEY = "changetheme_key";
    public static final String DEBUG_KEY = "debug_key";
    public static final String TRAININGMODE_KEY = "trainingmode_key";
    public static final String ROUNDS_KEY = "rounds_key";
    public static final String SESSIONLENGTH_KEY = "sessionlength_key";

    LinearLayout llSettingsWidgets;
    EditText etSubject, etSession, etChangeTheme, etRoundsTime;
    Switch swQuestions, swChangeTheme, swDebug, swTrainingMode;
	RelativeLayout rlChangeTheme;
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
	    swChangeTheme = (Switch) view.findViewById(R.id.swChangeTheme);
	    etChangeTheme = (EditText) view.findViewById(R.id.etAfterXRounds);
        swDebug = (Switch) view.findViewById(R.id.swDebug);
        swTrainingMode = (Switch) view.findViewById(R.id.swTrainingMode);
	    rlChangeTheme = (RelativeLayout) view.findViewById(R.id.rlAfterXRounds);
        tvRTPrompt = (TextView) view.findViewById(R.id.tvRTPrompt);
        etRoundsTime = (EditText) view.findViewById(R.id.etRoundsTime);
        tvRTUnit = (TextView) view.findViewById(R.id.tvRTUnit);
        bPerformChecks = (Button) view.findViewById(R.id.bPerformChecks);
        bPopulate = (Button) view.findViewById(R.id.bPopulate);
        bBack = (Button) view.findViewById(R.id.bSettingsBack);

        // initial setup
//        swQuestions.setTextOn("On");
//        swQuestions.setTextOff("Off");
//        swDebug.setTextOn("On");
//        swDebug.setTextOff("Off");
        swTrainingMode.setTextOn("Time");
        swTrainingMode.setTextOff("Levels");
        etSubject.setText(String.valueOf(LevelManager.getInstance().subject));
        etSession.setText(String.valueOf(LevelManager.getInstance().session));
	    etChangeTheme.setText(String.valueOf(LevelManager.getInstance().changeTheme));

        // set default mode of switches
        swQuestions.setChecked(LevelManager.getInstance().questions);
        swDebug.setChecked(LevelManager.getInstance().debug);
        boolean trainingModeIsTime = LevelManager.getInstance().trainingmode.equals(LevelManager.TRAININGMODE_TIME);
	    boolean changeThemeActivated = LevelManager.getInstance().changeTheme != LevelManager.THEME_NOCHANGE;
	    swChangeTheme.setChecked(changeThemeActivated);
        swTrainingMode.setChecked(trainingModeIsTime);

	    // setup ChangeTheme layout
	    if (!changeThemeActivated)
		    rlChangeTheme.setVisibility(View.GONE);

        // setup RoundsTime layout
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

	    swChangeTheme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		    @Override
		    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
			    if (isChecked)
				    rlChangeTheme.setVisibility(View.VISIBLE);
			    else
				    rlChangeTheme.setVisibility(View.GONE);
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

                    LevelManager.getInstance().questions = swQuestions.isChecked();
	                if (swChangeTheme.isChecked())
		                LevelManager.getInstance().changeTheme = Integer.valueOf(etChangeTheme.getText().toString());
	                else
		                LevelManager.getInstance().changeTheme = LevelManager.THEME_NOCHANGE;
                    LevelManager.getInstance().debug = swDebug.isChecked();
                    LevelManager.getInstance().saveSharedPreferences(); // save settings variables to preferences
                    Util.loadFragment((AppCompatActivity) getActivity(), new MainScreen());
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Invalid inputs: Subject, session & trial inputs must be integers", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

    public void setRTLayoutLevels() {
        tvRTPrompt.setText("Levels");
        etRoundsTime.setText(String.valueOf(LevelManager.getInstance().sessionLevels));
        tvRTUnit.setText("");
    }

    public void setRTLayoutTime() {
        tvRTPrompt.setText("Time");
        etRoundsTime.setText(String.valueOf(LevelManager.getInstance().sessionLength));
        tvRTUnit.setText("(s)");
    }
}
