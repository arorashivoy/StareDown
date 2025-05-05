package com.arorashivoy.staredown

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var setUsernameBtn: Button
    private lateinit var singlePlayerBtn: Button
    private lateinit var leaderboardbtn: Button
    private lateinit var database: DatabaseReference
    private var confirmedUsername: String? = null
    private val PREFS_NAME = "StaredownPrefs"
    private val USERNAME_KEY = "username"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001)
            }
        }


        database = Firebase.database.reference.child("leaderboard")


        val logo = findViewById<ImageView>(R.id.logoImage)
        val anim = AnimationUtils.loadAnimation(this, R.anim.logo_enter)
        logo.startAnimation(anim)



        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            RequestCamera.newInstance().show(supportFragmentManager, "RequestCamera")
        }

        setUsernameBtn = findViewById(R.id.setUsernameBtn)
        singlePlayerBtn = findViewById(R.id.singlePlayerBtn)
        leaderboardbtn = findViewById(R.id.viewLeaderboard)

        confirmedUsername = getSavedUsername()
        if (confirmedUsername != null) {
            setUsernameBtn.text = "Username: $confirmedUsername"
        }

        singlePlayerBtn.setOnClickListener {
            if (confirmedUsername != null) {
                startActivity(Intent(this, SinglePlayerActivity::class.java)
                    .putExtra("username", confirmedUsername))
            } else {
                Toast.makeText(this, "Please set a username first", Toast.LENGTH_SHORT).show()
            }
        }

        leaderboardbtn.setOnClickListener {
            if (confirmedUsername != null) {
                startActivity(Intent(this, LeaderboardActivity::class.java)
                    .putExtra("username", confirmedUsername))
            } else {
                Toast.makeText(this, "Please set a username first", Toast.LENGTH_SHORT).show()
            }
        }

        setUsernameBtn.setOnClickListener { showUsernameDialog() }

    }

    private fun showUsernameDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_set_username, null)
        val usernameInput = dialogView.findViewById<EditText>(R.id.usernameInput)
        val confirmBtn = dialogView.findViewById<Button>(R.id.confirmUsernameBtn)

        val dialog = BottomSheetDialog(this)
        dialog.setContentView(dialogView)
        dialog.show()

        confirmBtn.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            if (username.isEmpty()) {
                Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d("SHIVOY", "Username: $username")
            database.child(username).get().addOnSuccessListener {
                Log.d("SHIVOY", "Username check: ${it.exists()}")
                if (it.exists()) {
                    Toast.makeText(this, "Username already taken", Toast.LENGTH_SHORT).show()
                } else {
                    confirmedUsername = username
                    saveUsername(username)
                    setUsernameBtn.text = "Username: $username"
                    Toast.makeText(this, "Username set!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Error checking username", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUsername(username: String) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().putString(USERNAME_KEY, username).apply()
    }

    private fun getSavedUsername(): String? {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        return prefs.getString(USERNAME_KEY, null)
    }
}