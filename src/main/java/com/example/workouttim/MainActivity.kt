package com.example.workouttim

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val setsInput: EditText = findViewById(R.id.sets_input)
        val exerciseTimeMinutesInput: EditText = findViewById(R.id.exercise_time_minutes_input)
        val exerciseTimeSecondsInput: EditText = findViewById(R.id.exercise_time_seconds_input)
        val restTimeMinutesInput: EditText = findViewById(R.id.rest_time_minutes_input)
        val restTimeSecondsInput: EditText = findViewById(R.id.rest_time_seconds_input)

        // Set default values
        setsInput.setText("3")
        exerciseTimeMinutesInput.setText("1")
        exerciseTimeSecondsInput.setText("30")
        restTimeMinutesInput.setText("0")
        restTimeSecondsInput.setText("30")

        val createSetButton: Button = findViewById(R.id.create_set_button)
        createSetButton.setOnClickListener {
            val sets = setsInput.text.toString().toIntOrNull()
            val exerciseTimeMinutes = exerciseTimeMinutesInput.text.toString().toIntOrNull() ?: 0
            val exerciseTimeSeconds = exerciseTimeSecondsInput.text.toString().toIntOrNull() ?: 0
            val restTimeMinutes = restTimeMinutesInput.text.toString().toIntOrNull() ?: 0
            val restTimeSeconds = restTimeSecondsInput.text.toString().toIntOrNull() ?: 0

            val exerciseTime = exerciseTimeMinutes * 60 + exerciseTimeSeconds
            val restTime = restTimeMinutes * 60 + restTimeSeconds

            if (sets != null && exerciseTime > 0 && restTime > 0) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Enter Exercise Name")

                val input = EditText(this)
                builder.setView(input)

                builder.setPositiveButton("OK") { dialog, _ ->
                    val exerciseName = input.text.toString()
                    if (exerciseName.isNotEmpty()) {
                        saveExercise(exerciseName, sets, exerciseTime, restTime)
                        Toast.makeText(this, "Exercise saved", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Please enter a valid name", Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss()
                }
                builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

                builder.show()
            } else {
                Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
            }
        }

        val quickstartButton: Button = findViewById(R.id.quickstart_button)
        quickstartButton.setOnClickListener {
            val sets = setsInput.text.toString().toIntOrNull()
            val exerciseTimeMinutes = exerciseTimeMinutesInput.text.toString().toIntOrNull() ?: 0
            val exerciseTimeSeconds = exerciseTimeSecondsInput.text.toString().toIntOrNull() ?: 0
            val restTimeMinutes = restTimeMinutesInput.text.toString().toIntOrNull() ?: 0
            val restTimeSeconds = restTimeSecondsInput.text.toString().toIntOrNull() ?: 0

            val exerciseTime = exerciseTimeMinutes * 60 + exerciseTimeSeconds
            val restTime = restTimeMinutes * 60 + restTimeSeconds

            if (sets != null && exerciseTime > 0 && restTime > 0) {
                val intent = Intent(this, ExerciseStatusActivity::class.java).apply {
                    putExtra("sets", sets)
                    putExtra("exerciseTime", exerciseTime)
                    putExtra("restTime", restTime)
                    putExtra("status", "in_progress")
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
            }
        }

        val viewExerciseButton: Button = findViewById(R.id.view_exercise_button)
        viewExerciseButton.setOnClickListener {
            val intent = Intent(this, ViewExerciseActivity::class.java)
            startActivity(intent)
        }
    }

    private fun saveExercise(name: String, sets: Int, exerciseTime: Int, restTime: Int) {
        val sharedPreferences = getSharedPreferences("exercises", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val exerciseId = System.currentTimeMillis().toString()
        editor.putString("$exerciseId-name", name)
        editor.putInt("$exerciseId-sets", sets)
        editor.putInt("$exerciseId-exerciseTime", exerciseTime)
        editor.putInt("$exerciseId-restTime", restTime)
        editor.apply()
    }
}