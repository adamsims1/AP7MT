package com.example.workouttim

import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import android.speech.tts.TextToSpeech
import java.util.Locale

class ExerciseStatusActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private var countDownTimer: CountDownTimer? = null
    private var isPaused = false
    private var timeRemaining: Long = 0
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var pauseButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_excercise_status)

        textToSpeech = TextToSpeech(this, this)

        val status = intent.getStringExtra("status") ?: "default"
        val layout = findViewById<ConstraintLayout>(R.id.exercise_status_layout)

        updateBackgroundColor(status, layout)

        val statusText: TextView = findViewById(R.id.status_text)
        val timerText: TextView = findViewById(R.id.timer_text)
        pauseButton = findViewById(R.id.pause_button)
        val cancelButton: Button = findViewById(R.id.cancel_button)

        val sets = intent.getIntExtra("sets", 0)
        val exerciseTime = intent.getIntExtra("exerciseTime", 0)
        val restTime = intent.getIntExtra("restTime", 0)

        var currentSet = 1
        val exerciseMillis = exerciseTime * 1000L
        val restMillis = restTime * 1000L

        mediaPlayer = MediaPlayer.create(this, R.raw.beep_sound)

        pauseButton.setOnClickListener {
            if (isPaused) {
                startExercise(currentSet, sets, exerciseMillis, restMillis, statusText, timerText)
                pauseButton.text = "Pause"
            } else {
                countDownTimer?.cancel()
                isPaused = true
                pauseButton.text = "Resume"
            }
        }

        cancelButton.setOnClickListener {
            countDownTimer?.cancel()
            finish()
        }

        startPreparation(sets, exerciseMillis, restMillis, statusText, timerText)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.language = Locale.US
        }
    }

    private fun updateBackgroundColor(status: String, layout: ConstraintLayout) {
        when (status) {
            "completed" -> layout.setBackgroundColor(Color.GREEN)
            "in_progress" -> layout.setBackgroundColor(Color.YELLOW)
            "not_started" -> layout.setBackgroundColor(Color.RED)
            else -> layout.setBackgroundColor(Color.WHITE)
        }
    }

    private fun startPreparation(sets: Int, exerciseMillis: Long, restMillis: Long, statusText: TextView, timerText: TextView) {
        statusText.text = "Get Ready"
        textToSpeech.speak("Get Ready", TextToSpeech.QUEUE_FLUSH, null, null)
        mediaPlayer.start()
        pauseButton.visibility = Button.GONE
        countDownTimer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timerText.text = ((millisUntilFinished / 1000) + 1).toString()
                updateBackgroundColor("not_started", findViewById(R.id.exercise_status_layout))
            }

            override fun onFinish() {
                pauseButton.visibility = Button.VISIBLE
                startExercise(1, sets, exerciseMillis, restMillis, statusText, timerText)
            }
        }.start()
    }

    private fun startExercise(currentSet: Int, sets: Int, exerciseMillis: Long, restMillis: Long, statusText: TextView, timerText: TextView) {
        statusText.text = "Set $currentSet: Exercise"
        textToSpeech.speak("exercise", TextToSpeech.QUEUE_FLUSH, null, null)
        mediaPlayer.start()
        countDownTimer = object : CountDownTimer(if (isPaused) timeRemaining else exerciseMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemaining = millisUntilFinished
                updateBackgroundColor("in_progress", findViewById(R.id.exercise_status_layout))
                timerText.text = ((millisUntilFinished / 1000) + 1).toString()
            }

            override fun onFinish() {
                mediaPlayer.start()
                if (currentSet < sets) {
                    if (restMillis > 0) {
                        startRest(currentSet, sets, exerciseMillis, restMillis, statusText, timerText)
                    } else {
                        startExercise(currentSet + 1, sets, exerciseMillis, restMillis, statusText, timerText)
                    }
                } else {
                    statusText.text = "Workout Complete"
                    textToSpeech.speak("Workout Complete", TextToSpeech.QUEUE_FLUSH, null, null)
                    updateBackgroundColor("completed", findViewById(R.id.exercise_status_layout))
                    mediaPlayer.start()
                    pauseButton.visibility = Button.GONE
                }
            }
        }.start()
        isPaused = false
    }

    private fun startRest(currentSet: Int, sets: Int, exerciseMillis: Long, restMillis: Long, statusText: TextView, timerText: TextView) {
        statusText.text = "Set $currentSet: Rest"
        textToSpeech.speak("rest", TextToSpeech.QUEUE_FLUSH, null, null)
        mediaPlayer.start()
        countDownTimer = object : CountDownTimer(restMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timerText.text = ((millisUntilFinished / 1000) + 1).toString()
                updateBackgroundColor("not_started", findViewById(R.id.exercise_status_layout))
            }

            override fun onFinish() {
                mediaPlayer.start()
                startExercise(currentSet + 1, sets, exerciseMillis, restMillis, statusText, timerText)
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        textToSpeech.stop()
        textToSpeech.shutdown()
    }
}