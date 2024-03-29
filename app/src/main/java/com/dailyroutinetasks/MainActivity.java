package com.dailyroutinetasks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
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
import com.dailyroutinetasks.database.entities.DefaultTask;
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
    Dialog pickDayDialog;
    Dialog copyFromDayDialog;
    Dialog insertDefaultTaskDialog;
    Day day;

    boolean bottomPanelShown = false;
    boolean titleValidate = false;
    boolean durationValidate = false;

    List<Task> tasks = new ArrayList<>();
    TaskRecyclerAdapter taskRecyclerAdapter;
    int editPosition = -1;
    RecyclerView tasksRecyclerView;
    int[] start_day_time = {0, 0};

    int lastPositionNumber = -1;

    boolean showTasksDone = false;

    List<DefaultTask> defaultTasks = new ArrayList<>();
    DefaultTaskRecyclerAdapter defaultTaskRecyclerAdapter;
    RecyclerView defaultTasksRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean isDarkModeEnabled = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getBoolean("dark_mode", false);

        if(isDarkModeEnabled)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "dailyRoutineTasksDb").build();

        day = new Day();
        day.setDayTime(Calendar.getInstance(TimeZone.getDefault()));
        day.setDayString(GlobalFunctions.convertCalendarToDateString(Calendar.getInstance(TimeZone.getDefault())));

        pickDayDialog = new Dialog(this);
        pickDayDialog.setContentView(R.layout.dialog_pick_day_view);

        copyFromDayDialog = new Dialog(this);
        copyFromDayDialog.setContentView(R.layout.dialog_pick_day_view);

        insertDefaultTaskDialog = new Dialog(this);
        insertDefaultTaskDialog.setContentView(R.layout.dialog_insert_default_task);

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

                        Calendar newCalendar = (Calendar) day.getDayTime().clone();
                        long newId = this.db.taskDao().insert(new Task(taskTitleText.toString(), duration[0], duration[1], newPositionNumber, false, newCalendar, day.getId()));
                        tasks.add(new Task(newId, taskTitleText.toString(), duration[0], duration[1], newPositionNumber, false, newCalendar, day.getId()));

                        day.getDayTime().add(Calendar.HOUR, duration[0]);
                        day.getDayTime().add(Calendar.MINUTE, duration[1]);

                        sendNotificationIntent();

                        runOnUiThread(() -> taskRecyclerAdapter.notifyItemInserted(tasks.size() - 1));
                    });
                } else {
                    tasks.get(position).setTitle(taskTitleText.toString());

                    int durationHoursDifference = duration[0] - tasks.get(position).getDurationHours();
                    int durationMinutesDifference = duration[1] - tasks.get(position).getDurationMinutes();

                    for(int i=position+1; i < tasks.size(); i++){
                        tasks.get(i).getStartTime().add(Calendar.HOUR, durationHoursDifference);
                        tasks.get(i).getStartTime().add(Calendar.MINUTE, durationMinutesDifference);
                    }

                    day.getDayTime().add(Calendar.HOUR, durationHoursDifference);
                    day.getDayTime().add(Calendar.MINUTE, durationMinutesDifference);

                    tasks.get(position).setDurationHours(duration[0]);
                    tasks.get(position).setDurationMinutes(duration[1]);
                    AsyncTask.execute(() -> {
                        this.db.taskDao().updateAll(tasks);
                        sendNotificationIntent();
                    });
                    taskRecyclerAdapter.notifyDataSetChanged();

                }

                showBottomPanel(false);
                Toast.makeText(MainActivity.this, R.string.saved, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        start_day_time = GlobalFunctions.convertDayTime(PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString("start_day_time", "6:00"));

        updateTasksView();
        sendNotificationIntent();
    }

    private void sendNotificationIntent(){
        Intent sendNotification = new Intent(getApplicationContext(), GenerateNotification.class);
        sendBroadcast(sendNotification);
    }

    private void updateTasksDone(){
        Task task = db.taskDao().getNearestTask();
        if(task == null)
            return;

        long currentTime = System.currentTimeMillis();
        long timePlusDuration = task.getStartTime().getTimeInMillis() + task.getDurationHours() * 60*60*1000 + task.getDurationMinutes() * 60*1000;
        if(timePlusDuration < currentTime) {
            task.setDone(true);
            db.taskDao().update(task);
            updateTasksDone();
        }
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
        day.getDayTime().set(Calendar.HOUR_OF_DAY, start_day_time[0]);
        day.getDayTime().set(Calendar.MINUTE, start_day_time[1]);
        day.getDayTime().set(Calendar.SECOND, 0);
        day.getDayTime().set(Calendar.MILLISECOND, 0);
        tasks.clear();

        AsyncTask.execute(() ->{
            Calendar pickedDay = GlobalFunctions.convertStringDateToCalendar(day.getDayString());

            Day existingDay = db.dayDao().getDayByDayString(GlobalFunctions.convertCalendarToDateString(pickedDay));
            if(existingDay == null){
                Day newDay = new Day(pickedDay, GlobalFunctions.convertCalendarToDateString(pickedDay));
                long newDayId = db.dayDao().insert(newDay);
                day.setId(newDayId);
                day.setDayTime(newDay.getDayTime());
                day.setDayString(newDay.getDayString());
                lastPositionNumber = -1;
            }else{
                updateTasksDone();
                tasks.addAll(db.taskDao().getTasksByDayId(existingDay.getId()));
                lastPositionNumber = tasks.size()-1;
                day.setId(existingDay.getId());
                day.setDayString(GlobalFunctions.convertCalendarToDateString(existingDay.getDayTime()));
                if(tasks.size() > 0){
                    day.setDayTime((Calendar) tasks.get(tasks.size()-1).getStartTime().clone());
                    day.getDayTime().add(Calendar.HOUR, tasks.get(tasks.size()-1).getDurationHours());
                    day.getDayTime().add(Calendar.MINUTE, tasks.get(tasks.size()-1).getDurationMinutes());
                }

            }

            runOnUiThread(() -> {
                taskRecyclerAdapter.notifyDataSetChanged();

                task_day.setText(pickedDay.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()) + " " +
                        pickedDay.get(Calendar.DAY_OF_MONTH) + " " +
                        pickedDay.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) + " " +
                        pickedDay.get(Calendar.YEAR));
            });
        });
    }

    public void showPickDayDialog(View v){
        pickDayDialog.show();
        CalendarView pickDayCalendar = pickDayDialog.findViewById(R.id.pickDayCalendarView);
        pickDayCalendar.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            day.getDayTime().set(year, month, dayOfMonth);
            day.setDayString(GlobalFunctions.convertCalendarToDateString(day.getDayTime()));

            updateTasksView();
            pickDayDialog.hide();
        });
    }

    public void showCopyFromDayDialog(View v){
        pickDayDialog.show();
        CalendarView pickDayCalendar = pickDayDialog.findViewById(R.id.pickDayCalendarView);
        pickDayCalendar.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            day.getDayTime().set(year, month, dayOfMonth, start_day_time[0], start_day_time[1]);
            tasks.clear();
            lastPositionNumber = -1;

            AsyncTask.execute(() -> {
                Day foundDay = db.dayDao().getDayByDayString(GlobalFunctions
                        .convertCalendarToDateString(day.getDayTime()));

                if(foundDay != null){

                    List<Task> copyTasks = db.taskDao().getTasksByDayId(foundDay.getId());


                    copyTasks.forEach(ct -> {
                        int newPositionNumber = lastPositionNumber+1;
                        lastPositionNumber++;
                        Calendar newCalendar = (Calendar) day.getDayTime().clone();
                        long newId = this.db.taskDao().insert(new Task(ct.getTitle(),
                                ct.getDurationHours(), ct.getDurationMinutes(),
                                newPositionNumber, false, newCalendar, day.getId()));

                        tasks.add(new Task(newId, ct.getTitle(), ct.getDurationHours(), ct.getDurationMinutes(),
                                newPositionNumber, false, newCalendar, day.getId()));

                        day.getDayTime().add(Calendar.HOUR, ct.getDurationHours());
                        day.getDayTime().add(Calendar.MINUTE, ct.getDurationMinutes());
                    });
                }

                runOnUiThread(() -> taskRecyclerAdapter.notifyItemInserted(tasks.size() - 1));
            });

            pickDayDialog.hide();
            showBottomPanel(false);
            Toast.makeText(MainActivity.this, R.string.copied, Toast.LENGTH_SHORT).show();
        });
    }

    public void showInsertDefaultTaskDialog(View v){
        insertDefaultTaskDialog.show();

        defaultTasksRecyclerView = insertDefaultTaskDialog.findViewById(R.id.insertDefaultTaskRecyclerView);
        defaultTaskRecyclerAdapter = new DefaultTaskRecyclerAdapter(MainActivity.this);
        defaultTasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        defaultTasksRecyclerView.setAdapter(defaultTaskRecyclerAdapter);

        AsyncTask.execute(() -> {
            defaultTasks = db.defaultTaskDao().getAll();
            runOnUiThread(() -> defaultTaskRecyclerAdapter.notifyDataSetChanged());
        });

    }

    public void showTasksDone(View v){
        if(!showTasksDone){
            showTasksDone = true;
            taskRecyclerAdapter.notifyDataSetChanged();
            TextView showTasksDoneText = findViewById(R.id.showTasksDone);
            showTasksDoneText.setText(R.string.hide_tasks_done);
        }else{
            showTasksDone = false;
            taskRecyclerAdapter.notifyDataSetChanged();
            TextView showTasksDoneText = findViewById(R.id.showTasksDone);
            showTasksDoneText.setText(R.string.show_tasks_done);
        }
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
            holder.time.setText(GlobalFunctions.convertCalendarToTimeString(tasks.get(position).getStartTime()));
            String minutes = tasks.get(position).getDurationMinutes() < 10 ? "0" + tasks.get(position).getDurationMinutes() : "" + tasks.get(position).getDurationMinutes();
            holder.duration.setText(String.format("%d:%sh", tasks.get(position).getDurationHours(), minutes));
            Calendar finishTime = (Calendar) tasks.get(position).getStartTime().clone();
            finishTime.add(Calendar.HOUR, tasks.get(position).getDurationHours());
            finishTime.add(Calendar.MINUTE, tasks.get(position).getDurationMinutes());
            holder.finishTime.setText(GlobalFunctions.convertCalendarToTimeString(finishTime));

            holder.taskDone.setChecked(tasks.get(position).isDone());

            holder.itemView.setOnClickListener(v -> {
                editPosition = position;
                showBottomPanel(true);
            });

            if(!showTasksDone){
                if(tasks.get(position).isDone()){
                    holder.itemView.setVisibility(View.GONE);
                    ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
                    layoutParams.width = 0;
                    layoutParams.height = 0;
                    holder.itemView.setLayoutParams(layoutParams);
                }
            }else{
                holder.itemView.setVisibility(View.VISIBLE);
                ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                holder.itemView.setLayoutParams(layoutParams);
            }
        }




        @Override
        public int getItemCount() {
            return tasks.size();
        }

        public class TaskViewHolder extends RecyclerView.ViewHolder {
            TextView title, time, duration, finishTime;
            CheckBox taskDone;

            public TaskViewHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.task_row_title);
                time = itemView.findViewById(R.id.task_time_text);
                duration = itemView.findViewById(R.id.task_duration_text);
                finishTime = itemView.findViewById(R.id.task_finish_time_text);
                taskDone = itemView.findViewById(R.id.checkBoxTaskDone);
            }
        }
    }

    class DefaultTaskRecyclerAdapter extends RecyclerView.Adapter<DefaultTaskRecyclerAdapter.DefaultTaskViewHolder> {
        Context context;

        public DefaultTaskRecyclerAdapter(Context c) {
            this.context = c;
        }

        @NonNull
        @Override
        public DefaultTaskRecyclerAdapter.DefaultTaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.default_task_row, parent, false);
            return new DefaultTaskRecyclerAdapter.DefaultTaskViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DefaultTaskRecyclerAdapter.DefaultTaskViewHolder holder, int position) {
            holder.title.setText(defaultTasks.get(position).getTitle());
            String minutes = defaultTasks.get(position).getDurationMinutes() < 10 ? "0" + defaultTasks.get(position).getDurationMinutes() : "" + defaultTasks.get(position).getDurationMinutes();
            holder.duration.setText(String.format("%d:%sh", defaultTasks.get(position).getDurationHours(), minutes));

            holder.itemView.setOnClickListener(v -> {
                AsyncTask.execute(() -> {
                    int newPositionNumber = lastPositionNumber+1;
                    lastPositionNumber++;
                    Calendar newCalendar = (Calendar) day.getDayTime().clone();

                    long newId = db.taskDao().insert(new Task(defaultTasks.get(position).getTitle(),
                            defaultTasks.get(position).getDurationHours(), defaultTasks.get(position).getDurationMinutes(),
                            newPositionNumber, false, newCalendar, day.getId()));
                    tasks.add(new Task(newId, defaultTasks.get(position).getTitle(),
                            defaultTasks.get(position).getDurationHours(), defaultTasks.get(position).getDurationMinutes(),
                            newPositionNumber, false, newCalendar, day.getId()));

                    day.getDayTime().add(Calendar.HOUR, defaultTasks.get(position).getDurationHours());
                    day.getDayTime().add(Calendar.MINUTE,  defaultTasks.get(position).getDurationMinutes());

                    runOnUiThread(() -> {
                        taskRecyclerAdapter.notifyItemInserted(tasks.size() - 1);
                        insertDefaultTaskDialog.hide();
                        showBottomPanel(false);
                    });
                });

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
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();

            if(fromPosition != toPosition) {
                if (fromPosition < toPosition) {
                    tasks.get(toPosition).setStartTime((Calendar) tasks.get(fromPosition).getStartTime().clone());
                    tasks.get(fromPosition).getStartTime().add(Calendar.HOUR, tasks.get(toPosition).getDurationHours());
                    tasks.get(fromPosition).getStartTime().add(Calendar.MINUTE, tasks.get(toPosition).getDurationMinutes());
                } else {
                    tasks.get(fromPosition).setStartTime((Calendar) tasks.get(toPosition).getStartTime().clone());
                    tasks.get(toPosition).getStartTime().add(Calendar.HOUR, tasks.get(fromPosition).getDurationHours());
                    tasks.get(toPosition).getStartTime().add(Calendar.MINUTE, tasks.get(fromPosition).getDurationMinutes());
                }

                tasks.get(fromPosition).setPositionNumber(toPosition);
                tasks.get(toPosition).setPositionNumber(fromPosition);

                List<Task> updateTasks = Arrays.asList(tasks.get(fromPosition), tasks.get(toPosition));

                AsyncTask.execute(() -> {
                    db.taskDao().updateAll(updateTasks);
                    sendNotificationIntent();
                });

                recyclerView.getAdapter().notifyItemChanged(fromPosition);
                recyclerView.getAdapter().notifyItemChanged(toPosition);
            }

            Collections.swap(tasks, fromPosition, toPosition);
            recyclerView.getAdapter().notifyItemMoved(fromPosition, toPosition);
            recyclerView.getAdapter().notifyItemRangeChanged(Math.min(fromPosition, toPosition), Math.max(fromPosition, toPosition));

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
                lastPositionNumber--;
                day.getDayTime().add(Calendar.HOUR, -removedTask.getDurationHours());
                day.getDayTime().add(Calendar.MINUTE, -removedTask.getDurationMinutes());

                AsyncTask.execute(() -> {
                    db.taskDao().delete(removedTask);
                    int[] pos = {position};
                    tasks.forEach(dt -> {
                        if(dt.getPositionNumber() > position){
                            dt.setPositionNumber(pos[0]++);
                            dt.getStartTime().add(Calendar.HOUR, -removedTask.getDurationHours());
                            dt.getStartTime().add(Calendar.MINUTE, -removedTask.getDurationMinutes());
                        }
                    });

                    if(tasks.size() > 0)
                        runOnUiThread(() -> taskRecyclerAdapter.notifyDataSetChanged());

                    db.taskDao().updateAll(tasks);
                    sendNotificationIntent();
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