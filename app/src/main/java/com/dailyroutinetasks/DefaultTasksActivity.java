package com.dailyroutinetasks;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dailyroutinetasks.database.entities.DefaultTask;

import java.util.ArrayList;
import java.util.List;

public class DefaultTasksActivity extends AppCompatActivity {

    ListView defaultTasksListView;
    List<DefaultTask> tasks = new ArrayList<>();;

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

        DefaultTaskAdapter defaultTaskAdapter = new DefaultTaskAdapter(this, tasks);
        defaultTasksListView.setAdapter(defaultTaskAdapter);

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
}