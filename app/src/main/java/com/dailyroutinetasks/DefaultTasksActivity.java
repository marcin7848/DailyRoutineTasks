package com.dailyroutinetasks;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class DefaultTasksActivity extends AppCompatActivity {

    ListView defaultTasksListView;
    String[] text = {"facebook", "twitter"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_tasks);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        defaultTasksListView = findViewById(R.id.defaultTasksListView);

        DefaultTaskAdapter defaultTaskAdapter = new DefaultTaskAdapter(this, text);
        defaultTasksListView.setAdapter(defaultTaskAdapter);

    }

    class DefaultTaskAdapter extends ArrayAdapter<String>{
        Context context;
        String rTitle[];

        DefaultTaskAdapter(Context c, String title[]){
            super(c, R.layout.default_task_row, R.id.default_task_row_title, title);
            this.context = c;
            this.rTitle = title;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = layoutInflater.inflate(R.layout.default_task_row, parent, false);
            TextView title = row.findViewById(R.id.default_task_row_title);
            title.setText(rTitle[position]);

            return row;
        }
    }
}