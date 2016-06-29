package edu.uci.wmp.lineup.fragments;


import android.annotation.SuppressLint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import edu.uci.wmp.lineup.LevelManager;
import edu.uci.wmp.lineup.R;
import edu.uci.wmp.lineup.Util;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainScreen extends Fragment implements View.OnTouchListener {

    TextView tvLineUp;
    ImageView ivStart;
    ImageView ivDemo;
    MyRect topLeft, topRight, bottomLeft, bottomRight;
    MyRect[] rects;
    final static int NON_ASSIGNED_ID = 99;
    final int RECT_WIDTH = 100;
    final int RECT_HEIGHT = 100;
    final int POINTER_DOWN = 0;
    final int POINTER_UP = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // invisible settings buttons
        topLeft = new MyRect(0, 0, RECT_WIDTH, RECT_HEIGHT);
        topRight = new MyRect(LevelManager.getInstance().screenWidth - RECT_WIDTH, 0, LevelManager.getInstance().screenWidth, RECT_HEIGHT);
        bottomLeft = new MyRect(0, LevelManager.getInstance().screenHeight - RECT_HEIGHT, RECT_WIDTH, LevelManager.getInstance().screenHeight + RECT_HEIGHT);
        bottomRight = new MyRect(LevelManager.getInstance().screenWidth - RECT_WIDTH, LevelManager.getInstance().screenHeight - RECT_HEIGHT, LevelManager.getInstance().screenWidth, LevelManager.getInstance().screenHeight + RECT_HEIGHT);
        rects = new MyRect[] {topLeft, topRight, bottomLeft, bottomRight};

//        for (MyRect r : rects)
//            r.logDimensions();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        view.setOnTouchListener(this);

        tvLineUp = (TextView) view.findViewById(R.id.tvAnimalSpan);
        ivStart = (ImageView) view.findViewById(R.id.ivStart);
        ivDemo = (ImageView) view.findViewById(R.id.ivDemo);

        // set text font
        Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/azoft-sans.ttf");
        tvLineUp.setTypeface(tf);

        ivStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if user played demo mode, change it to level mode
                if (LevelManager.getInstance().trainingmode.equals(LevelManager.TRAININGMODE_DEMO))
                    LevelManager.getInstance().trainingmode = LevelManager.TRAININGMODE_LEVELS;
                LevelManager.getInstance().startSession();
                Util.loadFragment(getActivity(), new GetReady());
            }
        });

        ivDemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LevelManager.getInstance().trainingmode = LevelManager.TRAININGMODE_DEMO; // start demo mode and play max 3 rounds
                LevelManager.getInstance().startSession();
                Util.loadFragment(getActivity(), new GetReady());
//                Util.loadFragment(getActivity(), new ReflectionQuestion()); // used for testing questions easily
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Util.dimSystemBar(getActivity());
    }

    /**
     * Touch events
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int actionIndex = event.getActionIndex(); // get action index from the event object
        int pointerId = event.getPointerId(actionIndex); // get pointer ID
        int pointerIndex = event.findPointerIndex(pointerId); // get pointer index
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);

//        Log.d("pointerId", pointerId + "");
//        Log.d("pointer coords", x + " " + y);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                checkTouchRect(x, y, POINTER_DOWN);
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                checkTouchRect(x, y, POINTER_DOWN);
                break;

            case MotionEvent.ACTION_UP:
                checkTouchRect(x, y, POINTER_UP);
                break;

            case MotionEvent.ACTION_POINTER_UP:
                checkTouchRect(x, y, POINTER_UP);
                break;
        }
        return true;
    }

    /**
     * Returns the rectangle that contains the coords, else return null
     */
    public MyRect inRect(float x, float y) {
        for (MyRect r : rects)
            if (r.contains(x, y))
                return r;
        return null;
    }

    public void checkTouchRect(float x, float y, int pointerDirection) {
        MyRect r = inRect(x, y);
        if (r != null) {
            if (pointerDirection == POINTER_DOWN) {
                r.selected = true;
//                Log.w("touched", "down");
            }
            else if (pointerDirection == POINTER_UP) {
                r.selected = false;
//                Log.w("touched", "up");
            }
            checkAllRects();
        }
    }

    /**
     * If all rectangles are selected, call openSettings()
     */
    public void checkAllRects() {
        for (MyRect r : rects)
//            if (r.selected)
//                Util.loadFragment(getActivity(), new Settings());
            if (!r.selected)
                return;
//        Log.i("checkAllRects()", "Opening Settings");
        Util.loadFragment(getActivity(), new Settings());
    }

    @SuppressLint("ParcelCreator")
    private class MyRect extends RectF {

        int pointerId;
        boolean selected;

        public MyRect(float left, float top, float right, float bottom) {
            super(left, top, right, bottom);
            pointerId = NON_ASSIGNED_ID; // default non-assigned id
            selected = false;
        }

        public void logDimensions() {
            Log.i("Dimensions", "l: " + left + " t: " + top + " r: " + right + " b: " + bottom);
        }
    }
}
