package com.example.newstime

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.newstime.utils.SharedPrefManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class InterestActivity : AppCompatActivity() {

    private val httpClient = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_interest)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        SharedPrefManager.init(applicationContext)

        val nextFloatingBtn = findViewById<FloatingActionButton>(R.id.nextBtn)
        nextFloatingBtn.setOnClickListener {
            val user_interests = get_user_prefs()

            SharedPrefManager.saveInterests(user_interests)

            val homeActivity = Intent(applicationContext, HomeActivity::class.java)
            homeActivity.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(homeActivity)
            finish()
        }

        lifecycleScope.launch {
            val (code, data) = get_interests()
            if (code != 200){
                Toast.makeText(applicationContext, "Failed to get interests. $code", Toast.LENGTH_SHORT).show()
                print(code)
            }
            else{
                val gson = Gson()
                val type = object: TypeToken<Map<String, List<String>>>() {}.type
                val data_map: Map<String, List<String>> = gson.fromJson(data, type)
                prepareView(data_map)
                Toast.makeText(applicationContext, "Fetched prefs", Toast.LENGTH_SHORT).show()
            }
        }
    }

    suspend fun get_interests(): Pair<Int, String> = withContext(Dispatchers.IO){
        val request = Request.Builder().url("https://786df59885b4.ngrok-free.app/prefs").build()
        val response = httpClient.newCall(request).execute()
        val resData = response.body.string()
        response.close()
        Pair(response.code, resData)
    }

    private fun prepareView(data: Map<String, List<String>>) {
        val container = findViewById<LinearLayout>(R.id.container)

        for ((category, tags) in data) {
            val category_text = TextView(this).apply {
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
            container.addView(category_text)

            val chip_group = ChipGroup(this).apply {
                isSingleLine = false
                isSingleSelection = false
            }
            container.addView(chip_group)

            for (tag in tags) {
                val chip = Chip(this).apply {
                    text = tag
                    isCheckable = true
                    isClickable = true
                }
                chip_group.addView(chip)
            }
        }
    }

    private fun get_user_prefs(): MutableSet<String>{
        val interests = mutableSetOf<String>()
        val chipsParent = findViewById<LinearLayout>(R.id.container)
        for (element in 0 until chipsParent.childCount){
            val chip = chipsParent.getChildAt(element)
            if(chip is Chip){
                if(chip.isChecked){
                    interests.add(chip.text.toString())
                }
            }
        }
        return interests
    }
}