package com.example.newstime

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.node.InteroperableComposeUiNode
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

private lateinit var auth: FirebaseAuth

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        val submitBtn = findViewById<Button>(R.id.signupUpBtn)
        val loginScreenBtn = findViewById<Button>(R.id.loginScreenBtn)

        submitBtn.setOnClickListener {
            val name = findViewById<EditText>(R.id.userName).text.toString()
            val email = findViewById<EditText>(R.id.userEmail).text.toString()
            val password = findViewById<EditText>(R.id.userPassword).text.toString()

            createAccount(name, email, password)
        }

        loginScreenBtn.setOnClickListener {
            goToLoginScreen()
        }
    }

    private fun goToLoginScreen(){
        val loginIntent = Intent(this, LoginActivity::class.java)
        loginIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(loginIntent)
    }

    private fun createAccount(name: String, email: String, pass: String){
        try {
            print(name)
            print(email)
            print(pass)
            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill the required details.", Toast.LENGTH_SHORT).show()
            }
            else{
                auth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener (this) {task ->
                        if(task.isSuccessful){
                            Toast.makeText(this, "Account Created", Toast.LENGTH_SHORT).show()
                        }
                        else{
                            Toast.makeText(this, "Failed To Create Account", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        catch (e: Exception){
            print(e.message)
        }
    }
}