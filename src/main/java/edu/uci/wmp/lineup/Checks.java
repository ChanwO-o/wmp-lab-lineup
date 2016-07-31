package edu.uci.wmp.lineup;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Checks {
    private static final Checks INSTANCE = new Checks();

    public static final String LEVELFOLDER_PATH = "/wmplab/LineUp/levels/";
	public static final String STIMULIFOLDER_PATH = "/wmplab/LineUp/stimuli/";
	public static final String FEEDBACKFOLDER_PATH = "/wmplab/LineUp/feedback/";
    public static final String STIMULI_THEME1_PATH = "/wmplab/LineUp/stimuli/" + StimuliManager.DEFAULT_THEME_NAME + "/";

	static final String[] feedbackFiles = new String[] {"roundfeedback_down.txt", "roundfeedback_same.txt", "roundfeedback_up.txt"};

    private StringBuilder errorMessages = new StringBuilder();
    private Context context;

    public Checks() { }

    public class InvalidLevelFilesException extends IOException {}
	public class InvalidStimuliFilesException extends IOException {}
	public class InvalidFeedbackFilesException extends IOException {}

    public void setContext(Context context) { getInstance().context = context; }

    /**
     * Check Levels directory and stimuli directory, fire toast message of all missing directories and files
     */
    public boolean runAllChecks() {
        boolean checkPass = true;
        try { Checks.getInstance().checkLevelsDirectory(); }
        catch (Checks.InvalidLevelFilesException e) {
            Toast.makeText(getInstance().context, "Error checking level files", Toast.LENGTH_SHORT).show();
            Toast.makeText(getInstance().context, errorMessages.toString(), Toast.LENGTH_SHORT).show();
            checkPass = false;
        }
        errorMessages.setLength(0); // clear error message queue

        try { Checks.getInstance().checkStimuliDirectory(); }
        catch (Checks.InvalidStimuliFilesException e) {
            Toast.makeText(getInstance().context, "Error checking stimuli files", Toast.LENGTH_SHORT).show();
            Toast.makeText(getInstance().context, errorMessages.toString(), Toast.LENGTH_SHORT).show();
            checkPass = false;
        }
        errorMessages.setLength(0);

	    try { Checks.getInstance().checkFeedbackDirectory(); }
	    catch (Checks.InvalidFeedbackFilesException e) {
		    Toast.makeText(getInstance().context, "Error checking feedback files", Toast.LENGTH_SHORT).show();
		    Toast.makeText(getInstance().context, errorMessages.toString(), Toast.LENGTH_SHORT).show();
		    checkPass = false;
	    }
	    errorMessages.setLength(0);

        return checkPass;
    }

    /**
     * Populate all level & stimuli files, return true if successful
     */
    public boolean populateAssets() {
        try {
//	        populateFoldersAndThemeOrder();
            populateLevelDirectory();
            populateStimuliDirectory();
	        populateFeedbackDirectory();
            return true;
        }
        catch (Checks.InvalidLevelFilesException e) {
            Toast.makeText(getInstance().context, "Error populating level files", Toast.LENGTH_SHORT).show();
            return false;
        }
        catch (Checks.InvalidStimuliFilesException e) {
	        Toast.makeText(getInstance().context, "Error populating stimuli files", Toast.LENGTH_SHORT).show();
	        return false;
        }
        catch (Checks.InvalidFeedbackFilesException e) {
	        Toast.makeText(getInstance().context, "Error populating feedback files", Toast.LENGTH_SHORT).show();
	        return false;
        }
    }

    /**
     * Checks Levels directory for missing files
     * Append missing file names or directories to error message queue
     */
    public void checkLevelsDirectory() throws InvalidLevelFilesException {
        File root = android.os.Environment.getExternalStorageDirectory();
        String outLevelFolderPath = root.getAbsolutePath() + LEVELFOLDER_PATH;
        File outLevelFolder = new File(outLevelFolderPath);

        if (!outLevelFolder.exists()) { // no dir; don't bother looking through level files
            Log.e("checkLevelsDirectory()", "Level Folder does not exist");
            errorMessages.append("Level folder does not exist\n");
            throw new InvalidLevelFilesException();
        }

        String[] levelFiles = outLevelFolder.list();
        Set<Integer> checkedLevels = new HashSet<>();

        for (String level : levelFiles) {
            if (level.length() != 10 && level.length() != 11)               // level file names are 10 or 11 characters long
                continue;
            if (!(level.startsWith("level") && level.endsWith(".txt")))     // level file names start and end with "level", ".txt"
                continue;
            int numStartIndex = 5;
            int numEndIndex = level.indexOf(".txt");
            int levelNumber = Integer.valueOf(level.substring(numStartIndex, numEndIndex)); // get level number
            if (levelNumber < LevelManager.MIN_LEVEL || LevelManager.MAX_LEVEL < levelNumber)   // level file's number must be in range
                continue;
            checkedLevels.add(levelNumber);
        }

        for (int i = LevelManager.MIN_LEVEL; i <= LevelManager.MAX_LEVEL; ++i) {
            if (!checkedLevels.contains(i)) {
                String missingMsg = "level" + i + ".txt is missing\n";
                errorMessages.append(missingMsg);
            }
        }
        if (errorMessages.length() != 0)
            throw new InvalidLevelFilesException();
    }

    /**
     * Checks Stimuli directory for missing files
     * Append missing file names or directories to error message queue
     */
    public void checkStimuliDirectory() throws InvalidStimuliFilesException {
        String outStimuliFolderPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + STIMULIFOLDER_PATH;
	    String defaultThemeFolderPath = outStimuliFolderPath + StimuliManager.DEFAULT_THEME_NAME;
        File outStimuliFolder = new File(outStimuliFolderPath);
        File defaultThemeFolder = new File(defaultThemeFolderPath);

        if (!outStimuliFolder.exists()) { // no dir; don't bother looking through stimuli files
            Log.e("checkStimuliDirectory()", "Stimuli Folder does not exist");
            errorMessages.append("Stimuli folder does not exist\n");
            throw new InvalidStimuliFilesException();
        }
	    if (!defaultThemeFolder.exists()) { // no default theme folder
		    Log.e("checkStimuliDirectory()", "Default theme " + StimuliManager.DEFAULT_THEME_NAME + " does not exist");
		    errorMessages.append("Default theme does not exist");
		    throw new InvalidStimuliFilesException();
	    }

        for (int s = 1; s <= StimuliManager.DEFAULT_THEME_SETS; ++s) // check each stimuli folder one by one
        {
	        Log.i("checking set", "" + s);
	        File setFolder = new File(defaultThemeFolderPath + "/set" + s);
            if (!setFolder.exists()) {
	            Log.i("set" + s, "does not exist");
                String missingFolderMsg = setFolder.getName() + " folder missing\n";
                errorMessages.append(missingFolderMsg);
                continue;
            }

            String[] stimuliFiles = setFolder.list();
            Set<Integer> checkedStimuli = new HashSet<>();

            for (String stimuli : stimuliFiles) {
                if (stimuli.length() != 5 && stimuli.length() != 6)         // stimuli file names are 5 or 6 characters long
                    continue;
                if (!stimuli.endsWith(".png"))                              // stimuli file names end with ".png"
                    continue;
                int numEndIndex = stimuli.indexOf(".png");
                int stimuliNumber = Integer.valueOf(stimuli.substring(0, numEndIndex)); // get stimuli number
                if (stimuliNumber < StimuliManager.MIN_STIMULI_CHOICES || StimuliManager.DEFAULT_THEME_STIMULI < stimuliNumber)   // stimuli file's number must be in range
                    continue;
                checkedStimuli.add(stimuliNumber);
            }

            for (int i = StimuliManager.MIN_STIMULI_CHOICES; i <= StimuliManager.DEFAULT_THEME_STIMULI; ++i) {
                if (!checkedStimuli.contains(i)) {
                    String missingMsg = setFolder.getName() + "/" + i + ".png is missing\n";
                    errorMessages.append(missingMsg);
                }
            }
        }
        if (errorMessages.length() != 0)
            throw new InvalidStimuliFilesException();
    }

	/**
	 * Checks feedback directory for missing files
	 * Append missing file names or directories to error message queue
	 */
	public void checkFeedbackDirectory() throws InvalidFeedbackFilesException {
		File root = android.os.Environment.getExternalStorageDirectory();
		String outFeedbackFolderPath = root.getAbsolutePath() + FEEDBACKFOLDER_PATH;
		File outFeedbackFolder = new File(outFeedbackFolderPath);

		if (!outFeedbackFolder.exists()) {
			Log.e("checkFeedbackDir()", "Feedback Folder does not exist");
			errorMessages.append("Feedback folder does not exist\n");
			throw new InvalidFeedbackFilesException();
		}
		List<String> existingFeedbacks = Arrays.asList(outFeedbackFolder.list());
		for (String fname : feedbackFiles) {
			if (!existingFeedbacks.contains(fname)) {
				String missingMsg = fname + " is missing\n";
				errorMessages.append(missingMsg);
			}
		}
		if (errorMessages.length() != 0)
			throw new InvalidFeedbackFilesException();
	}

    public void populateLevelDirectory() throws InvalidLevelFilesException {
        File root = android.os.Environment.getExternalStorageDirectory();
        String outLevelFolderPath = root.getAbsolutePath() + LEVELFOLDER_PATH;
        File outLevelFolder = new File(outLevelFolderPath);
        outLevelFolder.mkdirs();

        try { copyDirectory("levels/", outLevelFolderPath); }
        catch (IOException e) { throw new InvalidLevelFilesException(); }
    }

    public void populateStimuliDirectory() throws InvalidStimuliFilesException {
        File root = android.os.Environment.getExternalStorageDirectory();
        String defaultThemeFolderPath = root.getAbsolutePath() + STIMULI_THEME1_PATH;
        File defaultThemeFolder = new File(defaultThemeFolderPath);
	    defaultThemeFolder.mkdirs();

        try {
	        // fill sets
            String[] setPaths = new String[StimuliManager.DEFAULT_THEME_SETS];
            for (int i = 1; i <= StimuliManager.DEFAULT_THEME_SETS; ++i) {
	            setPaths[i - 1] = "set" + i + "/";
            }
            for (String set : setPaths) {
                String setFolderPath = defaultThemeFolderPath + set;
                File setFolder = new File(setFolderPath);
	            Log.i("populateStimuliDir()", "folder " + setFolderPath + setFolder.mkdirs());
                copyDirectory("stimuli/" + StimuliManager.DEFAULT_THEME_NAME + "/" + set, setFolderPath);
            }
	        // copy background
	        File backgroundFile = new File(defaultThemeFolderPath, StimuliManager.BACKGROUND_FILENAME);
	        InputStream is = context.getAssets().open("stimuli/" + StimuliManager.DEFAULT_THEME_NAME + "/" + StimuliManager.BACKGROUND_FILENAME);
	        OutputStream os = new FileOutputStream(backgroundFile);
	        copyFile(is, os);
        }
        catch (IOException e) { e.printStackTrace(); throw new InvalidStimuliFilesException(); }
    }

	public void populateFeedbackDirectory() throws InvalidFeedbackFilesException {
		File root = android.os.Environment.getExternalStorageDirectory();
		String outFeedbackFolderPath = root.getAbsolutePath() + FEEDBACKFOLDER_PATH;
		File outFeedbackFolder = new File(outFeedbackFolderPath);
		outFeedbackFolder.mkdirs();

		try { copyDirectory("feedback/", outFeedbackFolderPath); }
		catch (IOException e) { throw new InvalidFeedbackFilesException(); }
	}

    private void copyDirectory(String assetFolderPath, String outFolderPath) throws IOException {
	    Log.i("copyDirectory()", assetFolderPath + " -> " + outFolderPath);
        String trimmedAssetFolderPath = assetFolderPath.substring(0, assetFolderPath.length() - 1);
        Log.i("copyDirectory()", "trimmedAssetFolderPath " + trimmedAssetFolderPath);
        AssetManager am = context.getAssets();
        InputStream in;
        OutputStream out;

        for (String filename : am.list(trimmedAssetFolderPath)) {
            Log.d("reading asset", filename);
            in = am.open(assetFolderPath + filename);
            File outFile = new File(outFolderPath, filename);
            out = new FileOutputStream(outFile);
            copyFile(in, out);
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
	    in.close();
	    out.flush();
	    out.close();
    }

    public static Checks getInstance() {
        return INSTANCE;
    }
}
