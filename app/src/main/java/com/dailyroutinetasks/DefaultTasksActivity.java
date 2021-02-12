package com.dailyroutinetasks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.content.Context;
import android.graphics.Canvas;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dailyroutinetasks.database.AppDatabase;
import com.dailyroutinetasks.database.entities.DefaultTask;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class DefaultTasksActivity extends AppCompatActivity {

    boolean bottomPanelShown = false;
    boolean titleValidate = false;
    boolean durationValidate = false;
    AppDatabase db;
    List<DefaultTask> defaultTasks = new ArrayList<>();
    DefaultTaskRecyclerAdapter defaultTaskRecyclerAdapter;
    int editPosition = -1;
    RecyclerView defaultTasksRecyclerView;

    int lastPositionNumber = 0;

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

        defaultTasksRecyclerView = findViewById(R.id.defaultTasksRecyclerView);
        defaultTaskRecyclerAdapter = new DefaultTaskRecyclerAdapter(DefaultTasksActivity.this);
        defaultTasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        defaultTasksRecyclerView.setAdapter(defaultTaskRecyclerAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(defaultTaskMoveCallback);
        itemTouchHelper.attachToRecyclerView(defaultTasksRecyclerView);

        AsyncTask.execute(() -> {
            defaultTasks.addAll(db.defaultTaskDao().getAll());
            runOnUiThread(() -> defaultTaskRecyclerAdapter.notifyDataSetChanged());
            lastPositionNumber = defaultTasks.size()-1;
        });

        FloatingActionButton defaultTasksAddButton = findViewById(R.id.defaultTasksAddButton);
        defaultTasksAddButton.setOnClickListener(v -> {
            editPosition = -1;
            this.showBottomPanel(true);
        });

        View defaultTasksShadow = findViewById(R.id.defaultTasksShadow);
        defaultTasksShadow.setOnClickListener(v -> {
            this.showBottomPanel(false);
        });

        EditText editTextDefaultTaskTitle = (EditText) findViewById(R.id.editTextDefaultTaskTitle);
        editTextDefaultTaskTitle.addTextChangedListener(new TextValidator(editTextDefaultTaskTitle) {
            @Override
            public void validate(TextView textView, String text) {
                titleValidate = GlobalFunctions.validateTaskTitle(DefaultTasksActivity.this, textView, text);
            }
        });

        EditText editTextDefaultTaskDuration = (EditText) findViewById(R.id.editTextDefaultTaskDuration);
        editTextDefaultTaskDuration.addTextChangedListener(new TextValidator(editTextDefaultTaskDuration) {
            @Override
            public void validate(TextView textView, String text) {
                durationValidate = GlobalFunctions.validateTaskDuration(DefaultTasksActivity.this, textView, text);
            }
        });

        View save = findViewById(R.id.defaultTasksSave);

        save.setOnClickListener(v -> {
            Editable taskTitleText = editTextDefaultTaskTitle.getText();
            Editable durationText = editTextDefaultTaskDuration.getText();
            if (!titleValidate || !durationValidate || taskTitleText.length() == 0 || durationText.length() == 0)
                Toast.makeText(DefaultTasksActivity.this, R.string.provide_correct_data, Toast.LENGTH_SHORT).show();
            else {
                int[] duration = GlobalFunctions.convertDurationText(durationText);
                int position = editPosition;

                if (position == -1) {
                    AsyncTask.execute(() -> {
                        int newPositionNumber = lastPositionNumber+1;
                        lastPositionNumber++;
                        long newId = this.db.defaultTaskDao().insert(new DefaultTask(taskTitleText.toString(), duration[0], duration[1], newPositionNumber));
                        defaultTasks.add(new DefaultTask(newId, taskTitleText.toString(), duration[0], duration[1], newPositionNumber));
                        runOnUiThread(() -> defaultTaskRecyclerAdapter.notifyItemInserted(defaultTasks.size() - 1));
                    });
                } else {
                    defaultTasks.get(position).setTitle(taskTitleText.toString());
                    defaultTasks.get(position).setDurationHours(duration[0]);
                    defaultTasks.get(position).setDurationMinutes(duration[1]);
                    AsyncTask.execute(() -> {
                        this.db.defaultTaskDao().update(defaultTasks.get(position));
                    });
                    defaultTaskRecyclerAdapter.notifyItemChanged(position);
                }

                showBottomPanel(false);
                Toast.makeText(DefaultTasksActivity.this, R.string.saved, Toast.LENGTH_SHORT).show();
            }
        });

    }

    class DefaultTaskRecyclerAdapter extends RecyclerView.Adapter<DefaultTaskRecyclerAdapter.DefaultTaskViewHolder> {
        Context context;

        public DefaultTaskRecyclerAdapter(Context c) {
            this.context = c;
        }

        @NonNull
        @Override
        public DefaultTaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.default_task_row, parent, false);
            return new DefaultTaskViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DefaultTaskViewHolder holder, int position) {
            holder.title.setText(defaultTasks.get(position).getTitle());
            String minutes = defaultTasks.get(position).getDurationMinutes() < 10 ? "0" + defaultTasks.get(position).getDurationMinutes() : "" + defaultTasks.get(position).getDurationMinutes();
            holder.duration.setText(String.format("%d:%sh", defaultTasks.get(position).getDurationHours(), minutes));

            holder.itemView.setOnClickListener(v -> {
                editPosition = position;
                showBottomPanel(true);
            });
        }

        @Override
        public int getItemCount() {
            return defaultTasks.size();
        }

        public class DefaultTaskViewHolder extends RecyclerView.ViewHolder {
            TextView title, duration;

            public DefaultTaskViewHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.default_task_row_title);
                duration = itemView.findViewById(R.id.default_task_duration_text);
            }
        }
    }

    private void showBottomPanel(boolean show) {
        bottomPanelShown = show;

        GlobalFunctions.showBottomPanel(DefaultTasksActivity.this, show,
                R.id.defaultTasksBottomPanel, R.id.defaultTasksParent, R.id.defaultTasksAddButton, R.id.defaultTasksShadow);

        if (!show) {
            editPosition = -1;
            closeKeyboard();
        } else if (editPosition != -1) {
            EditText editTextDefaultTaskTitle = findViewById(R.id.editTextDefaultTaskTitle);
            EditText editTextDefaultTaskDuration = findViewById(R.id.editTextDefaultTaskDuration);
            editTextDefaultTaskTitle.setText(defaultTasks.get(editPosition).getTitle());
            String minutes = defaultTasks.get(editPosition).getDurationMinutes() < 10 ? "0" +
                    defaultTasks.get(editPosition).getDurationMinutes() : "" +
                    defaultTasks.get(editPosition).getDurationMinutes();

            editTextDefaultTaskDuration.setText(String.format("%d:%s", defaultTasks.get(editPosition).getDurationHours(), minutes));
        }

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

    ItemTouchHelper.SimpleCallback defaultTaskMoveCallback = new ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT,
            ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            final int fromPosition = viewHolder.getAdapterPosition();
            final int toPosition = target.getAdapterPosition();
            DefaultTask defaultTask1 = defaultTasks.get(fromPosition);
            DefaultTask defaultTask2 = defaultTasks.get(toPosition);

            Collections.swap(defaultTasks, fromPosition, toPosition);
            recyclerView.getAdapter().notifyItemMoved(fromPosition, toPosition);
            recyclerView.getAdapter().notifyItemRangeChanged(Math.min(fromPosition, toPosition), Math.max(fromPosition, toPosition));

            defaultTask1.setPositionNumber(toPosition);
            defaultTask2.setPositionNumber(fromPosition);
            List<DefaultTask> updateDefaultTasks = Arrays.asList(defaultTask1, defaultTask2);

            AsyncTask.execute(() -> {
                db.defaultTaskDao().updateAll(updateDefaultTasks);
            });

            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getAdapterPosition();
            if (direction == ItemTouchHelper.LEFT) {
                DefaultTask removedDefaultTask = new DefaultTask(defaultTasks.get(position));
                lastPositionNumber--;

                defaultTasks.remove(position);
                defaultTaskRecyclerAdapter.notifyItemRemoved(position);
                defaultTaskRecyclerAdapter.notifyItemRangeChanged(position, defaultTasks.size() - position);

                AsyncTask.execute(() -> {
                    db.defaultTaskDao().delete(removedDefaultTask);
                    int[] pos = {position};
                    defaultTasks.forEach(dt -> {
                        if(dt.getPositionNumber() > position)
                            dt.setPositionNumber(pos[0]++);
                    });
                    db.defaultTaskDao().updateAll(defaultTasks);
                });

                Toast.makeText(DefaultTasksActivity.this, R.string.removed, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeLeftBackgroundColor(ContextCompat.getColor(DefaultTasksActivity.this, R.color.purple_200))
                    .addSwipeLeftActionIcon(R.drawable.ic_delete)
                    .create()
                    .decorate();
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };


}