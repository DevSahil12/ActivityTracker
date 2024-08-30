package com.master.dailydose.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.master.dailydose.R;
import com.master.dailydose.details.TaskDetail;

import java.util.ArrayList;
import java.util.List;

public class TaskDetailAdapter extends RecyclerView.Adapter<TaskDetailAdapter.TaskViewHolder> {

    private Context context;
    private List<TaskDetail> taskList;
    private FirebaseFirestore db;

    public TaskDetailAdapter(Context context, List<TaskDetail> taskList) {
        this.context = context;
        this.taskList = taskList;
        this.db = FirebaseFirestore.getInstance(); // Initialize Firestore
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.add_listitems, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskDetail taskDetail = taskList.get(position);
        holder.taskName.setText(taskDetail.getTaskName());
        holder.taskTime.setText(taskDetail.getTime());
        holder.taskDate.setText(taskDetail.getDate());
        Log.d("TaskDetailAdapter", "Binding task: " + taskDetail.getTaskName());

        holder.tagMonth.setVisibility(taskDetail.isMonthPlan() ? View.VISIBLE : View.GONE);
        holder.tagWeek.setVisibility(taskDetail.isWeekPlan() ? View.VISIBLE : View.GONE);
        holder.tagDay.setVisibility(taskDetail.isWorkPlan() ? View.VISIBLE : View.GONE);
        holder.tagYear.setVisibility(taskDetail.isYearPlan() ? View.VISIBLE : View.GONE);

        holder.taskTime.setText(taskDetail.getTime() != null ? taskDetail.getTime() : "No Time Set");
        holder.taskDate.setText(taskDetail.getDate() != null ? taskDetail.getDate() : "No Date Set");

        List<String> selectedTags = taskDetail.getSelectedTags();
        if (selectedTags == null) {
            selectedTags = new ArrayList<>();
        }

        holder.officeTagTextView.setVisibility(selectedTags.contains("Office") ? View.VISIBLE : View.GONE);
        holder.homeTagTextView.setVisibility(selectedTags.contains("Home") ? View.VISIBLE : View.GONE);
        holder.urgentTagTextView.setVisibility(selectedTags.contains("Urgent") ? View.VISIBLE : View.GONE);
        holder.workTagTextView.setVisibility(selectedTags.contains("Work") ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public void removeTask(int position) {
        TaskDetail task = taskList.get(position);
        taskList.remove(position);
        notifyItemRemoved(position);
        deleteTaskFromFirestore(task.getTaskName()); // Ensure you have an ID or unique identifier
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateTasks(List<TaskDetail> tasks) {
        this.taskList.clear();
        this.taskList.addAll(tasks);
        notifyDataSetChanged();
    }


    private void deleteTaskFromFirestore(String taskId) {
        db.collection("tasks").document(taskId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("TaskDetailAdapter", "Task successfully deleted from Firestore!"))
                .addOnFailureListener(e -> Log.w("TaskDetailAdapter", "Error deleting task from Firestore", e));
    }

    private void updateTaskInFirestore(TaskDetail task) {
        db.collection("tasks").document(task.getTaskName())
                .set(task)
                .addOnSuccessListener(aVoid -> Log.d("TaskDetailAdapter", "Task successfully updated in Firestore!"))
                .addOnFailureListener(e -> Log.w("TaskDetailAdapter", "Error updating task in Firestore", e));
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {

        TextView taskName, taskTime, taskDate, tagMonth, tagWeek, tagDay, tagYear, officeTagTextView, homeTagTextView, urgentTagTextView, workTagTextView;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskName = itemView.findViewById(R.id.display_task_name);
            taskTime = itemView.findViewById(R.id.display_task_time);
            taskDate = itemView.findViewById(R.id.display_task_date);
            tagMonth = itemView.findViewById(R.id.display_tag_month);
            tagWeek = itemView.findViewById(R.id.display_tag_week);
            tagDay = itemView.findViewById(R.id.display_tag_day);
            tagYear = itemView.findViewById(R.id.display_tag_year);
            officeTagTextView = itemView.findViewById(R.id.display_tag_office);
            homeTagTextView = itemView.findViewById(R.id.display_tag_home);
            urgentTagTextView = itemView.findViewById(R.id.display_tag_urgent);
            workTagTextView = itemView.findViewById(R.id.display_tag_work);
        }
    }
}
