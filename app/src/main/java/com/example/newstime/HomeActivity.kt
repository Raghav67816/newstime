package com.example.newstime

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request

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

        val api_key = "pub_94e0bc504a274c75aeaff2adb3badbfc"
        val fetcher = NewsFetcher(api_key)

        val sharedPref = getSharedPreferences(
            "interests",
            MODE_PRIVATE
        )
        val interests = sharedPref.getStringSet("Interests", null)

        lifecycleScope.launch {
            runOnUiThread {
                Toast.makeText(applicationContext, interests.toString(), Toast.LENGTH_LONG).show()
            }
//            if(interests != null){
//                val articles = fetcher.fetchArticles(interests.first())
//                print(articles)
//
//                if(!articles.isEmpty()){
//                    runOnUiThread {
//                        Toast.makeText(applicationContext, "We bought oursleves here.", Toast.LENGTH_SHORT).show()
//                    }
//                }
//                else{
//                    runOnUiThread {
//                        Toast.makeText(applicationContext, "We didn't make it cooper.", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
        }
    }
}


class NewsFetcher(api_key: String){
    val httpClient = OkHttpClient()
    val gson = Gson()

    val url = "https://newsdata.io/api/1/news?apikey=${api_key}"

    suspend fun fetchArticles(category: String): List<NewsArticle>{
        val request = Request.Builder()
            .url(url)
            .build()

        return try{
            val response = httpClient.newCall(request).execute()
            if(response.isSuccessful){
                val body = response.body.string()
                val news_data = gson.fromJson(body, ApiResponse::class.java)
                news_data.articles
            }
            else{
                emptyList<NewsArticle>()
            }
        }

        catch (e: Exception){
            print(e.toString())
            emptyList<NewsArticle>()
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
    val image_url: String?,
    val pubDate: String,
    val source_id: String,
    val creator: List<String>?,
    val content: String?
)