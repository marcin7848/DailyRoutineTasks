package com.dailyroutinetasks;

import android.text.Editable;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public abstract class GlobalFunctions  {

    public static <T extends AppCompatActivity> boolean validateTaskTitle(T activity, TextView textView, String text) {
        if (text.length() == 0) {
            textView.setError(activity.getString(R.string.to_short));
            return false;
        } else if (text.length() > 80) {
            textView.setError(activity.getString(R.string.to_long));
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

    public static int[] convertDayTime(String dayTime) {
        int[] dayTimeArray = {0, 0};
        String[] durationParts = dayTime.split(":");
        dayTimeArray[0] = Integer.parseInt(durationParts[0]);
        dayTimeArray[1] = Integer.parseInt(durationParts[1]);
        return dayTimeArray;
    }

    public static String convertCalendarToDateString(Calendar calendar) {
        return new SimpleDateFormat( "yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
    }

    public static String convertCalendarToTimeString(Calendar calendar) {
        return new SimpleDateFormat( "HH:mm", Locale.getDefault()).format(calendar.getTime());
    }

    public static <T extends AppCompatActivity> void showBottomPanel(T activity, boolean show,
                                                                     int viewBottomPanelId, int viewParentId,
                                                                     int viewAddBottomId, int viewShadowId){
        View tasksBottomPanel = activity.findViewById(viewBottomPanelId);
        ViewGroup parent = activity.findViewById(viewParentId);

        Transition transition = new Slide(Gravity.BOTTOM);
        transition.setDuration(600);
        transition.addTarget(viewBottomPanelId);

        TransitionManager.beginDelayedTransition(parent, transition);
        tasksBottomPanel.setVisibility(show ? View.VISIBLE : View.GONE);

        View tasksAddButton = activity.findViewById(viewAddBottomId);
        tasksAddButton.setVisibility(show ? View.GONE : View.VISIBLE);

        View tasksShadow = activity.findViewById(viewShadowId);
        tasksShadow.setVisibility(show ? View.VISIBLE : View.GONE);
    }

}
