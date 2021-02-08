package com.dailyroutinetasks;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dailyroutinetasks.database.AppDatabase;
import com.dailyroutinetasks.database.entities.DefaultTask;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class DefaultTasksActivity extends AppCompatActivity {

    boolean bottomPanelShown = false;
    boolean titleError = false;
    boolean durationError = false;
    AppDatabase db;
    List<DefaultTask> defaultTasks = new ArrayList<>();
    DefaultTaskAdapter defaultTaskAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_tasks);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        this.db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "dailyRoutineTasksDb").build();

        ListView defaultTasksListView = findViewById(R.id.defaultTasksListView);
        defaultTaskAdapter = new DefaultTaskAdapter(DefaultTasksActivity.this);
        defaultTasksListView.setAdapter(defaultTaskAdapter);

        AsyncTask.execute(() -> {
            defaultTasks.addAll(db.defaultTaskDao().getAll());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    defaultTaskAdapter.notifyDataSetChanged();
                }
            });
        });

        FloatingActionButton defaultTasksAddButton = findViewById(R.id.defaultTasksAddButton);
        defaultTasksAddButton.setOnClickListener(v -> {
            this.showBottomPanel(true);
        });

        View defaultTasksShadow = findViewById(R.id.defaultTasksShadow);
        defaultTasksShadow.setOnClickListener(v -> {
            this.showBottomPanel(false);
        });

        EditText editTextDefaultTaskTitle = (EditText)findViewById(R.id.editTextDefaultTaskTitle);
        editTextDefaultTaskTitle.addTextChangedListener(new TextValidator(editTextDefaultTaskTitle) {
            @Override public void validate(TextView textView, String text) {
                titleError = false;
                if(text.length() == 0){
                    textView.setError(getString(R.string.to_short));
                    titleError = true;
                }
                else if(text.length() > 80){
                    textView.setError(getString(R.string.to_long));
                    titleError = true;
                }
            }
        });

        EditText editTextDefaultTaskDuration = (EditText)findViewById(R.id.editTextDefaultTaskDuration);
        editTextDefaultTaskDuration.addTextChangedListener(new TextValidator(editTextDefaultTaskDuration) {
            @Override public void validate(TextView textView, String text) {
                durationError = false;
                if(text.length() == 0){
                    textView.setError(getString(R.string.to_short));
                    durationError = true;
                }
                else if(text.length() > 10){
                    textView.setError(getString(R.string.to_long));
                    durationError = true;
                }else if(((!text.matches("\\d+") || Integer.parseInt(text) <= 0) && !text.matches("\\d{1,2}:\\d{2}"))){
                    textView.setError(getString(R.string.title_must_match));
                    durationError = true;
                }
            }
        });

        View save = findViewById(R.id.defaultTasksSave);

        save.setOnClickListener(v -> {
            Editable taskTitleText = editTextDefaultTaskTitle.getText();
            Editable durationTitleText = editTextDefaultTaskDuration.getText();
            if(titleError || durationError || taskTitleText.length() == 0 || durationTitleText.length() == 0)
                Toast.makeText(DefaultTasksActivity.this, R.string.provide_correct_data, Toast.LENGTH_SHORT).show();
            else{
                int[] duration = {0, 0};
                if(durationTitleText.toString().matches("\\d+")) {
                    int durationToConvert = Integer.parseInt(durationTitleText.toString());
                    duration[0] = durationToConvert / 60;
                    duration[1] = durationToConvert - duration[0] * 60;
                }else{
                    String[] durationParts = durationTitleText.toString().split(":");
                    duration[0] = Integer.parseInt(durationParts[0]);
                    int durationToConvert = Integer.parseInt(durationParts[1]);
                    duration[0] += durationToConvert / 60;
                    duration[1] = durationToConvert - ((int)(durationToConvert / 60)) * 60;
                }

                AsyncTask.execute(() -> {
                    this.db.defaultTaskDao().insert(new DefaultTask(taskTitleText.toString(), duration[0], duration[1], 0));
                    defaultTasks.clear();
                    defaultTasks.addAll(db.defaultTaskDao().getAll());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            defaultTaskAdapter.notifyDataSetChanged();
                        }
                    });
                });

                showBottomPanel(false);
                Toast.makeText(DefaultTasksActivity.this, R.string.saved, Toast.LENGTH_SHORT).show();
            }
        });

    }

    class DefaultTaskAdapter extends ArrayAdapter<DefaultTask>{
        Context context;

        DefaultTaskAdapter(Context c){
            super(c, R.layout.default_task_row, R.id.default_task_row_title, defaultTasks);
            this.context = c;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = layoutInflater.inflate(R.layout.default_task_row, parent, false);
            TextView title = row.findViewById(R.id.default_task_row_title);
            title.setText(defaultTasks.get(position).getTitle());
            TextView duration = row.findViewById(R.id.default_task_duration_text);
            duration.setText(String.format("%d:%dh",defaultTasks.get(position).getDurationHours(), defaultTasks.get(position).getDurationMinutes()));

            ImageButton editDefaultTask = row.findViewById(R.id.default_task_edit_icon);
            editDefaultTask.setOnClickListener(v -> {
                Toast.makeText(DefaultTasksActivity.this, defaultTasks.get(position).getTitle(), Toast.LENGTH_SHORT).show();
            });

            ImageButton deleteDefaultTask = row.findViewById(R.id.default_task_delete_icon);
            deleteDefaultTask.setOnClickListener(v -> {
                AsyncTask.execute(() -> {
                    db.defaultTaskDao().delete(defaultTasks.get(position));
                    defaultTasks.remove(position);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            defaultTaskAdapter.notifyDataSetChanged();
                        }
                    });
                });

                Toast.makeText(DefaultTasksActivity.this, R.string.removed, Toast.LENGTH_SHORT).show();
            });

            return row;
        }
    }

    private void showBottomPanel(boolean show) {
        bottomPanelShown = show;
        View defaultTasksBottomPanel = findViewById(R.id.defaultTasksBottomPanel);
        ViewGroup parent = findViewById(R.id.defaultTasksParent);

        Transition transition = new Slide(Gravity.BOTTOM);
        transition.setDuration(600);
        transition.addTarget(R.id.defaultTasksBottomPanel);

        TransitionManager.beginDelayedTransition(parent, transition);
        defaultTasksBottomPanel.setVisibility(show ? View.VISIBLE : View.GONE);

        View defaultTasksAddButton = findViewById(R.id.defaultTasksAddButton);
        defaultTasksAddButton.setVisibility(show ? View.GONE : View.VISIBLE);

        View defaultTasksShadow = findViewById(R.id.defaultTasksShadow);
        defaultTasksShadow.setVisibility(show ? View.VISIBLE : View.GONE);

        if(!show)
            closeKeyboard();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && bottomPanelShown) {
            this.showBottomPanel(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}