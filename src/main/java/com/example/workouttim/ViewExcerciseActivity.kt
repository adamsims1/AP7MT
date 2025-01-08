package com.example.workouttim

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class ViewExerciseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wiew_excercise)

        val exerciseListLayout: LinearLayout = findViewById(R.id.exercise_list_layout)
        loadExercises(exerciseListLayout)


        val backToMainButton: Button = findViewById(R.id.back_to_main_button)
        backToMainButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadExercises(exerciseListLayout: LinearLayout) {
        exerciseListLayout.removeAllViews()
        val sharedPreferences = getSharedPreferences("exercises", MODE_PRIVATE)
        val allEntries = sharedPreferences.all

        for ((key, value) in allEntries) {
            if (key.endsWith("-name")) {
                val exerciseId = key.removeSuffix("-name")
                val exerciseName = value.toString()
                val sets = sharedPreferences.getInt("$exerciseId-sets", 0)
                val exerciseTime = sharedPreferences.getInt("$exerciseId-exerciseTime", 0)
                val restTime = sharedPreferences.getInt("$exerciseId-restTime", 0)

                val exerciseView = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 16, 0, 16)
                    }
                }

                val exerciseTextView = TextView(this).apply {
                    text = "$exerciseName, Sets: $sets, Exercise Time: $exerciseTime, Rest Time: $restTime"
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                    setOnClickListener {
                        val intent = Intent(this@ViewExerciseActivity, ExerciseStatusActivity::class.java).apply {
                            putExtra("sets", sets)
                            putExtra("exerciseTime", exerciseTime)
                            putExtra("restTime", restTime)
                        }
                        startActivity(intent)
                    }
                }

                val editButton = Button(this).apply {
                    text = "Edit"
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    setOnClickListener {
                        showEditDialog(exerciseId, exerciseName, sets, exerciseTime, restTime)
                    }
                }

                val deleteButton = Button(this).apply {
                    text = "Delete"
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    setOnClickListener {
                        deleteExercise(exerciseId)
                        loadExercises(exerciseListLayout)
                    }
                }

                exerciseView.addView(exerciseTextView)
                exerciseView.addView(editButton)
                exerciseView.addView(deleteButton)
                exerciseListLayout.addView(exerciseView)
            }
        }
    }

    private fun showEditDialog(exerciseId: String, exerciseName: String, sets: Int, exerciseTime: Int, restTime: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Edit Exercise")

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val nameTitle = TextView(this).apply {
            text = "Exercise Name"
        }
        val nameInput = EditText(this).apply {
            hint = "Exercise Name"
            setText(exerciseName)
        }

        val setsTitle = TextView(this).apply {
            text = "Sets"
        }
        val setsInput = EditText(this).apply {
            hint = "Sets"
            setText(sets.toString())
        }

        val exerciseTimeTitle = TextView(this).apply {
            text = "Exercise Time"
        }
        val exerciseTimeLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        val exerciseTimeMinutesInput = EditText(this).apply {
            hint = "Exercise Time (min)"
            setText((exerciseTime / 60).toString())
        }
        val exerciseTimeSecondsInput = EditText(this).apply {
            hint = "Exercise Time (s)"
            setText((exerciseTime % 60).toString())
        }
        exerciseTimeLayout.addView(exerciseTimeMinutesInput)
        exerciseTimeLayout.addView(exerciseTimeSecondsInput)

        val restTimeTitle = TextView(this).apply {
            text = "Rest Time"
        }
        val restTimeLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        val restTimeMinutesInput = EditText(this).apply {
            hint = "Rest Time (min)"
            setText((restTime / 60).toString())
        }
        val restTimeSecondsInput = EditText(this).apply {
            hint = "Rest Time (s)"
            setText((restTime % 60).toString())
        }
        restTimeLayout.addView(restTimeMinutesInput)
        restTimeLayout.addView(restTimeSecondsInput)

        layout.addView(nameTitle)
        layout.addView(nameInput)
        layout.addView(setsTitle)
        layout.addView(setsInput)
        layout.addView(exerciseTimeTitle)
        layout.addView(exerciseTimeLayout)
        layout.addView(restTimeTitle)
        layout.addView(restTimeLayout)
        builder.setView(layout)

        builder.setPositiveButton("Save") { dialog, _ ->
            val newName = nameInput.text.toString()
            val newSets = setsInput.text.toString().toIntOrNull()
            val newExerciseTimeMinutes = exerciseTimeMinutesInput.text.toString().toIntOrNull() ?: 0
            val newExerciseTimeSeconds = exerciseTimeSecondsInput.text.toString().toIntOrNull() ?: 0
            val newRestTimeMinutes = restTimeMinutesInput.text.toString().toIntOrNull() ?: 0
            val newRestTimeSeconds = restTimeSecondsInput.text.toString().toIntOrNull() ?: 0

            val newExerciseTime = newExerciseTimeMinutes * 60 + newExerciseTimeSeconds
            val newRestTime = newRestTimeMinutes * 60 + newRestTimeSeconds

            if (newName.isNotEmpty() && newSets != null && newExerciseTime > 0 && newRestTime > 0) {
                saveExercise(exerciseId, newName, newSets, newExerciseTime, newRestTime)
                loadExercises(findViewById(R.id.exercise_list_layout))
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Please enter valid details", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun saveExercise(exerciseId: String, name: String, sets: Int, exerciseTime: Int, restTime: Int) {
        val sharedPreferences = getSharedPreferences("exercises", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("$exerciseId-name", name)
        editor.putInt("$exerciseId-sets", sets)
        editor.putInt("$exerciseId-exerciseTime", exerciseTime)
        editor.putInt("$exerciseId-restTime", restTime)
        editor.apply()
    }

    private fun deleteExercise(exerciseId: String) {
        val sharedPreferences = getSharedPreferences("exercises", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("$exerciseId-name")
        editor.remove("$exerciseId-sets")
        editor.remove("$exerciseId-exerciseTime")
        editor.remove("$exerciseId-restTime")
        editor.apply()
    }
}