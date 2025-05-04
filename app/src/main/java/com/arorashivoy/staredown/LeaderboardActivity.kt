package com.arorashivoy.staredown

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

data class Leader(
    val username: String = "",
    val score: Long = 0,
    val score2: Long = 0,
    val score3: Long = 0
)

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var username: String
    private lateinit var playAgainBtn: Button
    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LeaderboardAdapter
    private val leaderList = mutableListOf<Leader>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        username = intent.getStringExtra("username") ?: "unknown"

        FirebaseApp.initializeApp(this)
        database = Firebase.database.reference.child("leaderboard")
        FirebaseAuth.getInstance().signInAnonymously()

        playAgainBtn = findViewById(R.id.playBtn)
        recyclerView = findViewById(R.id.leaderboardRecycler)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = LeaderboardAdapter(leaderList)
        recyclerView.adapter = adapter

        playAgainBtn.setOnClickListener {
            val intent = Intent(this, SinglePlayerActivity::class.java)
                .putExtra("username", username)
//            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        fetchLeaderboard()
    }

    private fun fetchLeaderboard() {
        database.orderByChild("score").limitToLast(100)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    leaderList.clear()
                    for (child in snapshot.children) {
                        val leader = child.getValue(Leader::class.java)
                        leader?.let { leaderList.add(it) }
                    }
                    leaderList.reverse()
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }
}
