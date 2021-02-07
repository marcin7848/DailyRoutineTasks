package com.dailyroutinetasks;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dailyroutinetasks.database.entities.DefaultTask;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class DefaultTasksActivity extends AppCompatActivity {

    ListView defaultTasksListView;
    List<DefaultTask> tasks = new ArrayList<>();;

    boolean test = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_tasks);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        defaultTasksListView = findViewById(R.id.defaultTasksListView);

        tasks.add(new DefaultTask("Title1", 1, 34, 1));
        tasks.add(new DefaultTask("Title2", 2, 11, 2));
        tasks.add(new DefaultTask("Title2", 2, 11, 2));
        tasks.add(new DefaultTask("Title2", 2, 11, 2));
        tasks.add(new DefaultTask("Title2", 2, 11, 2));
        tasks.add(new DefaultTask("Title2", 2, 11, 2));
        tasks.add(new DefaultTask("Title2", 2, 11, 2));
        tasks.add(new DefaultTask("Title2", 2, 11, 2));
        tasks.add(new DefaultTask("Title2", 2, 11, 2));
        tasks.add(new DefaultTask("Title3", 2, 11, 2));
        tasks.add(new DefaultTask("Title4", 2, 11, 2));

        DefaultTaskAdapter defaultTaskAdapter = new DefaultTaskAdapter(this, tasks);
        defaultTasksListView.setAdapter(defaultTaskAdapter);

        FloatingActionButton defaultTasksAddButton = findViewById(R.id.defaultTasksAddButton);
        defaultTasksAddButton.setOnClickListener(v -> {
            this.toggle(!test);
        });

        View defaultTasksShadow = findViewById(R.id.defaultTasksShadow);
        defaultTasksShadow.setOnClickListener(v -> {
            this.toggle(!test);
        });

    }

    class DefaultTaskAdapter extends ArrayAdapter<DefaultTask>{
        Context context;
        List<DefaultTask> rTasks = new ArrayList<>();

        DefaultTaskAdapter(Context c, List<DefaultTask> tasks){
            super(c, R.layout.default_task_row, R.id.default_task_row_title, tasks);
            this.context = c;
            this.rTasks = tasks;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = layoutInflater.inflate(R.layout.default_task_row, parent, false);
            TextView title = row.findViewById(R.id.default_task_row_title);
            title.setText(rTasks.get(position).getTitle());
            TextView duration = row.findViewById(R.id.default_task_duration_text);
            duration.setText(String.format("%d:%dh",rTasks.get(position).getDurationHours(), rTasks.get(position).getDurationMinutes()));

            ImageButton editDefaultTask = row.findViewById(R.id.default_task_edit_icon);
            editDefaultTask.setOnClickListener(v -> {
                Toast.makeText(DefaultTasksActivity.this, rTasks.get(position).getTitle(), Toast.LENGTH_SHORT).show();
            });

            ImageButton deleteDefaultTask = row.findViewById(R.id.default_task_delete_icon);
            deleteDefaultTask.setOnClickListener(v -> {
                Toast.makeText(DefaultTasksActivity.this, rTasks.get(position).getTitle(), Toast.LENGTH_SHORT).show();
            });

            return row;
        }
    }

    private void toggle(boolean show) {
        test = !test;
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
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && test) {
            this.toggle(!test);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}