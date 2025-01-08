package com.example.workouttim

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CreateSetActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_set)

        val startButton: Button = findViewById(R.id.start_button)
        startButton.setOnClickListener {
            val sharedPreferences = getSharedPreferences("exercises", MODE_PRIVATE)
            val allEntries = sharedPreferences.all
            if (allEntries.isNotEmpty()) {
                val intent = Intent(this, ExerciseStatusActivity::class.java)
                for ((key, value) in allEntries) {
                    intent.putExtra(key, value.toString())
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "No exercises found", Toast.LENGTH_SHORT).show()
            }
        }

        val backToMainButton: Button = findViewById(R.id.back_to_main_button)
        backToMainButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}