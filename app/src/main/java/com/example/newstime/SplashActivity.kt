package com.example.newstime

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import org.json.JSONObject

class SplashActivity : AppCompatActivity() {
    private var isChecking = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { isChecking }

        lifecycleScope.launch {
            checkIfLoggedIn()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private suspend fun checkIfLoggedIn() {
        val sharedPref = getSharedPreferences("LoginDetails", MODE_PRIVATE)
        val token = sharedPref.getString("token", null)
        if (token == null || !isValid(token)) {
            isChecking = false
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {

            val payload = try {
                decodePayload(token)
            } catch (e: Exception) {
                null
            }

            isChecking = false
        }
    }

    private fun isValid(token: String): Boolean {
        return try {
            val payload = decodePayload(token)
            val exp = payload.optLong("exp", 0L) * 1000
            System.currentTimeMillis() < exp
        } catch (e: Exception) {
            false
        }
    }

    private fun decodePayload(token: String): JSONObject {
        val parts = token.split(".")
        if (parts.size != 3) throw IllegalArgumentException("Invalid JWT format")
        val decoder = java.util.Base64.getUrlDecoder()
        val decodedBytes = decoder.decode(parts[1])
        val decodedString = String(decodedBytes, Charsets.UTF_8)
        return JSONObject(decodedString)
    }
}
