package edu.uci.wmp.lineup;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import edu.uci.wmp.lineup.fragments.Settings;

/**
 * Created by ChanWoo on 6/21/2016
 * TODO:
 */
public class LevelManager {

    private static final LevelManager INSTANCE = new LevelManager();

    /** Constants */
    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 10;
    public static final int START_LEVEL = 1;
    public static final int DEMO_MAX_LEVELS = 3;            // demo mode plays only 3 levels
    public static final int STAGE1 = 1;                     // stage 1
    public static final int STAGE2 = 2;                     // stage 2
    public static final int STAGE0 = 0;                     // neither
    public static final String TRAININGMODE_LEVELS = "train_levels";
    public static final String TRAININGMODE_TIME = "train_time";
    public static final String TRAININGMODE_DEMO = "train_demo";
    private static final String SAVE_FOLDER_PATH = "/wmplab/LineUp/data/";
    public static final String SAVE_LEVEL_FILENAME = "_data.txt";
	private static final String THEME_ORDER_FILENAME = "/wmplab/LineUp/theme_order.txt";
    public static final String SHAREDPREF_KEY = "shared_pref";
	public static final int THEME_NOCHANGE = 0;

    private Context context;
    private Random random;

    public int screenWidth = 0;
    public int screenHeight = 0;

    public int subject = 1;
    public int session = 1;
    public int level = 1;                                   // current level
    public int levelsPlayed = 0;                            // cumulative number of levels played; if == sessionLevels: end game
	public int repetitions = 3;                             // number of rounds to play for current level
    public int round = 1;                                   // current round out of repetitions; if == repetitions: load next level
    public int roundsPlayedThisSession = 0;                 // equivalent of 'current index'
    public int wrongs = 0;                                  // number of rounds answered wrong out of repetitions; reset to 0 upon start of new level
    public int points = 0;
    public String trainingmode = TRAININGMODE_LEVELS;
    public int sessionLevels = 10;
    public int sessionLength = 300;
    public boolean questions = true;
	public int changeTheme = THEME_NOCHANGE;
	public List<String> themeOrder;
    public boolean debug = false;
    public boolean testStarted = false;
    public long sessionStartMills = 0;

	/** Level file variables */
    public String theme = StimuliManager.DEFAULT_THEME_NAME;
    public int setsize = 0;
    public int lurespartone = 0;
    public int nonlurespartone = 0;
    public int choices = 0;
    public List<Integer[]> responsechoice;
    public int presentationtimeperstimulus = 0;             // display time in mills per stimulus
	public int choicetimelimit = 0;                         // seconds for player to answer in stage 2
	public int goup = 1;                                    // max number of wrongs player can get to progress up a level
	public int godown = 2;                                  // min number of wrongs player can get to move down a level

    // Part I
    public List<Integer> stimuliSequence;                       // defines what stimuli have to be shown
    public List<Integer> firstPartTargetSets;                   // distinct sets that stimuli are chosen from
    public List<Integer> firstPartLureSets;                     // distinct sets that lures are chosen from (same shape as target, different color)

    // Part II
    public List<Integer> secondPartSequence;                    // keep track of sequence of buttons in second stage
	public List<Integer> secondPartPotentialTargets;            // collection of all possible target stimuli candidates (exactly same as presented)
	public List<Integer> secondPartPotentialLures;              // collection of all possible lure stimuli candidates (same shape, different color)
	public List<Integer> secondPartPotentialDistractors;        // collection of all possible distractor stimuli candidates (new shape, can have same color)
	public List<Integer> secondPartRPLures;                     //
	public int response;                                        // label of button the player has clicked
	public long reactionTime;                                   // reaction times of click
	public int accuracy;                                        // correct or incorrect answer for the level

    // -------------------------------------------------------------------------------------------

    public LevelManager() {
        random = new Random();
        level = START_LEVEL;
        round = 0;
        reset();
    }

    /**
     * Set pixel values for width & height.
     * Can be used only if context is given; thus not called in default constructor
     */
    public void setScreenDimensions() {
        WindowManager windowManager = (WindowManager) this.context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();

        // display size in pixels
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
        Log.d("Width", "" + screenWidth);
        Log.d("Height", "" + screenHeight);
    }

    public void reset() {
        stimuliSequence = new ArrayList<>();
        firstPartTargetSets = new ArrayList<>();
        firstPartLureSets = new ArrayList<>();
        secondPartSequence = new ArrayList<>();
        secondPartPotentialTargets = new ArrayList<>();
        secondPartPotentialLures = new ArrayList<>();
        secondPartPotentialDistractors = new ArrayList<>();
	    secondPartRPLures = new ArrayList<>();
	    responsechoice = new ArrayList<>();
	    themeOrder = new ArrayList<>();
    }

    /**
     * Setup LevelManager for new session
     */
    public void startSession() {
        stimuliSequence.clear();
        firstPartTargetSets.clear();
        firstPartLureSets.clear();
        secondPartSequence.clear();
        secondPartPotentialTargets.clear();
        secondPartPotentialLures.clear();
        secondPartPotentialDistractors.clear();
	    secondPartRPLures.clear();
	    responsechoice.clear();

        if (trainingmode.equals(TRAININGMODE_DEMO)) {
            loadLevel(START_LEVEL);
            sessionLevels = DEMO_MAX_LEVELS;
        }
        else
            loadSavedLevel(); // sets level variable if there is a saved instance

	    if (changeTheme != THEME_NOCHANGE)
		    readThemeOrder();

        sessionStartMills = SystemClock.uptimeMillis(); // record session starting time (used for trainingmode = "time")
        round = 0;
	    roundsPlayedThisSession = 0;
        levelsPlayed = 0;
        testStarted = true;
        points = 0; // reset score
        CSVWriter.getInstance().createCsvFile();
    }

    /**
     * Called at the beginning of a level
     */
    public void startLevel() {
	    responsechoice.clear();
	    repetitions = 0;
	    round = 1;
	    wrongs = 0;
	    loadLevel(level);
	    // limit to max stimuli
	    if (setsize > StimuliManager.MAX_STIMULI_SET)
		    setsize = StimuliManager.MAX_STIMULI_SET;
	    // setting theme from level file or auto change
	    if (changeTheme != THEME_NOCHANGE) {
		    int index = (roundsPlayedThisSession / changeTheme) % changeTheme;
		    theme = themeOrder.get(index);
		    Log.wtf("theme at index " + index, "set to " + theme);
	    }
	    StimuliManager.getInstance().setTheme(theme);
    }

    /**
     * Called at the beginning of a trial
     */
    public void startRound() {
        stimuliSequence.clear();
        firstPartTargetSets.clear();
        firstPartLureSets.clear();
        secondPartSequence.clear();
        secondPartPotentialTargets.clear();
        secondPartPotentialLures.clear();
        secondPartPotentialDistractors.clear();
    }

    public void loadLevel(int level) {
        try {
            setLevel(level);
            BufferedReader br = openFileAsReader();

            String line;
            while ((line = br.readLine()) != null) {
                Log.d("loadLevel()", line);
                processLine(line);
            }
        } catch (InvalidLevelException e) {
            Log.e("loadLevel()", "Invalid level");
        } catch (IOException e) {
            Log.e("loadLevel()", "IO exception");
        } catch (NoSuchFieldException e) {
            Log.e("loadLevel()", "NoSuchField exception");
        } catch (IllegalAccessException e) {
            Log.e("loadLevel()", "IllegalAccessException");
        }
    }

    /**
     * Sets level variable if there is a saved instance. If none, sets it to startlevel
     */
    public void loadSavedLevel() {
        try {
            File root = android.os.Environment.getExternalStorageDirectory();
            BufferedReader reader = new BufferedReader(new FileReader(root.getAbsolutePath() + SAVE_FOLDER_PATH + subject + SAVE_LEVEL_FILENAME));
            String savedLevel = reader.readLine();
            level = Integer.valueOf(savedLevel);
            Log.i("loadSavedLevel()", "Loaded level " + savedLevel);
        } catch (FileNotFoundException e) {
            Log.e("loadSavedLevel()", "No save file found, setting to level " + START_LEVEL);
            level = START_LEVEL;
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("loadSavedLevel()", "Error loading save, setting to level " + START_LEVEL);
            level = START_LEVEL;
            e.printStackTrace();
        }

    }

    /**
     * Save final level user has reached/will continue from.
     * If sessionFin == true, user has correctly completed the session and will continue onwards from the last level + 1 unless at level 30.
     * ELse if sessionFin == false, user has aborted the game and will have to continue from level - 1 unless at level 1.
     */
    public void saveLevelToFile(boolean sessionFin) {
        try {
            File root = android.os.Environment.getExternalStorageDirectory();
            File saveFolder = new File (root.getAbsolutePath() + SAVE_FOLDER_PATH);
            if (!saveFolder.exists())
                Log.i("saveLevelToFile()", "Save folder created " + saveFolder.mkdirs());
            File saveFile = new File (saveFolder, subject + SAVE_LEVEL_FILENAME);
            FileWriter fw = new FileWriter(saveFile, false);
            BufferedWriter writer = new BufferedWriter(fw);
            if (sessionFin)
                writer.write(Integer.toString(level));
            else { // 1 level down
                if (level > 1)
                    writer.write(Integer.toString(--level));
                else
                    writer.write(Integer.toString(level));
            }
            Log.i("saveLevelToFile()", "Saved on level " + level);
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

	/**
	 * Fill themeOrder with list of themes specified in theme_order.txt in root wmp directory
	 */
	private void readThemeOrder() {
		try {
			File root = android.os.Environment.getExternalStorageDirectory();
			BufferedReader reader = new BufferedReader(new FileReader(root.getAbsolutePath() + THEME_ORDER_FILENAME));

			String line;
			while ((line = reader.readLine()) != null) {
				Log.d("readThemeOrder()", line);
				if (StimuliManager.hasTheme(line))
					themeOrder.add(line);
			}
		} catch (FileNotFoundException e) {
			Log.e("readThemeOrder()", "No theme order file found");
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("readThemeOrder()", "Error reading theme order file");
			e.printStackTrace();
		}
	}

    public void setContext(Context context) { this.context = context; }

    /**
     * Store selected variables into preferences
     */
    public void saveSharedPreferences() {
        SharedPreferences sharedPref = context.getSharedPreferences(SHAREDPREF_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(Settings.SUBJECT_KEY, subject);
        editor.putInt(Settings.SESSION_KEY, session);
	    editor.putBoolean(Settings.QUESTIONS_KEY, questions);
	    editor.putInt(Settings.CHANGETHEME_KEY, changeTheme);
        editor.putBoolean(Settings.DEBUG_KEY, debug);
        editor.putString(Settings.TRAININGMODE_KEY, trainingmode);
        editor.putInt(Settings.ROUNDS_KEY, sessionLevels);
        editor.putInt(Settings.SESSIONLENGTH_KEY, sessionLength);
        editor.apply();
    }

    /**
     * Read variables from preferences
     */
    public void readSharedPreferences() {
        SharedPreferences sharedPref = context.getSharedPreferences(SHAREDPREF_KEY, Context.MODE_PRIVATE);
        subject = sharedPref.getInt(Settings.SUBJECT_KEY, 1);
        session = sharedPref.getInt(Settings.SESSION_KEY, 1);
        questions = sharedPref.getBoolean(Settings.QUESTIONS_KEY, true);
	    changeTheme = sharedPref.getInt(Settings.CHANGETHEME_KEY, LevelManager.THEME_NOCHANGE);
        debug = sharedPref.getBoolean(Settings.DEBUG_KEY, true);
        trainingmode = sharedPref.getString(Settings.TRAININGMODE_KEY, TRAININGMODE_LEVELS);
        sessionLevels = sharedPref.getInt(Settings.ROUNDS_KEY, 10);
        sessionLength = sharedPref.getInt(Settings.SESSIONLENGTH_KEY, 300);
    }

    public void setLevel(int newLevel) throws InvalidLevelException {
        if (newLevel < MIN_LEVEL || newLevel > MAX_LEVEL) {
            throw new InvalidLevelException("Level must be " + MIN_LEVEL + " <= n <= " + MAX_LEVEL);
        }
        level = newLevel;
    }

    public class InvalidLevelException extends Exception {
        public InvalidLevelException(String message) {
            super(message);
        }
    }

    public String getLevelFilePath() { return "/wmplab/LineUp/levels/level" + level + ".txt"; }

    public BufferedReader openFileAsReader() throws IOException {
        String path = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + getLevelFilePath();
        File levelFile = new File(path);
        InputStream is = new FileInputStream(levelFile);
        return new BufferedReader(new InputStreamReader(is));
    }

    /**
     * Set value of variable through reflection
     * @param varName name of variable to modify
     * @throws NoSuchFieldException
     */
    private void setLevelVariable(String varName, String newValue) throws NoSuchFieldException, IllegalAccessException {

        if (CSVWriter.getInstance().canIgnore(varName)) // ignore certain fields in level file
            return;

	    if (varName.equals("responsechoice")) { // set responsechoice variable manually
		    String[] respArrays = newValue.split(";");
		    for (String array : respArrays)
		        processResponseChoiceArray(array.trim());
		    Collections.shuffle(responsechoice); // random orders

		    if (Util.arrayContains(responsechoice.get(0), StimuliManager.RP_LURE_CODE)) // first trial cannot have rp lures
		    {
			    int indexOfNoRP = 0; // find index of an array that doesn't have rp lures, swap with first
			    for (int i = 1; i < responsechoice.size(); ++i)
				    if (!Util.arrayContains(responsechoice.get(i), StimuliManager.RP_LURE_CODE)) {
					    indexOfNoRP = i;
					    break;
				    }
			    Collections.swap(responsechoice, 0, indexOfNoRP);
		    }
		    return;
	    }

        Field var = this.getClass().getDeclaredField(varName);

        // check field type and assign correct value
        if (var.getType().getName().equals("int")) {
            var.setInt(this, Integer.valueOf(newValue));
//            Log.i("typecheck", "Integer");
        }
        else if (var.getType().getName().equals("java.lang.String")) {
            var.set(this, newValue);
//            Log.i("typecheck", "String");
        }
        else if (var.getType().getName().equals("boolean")) {
            var.setBoolean(this, newValue.equals("1"));
//            Log.i("typecheck", "Boolean");
        }
        else if (var.getType().getName().equals("double")) {
            var.setDouble(this, Double.valueOf(newValue));
//            Log.i("typecheck", "Double");
        }
//        else if (var.getType().isArray() && var.getType().getComponentType().getName().equals("int")) { // checking for array of type int
//            var.set(this, convertToIntArray(newValue));
//            Log.i("typecheck", "int Array");
//        }
    }

    private void processLine(String s) throws NoSuchFieldException, IllegalAccessException {
        int i = s.indexOf("=");
        if (i != -1)
            setLevelVariable(s.substring(0, i), s.substring(i + 1));
    }

	/**
	 * Fill responsechoice with given sequence starting at second element
	 * Insert sequence by first element number of times
	 * @param array String representation of one segment array (e.g. "[5,0,1]", adds [0,1] 5 times)
	 */
    private void processResponseChoiceArray(String array) {
	    Log.wtf("processing array", array);
        int[] translatedArray = new int[array.split(",").length];
        String[] arrayContents = array.substring(array.indexOf('[') + 1, array.indexOf(']')).split(",");
	    for (int i = 0; i < translatedArray.length; ++i)
		    translatedArray[i] = Integer.valueOf(arrayContents[i]);

	    // get first element of translatedArray, add 'sub-array' by that number of times to responsechoice
	    int multiply = translatedArray[0];
	    repetitions += multiply; // elements add up to repetitions in level
	    Integer[] theActualSequence = new Integer[choices];
	    if (translatedArray[1] == StimuliManager.RANDOM_CODE) { // randomize theActualSequence
		    for (int i = 0; i < multiply; ++i) {
			    responsechoice.add(generateRandomChoiceArray(choices));
		    }
	    }
	    else { // read second element onwards
		    for (int j = 1; j < translatedArray.length; ++j) {
			    theActualSequence[j - 1] = translatedArray[j];
		    }
		    for (int i = 0; i < multiply; ++i)
			    responsechoice.add(theActualSequence);
	    }

	    /*
        // check for RANDOM_CODE in arrayContents
        // if yes generate random classes, if not fill array with given contents
        if (arrayContents.length == 1 && arrayContents[0].equals(String.valueOf(StimuliManager.RANDOM_CODE))) // responsechoices == [3]
            for (int i = 0; i < choices; ++i)
                result[i] = random.nextInt(StimuliManager.RANDOM_CODE);
        else
            for (int i = 0; i < arrayContents.length; ++i)
                result[i] = Integer.valueOf(arrayContents[i].trim());
        /*
         * This is the solution for if 3's aren't always stored alone as responsechoice.
         * e.g. [0, 1, 3] is legal.
         *
        // change all 'RANDOM_CODE's in responsechoice to random TARGET, LURE, or DISTRACTOR CODES
        // the switch case below will process the random case as one of the three classes
        for (int i = 0; i < responsechoice.length; ++i) {
            if (responsechoice[i] == StimuliManager.RANDOM_CODE)
                responsechoice[i] = random.nextInt(StimuliManager.DISTRACTOR_CODE + 1);
        } */
    }

	private Integer[] generateRandomChoiceArray(int size) {
		Integer[] result = new Integer[size];
		for (int i = 0; i < size; ++i)
			result[i] = random.nextInt(StimuliManager.RANDOM_CODE);
		return result;
	}

	/**
	 * Generate random rp rounds leaving first round as a non-rp round
	 */
//	private void generateRPRounds() {
//		if (repetitions < 2)
//			return; // rp rounds require at least 2 repetitions
//		rpRoundsOrder.clear();
//		for (int i = 0; i < rprounds; ++i)
//			rpRoundsOrder.add(RP_ROUND);
//		for (int i = 1; i < repetitions - rprounds; ++i) // start i at 1 to leave first round (a definite NON_RP_ROUND)
//			rpRoundsOrder.add(NON_RP_ROUND);
//		Collections.shuffle(rpRoundsOrder);
//		rpRoundsOrder.add(0, NON_RP_ROUND); // add non-rp round at first, shift shuffled rounds to the right
//	}

    public void generateStimuliFirstPart() {
        List<Integer> sets = new ArrayList<>(); // temporary list holding all set numbers
        for (int i = 1; i <= StimuliManager.getInstance().numberOfSetsInTheme; ++i)
            sets.add(i);

        // choose non-lures first
        for (int i = 0; i < nonlurespartone; ++i) {
            int randomSetIndex = random.nextInt(sets.size());
            firstPartTargetSets.add(sets.get(randomSetIndex)); // add randomly chosen set number as target set
            sets.remove(randomSetIndex);
//            firstPartTargetSets.add(Util.chooseRandomFromIterable(sets, numberOfSetsInTheme, true));
        }
        Log.i("firstPartTargetSets", Util.iterableToString(firstPartTargetSets));

        // choose lures from among target sets
        for (int i = 0; i < lurespartone; ++i) {
            int randomSetIndex = random.nextInt(firstPartTargetSets.size()); // choosing lure from one of the target sets
            firstPartLureSets.add(firstPartTargetSets.get(randomSetIndex));
	        // TODO: remove here? or no? (more than 1 lure of same kind allowed?)
//            firstPartLureSets.add(Util.chooseRandomFromIterable(firstPartTargetSets, nonlurespartone, false));
        }
        Log.i("firstPartLureSets", Util.iterableToString(firstPartLureSets));

        // populate stimuliSequence
        // targets
        for (int setNum : firstPartTargetSets)
        {
            int picNum, labeledStimulus;
            do {
                picNum = random.nextInt(StimuliManager.getInstance().numberOfPicturesInSet) + 1; // choose random pic in set
                labeledStimulus = 100 * setNum + picNum; // 101 == set 1, pic 1
            }
            while (stimuliSequence.contains(labeledStimulus) // prevent duplicates
		            || secondPartRPLures.contains(labeledStimulus)); // rp lures cannot be shown in stage 1 of next round
	        Log.i("generateStimuli()", "Adding target" + labeledStimulus);
            stimuliSequence.add(labeledStimulus);
        }
        // lures
        for (int setNum : firstPartLureSets)
        {
            int picNum, labeledStimulus;
            do {
                picNum = random.nextInt(StimuliManager.getInstance().numberOfPicturesInSet) + 1;
                labeledStimulus = 100 * setNum + picNum;
            }
            while (stimuliSequence.contains(labeledStimulus)
		            || secondPartRPLures.contains(labeledStimulus)); // TODO: combine this part with above, exactly the same
	        Log.i("generateStimuli()", "Adding lure" + labeledStimulus);
            stimuliSequence.add(labeledStimulus);
        }
        Collections.shuffle(stimuliSequence);
    }

	/**
	 * If current round is an rp round, generate 1 (and only one) rp lure. Remaining buttons are randomly generated
	 * Else, follow as responsechoice specifies
	 */
    public void generateStimuliSecondPart() {

	    setupPotentialStructures();

//	    if (rpRoundsOrder.get(round - 1) == RP_ROUND) { // current round is rp round
//		    // generate 1 rp lure
//		    int randomRPLure = secondPartRPLures.get(random.nextInt(secondPartRPLures.size()));
//		    secondPartSequence.add(randomRPLure);
//
//		    // generate randoms for remaining buttons
//		    for (int i = 1; i < choices; ++i) {
//			    int randomClass = random.nextInt(StimuliManager.RANDOM_CODE);
//			    secondPartSequence.add(getStimulusFromPotentialStructure(randomClass));
//		    }
//		    return;
//	    }

	    // current round is non-rp round
	    for (int classNum : responsechoice.get(round - 1))
		    secondPartSequence.add(getStimulusFromPotentialStructure(classNum));
    }

	private int getStimulusFromPotentialStructure(int classNum) {
		switch (classNum) {
			case StimuliManager.TARGET_CODE:
				return Util.chooseRandomFromIterable(secondPartPotentialTargets, secondPartPotentialTargets.size(), true);
			case StimuliManager.LURE_CODE:
				return Util.chooseRandomFromIterable(secondPartPotentialLures, secondPartPotentialLures.size(), true);
			case StimuliManager.DISTRACTOR_CODE:
				return Util.chooseRandomFromIterable(secondPartPotentialDistractors, secondPartPotentialDistractors.size(), true);
			case StimuliManager.RP_LURE_CODE:
				return Util.chooseRandomFromIterable(secondPartRPLures, secondPartRPLures.size(), true);
			default:
				return 0;
		}
	}

	private void setupPotentialStructures() {
		// fill potential targets
		secondPartPotentialTargets.addAll(stimuliSequence); // potential targets is same as stimuli sequence
		for (int rplure : secondPartRPLures) // rp lures cannot be potential targets
			if (secondPartPotentialTargets.contains(rplure))
				secondPartPotentialTargets.remove(secondPartPotentialTargets.indexOf(rplure));

		// fill potential lures
		for (int targetSet : firstPartTargetSets)
		{
			for (int picNum = 1; picNum <= StimuliManager.getInstance().numberOfPicturesInSet; ++picNum) // among all stimuli from all target sets
			{
				int potentialLure = 100 * targetSet + picNum;
				if (!secondPartPotentialTargets.contains(potentialLure)) // if stimulus appeared as a target it's not a lure
					secondPartPotentialLures.add(potentialLure);
			}
		}
		for (int rplure : secondPartRPLures) // rp lures cannot be potential lures
			if (secondPartPotentialLures.contains(rplure))
				secondPartPotentialLures.remove(secondPartPotentialLures.indexOf(rplure));

		// fill potential distractors
		for (int setNum = 1; setNum <= StimuliManager.getInstance().numberOfSetsInTheme; ++setNum)
		{
			if (!firstPartTargetSets.contains(setNum)) { // if a new shape
				for (int picNum = 1; picNum <= StimuliManager.getInstance().numberOfPicturesInSet; ++picNum) // add all colors of the new shape
				{
					int potentialDistractor = 100 * setNum + picNum;
					secondPartPotentialDistractors.add(potentialDistractor);
				}
			}
		}
		for (int rplure : secondPartRPLures) // rp lures cannot be potential distractors
			if (secondPartPotentialDistractors.contains(rplure))
				secondPartPotentialDistractors.remove(secondPartPotentialDistractors.indexOf(rplure));
	}

	/**
	 * Push previous round's stimuli as potential four lures
	 * Thus, this method is called before modifications are made to stimuliSequence
	 */
	public void fillPotentialRPLures() {
		secondPartRPLures.clear();
		secondPartRPLures.addAll(stimuliSequence);
	}

    /**
     * Displays loaded variables to Logcat for debug purposes
     */
    public void logVariables() {

    }

    public static LevelManager getInstance() { return INSTANCE; }
}
