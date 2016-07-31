package edu.uci.wmp.lineup;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
//import android.app.Fragment;
//import android.app.FragmentManager;
//import android.app.FragmentTransaction;
import android.view.View;

public final class Util {

    private static final String LOG_TAG = "Util";

    private Util() { } // private constructor to avoid unnecessary instantiation of the class

    /**
     * Clear status & navigation bars from screen, keep them off with STICKY flag
     */
    public static void dimSystemBar(AppCompatActivity activity) {
        final View window = activity.getWindow().getDecorView();
        setFlags(window);

        window.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() { // don't need to run thread; STICKY flag automatically keeps them cleared
            public void onSystemUiVisibilityChange(int visibility) {
                if (visibility == View.SYSTEM_UI_FLAG_VISIBLE) {
                    window.postDelayed(new Runnable() {
                        public void run() {
                            setFlags(window);
                        }
                    }, 2000);
                }
            }
        });
    }

    private static void setFlags(View window) {
        window.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    /**
     * Replace main fragment container with fragment given in parameter
     */
    public static void loadFragment(AppCompatActivity activity, Fragment fragment) {
        FragmentManager fm = activity.getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_container, fragment);
        ft.commit();
    }

    /**
     * Get current timestamp
     */
    public static String getTimestamp(final String TIMESTAMP_DATE, final String TIMESTAMP_TIME) {
        final String DATE = new SimpleDateFormat(TIMESTAMP_DATE, Locale.US).format(Calendar.getInstance().getTime());
        final String TIME = new SimpleDateFormat(TIMESTAMP_TIME, Locale.US).format(Calendar.getInstance().getTime());
        return DATE + " " + TIME;
    }

    /**
     * Converts any iterable list to a String object representing its components.
     * For Arrays, pass in using Arrays.asList(...)
     */
    public static <T> String iterableToString(Iterable<T> iterable) {
        StringBuilder result = new StringBuilder();
        for (T t : iterable)
            result.append(t).append(" ");
        return result.toString();
    }

    /**
     * Return a random element from iterable.
     * If remove == true, remove the extracted element from the iterable
     */
    public static <T> T chooseRandomFromIterable(Iterable<T> iterable, int size, boolean remove) {
        int randomIndex = new Random().nextInt(size);
        Iterator<T> it = iterable.iterator();
        T t = it.next(); // start from first element
        for (int i = 0; i < randomIndex; ++i)
            t = it.next();
        if (remove)
            it.remove();
        return t;
    }

	/**
	 * Return true if array contains element; false otherwise
	 */
	public static <T> boolean arrayContains(T[] array, T element) {
		for (T  t : array)
			if (t.equals(element))
				return true;
		return false;
	}

	/**
	 * Set activity background to match current theme, or reset to default background
	 */
	public static void setActivityBackground(Context context) {
		try {
			((Activity) context).findViewById(R.id.fragment_container).setBackground(StimuliManager.getInstance().getBackground());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
