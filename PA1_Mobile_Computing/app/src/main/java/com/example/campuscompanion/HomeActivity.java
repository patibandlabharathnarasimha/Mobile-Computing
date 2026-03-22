package com.example.campuscompanion;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class HomeActivity extends AppCompatActivity {

    private TextInputLayout nameInputLayout;
    private TextInputEditText etName;
    private MaterialButton btnViewTasks;

    private static final String KEY_USERNAME = "username_state";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        nameInputLayout = findViewById(R.id.nameInputLayout);
        etName = findViewById(R.id.etName);
        btnViewTasks = findViewById(R.id.btnViewTasks);

        if (savedInstanceState != null) {
            String savedName = savedInstanceState.getString(KEY_USERNAME);
            etName.setText(savedName);
        }

        btnViewTasks.setOnClickListener(v -> {
            String name = etName.getText() != null ? etName.getText().toString().trim() : "";

            if (name.isEmpty()) {
                nameInputLayout.setError(getString(R.string.name_required));
                return;
            }

            nameInputLayout.setError(null);

            Intent intent = new Intent(HomeActivity.this, TaskListActivity.class);
            intent.putExtra("USERNAME", name);
            startActivity(intent);
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String currentName = etName.getText() != null ? etName.getText().toString() : "";
        outState.putString(KEY_USERNAME, currentName);
    }
}