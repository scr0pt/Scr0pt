package thirdservice.mal

import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import crawl.school.insertOneUnique
import org.apache.commons.lang3.StringUtils
import org.jsoup.nodes.Document
import utils.curl.LongConnection

/**
 * Created by Long
 * Date: 10/13/2019
 * Time: 10:49 PM
 */
fun main() {
    val mongoClient = MongoClients.create("mongodb+srv://root:mongo@cluster0-yuuro.mongodb.net/test")
    val serviceAccountDatabase = mongoClient.getDatabase("mal")
    val collection: MongoCollection<org.bson.Document> = serviceAccountDatabase.getCollection("anime")
    val crawl = Crawl()
    var malId = 0L
    while (true){
        crawl.getAnime(malId++)?.toDocument()?.let { doc ->
            collection.insertOneUnique(doc, org.bson.Document("mal_id", doc.get("mal_id")))
        }
    }
}

class Crawl {
    val conn = LongConnection().also {
        it.headers(
            hashMapOf(
                "authority" to "myanimelist.net",
                "pragma" to "no-cache",
                "cache-control" to "no-cache",
                "dnt" to "1",
                "upgrade-insecure-requests" to "1",
                "user-agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36",
                "sec-fetch-mode" to "navigate",
                "sec-fetch-user" to "?1",
                "accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3",
                "sec-fetch-site" to "same-origin",
                "referer" to "https://myanimelist.net/login.php?from=%2Fanime%2F37403%2FAhiru_no_Sora",
                "accept-encoding" to "gzip, deflate, br",
                "accept-language" to "en,en-GB;q=0.9,vi;q=0.8,fr-FR;q=0.7,fr;q=0.6,en-US;q=0.5,ja;q=0.4",
                "cookie" to "MALHLOGSESSID=ec55f9f0e6e2d868cc27fc138a2a57fd; m_gdpr_mdl=1; MALSESSIONID=i6qn05op0gdemisrl141c8vlp3; is_logged_in=1"
            )
        )
    }

    fun getAnime(id: Long): AnimeModel? {
        val response = conn.get("https://myanimelist.net/anime/$id") ?: return null
        val doc = response.doc ?: return null

        if (response.body?.contains("Too Many Requests") == true) {
            Thread.sleep(5000)
            return getAnime(id)
        }

        val animeModel = AnimeModel()
        animeModel.malId = doc.selectFirst("#myinfo_anime_id")?.`val`()?.toLong() ?: return null
        animeModel.name = doc.selectFirst("#contentWrapper h1.h1 > span[itemprop=\"name\"]")?.text() ?: return null
        animeModel.image = doc.selectFirst("meta[property=\"og:image\"]")?.attr("content")
            ?: doc.selectFirst(".page-common a img.ac[itemprop=\"image\"]")?.attr("src") ?: return null

        animeModel.descriptionEn = (getDescription(doc))
        animeModel.enName = (getChild(doc, "English:"))
        animeModel.jaName = (getChild(doc, "Japanese:"))
        animeModel.syName = (getChild(doc, "Synonyms:"))
        animeModel.type = (getChild(doc, "Type:"))
        animeModel.source = (getChild(doc, "Source:"))
        animeModel.duration = (getChild(doc, "Duration:"))
        animeModel.rating = (ratingProcessing(getChild(doc, "Rating:")))
        animeModel.uservote = (getUserVote(doc))
        animeModel.status = getChild(doc, "Status:")
        animeModel.broadcast = getChild(doc, "Broadcast:")
        animeModel.premiered = getChild(doc, "Premiered:")
        animeModel.aired = getChild(doc, "Aired:")
        animeModel.members = getChild(doc, "Members:")?.replace(",", "")?.toLong()
        animeModel.fav = getChild(doc, "Favorites:")?.replace(",", "")?.toLong()
        getChild(doc, "Episodes:")?.replace(",", "")?.let {
            if (StringUtils.isNumeric(it)) {
                animeModel.numEp = it.toLong()
            }
        }


        //get External Links
        doc.selectFirst("h2:contains(External Links)")?.let { externalLinks ->
            val element = externalLinks?.nextElementSibling()
            animeModel.officialPage = element?.selectFirst("a[href]:contains(Official Site)")?.attr("href")
            animeModel.anidb = element?.selectFirst("a[href]:contains(AnimeDB)")?.attr("href")
            animeModel.ann = element?.selectFirst("a[href]:contains(AnimeNewsNetwork)")?.attr("href")
            animeModel.wiki = element?.selectFirst("a[href]:contains(Wikipedia)")?.attr("href")
        }

        return animeModel
    }


    private fun getChild(doc: Document, title: String): String? {
        doc.select(".dark_text")?.let { dark_text ->
            dark_text.forEach { element ->
                val text = element.text()
                if (text.startsWith(title)) {
                    return element.parent().text()?.removePrefix(title)?.trim()
                }
            }
        }
        return null
    }

    private fun ratingProcessing(rating: String?): String? {
        return when (rating?.replace("  ", " ")?.toLowerCase()?.trim()) {
            "PG-13 - Teens 13 or older".toLowerCase() -> "PG-13"
            "G - All Ages".toLowerCase() -> "G"
            "PG - Children".toLowerCase() -> "PG"
            "Rx - Hentai".toLowerCase() -> "Rx"
            "R - 17+ (violence & profanity)".toLowerCase() -> "R-17+"
            "R+ - Mild Nudity".toLowerCase() -> "R+"
            "None".toLowerCase() -> "None"
            else -> null
        }
    }

    fun getUserVote(doc: Document): Long? {
        //Tìm theo cách 1
        val userVote = doc.select("td.borderClass span[itemprop=ratingCount]")?.first()?.text()?.replace(",", "")
        if (userVote != null) {
            return userVote.toLong()
        } else {//Tìm theo cách 2
            doc.select("td.borderClass .di-ib:has(span.dark_text:contains(Score:)) > span")?.forEach {
                val _userVote = it.text().trim()
                if (StringUtils.isNumeric(_userVote)) {
                    return _userVote.toLong()
                }
            }
        }
        return null
    }

    //Lấy thông tin mô tả của anime này
    fun getDescription(doc: Document): String? {
        val description =
            doc.selectFirst("span[itemprop=description]")?.text()?.replace("  ", " ")?.trim() ?: return null
        if (!description.contains("No synopsis has been added for this series yet. Click here to update this information.")) {
            return description
        }
        return null
    }
}