package com.dailyroutinetasks;

import android.content.Context;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.EditTextPreference;
import androidx.preference.EditTextPreferenceDialogFragmentCompat;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment(this))
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        Context context;
        boolean startDayTimeValidate = false;

        public SettingsFragment(Context c){
            this.context = c;
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            EditTextPreference start_day_time = getPreferenceManager().findPreference("start_day_time");
            SwitchPreferenceCompat dark_mode = getPreferenceManager().findPreference("dark_mode");
            SwitchPreferenceCompat disable_notifications = getPreferenceManager().findPreference("disable_notifications");

            start_day_time.setOnPreferenceChangeListener((preference, newValue) -> {
                if(startDayTimeValidate){
                    return true;
                }else {
                    Toast.makeText(context, R.string.not_correct_value, Toast.LENGTH_SHORT).show();
                    return false;
                }
            });

            start_day_time.setOnBindEditTextListener(editText -> {
                editText.setSingleLine(true);
                editText.addTextChangedListener(new TextValidator(editText) {
                    @Override
                    public void validate(TextView textView, String text) {
                        startDayTimeValidate = validateStartDayTime(textView, text);
                    }
                });

                editText.setOnEditorActionListener((v, actionId, event) -> {
                    if(actionId== EditorInfo.IME_ACTION_DONE) {
                        //After pressing V(tick) on keyboard
                        closeKeyboard(v);
                        dismissDialog();
                    }
                    return false;
                });
            });


            dark_mode.setOnPreferenceChangeListener((preference, newValue) -> {
                //TODO: changing to dark mode
                boolean value = (Boolean) newValue;
                if(value){
                    Toast.makeText(context, R.string.dark_mode_active, Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(context, R.string.dark_mode_inactive, Toast.LENGTH_SHORT).show();
                }
                return true;
            });

            disable_notifications.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean value = (Boolean) newValue;
                if(value){
                    Toast.makeText(context, R.string.notifications_disabled, Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(context, R.string.notifications_enabled, Toast.LENGTH_SHORT).show();
                }
                return true;
            });
        }

        private boolean validateStartDayTime(TextView textView, String text) {
            if (text.length() == 0) {
                textView.setError(getString(R.string.to_short));
                return false;
            } else if (text.length() > 5) {
                textView.setError(getString(R.string.to_long));
                return false;
            } else if (!text.matches("\\d{1,2}:\\d{2}")) {
                textView.setError(getString(R.string.day_time_match));
                return false;
            }else{
                String[] dayTime = text.split(":");
                int hours = Integer.parseInt(dayTime[0]);
                int minutes = Integer.parseInt(dayTime[1]);
                if(hours > 23 || minutes > 59){
                    textView.setError(getString(R.string.day_time_range));
                    return false;
                }
            }
            return true;
        }



        private void closeKeyboard(TextView view) {
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }

        private void dismissDialog(){
            for(Fragment fragments : SettingsFragment.this.getActivity().getSupportFragmentManager().getFragments()){
                if(fragments instanceof EditTextPreferenceDialogFragmentCompat){
                    EditTextPreferenceDialogFragmentCompat dialog = ((EditTextPreferenceDialogFragmentCompat) fragments);
                    dialog.onDialogClosed(true);
                    dialog.dismiss();
                    return;
                }
            }
        }
    }
}