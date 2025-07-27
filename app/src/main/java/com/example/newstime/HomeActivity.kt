package com.example.newstime

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import com.google.gson.annotations.SerializedName
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

        val fetcher = NewsFetcher()

        val interests = SharedPrefManager.getInterests()
        val container = findViewById<LinearLayout>(R.id.cardContainer)

        lifecycleScope.launch {
            withContext(Dispatchers.IO){
                val articles = fetcher.fetchArticles()
                if(articles.isEmpty()) {
                    Log.d("ARTICLE_TYPE", "articles is null")
                }

                else{
//                        prepareArticlesView(articles)
                    Log.d("ARTICLE_TYPE", "articles is not null")
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
        for(article in articles){
            Log.d("ARTICLE_TAG", article.articleId)
        }
    }
}


class NewsFetcher() {
    val httpClient = OkHttpClient()
    val gson = Gson()

    val url = "https://966d376a3ec1.ngrok-free.app/latest"

    fun fetchArticles(): Array<NewsArticle> {
        val request = Request.Builder()
            .url(url)
            .build()

        val response = httpClient.newCall(request).execute()
        val bodyString = response.body.string()
        Log.d("RESP_STR", bodyString)
        var globalArticles: Array<NewsArticle> = emptyArray()

        if(response.code == 200 && !bodyString.isEmpty()){
            val articles = gson.fromJson(bodyString, Array<NewsArticle>::class.java)
            globalArticles = articles
            Log.d("FIRST_ARTICLE", globalArticles[0].toString())
        }

        return globalArticles
    }
}

data class ApiResponse(
    val status: String,
    val articles: List<NewsArticle>
)

data class NewsArticle(
    @SerializedName("article_id")
    val articleId: String,
    val link: String,
    val creator: List<String>?,
    val description: String,
    val pubDate: String,
    @SerializedName("image_url")
    val imageUrl: String?,
    @SerializedName("source_name")
    val sourceName: String
)

@Composable
fun ArticleCard(articleTitle: String, articleDesc: String, imageUrl: String, pubDate: String){
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
                    .data(imageUrl)
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
            text = articleTitle,
            style = MaterialTheme.typography.displayMedium,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column {
            Text(
                text= articleDesc,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 3
            )
            Spacer(modifier = Modifier.width(8.dp).height(4.dp))

            Text(
                text = pubDate,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}
