package com.arorashivoy.staredown

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.Locale

class ScoreboardActivity : AppCompatActivity() {

    private lateinit var goldScore: TextView
    private lateinit var silverScore: TextView
    private lateinit var bronzeScore: TextView
    private lateinit var viewLeaderboardBtn: Button
    private lateinit var playAgainBtn: Button
    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scoreboard)

        goldScore = findViewById(R.id.goldScore)
        silverScore = findViewById(R.id.silverScore)
        bronzeScore = findViewById(R.id.bronzeScore)
        viewLeaderboardBtn = findViewById(R.id.viewLeaderboardBtn)
        playAgainBtn = findViewById(R.id.playAgainBtn)

        // Example: Load leaderboard data from intent or static source
        username = intent.getStringExtra("username") ?: "unknown"

        val dbRef = Firebase.database.reference.child("leaderboard").child(username)

        dbRef.get().addOnSuccessListener { snapshot ->
            val existing = snapshot.getValue(Leader::class.java)


            existing?.let {
                goldScore.text = String.format(
                    Locale.US,
                    "%02d:%02d:%03d",
                    it.score / 60000,
                    (it.score % 60000) / 1000,
                    it.score % 1000
                )

                silverScore.text = String.format(
                    Locale.US,
                    "%02d:%02d:%03d",
                    it.score2 / 60000,
                    (it.score2 % 60000) / 1000,
                    it.score2 % 1000
                )

                bronzeScore.text = String.format(
                    Locale.US,
                    "%02d:%02d:%03d",
                    it.score3 / 60000,
                    (it.score3 % 60000) / 1000,
                    it.score3 % 1000
                )
            }
        }.addOnFailureListener {
            Log.e("SHIVOY", "Failed to fetch existing scores", it)
        }
        // For demo purposes, you can update this to pull from SharedPreferences or a DB


        // Navigate to leaderboard
        viewLeaderboardBtn.setOnClickListener {
            startActivity(Intent(this, LeaderboardActivity::class.java)
                .putExtra("username", username))
        }

        // Restart game
        playAgainBtn.setOnClickListener {
            startActivity(Intent(this, SinglePlayerActivity::class.java)
                .putExtra("username", username))

            finish()
        }
    }
}