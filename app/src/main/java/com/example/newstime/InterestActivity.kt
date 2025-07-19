package com.example.newstime

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.newstime.utils.SharedPrefManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

private lateinit var auth: FirebaseAuth

class InterestActivity : AppCompatActivity() {

    private val httpClient = OkHttpClient()
    private var isViewReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_interest)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        SharedPrefManager.init(applicationContext)

        val nextFloatingBtn = findViewById<FloatingActionButton>(R.id.nextBtn)
        nextFloatingBtn.setOnClickListener {
            if(isViewReady){
                val userInterests = getUserPrefs()
                Log.d("INTERESTS", userInterests)

                SharedPrefManager.saveInterests(userInterests)
                pushToDb(userInterests)

                val homeActivity = Intent(applicationContext, HomeActivity::class.java)
                homeActivity.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(homeActivity)
                finish()
            }
            else{
                Log.d("INTERESTS", "View is not ready")
            }
        }

        lifecycleScope.launch {
            val (code, data) = getInterests()
            if (code != 200){
                Toast.makeText(applicationContext, "Failed to get interests. $code", Toast.LENGTH_SHORT).show()
                print(code)
            }
            else{
                val gson = Gson()
                val type = object: TypeToken<Map<String, List<String>>>() {}.type
                val dataMap: Map<String, List<String>> = gson.fromJson(data, type)
                prepareView(dataMap)
                Toast.makeText(applicationContext, "Fetched prefs", Toast.LENGTH_SHORT).show()
            }
        }
    }

    suspend fun getInterests(): Pair<Int, String> = withContext(Dispatchers.IO){
        val request = Request.Builder().url("https://01d1ffce6f66.ngrok-free.app/prefs").build()
        val response = httpClient.newCall(request).execute()
        val resData = response.body.string()
        response.close()
        Pair(response.code, resData)
    }

    private fun prepareView(data: Map<String, List<String>>) {
        val container = findViewById<LinearLayout>(R.id.container)

        for ((category, tags) in data) {
            val categoryText = TextView(this).apply {
                text = category
                textSize = 14f
                setTypeface(null, android.graphics.Typeface.NORMAL)

                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ).apply {
                    setMargins(0, 10, 0, 0)
                }
            }
            container.addView(categoryText)

            val chipGroup = ChipGroup(this).apply {
                isSingleLine = false
                isSingleSelection = false
            }
            container.addView(chipGroup)

            for (tag in tags) {
                val chip = Chip(this).apply {
                    text = tag
                    isCheckable = true
                    isClickable = true
                }
                chipGroup.addView(chip)
            }
        }
        isViewReady = true
    }

    private fun getUserPrefs(): String {
        val interests = mutableSetOf<String>()
        val chipsParent = findViewById<LinearLayout>(R.id.container)

        for (i in 0 until chipsParent.childCount) {
            val child = chipsParent.getChildAt(i)
            if (child is ChipGroup) {
                for (j in 0 until child.childCount) {
                    val chip = child.getChildAt(j)
                    if (chip is Chip && chip.isChecked) {
                        interests.add(chip.text.toString())
                    }
                }
            }
        }

        Log.d("DIRECT_FUNCTION", interests.joinToString(","))
        return interests.joinToString(",")
    }


    private fun pushToDb(interests: String){
        Toast.makeText(applicationContext, "pushToDb: $interests", Toast.LENGTH_LONG).show()

        val uid = auth.currentUser?.uid
        Toast.makeText(applicationContext, "UID: ${uid.toString()}", Toast.LENGTH_SHORT).show()

        if(!uid.isNullOrEmpty()){
            val db = FirebaseDatabase.getInstance().getReference("Users")
            val data = mapOf(
                "name" to auth.currentUser?.displayName,
                "interests" to interests
            )
            db.child(uid).setValue(data)
        }
    }
}