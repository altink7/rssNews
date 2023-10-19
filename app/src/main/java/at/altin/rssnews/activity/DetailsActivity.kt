package at.altin.rssnews.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Base64
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import at.altin.rssnews.R
import at.altin.rssnews.data.NewsItem
import at.altin.rssnews.repository.download.ImageDownloader
import java.net.MalformedURLException
import java.net.URL


class DetailsActivity : AppCompatActivity() {
    companion object {
        val LOG_TAG : String = DetailsActivity::class.java.simpleName
        const val ITEM_KEY = "item"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        var item = intent?.extras?.getSerializable(ITEM_KEY) as? NewsItem
        if(item == null) {
            val title = intent?.extras?.getString("title")?: ""
            val url = intent?.extras?.getString("url")
            val description = intent?.extras?.getString("description")
            val author = intent?.extras?.getString("author")
            val link = intent?.extras?.getString("link")

            if(title == "" || url == null || description == null || author == null) {
                Log.e(LOG_TAG, "Missing data in intent")
            }else {
                item = NewsItem("info",title, link, description, url, author)
            }
        }
        if (item != null) {
            findViewById<TextView>(R.id.tv_news_item_title).text = item.title


            val webView = findViewById<WebView>(R.id.wv_news_item_description)
            var html =
                "<html>\n" +
                        "<head>\n" +
                        "<style>\n" +
                        "img {\n" +
                        "  width: 100% !important;\n" +
                        "  height: auto !important;\n"
            if (!getImageDisplay())
                html += "  display: none !important;\n"
            html +=             "},\n" +
                    "</style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    (item.description ?: "") + "\n" +
                    "</body>\n" +
                    "</html>"
            val encodedHtml = Base64.encodeToString(html.toByteArray(), Base64.NO_PADDING)
            webView.loadData(encodedHtml, "text/html", "base64")

            findViewById<TextView>(R.id.tv_news_item_author).text = item.author
            findViewById<TextView>(R.id.tv_news_item_publication_date).text = item.publicationDate.toString()
            findViewById<TextView>(R.id.tv_news_item_keywords).text = item.keywords.joinToString(", ")
            findViewById<Button>(R.id.btn_news_item_full_article).setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(item.link)
                if (intent.resolveActivity(packageManager) != null) startActivity(intent)
            }

            val imageView = findViewById<ImageView>(R.id.iv_news_item_icon)
            val progressBar = findViewById<ProgressBar>(R.id.pb_news_item_icon)
            imageView.visibility = View.INVISIBLE
            progressBar.visibility = View.INVISIBLE
            if(getImageDisplay()) {
                try {
                    progressBar.visibility = View.VISIBLE
                    val imageUrl = URL(item.imageUrl)
                    imageView.tag = imageUrl

                    ImageDownloader.downloadImage(imageUrl) { url, bitmap ->
                        imageView.post {
                            if (bitmap != null && imageView.tag == url) {
                                imageView.setImageBitmap(bitmap)
                                imageView.visibility = View.VISIBLE
                            }
                            progressBar.visibility = View.INVISIBLE
                        }
                    }
                } catch (ex : MalformedURLException) {
                    Log.d(LOG_TAG, "Invalid Image URL.", ex)
                }
            }
        }
    }

    private fun getImageDisplay(): Boolean {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        return sharedPreferences.getBoolean(
            getString(R.string.settings_image_display_key),
            resources.getBoolean(R.bool.settings_image_display_default)
        )
    }
}
