data class NewsArticle(
    val title: String
    val link: String
    val image_url: String?
    val pubDate: String
    val source: String?
)

data class ApiResponse(
    val status: Int
    val articles: List<NewsArticle>
)

