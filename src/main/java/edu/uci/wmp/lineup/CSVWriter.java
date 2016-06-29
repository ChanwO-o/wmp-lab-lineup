package edu.uci.wmp.lineup;

import android.content.Context;
import android.util.Log;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CSVWriter {
    public static final CSVWriter INSTANCE = new CSVWriter();

    private static final String[] ignoreFields = {};
    private static final String FOLDERPATH = "/wmplab/LineUp/csvdata";
    private static final String COMMA = ", ";
    private static final String NEW_LINE = "\n ";
    private static final String NULL = "na ";
    private static final String LAST_FIELD = "accuracy";
    private static final String FORMAT_DATE = "yyyy_MM_dd";
    private static final String FORMAT_TIME = "HHmmss";
    private static final String TIMESTAMP_DATE = "MM/dd/yyyy";
    private static final String TIMESTAMP_TIME = "HH:mm:ss";

    private StringBuilder questionResponses = new StringBuilder();

    private Context context;
    private File csvFile;

    public CSVWriter() {

    }

    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * Makes directory wmplab/LineUp/csvdata/
     * Generates filename (current date & time)
     * Writes first line of fields
     */
    public void createCsvFile() {
        File root = android.os.Environment.getExternalStorageDirectory();
        File csvFolder = new File (root.getAbsolutePath() + FOLDERPATH);
        if (!csvFolder.exists())
            Log.i("createCsvFile()", "CSV folder created " + csvFolder.mkdirs());

        final String DATE = new SimpleDateFormat(FORMAT_DATE, Locale.US).format(Calendar.getInstance().getTime());
        final String TIME = new SimpleDateFormat(FORMAT_TIME, Locale.US).format(Calendar.getInstance().getTime());

        String subj = (LevelManager.getInstance().trainingmode.equals(LevelManager.TRAININGMODE_DEMO)) ? "DEMO" : LevelManager.getInstance().subject + "";

        final String filename = subj + "_" +
                LevelManager.getInstance().session + "_" +
                DATE + "_" + TIME + "_" + ".csv";

        csvFile = new File(csvFolder, filename);
        writeLine(getFields());
    }

    /**
     * Writes single line to the csv file
     */
    private void writeLine(String line) {
        FileWriter fw;
        BufferedWriter out = null;
        try {
//            Log.d("writeLine()", line);
            fw = new FileWriter(csvFile, true);
            out = new BufferedWriter(fw);
            out.write(line); // write line
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null)
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    /**
     * Reads fields from csvfields.txt, concatenate and return as one string
     */
    private String getFields() {
        StringBuilder result = new StringBuilder();
        InputStream inputStream = context.getResources().openRawResource(R.raw.csvfields);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                String field = line.trim();
                result.append(field);
                if (!field.equals(LAST_FIELD)) // last field must not have a comma
                    result.append(COMMA);
            }
            result.append(NEW_LINE); // new line
        } catch (IOException e) {
            Log.e("getFields()", "Error reading fields");
            e.printStackTrace();
        }
        return result.toString();
    }

    /**
     * Returns if field can be ignored
     */
    public boolean canIgnore(String field) {
        for (String igField : ignoreFields)
            if (field.equals(igField))
                return true;
        return false;
    }

    public void collectData() {
////        if (LevelManager.getInstance().trainingmode.equals(LevelManager.TRAININGMODE_DEMO)) // demo mode does not output a data file
////            return;
//        int curInd = LevelManager.getInstance().currentStimuliIndex;
//        StringBuilder data = new StringBuilder();
//
//        data.append(Build.VERSION.RELEASE).append(COMMA);                                                               // OS
//        data.append(BuildConfig.VERSION_NAME).append(COMMA);                                                            // version name
//
//        if (LevelManager.getInstance().trainingmode.equals(LevelManager.TRAININGMODE_DEMO))                             // should not write subject id on demo mode
//            data.append("DEMO").append(COMMA);
//        else
//            data.append(LevelManager.getInstance().subject).append(COMMA);
//
//        data.append(LevelManager.getInstance().session).append(COMMA);
//        data.append(LevelManager.getInstance().level).append(COMMA);
//        data.append(LevelManager.getInstance().round).append(COMMA);
//        data.append(LevelManager.getInstance().part).append(COMMA);
//        data.append(StimuliManager.getImagePath(LevelManager.getInstance().stimulisequence.get(curInd))).append(COMMA); // stimulus
//        data.append(LevelManager.getInstance().stimulisequence.get(curInd) / 100).append(COMMA);                        // stimulus class
//        data.append(LevelManager.getInstance().presentationstyle.get(curInd)).append(COMMA);                            // presentation style
//
//        if (LevelManager.getInstance().part == LevelManager.STAGE1)                                                     // 1. overall acc
//            data.append(LevelManager.getInstance().accuracyfirstpart.get(curInd)).append(COMMA); // accuracy stage1
//        else if (LevelManager.getInstance().part == LevelManager.STAGE2)
//            data.append(LevelManager.getInstance().accuracysecondpart.get(curInd)).append(COMMA); // accuracy stage2
//
//        if (LevelManager.getInstance().part == LevelManager.STAGE1)                                                     // 2. stage1 & stage2 individual acc (fills two columns)
//            data.append(LevelManager.getInstance().accuracyfirstpart.get(curInd)).append(COMMA).append(NULL).append(COMMA);
//        else if (LevelManager.getInstance().part == LevelManager.STAGE2)
//            data.append(NULL).append(COMMA).append(LevelManager.getInstance().accuracysecondpart.get(curInd)).append(COMMA);
//
//        double seconds = 0;                                                                                             // reaction time (rt)
//        if (LevelManager.getInstance().part == LevelManager.STAGE1)
//            seconds = (double) (LevelManager.getInstance().rtfirstpart.get(curInd)) / 1000;
//        else if (LevelManager.getInstance().part == LevelManager.STAGE2)
//            seconds = (double) (LevelManager.getInstance().rtsecondpart.get(curInd)) / 1000;
//        data.append(seconds).append(COMMA);
//
//        data.append(Util.getTimestamp(TIMESTAMP_DATE, TIMESTAMP_TIME)).append(COMMA);                                   // timestamp
//
//        data.append(LevelManager.getInstance().trainingmode).append(COMMA);
//
//        if (LevelManager.getInstance().trainingmode.equals(LevelManager.TRAININGMODE_ROUNDS))                           // sessionlength & numberofrounds
//            data.append(NULL).append(COMMA).append(LevelManager.getInstance().numberoftrials).append(COMMA);
//        else if (LevelManager.getInstance().trainingmode.equals(LevelManager.TRAININGMODE_TIME))
//            data.append(LevelManager.getInstance().sessionLength).append(COMMA).append(NULL).append(COMMA);
//
//
//        data.append(LevelManager.getInstance().setsize).append(COMMA);             // not sure
//        data.append(LevelManager.getInstance().distractorsize).append(COMMA);
//
//        data.append(NEW_LINE);
//        writeLine(data.toString());
    }

    /**
     * Collect data for each question: exp subject session nameofquestion response timestamp
     */
    public void collectQuestionResponse(String theQuestion, int resp) {
        questionResponses
                .append("CST").append(COMMA)
                .append(LevelManager.getInstance().subject).append(COMMA)
                .append(LevelManager.getInstance().session).append(COMMA)
                .append(theQuestion).append(COMMA)
                .append(resp).append(COMMA)
                .append(Util.getTimestamp(TIMESTAMP_DATE, TIMESTAMP_TIME)).append(COMMA).append(NEW_LINE);
    }

    /**
     * Writes data to data file
     */
    public void writeQuestionResponse() {
        writeLine(questionResponses.toString());
        questionResponses.setLength(0); // clear StringBuilder
    }

    public static CSVWriter getInstance() {
        return INSTANCE;
    }
}
