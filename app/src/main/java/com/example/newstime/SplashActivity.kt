package com.example.newstime

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.newstime.utils.SharedPrefManager
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    private var isChecking = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { isChecking }
        SharedPrefManager.init(applicationContext)

        lifecycleScope.launch {
            checkIfLoggedIn()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun checkIfLoggedIn() {
        val token = SharedPrefManager.getToken()
        if (token == null || token.isEmpty()){
            val loginActivity = Intent(applicationContext, LoginActivity::class.java)
            isChecking = false
            startActivity(loginActivity)
            finish()
        }

        else{
            val homeActivity = Intent(applicationContext, HomeActivity::class.java)
            startActivity(homeActivity)
            finish()
        }
    }
}
