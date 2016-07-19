package edu.uci.wmp.lineup;

/**
 * TODO:
 */
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import edu.uci.wmp.lineup.fragments.LevelFeedback;

public class StimuliManager {

    private static final StimuliManager INSTANCE = new StimuliManager();

    public static final int CORRECT = 1;
    public static final int INCORRECT = -1;
    public static final String MISC = "miscellaneous/";
	public static final String DEFAULT_THEME_NAME = "shapes";
	public static final int DEFAULT_THEME_SETS = 24;
	public static final int DEFAULT_THEME_STIMULI = 9;
    public static final int MIN_STIMULI_CHOICES = 1;
	public static final int MAX_STIMULI_SET = 16;
    public static final int TARGET_CODE = 0;
    public static final int LURE_CODE = 1;
    public static final int DISTRACTOR_CODE = 2;
	public static final int RP_LURE_CODE = 3;
	public static final int RANDOM_CODE = 4;

    private Context context;
	private String themeName = "";

	public int numberOfSetsInTheme;
	public int numberOfPicturesInSet;

    public StimuliManager() {

    }

    public void setContext(Context context) { this.context = context; }

    /**
     * @param labeledFileName folder label in first digit using static int from StimuliManager.java + filename
     */
    public Bitmap getStimuli(int labeledFileName) throws IOException {
        String path = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + getImagePath(labeledFileName);
        File imageFile = new File(path);
        InputStream is = new FileInputStream(imageFile);
        return BitmapFactory.decodeStream(is);
    }

    public Bitmap getFeedbackAsset(Context context, int result) throws IOException {
        AssetManager assetManager = context.getAssets();
        InputStream is = null;
        switch (result) {
            case CORRECT:
            case LevelFeedback.LEVEL_UP:
                is = assetManager.open("stimuli/" + MISC + "correct.png");
                break;

            case LevelFeedback.LEVEL_SAME:
                is = assetManager.open("stimuli/" + MISC + "neutral.png");
                break;

            case INCORRECT:
            case LevelFeedback.LEVEL_DOWN:
                is = assetManager.open("stimuli/" + MISC + "incorrect.png");
                break;
        }
        return BitmapFactory.decodeStream(is);
    }

//    public String getImagePath(String folder, int filename) { // DELETE THIS, JUST FOR REMOVING ERRORS FOR RUNNING
//        return "stimuli/" + folder + filename + ".png";
//    }

    public String getImagePath(String theme, String set, int filename) {
        return "stimuli/" + theme + set + filename + ".png";
    }

    public String getImagePath(int labeledFileName) {
        int picNum = labeledFileName % 100;
        int setNum = (labeledFileName - (picNum)) / 100;
        return "/wmplab/LineUp/stimuli/" + themeName + "/set" + setNum + "/" + picNum + ".png";
    }

	/**
	 * Check if given theme exists, set theme to default if not
	 * Configure number of sets in theme & number of stimuli in each set
	 */
	public void setTheme(String theme) {
		String stimPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/wmplab/LineUp/stimuli/";
		File themeFolder = new File(stimPath + theme);
		// set theme name
		if (themeFolder.exists())
			themeName = theme;
		else {
			Log.i("theme does not exist", "set to default theme");
			themeName = DEFAULT_THEME_NAME;
		}

		// set @numberOfSetsInTheme & @numberOfPicturesInSet
		File temp = new File(stimPath + themeName);
		numberOfSetsInTheme = temp.list().length;
		temp = new File(stimPath + themeName + "/set1");
		numberOfPicturesInSet = temp.list().length;

		Log.d("numberOfSetsInTheme", "" + numberOfSetsInTheme);
		Log.d("numberOfPicturesInSet", "" + numberOfPicturesInSet);
	}

    public static StimuliManager getInstance() { return INSTANCE; }
}
