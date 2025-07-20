package com.example.newstime

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.newstime.utils.SharedPrefManager
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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
        finish()
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

                                    validateInterests()

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
        val name = auth.currentUser?.displayName.toString()
        val email = auth.currentUser?.email.toString()

        if (name.isEmpty() || email.isEmpty()){
            Toast.makeText(applicationContext, "Can't get name and email", Toast.LENGTH_SHORT).show()
        }

        else{
            SharedPrefManager.saveUser(name, email, token)
            SharedPrefManager.saveUid(auth.currentUser?.uid.toString())
            Toast.makeText(applicationContext, "Logging in as $name", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInterests() {
        val db = FirebaseDatabase.getInstance()
        db.getReference("Users/${auth.currentUser?.uid}/interests").get().addOnCompleteListener {
            task ->
            val interests = task.result?.toString()
            if(interests.isNullOrEmpty()){
                Log.d("LOGIN_FETCH", "Interests are either null or empty")
            }
            else{

                SharedPrefManager.saveInterests(interests)
                Log.d("LOGIN_FETCH", "Going to home")
                val homeActivity = Intent(applicationContext, HomeActivity::class.java)
                startActivity(homeActivity)

            }
        }

    }

}