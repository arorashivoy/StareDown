// ResultActivity.kt
package com.arorashivoy.staredown

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.Locale
import kotlin.properties.Delegates

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val blinkTime = intent.getLongExtra("blinkTime", 0L)
        val username = intent.getStringExtra("username") ?: "unknown"
        val dbRef = Firebase.database.reference.child("leaderboard").child(username)
        dbRef.get().addOnSuccessListener { snapshot ->
            val existing = snapshot.getValue(Leader::class.java)


            existing?.let {
                val highScore = it.score
                val formattedBlinkTime = String.format(
                    Locale.US,
                    "%02d:%02d:%03d",
                    blinkTime / 60000,
                    (blinkTime % 60000) / 1000,
                    blinkTime % 1000
                )

                val formattedHighScore = String.format(
                    Locale.US,
                    "%02d:%02d:%03d",
                    highScore / 60000,
                    (highScore % 60000) / 1000,
                    highScore % 1000
                )

                if (blinkTime >= highScore) {
                    findViewById<TextView>(R.id.scoreText).text =
                        "You set a new highscore of $formattedHighScore!"
                } else {
                    findViewById<TextView>(R.id.scoreText).text =
                        "Your score of $formattedBlinkTime was lower than your highscore of $formattedHighScore!"
                }
            }
        }



        findViewById<Button>(R.id.tryAgainBtn).setOnClickListener {
            startActivity(Intent(this, SinglePlayerActivity::class.java)
                .putExtra("username", username))
            finish()
        }

        findViewById<Button>(R.id.leaderboardBtn).setOnClickListener {

            startActivity(Intent(this, ScoreboardActivity::class.java)
                .putExtra("username", username))
        }
    }
}