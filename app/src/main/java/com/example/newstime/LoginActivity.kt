package com.example.newstime

import android.util.Base64
import android.content.Intent
import com.example.newstime.utils.SharedPrefManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONObject



private lateinit var auth: FirebaseAuth

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        SharedPrefManager.init(applicationContext)


        val loginBtn = findViewById<Button>(R.id.loginSubmitBtn)
        val createAccBtn = findViewById<Button>(R.id.signupScreenBtn)

        createAccBtn.setOnClickListener {
            goToSignUpScreen()
        }

        loginBtn.setOnClickListener {
            val email = findViewById<TextInputEditText>(R.id.userEmail).text.toString()
            val pass = findViewById<TextInputEditText>(R.id.userPassword).text.toString()

            loginUser(email, pass)
        }
    }

    private fun goToSignUpScreen(){
        val signupIntent = Intent(applicationContext, SignUpActivity::class.java)
        signupIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(signupIntent)
    }

    private fun loginUser(email: String, pass: String) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val currentUser = auth.currentUser
                    currentUser?.getIdToken(false)
                        ?.addOnCompleteListener(this) { tokenTask ->
                            if (tokenTask.isSuccessful) {
                                val token = tokenTask.result?.token
                                if (!token.isNullOrEmpty()) {
                                    storeInfo(token)

                                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()

                                    val intent = Intent(this, InterestActivity::class.java)
                                    startActivity(intent)
                                    finish()

                                } else {
                                    Toast.makeText(this, "Failed to retrieve token", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(
                                    this,
                                    "Token fetch failed: ${tokenTask.exception?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(
                        this,
                        "Login failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }


    private fun storeInfo(token: String){
        val decoded_payload = decodePayload(token)
        val userName = decoded_payload.getString("user_id")
        val email = decoded_payload.getString("email")

        SharedPrefManager.setToken(token)
    }

    private fun decodePayload(token: String): JSONObject {
        val parts = token.split(".")
        if (parts.size != 3) throw IllegalArgumentException("Invalid JWT format")

        val payload = parts[1]
        val decodedBytes = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
        val decodedString = decodedBytes.toString(Charsets.UTF_8)

        return JSONObject(decodedString)
    }

}