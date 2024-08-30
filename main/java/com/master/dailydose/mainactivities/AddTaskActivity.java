package com.master.dailydose.mainactivities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.master.dailydose.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AddTaskActivity extends AppCompatActivity {

    private EditText taskNameEditText;
    private CheckBox monthPlanCheckBox, weekPlanCheckBox, workPlanCheckBox, yearPlanCheckBox;
    private Button saveButton;
    private TextView estDateEditText, estTimeEditText;
    private TextView officeTag, homeTag, urgentTag, workTag;
    private String selectedDate, selectedStartTime, selectedEndTime, selectedTime;
    private Set<String> selectedTags = new HashSet<>();
    private Calendar taskTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // Initialize views
        taskNameEditText = findViewById(R.id.task_name);
        estDateEditText = findViewById(R.id.est_date);
        estTimeEditText = findViewById(R.id.est_time);
        saveButton = findViewById(R.id.save);

        // Initialize checkboxes
        monthPlanCheckBox = findViewById(R.id.monthplan);
        weekPlanCheckBox = findViewById(R.id.weekPlan);
        workPlanCheckBox = findViewById(R.id.workPlan);
        yearPlanCheckBox = findViewById(R.id.yearPlan);

        // Initialize tags
        officeTag = findViewById(R.id.office_tag);
        homeTag = findViewById(R.id.home_tag);
        urgentTag = findViewById(R.id.urgent_tag);
        workTag = findViewById(R.id.work_tag);

        estDateEditText.setOnClickListener(v -> showDatePicker());
        estTimeEditText.setOnClickListener(v -> showTimeRangePicker());

        setupTagButton(officeTag, "Office");
        setupTagButton(homeTag, "Home");
        setupTagButton(urgentTag, "Urgent");
        setupTagButton(workTag, "Work");

        saveButton.setOnClickListener(v -> saveTask());
    }

    private void setupTagButton(TextView tag, String tagName) {
        tag.setOnClickListener(v -> toggleTag(tag, tagName));
    }

    private void toggleTag(TextView tag, String tagName) {
        if (tag.isSelected()) {
            tag.setSelected(false);
            tag.setBackgroundColor(getResources().getColor(R.color.tag_button_urgent));
            selectedTags.remove(tagName);
        } else {
            tag.setSelected(true);
            tag.setBackgroundColor(getResources().getColor(R.color.tag_button_selected));
            selectedTags.add(tagName);
        }
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
            estDateEditText.setText(selectedDate);
            taskTime = Calendar.getInstance();
            taskTime.set(Calendar.YEAR, year1);
            taskTime.set(Calendar.MONTH, month1);
            taskTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        }, year, month, day);
        datePickerDialog.show();
    }

    private void showTimeRangePicker() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog startTimePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute1) -> {
            selectedStartTime = formatTime(hourOfDay, minute1);
            if (taskTime != null) {
                taskTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                taskTime.set(Calendar.MINUTE, minute1);
                taskTime.set(Calendar.SECOND, 0);
            }

            TimePickerDialog endTimePickerDialog = new TimePickerDialog(this, (view1, endHour, endMinute) -> {
                selectedEndTime = formatTime(endHour, endMinute);
                selectedTime = selectedStartTime + " - " + selectedEndTime;
                estTimeEditText.setText(selectedTime);
            }, hour, minute, false);
            endTimePickerDialog.show();
        }, hour, minute, false);
        startTimePickerDialog.show();
    }

    @SuppressLint("DefaultLocale")
    private String formatTime(int hourOfDay, int minute) {
        String amPm = (hourOfDay >= 12) ? "PM" : "AM";
        int hour = (hourOfDay % 12 == 0) ? 12 : hourOfDay % 12;
        return String.format("%02d:%02d %s", hour, minute, amPm);
    }

    private void saveTask() {
        String taskName = taskNameEditText.getText().toString().trim();
        boolean monthPlan = monthPlanCheckBox.isChecked();
        boolean weekPlan = weekPlanCheckBox.isChecked();
        boolean workPlan = workPlanCheckBox.isChecked();
        boolean yearPlan = yearPlanCheckBox.isChecked();

        if (taskName.isEmpty() || selectedDate == null || selectedStartTime == null || selectedEndTime == null) {
            Toast.makeText(this, "Please fill all the details", Toast.LENGTH_SHORT).show();
        } else {
            // Get the current user's ID
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid(); // Use UID instead of email

                Map<String, Object> task = new HashMap<>();
                task.put("taskName", taskName);
                task.put("Date", selectedDate);
                task.put("Time", selectedTime);
                task.put("monthPlan", monthPlan);
                task.put("weekPlan", weekPlan);
                task.put("workPlan", workPlan);
                task.put("yearPlan", yearPlan);
                task.put("selectedTags", new ArrayList<>(selectedTags));

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("users").document(userId)
                        .collection("tasks")
                        .document(taskName) // Use taskName as document ID
                        .set(task)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(AddTaskActivity.this, "Task saved", Toast.LENGTH_SHORT).show();

                            // Return the result to MainPage
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("taskName", taskName);
                            resultIntent.putExtra("Date", selectedDate);
                            resultIntent.putExtra("Time", selectedTime);
                            resultIntent.putExtra("monthPlan", monthPlan);
                            resultIntent.putExtra("weekPlan", weekPlan);
                            resultIntent.putExtra("workPlan", workPlan);
                            resultIntent.putExtra("yearPlan", yearPlan);
                            resultIntent.putStringArrayListExtra("selectedTags", new ArrayList<>(selectedTags));

                            setResult(RESULT_OK, resultIntent);
                            finish();
                        })
                        .addOnFailureListener(e -> Toast.makeText(AddTaskActivity.this, "Error adding task", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // No need to handle the alarm permission result anymore
    }

    @Override
    protected void onResume() {
        super.onResume();
        // No need to check for alarm permissions anymore
    }
}
