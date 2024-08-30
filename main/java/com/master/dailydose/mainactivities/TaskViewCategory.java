package com.master.dailydose.mainactivities;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.master.dailydose.R;
import com.master.dailydose.adapter.TaskDetailAdapter;
import com.master.dailydose.details.TaskDetail;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TaskViewCategory extends AppCompatActivity {
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_view_category);

        RecyclerView completedRecyclerView = findViewById(R.id.completed_task_list);
        RecyclerView pendingRecyclerView = findViewById(R.id.pending_task_list);
        RecyclerView canceledRecyclerView = findViewById(R.id.canceled_task_list);
        RecyclerView ongoingRecyclerView = findViewById(R.id.ongoing_task_list);

        completedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        pendingRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        canceledRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ongoingRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        completedRecyclerView.setItemAnimator(null);
        pendingRecyclerView.setItemAnimator(null);
        canceledRecyclerView.setItemAnimator(null);
        ongoingRecyclerView.setItemAnimator(null);

        // Initialize adapters for each category
        TaskDetailAdapter completedAdapter = new TaskDetailAdapter(this, new ArrayList<>());
        TaskDetailAdapter pendingAdapter = new TaskDetailAdapter(this, new ArrayList<>());
        TaskDetailAdapter canceledAdapter = new TaskDetailAdapter(this, new ArrayList<>());
        TaskDetailAdapter ongoingAdapter = new TaskDetailAdapter(this, new ArrayList<>());

        completedRecyclerView.setAdapter(completedAdapter);
        pendingRecyclerView.setAdapter(pendingAdapter);
        canceledRecyclerView.setAdapter(canceledAdapter);
        ongoingRecyclerView.setAdapter(ongoingAdapter);

        db = FirebaseFirestore.getInstance();

        // Get the current user's ID
        userId = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        // Load tasks categorized by status for the current user
        loadTasksByCategory(completedAdapter, pendingAdapter, canceledAdapter, ongoingAdapter);
    }

    private void updateTaskStatusInFirestore(String userId, TaskDetail taskDetail, String newStatus) {
        String taskName = taskDetail.getTaskName();
        if (taskName != null && !taskName.isEmpty()) {
            taskDetail.setStatus(newStatus); // Update status in the task detail
            db.collection("users").document(userId)
                    .collection("tasks").document(taskName)
                    .set(taskDetail, SetOptions.merge()) // Ensure merging to update existing tasks
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Task status updated for: " + taskName))
                    .addOnFailureListener(e -> Log.e(TAG, "Error updating task status for " + taskName, e));
        } else {
            Log.w(TAG, "Task name is null or empty for status update.");
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadTasksByCategory(TaskDetailAdapter completedAdapter, TaskDetailAdapter pendingAdapter,
                                     TaskDetailAdapter canceledAdapter, TaskDetailAdapter ongoingAdapter) {
        db.collection("users").document(userId)
                .collection("tasks")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<TaskDetail> completedTasks = new ArrayList<>();
                        List<TaskDetail> pendingTasks = new ArrayList<>();
                        List<TaskDetail> canceledTasks = new ArrayList<>();
                        List<TaskDetail> ongoingTasks = new ArrayList<>();

                        Date now = new Date();
                        long oneDayInMillis = 24 * 60 * 60 * 1000;
                        long twoDaysInMillis = 2 * 24 * 60 * 60 * 1000;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            TaskDetail taskDetail = document.toObject(TaskDetail.class);
                            if (taskDetail != null && taskDetail.getTaskName() != null) {
                                String dateStr = taskDetail.getDate();
                                String timeStr = taskDetail.getTime();

                                if ("Completed".equals(taskDetail.getStatus())) {
                                    completedTasks.add(taskDetail);
                                } else if ("Canceled".equals(taskDetail.getStatus())) {
                                    Date taskDate = getTaskDate(dateStr, timeStr);
                                    if (taskDate != null && (now.getTime() - taskDate.getTime()) > oneDayInMillis) {
                                        // Remove from Firebase
                                        deleteTaskFromFirestore(userId, taskDetail);
                                    } else {
                                        canceledTasks.add(taskDetail);
                                    }
                                } else if (dateStr != null && timeStr != null) {
                                    Date taskDate = getTaskDate(dateStr, timeStr);
                                    if (taskDate != null) {
                                        long diffInMillis = taskDate.getTime() - now.getTime();

                                        if (diffInMillis > twoDaysInMillis) {
                                            pendingTasks.add(taskDetail);
                                        } else if (diffInMillis > 0 && diffInMillis <= oneDayInMillis) {
                                            ongoingTasks.add(taskDetail);
                                        } else if (diffInMillis <= 0) {
                                            canceledTasks.add(taskDetail);
                                        }
                                    }
                                }
                            }
                        }

                        // Remove tasks from completedAdapter after 2 days
                        removeOldCompletedTasks(completedTasks);

                        completedAdapter.updateTasks(completedTasks);
                        pendingAdapter.updateTasks(pendingTasks);
                        canceledAdapter.updateTasks(canceledTasks);
                        ongoingAdapter.updateTasks(ongoingTasks);

                        completedAdapter.notifyDataSetChanged();
                        pendingAdapter.notifyDataSetChanged();
                        canceledAdapter.notifyDataSetChanged();
                        ongoingAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Error getting documents: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private Date getTaskDate(String dateStr, String timeStr) {
        try {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            return format.parse(dateStr + " " + timeStr);
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date: " + dateStr + " " + timeStr, e);
            return null;
        }
    }

    private void removeOldCompletedTasks(List<TaskDetail> completedTasks) {
        Date now = new Date();
        long twoDaysInMillis = 2 * 24 * 60 * 60 * 1000;
        List<TaskDetail> tasksToRemove = new ArrayList<>();

        for (TaskDetail task : completedTasks) {
            Date taskDate = getTaskDate(task.getDate(), task.getTime());
            if (taskDate != null && (now.getTime() - taskDate.getTime()) > twoDaysInMillis) {
                tasksToRemove.add(task);
            }
        }

        for (TaskDetail task : tasksToRemove) {
            deleteTaskFromFirestore(userId, task);
            completedTasks.remove(task);
        }
    }


    private void deleteTaskFromFirestore(String userId, TaskDetail taskDetail) {
        String taskName = taskDetail.getTaskName();
        if (taskName != null && !taskName.isEmpty()) {
            db.collection("users").document(userId)
                    .collection("tasks").document(taskName)
                    .delete()
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Task successfully deleted: " + taskName))
                    .addOnFailureListener(e -> Log.e(TAG, "Error deleting task: " + taskName, e));
        } else {
            Log.w(TAG, "Task name is null or empty for deletion.");
        }
    }
}
