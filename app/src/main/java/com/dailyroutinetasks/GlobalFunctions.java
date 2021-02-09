package com.dailyroutinetasks;

import android.text.Editable;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public abstract class GlobalFunctions  {

    public static <T extends AppCompatActivity> boolean validateTaskTitle(T app, TextView textView, String text) {
        if (text.length() == 0) {
            textView.setError(app.getString(R.string.to_short));
            return false;
        } else if (text.length() > 80) {
            textView.setError(app.getString(R.string.to_long));
            return false;
        }
        return true;
    }

    public static <T extends AppCompatActivity> boolean validateTaskDuration(T app, TextView textView, String text) {
        if (text.length() == 0) {
            textView.setError(app.getString(R.string.to_short));
            return false;
        } else if (text.length() > 10) {
            textView.setError(app.getString(R.string.to_long));
            return false;
        } else if (((!text.matches("\\d+") || Integer.parseInt(text) <= 0) && !text.matches("\\d{1,2}:\\d{2}"))) {
            textView.setError(app.getString(R.string.duration_must_match));
            return false;
        }
        return true;
    }

    public static int[] convertDurationText(Editable durationText) {
        int[] duration = {0, 0};
        if (durationText.toString().matches("\\d+")) {
            int durationToConvert = Integer.parseInt(durationText.toString());
            duration[0] = durationToConvert / 60;
            duration[1] = durationToConvert - duration[0] * 60;
        } else {
            String[] durationParts = durationText.toString().split(":");
            duration[0] = Integer.parseInt(durationParts[0]);
            int durationToConvert = Integer.parseInt(durationParts[1]);
            duration[0] += durationToConvert / 60;
            duration[1] = durationToConvert - ((int) (durationToConvert / 60)) * 60;
        }
        return duration;
    }

}
