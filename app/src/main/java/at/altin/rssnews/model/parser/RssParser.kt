package at.altin.rssnews.model.parser

import org.xmlpull.v1.XmlPullParser

import android.util.Xml

import org.xmlpull.v1.XmlPullParserException

import java.io.IOException
import java.io.InputStream
import java.text.ParseException

import android.util.Log
import at.altin.rssnews.data.NewsItem
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class RssParser {
    companion object {
        val LOG_TAG: String = RssParser::class.java.canonicalName ?: "RssParser"
        val ns : String? = null
    }

    @Throws(XmlPullParserException::class, IOException::class, ParseException::class)
    fun parse(inputStream : InputStream): List<NewsItem> {
        return inputStream.use {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(it, null)
            parser.nextTag()
            readRss(parser)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class, ParseException::class)
    private fun readRss(parser: XmlPullParser): List<NewsItem> {
        val entries: MutableList<NewsItem> = ArrayList()
        parser.require(XmlPullParser.START_TAG, ns, "rss")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            val name = parser.name
            if (name == "channel") {
                entries.addAll(readChannel(parser))
            } else {
                skip(parser)
            }
        }
        return entries
    }

    @Throws(XmlPullParserException::class, IOException::class, ParseException::class)
    private fun readChannel(parser: XmlPullParser): List<NewsItem> {
        val entries: MutableList<NewsItem> = ArrayList()
        parser.require(XmlPullParser.START_TAG, ns, "channel")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            val name = parser.name
            if (name == "item") {
                val item = readItem(parser)
                if (item == null)
                    Log.w(LOG_TAG, "Invalid item found. Ignoring it")
                else
                    entries.add(item)
            } else {
                skip(parser)
            }
        }
        return entries
    }

    @Throws(XmlPullParserException::class, IOException::class, ParseException::class)
    private fun readItem(parser: XmlPullParser): NewsItem? {
        parser.require(XmlPullParser.START_TAG, ns, "item")
        var id: String? = null
        var title: String? = null
        var link: String? = null
        var author: String? = null
        var description: String? = null
        var imgString: String? = null
        var publishedOn: Date? = null
        val keywords: MutableSet<String> = HashSet()
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "guid" -> id = readBasicTag(parser, "guid")?.trim()
                "title" -> title = readBasicTag(parser, "title")?.trim()
                "category" -> {
                    val keyword = readBasicTag(parser, "category")?.trim()
                    if (keyword != null)
                        keywords.add(keyword)
                }
                "link" -> link = readBasicTag(parser, "link")?.trim()
                "pubDate" -> publishedOn = try {
                    SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.getDefault()).parse(
                        readBasicTag(
                            parser,
                            "pubDate"
                        ) ?: ""
                    )
                } catch (e: ParseException) {
                    Date()
                }
                "dc:creator" -> author = readBasicTag(parser, "dc:creator")?.trim()
                "description" -> description = readBasicTag(parser, "description")?.trim()
                "media:content" -> {
                    val newImage = readMediaTag(parser)
                    if (newImage != null) imgString = newImage
                }
                else -> skip(parser)
            }
        }
        return if (id == null || title == null || publishedOn == null)
            null
        else
            NewsItem(
                id, title,
                link, description, imgString, author,
                publishedOn, keywords)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readBasicTag(parser: XmlPullParser, tag: String): String? {
        parser.require(XmlPullParser.START_TAG, ns, tag)
        val result = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, tag)
        return result
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readMediaTag(parser: XmlPullParser): String? {
        var url : String? = null
        var image = false
        var type : String? = null

        parser.require(XmlPullParser.START_TAG, ns, "media:content")
        for (i in 0 until parser.attributeCount) {
            if (parser.getAttributeName(i) == "medium") {
                if (parser.getAttributeValue(i) == "image") {
                    image = true
                }
            }
            if (parser.getAttributeName(i) == "url") {
                url = parser.getAttributeValue(i)
            }
        }
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "media:keywords" -> type = readBasicTag(parser, "media:keywords")
                else -> skip(parser)
            }
        }
        return if (type == "headline" && image) url else null
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readText(parser: XmlPullParser): String? {
        var result: String? = null
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        check(parser.eventType == XmlPullParser.START_TAG)
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}
