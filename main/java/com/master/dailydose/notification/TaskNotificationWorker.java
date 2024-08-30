package com.master.dailydose.notification;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.master.dailydose.details.TaskDetail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TaskNotificationWorker extends Worker {

    public TaskNotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Logic to check for tasks that are due and send notifications
        checkAndSendTaskNotifications();
        return Result.success();
    }

    private void checkAndSendTaskNotifications() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        db.collection("users").document(userId)
                .collection("tasks")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            TaskDetail taskDetail = document.toObject(TaskDetail.class);
                            String dateStr = taskDetail.getDate();
                            String timeStr = taskDetail.getTime();

                            if (dateStr != null && timeStr != null) {
                                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                                try {
                                    Date taskDate = format.parse(dateStr + " " + timeStr);
                                    Date now = new Date();

                                    if (taskDate != null && taskDate.equals(now)) {
                                        NotificationHelper.sendNotification(
                                                getApplicationContext(),
                                                "Task Due",
                                                "Your task '" + taskDetail.getTaskName() + "' is due now."
                                        );
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
    }

}
