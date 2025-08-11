package com.example.newstime

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.newstime.utils.BookmarksManager
import kotlinx.coroutines.launch

class Bookmarks : AppCompatActivity() {
    private val articlesState = mutableStateOf<List<NewsArticle>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bookmarks)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bdb = BookmarksManager.initDb(this)
        val homeBtn = findViewById<Button>(R.id.homeBtn)

        homeBtn.setOnClickListener {
            val homeActivity = Intent(this, HomeActivity::class.java)
            startActivity(homeActivity)
        }

        lifecycleScope.launch {
            val bookmarks = bdb.bookmarkDao().getAll()

            articlesState.value = bookmarks.map {
                NewsArticle(
                    articleId = it.articleId,
                    title = it.title ?: "",
                    description = it.desc ?: "",
                    imageUrl = it.imageUrl ?: "",
                    pubDate = it.dateSaved ?: "",
                    link = it.link ?: "",
                    creator = emptyList(),
                    sourceName = ""
                )
            }
        }

        val composeView = findViewById<ComposeView>(R.id.composeView)
        composeView.setContent {
            PrepareArticlesView(articles = articlesState.value)
        }
    }

    @Composable
    private fun PrepareArticlesView(articles: List<NewsArticle>) {
        LazyColumn {
            items(articles) { article ->
                ArticleCard(
                    article.articleId,
                    article.title,
                    article.description,
                    article.imageUrl,
                    article.pubDate,
                    article.link
                )
            }
        }
    }
}
