package com.example.newstime

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.newstime.utils.SharedPrefManager
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request

private lateinit var auth: FirebaseAuth

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.homeMain)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        SharedPrefManager.init(applicationContext)
        Toast.makeText(applicationContext, "UID is ${auth.currentUser?.uid.toString()}", Toast.LENGTH_LONG).show()

        val logoutBtn = findViewById<Button>(R.id.logoutBtn)
        logoutBtn.setOnClickListener {
            SharedPrefManager.clearUser()
        }

        val apiKey = "pub_94e0bc504a274c75aeaff2adb3badbfc"
        val fetcher = NewsFetcher(apiKey)

        val interests = SharedPrefManager.getInterests()

        lifecycleScope.launch {
//            if(interests != null){
//                runOnUiThread {
//                    Toast.makeText(applicationContext, interests.toString(), Toast.LENGTH_LONG).show()
//                }
//                val articles = fetcher.fetchArticles(interests.first())
//                print(articles)
//
//                if(!articles.isEmpty()){
//                    runOnUiThread {
//                        Toast.makeText(applicationContext, "We bought ourselves here.", Toast.LENGTH_SHORT).show()
//                    }
//                }
//                else{
//                    runOnUiThread {
//                        Toast.makeText(applicationContext, "We didn't make it cooper.", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//        }
        }
    }


    class NewsFetcher(apiKey: String) {
        val httpClient = OkHttpClient()
        val gson = Gson()

        val url = "https://newsdata.io/api/1/news?apikey=${apiKey}"

        fun fetchArticles(category: String): List<NewsArticle> {
            val request = Request.Builder()
                .url(url)
                .build()

            return try {
                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body.string()
                    val newsData = gson.fromJson(body, ApiResponse::class.java)
                    newsData.articles
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                print(e.toString())
                emptyList()
            }
        }
    }
}

data class ApiResponse(
    val status: Int,
    val articles: List<NewsArticle>
)

data class NewsArticle(
    val title: String,
    val link: String,
    val imageUrl: String?,
    val pubDate: String,
    val sourceId: String,
    val creator: List<String>?,
    val content: String?
)