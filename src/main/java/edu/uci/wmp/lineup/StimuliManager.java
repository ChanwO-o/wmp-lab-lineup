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
    public static final String TARGET = "list1/";
    public static final String SEMANTIC = "list2/";
    public static final String PERCEPTUAL = "list3/";
    public static final String DISTRACTOR = "distractors/";
    public static final String MISC = "miscellaneous/";
    public static final String FACE = "faces/";
    public static final int MIN_STIMULI_CHOICES = 1;
    public static final int MAX_STIMULI_CHOICES = 12;
    public static final int TARGET_CODE = 0;
    public static final int LURE_CODE = 1;
    public static final int DISTRACTOR_CODE = 2;
    public static final int RANDOM_CODE = 3;
    public static final int MIN_CHOICE_STIMULI_SIZE = 100;
    public static final int CHOICE_STIMULI_SIZE_MULTIPLIER = 25;

    private Context context;

//    public static final ArrayList<Integer> TEMPLATE = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));

    public StimuliManager() {

    }

    public void setContext(Context context) { this.context = context; }

    /**
     * @param folder using static string from StimuliManager.java
     * @param filename just the filename only (e.g. 1)
     */
//    public Bitmap getStimuli(Context context, String folder, int filename) throws IOException {
//        AssetManager assetManager = context.getAssets();
//        InputStream is = assetManager.open(getImagePath(folder, filename));
//        return BitmapFactory.decodeStream(is);
//    }

    /**
     * @param labeledFileName folder label in first digit using static int from StimuliManager.java + filename
     */
    public Bitmap getStimuli(int labeledFileName) throws FileNotFoundException, IOException {
        AssetManager assetManager = context.getAssets();
        InputStream is = assetManager.open(getImagePath(labeledFileName));
//        String path = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + ;
//        File imageFile = new File(path);
//        InputStream in = new FileInputStream(imageFile);
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
        return "stimuli/geometry/set " + setNum + "/" + picNum + ".png";
    }

    public static StimuliManager getInstance() { return INSTANCE; }
}
