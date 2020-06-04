package com.ethernom.helloworld;

import android.app.Application;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MyApplication extends Application {

    static public void appendLog(String logs) {

        try {
            File path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);
            File myFile = new File(path, "ethernom_log.txt");
            FileOutputStream fOut = new FileOutputStream(myFile, true);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(logs);
            myOutWriter.close();
            fOut.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public static String getCurrentDate() {
        long milliSeconds = System.currentTimeMillis();
        String dateFormat = "dd/MM/yyyy HH:mm:ss.SSS";
        // Create a DateFormatter object for displaying date in specified format.
        DateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }
}
