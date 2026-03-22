package com.example.campuscompanion;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class TaskDetailActivity extends AppCompatActivity {

    private TextView tvDetailTitle;
    private TextView tvDetailDescription;
    private TextView tvDetailPriority;
    private MaterialButton btnMarkComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailDescription = findViewById(R.id.tvDetailDescription);
        tvDetailPriority = findViewById(R.id.tvDetailPriority);
        btnMarkComplete = findViewById(R.id.btnMarkComplete);

        Bundle extras = getIntent().getExtras();

        if (extras != null
                && extras.containsKey("TASK_TITLE")
                && extras.containsKey("TASK_DESCRIPTION")
                && extras.containsKey("TASK_PRIORITY")) {

            String title = extras.getString("TASK_TITLE", getString(R.string.missing_task_data));
            String description = extras.getString("TASK_DESCRIPTION", getString(R.string.missing_task_data));
            String priority = extras.getString("TASK_PRIORITY", getString(R.string.missing_task_data));

            tvDetailTitle.setText(title);
            tvDetailDescription.setText(description);
            tvDetailPriority.setText(priority);

        } else {
            tvDetailTitle.setText(getString(R.string.missing_task_data));
            tvDetailDescription.setText(getString(R.string.missing_task_data));
            tvDetailPriority.setText(getString(R.string.missing_task_data));
        }

        btnMarkComplete.setOnClickListener(v ->
                Toast.makeText(TaskDetailActivity.this,
                        getString(R.string.task_completed),
                        Toast.LENGTH_SHORT).show()
        );
    }
}