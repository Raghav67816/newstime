package com.example.newstime

import androidx.compose.runtime.Composable
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.newstime.utils.SharedPrefManager
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
            onLogout(this)
        }

        val apiKey = "pub_94e0bc504a274c75aeaff2adb3badbfc"
        val fetcher = NewsFetcher(apiKey)

        val interests = SharedPrefManager.getInterests()

        lifecycleScope.launch {
            val results = withContext(Dispatchers.IO){
                    val articles = fetcher.fetchArticles(interests.split(",").first())
                    if(articles == null){
                        Log.d("ARTICLE_TYPE", "articles is null")
                    }
                }
            }
        }


    private fun onLogout(context: Context){
        val alert = AlertDialog.Builder(context)
        alert.apply {
            setTitle("Logout")
            setMessage("Are you sure you want to logout ?")

            setPositiveButton("Exit") { dialog, which ->
                finishAffinity()
            }

            setNegativeButton("Logout & Exit"){
                dialog, which ->
                SharedPrefManager.clearUser()
                finishAffinity()
            }

            setNeutralButton("Cancel"){
                dialog, which -> dialog.dismiss()
            }

            val dialog = alert.create()
            dialog.show()
        }
    }

    private fun prepareArticlesView(articles: List<NewsArticle>){

    }
}


    class NewsFetcher(apiKey: String) {
        val httpClient = OkHttpClient()
        val gson = Gson()

        val url = "https://newsdata.io/api/1/latest?apikey=${apiKey}"

        fun fetchArticles(category: String): List<NewsArticle> {
            val request = Request.Builder()
                .url(url)
                .build()

            return try {
                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body.string()
                    val newsData_ = gson.fromJson(body, ApiResponse::class.java)
                    Log.d("NEWS_DATA", newsData_.toString())
                    val newsData = NewsArticle("Sample", "Link", "", "", "", listOf(""), "")
                    return listOf(newsData)
                } else {
                    Log.d("RESP", response.code.toString())
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e("A_ERROR", e.toString())
                emptyList()
            }
        }
    }

data class ApiResponse(
    val status: String,
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

@Preview
@Composable
fun ArticleCard(){
    Card(
        Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ){
        Column(modifier = Modifier.padding(18.dp)) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("https://images.unsplash.com/photo-1579353977828-2a4eab540b9a?q=80&w=774&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D")
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.clip(CircleShape),
                placeholder = painterResource(R.drawable.sample)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(
            text = "Article Title",
            style = MaterialTheme.typography.displayMedium,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column {
            Text(
                text="Sample description",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 3
            )
            Spacer(modifier = Modifier.width(8.dp).height(4.dp))

            Text(
                text="Publish Date",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}