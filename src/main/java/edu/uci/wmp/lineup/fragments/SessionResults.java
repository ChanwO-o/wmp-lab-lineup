package edu.uci.wmp.lineup.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import edu.uci.wmp.lineup.LevelManager;
import edu.uci.wmp.lineup.R;
import edu.uci.wmp.lineup.Util;

public class SessionResults extends Fragment {


    public SessionResults() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LevelManager.getInstance().testStarted = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_session_results, container, false);

//        TextView tvResults = (TextView) view.findViewById(R.id.tvResults);
        ImageView ivBackToMain = (ImageView) view.findViewById(R.id.ivBackToMain);

//        if (!LevelManager.getInstance().trainingmode.equals(LevelManager.TRAININGMODE_DEMO))
//            LevelManager.getInstance().saveLevelToFile(true); // save progress

//        String result = "You earned " + LevelManager.getInstance().points + " points!";
//        tvResults.setText(result);

        ivBackToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.loadFragment((AppCompatActivity) getActivity(), new MainScreen());
            }
        });
        return view;
    }

}
