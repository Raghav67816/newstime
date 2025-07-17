import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request

class NewsFetcherService(apiKey: String) {
    private val httpClient = OkHttpClient()
    private val gson = Gson()

    fun fetchArticles(category: String): List<NewsArticle>{

    }
}