package com.example.soundplayer

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    private var ringtone: Ringtone? = null
    private val RINGTONE_PICKER_REQUEST = 999  // Request code for the ringtone picker
    private val PREFS_NAME = "SoundPreferences"
    private val SELECTED_RINGTONE_KEY = "selected_ringtone"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Link to activity_main.xml
        setContentView(R.layout.activity_main)

        // Find the stop button from the layout
        val stopButton = findViewById<Button>(R.id.stopButton)

        // Button to open the ringtone picker
        val pickSoundButton = findViewById<Button>(R.id.pickSoundButton)

        // Set an OnClickListener for the Stop button
        stopButton.setOnClickListener {
            ringtone?.stop()  // Stop any currently playing ringtone
            finish()  // Exit the app
        }

        // Set OnClickListener for the pick sound button
        pickSoundButton.setOnClickListener {
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                putExtra(
                    RingtoneManager.EXTRA_RINGTONE_TYPE,
                    RingtoneManager.TYPE_ALARM or RingtoneManager.TYPE_NOTIFICATION or RingtoneManager.TYPE_RINGTONE
                )
                putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select a sound")
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
            }
            startActivityForResult(intent, RINGTONE_PICKER_REQUEST)
        }

        // Play the saved or default sound when the app starts
        playSavedRingtone()
    }

    private fun playSavedRingtone() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedRingtoneUriString = prefs.getString(SELECTED_RINGTONE_KEY, null)

        val ringtoneUri: Uri? = if (savedRingtoneUriString != null) {
            Uri.parse(savedRingtoneUriString)  // Use the saved ringtone
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)  // Default ringtone
        }

        // Stop any currently playing ringtone and start the new one
        ringtone?.stop()
        ringtone = RingtoneManager.getRingtone(this, ringtoneUri)
        ringtone?.play()
    }

    // Handle the result from the ringtone picker
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RINGTONE_PICKER_REQUEST && resultCode == Activity.RESULT_OK) {
            val uri: Uri? = data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            if (uri != null) {
                // Stop any currently playing sound
                ringtone?.stop()

                // Save the selected sound
                saveRingtoneUri(uri)

                // Get and play the selected sound
                ringtone = RingtoneManager.getRingtone(this, uri)
                ringtone?.play()
            }
        }
    }

    private fun saveRingtoneUri(uri: Uri) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(SELECTED_RINGTONE_KEY, uri.toString())  // Save the selected ringtone URI
        editor.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop the ringtone when the activity is destroyed to prevent sound leaks
        ringtone?.stop()
    }
}
