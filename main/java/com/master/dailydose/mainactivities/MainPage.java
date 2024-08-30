package com.master.dailydose.mainactivities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.master.dailydose.R;
import com.master.dailydose.adapter.TaskDetailAdapter;
import com.master.dailydose.details.TaskDetail;
import com.master.dailydose.notification.NotificationHelper;
import com.master.dailydose.notification.NotificationWorker;
import com.master.dailydose.notification.TaskNotificationWorker;
import com.master.dailydose.viewmodel.DailyViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainPage extends AppCompatActivity {

    private static final String TAG = "MainPage";
    private RecyclerView recyclerView;
    private TaskDetailAdapter customAdapter;
    private List<TaskDetail> taskList;
    private FirebaseFirestore db;
    DailyViewModel viewModel;
    ImageView complete, pending, cancel, ongoing;
    FirebaseUser firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);



        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                TaskNotificationWorker.class,
                15, TimeUnit.MINUTES)
                .build();

        WorkManager.getInstance(this).enqueue(workRequest);


        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setItemAnimator(null);
        taskList = new ArrayList<>();
        viewModel = new ViewModelProvider(this).get(DailyViewModel.class);
        db = FirebaseFirestore.getInstance();
        complete = findViewById(R.id.complete);
        pending = findViewById(R.id.pending);
        cancel = findViewById(R.id.canceltask);
        ongoing = findViewById(R.id.ongoingtask);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        customAdapter = new TaskDetailAdapter(this, taskList);
        recyclerView.setAdapter(customAdapter);
        firestore = FirebaseAuth.getInstance().getCurrentUser();

        loadTasksByCategory(firestore.getEmail());

        View.OnClickListener categoryClickListener = v -> {
            Intent intent = new Intent(MainPage.this, TaskViewCategory.class);
            startActivity(intent);
        };

        complete.setOnClickListener(categoryClickListener);
        pending.setOnClickListener(categoryClickListener);
        cancel.setOnClickListener(categoryClickListener);
        ongoing.setOnClickListener(categoryClickListener);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainPage.this, AddTaskActivity.class);
            startActivityForResult(intent, 1);
        });

        // Setup swipe actions
        setupSwipeActions();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data != null) {
                String taskName = data.getStringExtra("taskName");
                String date = data.getStringExtra("Date");
                String time = data.getStringExtra("Time");
                boolean monthPlan = data.getBooleanExtra("monthPlan", false);
                boolean weekPlan = data.getBooleanExtra("weekPlan", false);
                boolean workPlan = data.getBooleanExtra("workPlan", false);
                boolean yearPlan = data.getBooleanExtra("yearPlan", false);
                ArrayList<String> selectedTags = data.getStringArrayListExtra("selectedTags");

                TaskDetail newTask = new TaskDetail(taskName, date, time, selectedTags, monthPlan, weekPlan, workPlan, yearPlan);

                taskList.add(newTask);
                customAdapter.notifyDataSetChanged();
                addTaskToFirestore(firestore.getEmail(), newTask);

                // Schedule notification
                if (date != null && time != null) {
                    String dateTime = date + " " + time;
                    scheduleNotification(newTask, dateTime);
                }
            } else {
                Toast.makeText(this, "No data received from AddTaskActivity", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void loadTasksByCategory(String userId) {
        db.collection("users").document(userId)
                .collection("tasks")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int completedCount = 0;
                        int pendingCount = 0;
                        int canceledCount = 0;
                        int ongoingCount = 0;
                        taskList.clear();  // Clear the list to prevent duplicates

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            TaskDetail taskDetail = document.toObject(TaskDetail.class);
                            taskList.add(taskDetail); // Add task to the list

                            String dateStr = taskDetail.getDate();
                            String timeStr = taskDetail.getTime();

                            if (dateStr != null && timeStr != null) {
                                @SuppressLint("SimpleDateFormat")
                                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                                try {
                                    Date taskDate = format.parse(dateStr + " " + timeStr);
                                    Date now = new Date();

                                    if (taskDate != null) {
                                        long timeDifference = now.getTime() - taskDate.getTime();

                                        // Handle Completed tasks
                                        if ("Completed".equals(taskDetail.getStatus())) {
                                            completedCount++;
                                            if (timeDifference > 2 * 24 * 60 * 60 * 1000) { // 2 days in milliseconds
                                                removeTaskFromFirestore(userId, taskDetail.getTaskName());
                                                taskList.remove(taskDetail);
                                            }
                                        }
                                        // Handle Canceled tasks
                                        else if ("Canceled".equals(taskDetail.getStatus())) {
                                            canceledCount++;
                                            if (timeDifference > 24 * 60 * 60 * 1000) { // 24 hours in milliseconds
                                                removeTaskFromFirestore(userId, taskDetail.getTaskName());
                                                taskList.remove(taskDetail);
                                            }
                                        }
                                        // Handle Ongoing tasks
                                        else if (taskDate.compareTo(now) <= 0) {
                                            ongoingCount++;
                                            taskDetail.setStatus("Ongoing");
                                            updateTaskStatusInFirestore(userId, taskDetail);
                                        }
                                        // Handle Pending tasks
                                        else {
                                            pendingCount++;
                                        }

                                        // Schedule notification if needed
                                        if (taskDetail.getDate() != null && taskDetail.getTime() != null) {
                                            String dateTime = taskDetail.getDate() + " " + taskDetail.getTime();
                                            scheduleNotification(taskDetail, dateTime);
                                        }
                                    }
                                } catch (ParseException e) {
                                    Log.e(TAG, "Error parsing date and time for task: " + taskDetail.getTaskName(), e);
                                }
                            }
                        }

                        // Update TextViews
                        updateTaskCountViews(completedCount, pendingCount, canceledCount, ongoingCount);
                        customAdapter.notifyDataSetChanged(); // Notify the adapter of data changes
                    } else {
                        Toast.makeText(this, "Error getting documents: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void removeTaskFromFirestore(String userId, String taskName) {
        db.collection("users").document(userId)
                .collection("tasks").document(taskName)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Task successfully removed from Firestore"))
                .addOnFailureListener(e -> Log.w(TAG, "Error removing task from Firestore", e));
    }
    private void scheduleNotification(TaskDetail taskDetail, String dateTime) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        try {
            Date taskDate = format.parse(dateTime);
            if (taskDate != null) {
                long delay = taskDate.getTime() - System.currentTimeMillis();
                if (delay > 0) {
                    Data data = new Data.Builder()
                            .putString("TASK_NAME", taskDetail.getTaskName())
                            .build();

                    OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                            .setInputData(data)
                            .build();

                    WorkManager.getInstance(this).enqueue(workRequest);
                }
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date and time for notification", e);
        }
    }


    private void updateTaskCountViews(int completedCount, int pendingCount, int canceledCount, int ongoingCount) {
        TextView completedTaskTextView = findViewById(R.id.completed_task_label);
        TextView pendingTaskTextView = findViewById(R.id.pending_task_label);
        TextView canceledTaskTextView = findViewById(R.id.canceled_task_label);
        TextView ongoingTaskTextView = findViewById(R.id.ongoing_task_label);

        if (completedTaskTextView != null) {
            completedTaskTextView.setText("Completed Tasks: " + completedCount);
        }

        if (pendingTaskTextView != null) {
            pendingTaskTextView.setText("Pending Tasks: " + pendingCount);
        }

        if (canceledTaskTextView != null) {
            canceledTaskTextView.setText("Canceled Tasks: " + canceledCount);
        }

        if (ongoingTaskTextView != null) {
            ongoingTaskTextView.setText("Ongoing Tasks: " + ongoingCount);
        }
    }

    private void setupSwipeActions() {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                TaskDetail task = taskList.get(position);

                String notificationTitle;
                String notificationMessage;

                if (direction == ItemTouchHelper.RIGHT) {
                    task.setStatus("Completed");
                    notificationTitle = "Task Completed";
                    notificationMessage = "Your task '" + task.getTaskName() + "' has been marked as completed.";
                } else if (direction == ItemTouchHelper.LEFT) {
                    task.setStatus("Canceled");
                    notificationTitle = "Task Canceled";
                    notificationMessage = "Your task '" + task.getTaskName() + "' has been canceled.";
                } else {
                    return;
                }

                // Update Firestore and send notification
                updateTaskStatusInFirestore(firestore.getEmail(), task);
                NotificationHelper.sendNotification(MainPage.this, notificationTitle, notificationMessage);

                taskList.remove(position);
                customAdapter.notifyItemRemoved(position);
            }
        };

        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);
    }

    private void addTaskToFirestore(String userId, TaskDetail taskDetail) {
        String taskName = taskDetail.getTaskName();
        if (taskName != null) {
            db.collection("users").document(userId)
                    .collection("tasks").document(taskName)
                    .set(taskDetail, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Task successfully added to Firestore"))
                    .addOnFailureListener(e -> Log.w(TAG, "Error adding task to Firestore", e));
        } else {
            Log.e(TAG, "Task name is null, cannot add task to Firestore");
        }
    }

    private void updateTaskStatusInFirestore(String userId, TaskDetail taskDetail) {
        String taskName = taskDetail.getTaskName();
        if (taskName != null) {
            db.collection("users").document(userId)
                    .collection("tasks").document(taskName)
                    .set(taskDetail, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Task status successfully updated in Firestore"))
                    .addOnFailureListener(e -> Log.w(TAG, "Error updating task status in Firestore", e));
        } else {
            Log.e(TAG, "Task name is null, cannot update task status in Firestore");
        }
    }
}
