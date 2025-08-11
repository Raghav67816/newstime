package com.example.newstime

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import com.example.newstime.utils.BookmarkMeta
import com.example.newstime.utils.BookmarkDao
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import coil3.util.Logger
import com.example.newstime.utils.BookmarksManager
import kotlinx.coroutines.launch

class ArticleView : AppCompatActivity() {
    @SuppressLint("SetJavaScriptEnabled", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_article_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets

        }
        val bundle = intent.extras
        val b_db = BookmarksManager.initDb(applicationContext)

        val article_id = bundle?.getString("articleId").toString()
        val bookmarkBtn = findViewById<Button>(R.id.bookmarkBtn)

        lifecycleScope.launch {
            val output = b_db.bookmarkDao().getById(article_id)
            if(output != null){
                bookmarkBtn.text = "Remove Bookmark"
            }
        }

        bookmarkBtn.setOnClickListener {
            lifecycleScope.launch {
                if(bookmarkBtn.text != "Remove Bookmark"){
                    val bookmark = BookmarkMeta(
                        bundle!!.getString("articleId").toString(),
                        bundle.getString("title"),
                        bundle.getString("url"),
                        bundle.getString("date_published"),
                        bundle.getString("imageUrl"),
                        bundle.getString("desc"),
                    )

                    b_db.bookmarkDao().insertBookmark(bookmark)
                    Log.d("DB", "article bookmarked")
                }

                else{
                    b_db.bookmarkDao().deleteById(article_id)
                }
            }
        }

        val webView = findViewById<WebView>(R.id.webView)
        webView.getSettings().javaScriptEnabled = true


        webView.webViewClient = WebViewClient()
        webView.loadUrl(bundle?.getString("url") as String)
    }
}



