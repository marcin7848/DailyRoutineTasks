package com.dailyroutinetasks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dailyroutinetasks.database.AppDatabase;
import com.dailyroutinetasks.database.entities.Day;
import com.dailyroutinetasks.database.entities.Setting;
import com.dailyroutinetasks.database.entities.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MainActivity extends AppCompatActivity {

    AppDatabase db;
    Calendar pickedCalendarDay;
    Dialog pickDayDialog;
    Day day;

    boolean bottomPanelShown = false;
    boolean titleValidate = false;
    boolean durationValidate = false;

    List<Task> tasks = new ArrayList<>();
    TaskRecyclerAdapter taskRecyclerAdapter;
    int editPosition = -1;
    RecyclerView tasksRecyclerView;
    int[] start_day_time = {0, 0};

    int lastPositionNumber = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "dailyRoutineTasksDb").build();

        pickedCalendarDay = Calendar.getInstance(TimeZone.getDefault());

        start_day_time = GlobalFunctions.convertDayTime(PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString("start_day_time", "6:00"));

        pickedCalendarDay.set(Calendar.HOUR_OF_DAY, start_day_time[0]);
        pickedCalendarDay.set(Calendar.MINUTE, start_day_time[1]);
        pickedCalendarDay.set(Calendar.SECOND, 0);
        pickedCalendarDay.set(Calendar.MILLISECOND, 0);

        updateTasksView();

        pickDayDialog = new Dialog(this);
        pickDayDialog.setContentView(R.layout.pick_day_view);

        tasksRecyclerView = findViewById(R.id.tasksRecyclerView);
        taskRecyclerAdapter = new TaskRecyclerAdapter(MainActivity.this);
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tasksRecyclerView.setAdapter(taskRecyclerAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(taskMoveCallback);
        itemTouchHelper.attachToRecyclerView(tasksRecyclerView);

        FloatingActionButton tasksAddButton = findViewById(R.id.tasksAddButton);
        tasksAddButton.setOnClickListener(v -> {
            editPosition = -1;
            this.showBottomPanel(true);
        });

        View tasksShadow = findViewById(R.id.tasksShadow);
        tasksShadow.setOnClickListener(v -> {
            this.showBottomPanel(false);
        });

        EditText editTextTaskTitle = (EditText) findViewById(R.id.editTextTaskTitle);
        editTextTaskTitle.addTextChangedListener(new TextValidator(editTextTaskTitle) {
            @Override
            public void validate(TextView textView, String text) {
                titleValidate = GlobalFunctions.validateTaskTitle(MainActivity.this, textView, text);
            }
        });

        EditText editTextTaskDuration = (EditText) findViewById(R.id.editTextTaskDuration);
        editTextTaskDuration.addTextChangedListener(new TextValidator(editTextTaskDuration) {
            @Override
            public void validate(TextView textView, String text) {
                durationValidate = GlobalFunctions.validateTaskDuration(MainActivity.this, textView, text);
            }
        });

        View save = findViewById(R.id.tasksSave);

        save.setOnClickListener(v -> {
            Editable taskTitleText = editTextTaskTitle.getText();
            Editable durationText = editTextTaskDuration.getText();
            if (!titleValidate || !durationValidate || taskTitleText.length() == 0 || durationText.length() == 0)
                Toast.makeText(MainActivity.this, R.string.provide_correct_data, Toast.LENGTH_SHORT).show();
            else {
                int[] duration = GlobalFunctions.convertDurationText(durationText);
                int position = editPosition;

                if (position == -1) {
                    AsyncTask.execute(() -> {
                        int newPositionNumber = lastPositionNumber+1;
                        lastPositionNumber++;

                        pickedCalendarDay.add(Calendar.HOUR, duration[0]);
                        pickedCalendarDay.add(Calendar.MINUTE, duration[1]);

                        long newId = this.db.taskDao().insert(new Task(taskTitleText.toString(), duration[0], duration[1], newPositionNumber, false, pickedCalendarDay, day.getId()));
                        tasks.add(new Task(newId, taskTitleText.toString(), duration[0], duration[1], newPositionNumber, false, pickedCalendarDay, day.getId()));
                        runOnUiThread(() -> taskRecyclerAdapter.notifyItemInserted(tasks.size() - 1));
                    });
                } else {
                    tasks.get(position).setTitle(taskTitleText.toString());
                    tasks.get(position).setDurationHours(duration[0]);
                    tasks.get(position).setDurationMinutes(duration[1]);
                    AsyncTask.execute(() -> {
                        this.db.taskDao().update(tasks.get(position));
                    });
                    taskRecyclerAdapter.notifyItemChanged(position);
                }

                showBottomPanel(false);
                Toast.makeText(MainActivity.this, R.string.saved, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.app_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.defaultTasks:
                Intent intentDefaultTasks = new Intent(this, DefaultTasksActivity.class);
                startActivity(intentDefaultTasks);
                return true;
            case R.id.settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateTasksView(){
        TextView task_day = findViewById(R.id.task_day);
        task_day.setText(pickedCalendarDay.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()) + " " +
                pickedCalendarDay.get(Calendar.DAY_OF_MONTH) + " " +
                pickedCalendarDay.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) + " " +
                pickedCalendarDay.get(Calendar.YEAR));

        AsyncTask.execute(() ->{
            day = db.dayDao().getDayByDayString(GlobalFunctions.convertCalendarToDateString(pickedCalendarDay));
            if(day == null){
                Day newDay = new Day(pickedCalendarDay, GlobalFunctions.convertCalendarToDateString(pickedCalendarDay));
                long newDayId = db.dayDao().insert(newDay);
                day = db.dayDao().getDayById(newDayId);
            }else{
                tasks.addAll(db.taskDao().getTasksByDayId(day.getId()));
                runOnUiThread(() -> taskRecyclerAdapter.notifyDataSetChanged());
                lastPositionNumber = tasks.size()-1;
                pickedCalendarDay = (Calendar) tasks.get(tasks.size()-1).getStartTime().clone();
            }
        });
    }

    public void showPickDayDialog(View v){
        pickDayDialog.show();
        CalendarView pickDayCalendar = pickDayDialog.findViewById(R.id.pickDayCalendarView);
        pickDayCalendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                pickedCalendarDay.set(year, month, dayOfMonth, start_day_time[0], start_day_time[1], 0);
                pickedCalendarDay.set(Calendar.MILLISECOND, 0);

                updateTasksView();
                pickDayDialog.hide();
            }
        });
    }

    class TaskRecyclerAdapter extends RecyclerView.Adapter<TaskRecyclerAdapter.TaskViewHolder> {
        Context context;

        public TaskRecyclerAdapter(Context c) {
            this.context = c;
        }

        @NonNull
        @Override
        public TaskRecyclerAdapter.TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.task_row, parent, false);
            return new TaskRecyclerAdapter.TaskViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TaskRecyclerAdapter.TaskViewHolder holder, int position) {
            holder.title.setText(tasks.get(position).getTitle());
            String minutes = tasks.get(position).getDurationMinutes() < 10 ? "0" + tasks.get(position).getDurationMinutes() : "" + tasks.get(position).getDurationMinutes();
            holder.duration.setText(String.format("%d:%sh", tasks.get(position).getDurationHours(), minutes));
            holder.time.setText(GlobalFunctions.convertCalendarToTimeString(tasks.get(position).getStartTime()));
            holder.taskDone.setChecked(tasks.get(position).isDone());

            holder.itemView.setOnClickListener(v -> {
                editPosition = position;
                showBottomPanel(true);
            });
        }

        @Override
        public int getItemCount() {
            return tasks.size();
        }

        public class TaskViewHolder extends RecyclerView.ViewHolder {
            TextView title, duration, time;
            CheckBox taskDone;

            public TaskViewHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.task_row_title);
                duration = itemView.findViewById(R.id.task_duration_text);
                time = itemView.findViewById(R.id.task_time_text);
                taskDone = itemView.findViewById(R.id.checkBoxTaskDone);
            }
        }
    }

    private void showBottomPanel(boolean show) {
        bottomPanelShown = show;

        GlobalFunctions.showBottomPanel(MainActivity.this, show,
                R.id.tasksBottomPanel, R.id.tasksParent, R.id.tasksAddButton, R.id.tasksShadow);

        if (!show) {
            editPosition = -1;
            closeKeyboard();
        } else if (editPosition != -1) {
            EditText editTextTaskTitle = findViewById(R.id.editTextTaskTitle);
            EditText editTextTaskDuration = findViewById(R.id.editTextTaskDuration);
            editTextTaskTitle.setText(tasks.get(editPosition).getTitle());
            String minutes = tasks.get(editPosition).getDurationMinutes() < 10 ? "0" +
                    tasks.get(editPosition).getDurationMinutes() : "" +
                    tasks.get(editPosition).getDurationMinutes();

            editTextTaskDuration.setText(String.format("%d:%s", tasks.get(editPosition).getDurationHours(), minutes));
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

    ItemTouchHelper.SimpleCallback taskMoveCallback = new ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT,
            ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            final int fromPosition = viewHolder.getAdapterPosition();
            final int toPosition = target.getAdapterPosition();
            Task task1 = tasks.get(fromPosition);
            Task task2 = tasks.get(toPosition);

            Collections.swap(tasks, fromPosition, toPosition);
            recyclerView.getAdapter().notifyItemMoved(fromPosition, toPosition);
            recyclerView.getAdapter().notifyItemRangeChanged(Math.min(fromPosition, toPosition), Math.max(fromPosition, toPosition));

            task1.setPositionNumber(toPosition);
            task2.setPositionNumber(fromPosition);
            List<Task> updateTasks = Arrays.asList(task1, task2);

            AsyncTask.execute(() -> {
                db.taskDao().updateAll(updateTasks);
            });

            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getAdapterPosition();
            if (direction == ItemTouchHelper.LEFT) {
                Task removedTask = new Task(tasks.get(position));
                tasks.remove(position);
                taskRecyclerAdapter.notifyItemRemoved(position);
                taskRecyclerAdapter.notifyItemRangeChanged(position, tasks.size() - position);

                AsyncTask.execute(() -> {
                    db.taskDao().delete(removedTask);
                    int[] pos = {0};
                    tasks.forEach(dt -> {
                        dt.setPositionNumber(pos[0]++);
                    });
                    db.taskDao().updateAll(tasks);
                });

                Toast.makeText(MainActivity.this, R.string.removed, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeLeftBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.purple_200))
                    .addSwipeLeftActionIcon(R.drawable.ic_delete)
                    .create()
                    .decorate();
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };
}