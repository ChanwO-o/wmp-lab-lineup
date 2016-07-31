package edu.uci.wmp.lineup;

/**
 * TODO:
 */
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.uci.wmp.lineup.fragments.LevelFeedback;

public class StimuliManager {

    private static final StimuliManager INSTANCE = new StimuliManager();

    public static final int CORRECT = 1;
    public static final int INCORRECT = -1;
	public static final String WMP_STIMULI_PATH = "/wmplab/LineUp/stimuli/";
	public static final String MISC = "miscellaneous/";
	public static final String BACKGROUND_FILENAME = "background.jpeg";
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

	/**
	 * Scale down and return bitmap to fit screen, prevent OutOfMemoryError
	 */
	public Drawable getBackground() throws IOException {
		int part = LevelManager.getInstance().part;
		int reqWidth = LevelManager.getInstance().screenWidth;
		int reqHeight = LevelManager.getInstance().screenHeight;
		Bitmap bitmap;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true; // First decode with inJustDecodeBounds=true to check dimensions
		switch (part) {
			case LevelManager.MAINSCREEN:
				BitmapFactory.decodeResource(context.getResources(), R.drawable.mainscreen_lineup, options);
				options = getBitOptionsForDecodingSampleBitmap(options, reqWidth, reqHeight);
				bitmap =  BitmapFactory.decodeResource(context.getResources(), R.drawable.mainscreen_lineup, options);
				break;
//				return ResourcesCompat.getDrawable(context.getResources(), R.drawable.mainscreen_lineup, null);
			case LevelManager.GETREADY: // GAME
				String path = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + WMP_STIMULI_PATH + LevelManager.getInstance().theme + "/" + BACKGROUND_FILENAME;
				File backgroundFile = new File(path);
				if (backgroundFile.exists()) {
					BitmapFactory.decodeFile(path, options);
					options = getBitOptionsForDecodingSampleBitmap(options, reqWidth, reqHeight);
					bitmap = BitmapFactory.decodeFile(path, options);
				}
				else { // some themes may not have background file; in this case, load default background
					BitmapFactory.decodeResource(context.getResources(), R.drawable.background, options);
					options = getBitOptionsForDecodingSampleBitmap(options, reqWidth, reqHeight);
					bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.background, options);
				}
				break;
//				return Drawable.createFromPath(path);
			default:
				BitmapFactory.decodeResource(context.getResources(), R.drawable.background, options);
				options = getBitOptionsForDecodingSampleBitmap(options, reqWidth, reqHeight);
				bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.background, options);
//				return ResourcesCompat.getDrawable(context.getResources(), R.drawable.background, null);
		}
//		Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
		return new BitmapDrawable(context.getResources(), Bitmap.createScaledBitmap(bitmap, LevelManager.getInstance().screenWidth, LevelManager.getInstance().screenHeight, true));
	}

	private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			final int halfHeight = height / 2;
			final int halfWidth = width / 2;
			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth)
				inSampleSize *= 2;
		}
		return inSampleSize;
	}

	private BitmapFactory.Options getBitOptionsForDecodingSampleBitmap(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return options;
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

    public String getImagePath(String theme, String set, int filename) {
        return "stimuli/" + theme + set + filename + ".png";
    }

    public String getImagePath(int labeledFileName) {
        int picNum = labeledFileName % 100;
        int setNum = (labeledFileName - (picNum)) / 100;
        return WMP_STIMULI_PATH + LevelManager.getInstance().theme + "/set" + setNum + "/" + picNum + ".png";
    }

	public static boolean hasTheme(String theme) {
		String stimPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + WMP_STIMULI_PATH;
		File themeFolder = new File(stimPath + theme);
		return themeFolder.exists();
	}

    public static StimuliManager getInstance() { return INSTANCE; }
}
