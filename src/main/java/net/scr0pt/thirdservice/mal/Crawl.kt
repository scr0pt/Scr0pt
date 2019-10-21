package net.scr0pt.thirdservice.mal

import com.mongodb.client.MongoCollection
import org.bson.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import net.scr0pt.utils.curl.LongConnection

/**
 * Created by Long
 * Date: 10/14/2019
 * Time: 12:24 AM
 */


class Crawl(val collection: MongoCollection<Document>) {
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
        val animeId = doc.selectFirst("#myinfo_anime_id")?.`val`()?.toLong() ?: return null
        animeModel.malId = animeId
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

        getChilds(doc, "Genres:") { href -> parserGenreId(href) }?.let {
            animeModel.genres = it
        }

        getChilds(doc, "Studios:") { href -> parserProducerId(href) }?.let {
            animeModel.studios = it
        }

        getChilds(doc, "Producers:") { href -> parserProducerId(href) }?.let {
            animeModel.producers = it
        }


        var ratingValue = doc.selectFirst("td.borderClass span[itemprop=ratingValue]")
        if (ratingValue == null) {
            ratingValue = doc.selectFirst("div[itemprop=\"aggregateRating\"] span.ratingValue")
        }

        ratingValue?.text()?.let {
            try {
                animeModel.score = it.toDouble()
            } catch (e: Exception) {
            }
        }

        animeModel.aired = getChild(doc, "Aired:")
        try {
            animeModel.members = getChild(doc, "Members:")?.replace(",", "")?.toLong()
        } catch (e: Exception) {
        }
        try {
            animeModel.fav = getChild(doc, "Favorites:")?.replace(",", "")?.toLong()
        } catch (e: Exception) {
        }
        getChild(doc, "Episodes:")?.replace(",", "")?.let {
            try {
                animeModel.numEp = it.toLong()
            } catch (e: Exception) {
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

        getRecommendations(doc, currentAnimeId = animeId)?.let {
            animeModel.recommendations = it
        }

        getRelated(doc)?.let {
            animeModel.related = it
        }

        return animeModel
    }

    fun getRelated(doc: org.jsoup.nodes.Document): ArrayList<RelatedAnime>? {
        val els = doc.select("table.anime_detail_related_anime > tbody > tr") ?: return null
        val kq = arrayListOf<RelatedAnime>()
        for (el in els) {
            val td: Elements = el.select("td.fw-n, td.ar, td[valign=top]") ?: continue
            val relationship = td.first().text().replace(":", "").trim()
            val _e1 = el.select("td[width=100%] > a[href]") ?: continue
            for (_ell in _e1) {
                val link = _ell.attr("href") ?: continue
                val name = _ell.text().trim() ?: continue
                val idAnime = parserMalId(link)
                val idManga = parserMangaId(link)
                val type = if (idAnime != null) "Anime" else "Manga"
                val id = if (idAnime != null) idAnime else idManga
                id?.let {
                    val related = RelatedAnime(relationship, name, id, type)
                    kq.add(related)
                }
            }
        }
        return if (kq.isEmpty()) null else kq
    }

    fun getRecommendations(doc: org.jsoup.nodes.Document, currentAnimeId: Long): ArrayList<Recommendation>? {
        val list = arrayListOf<Recommendation>()
        val recommendations = doc.select("#anime_recommendation .anime-slide-outer ul.anime-slide > li > a")
        for (recommendation in recommendations) {
            val malId = parserRecommendationId(recommendation.attr("href"), currentAnimeId) ?: continue
            val title = recommendation.selectFirst("span.title")?.text() ?: continue
            var users = recommendation.selectFirst("span.users")?.text() ?: continue


            users = when {
                users.endsWith("Users") -> users.removeSuffix("Users")
                users.endsWith("User") -> users.removeSuffix("User")
                users == "AutoRec" -> "0"
                else -> users
            }.trim()

            list.add(Recommendation(users, malId, title))
        }
        return if (list.isEmpty()) null else list
    }


    private fun getChild(doc: org.jsoup.nodes.Document, title: String): String? {
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

    private fun getChilds(doc: org.jsoup.nodes.Document, title: String, parser: (String) -> Long?): ArrayList<PairIdName>? {
        doc.select(".dark_text")?.let { dark_text ->
            for (element in dark_text) {
                val headerText = element.text() ?: continue
                if (!headerText.startsWith(title)) continue

                val list = arrayListOf<PairIdName>()
                element.parent().select("a[href]")?.forEach {
                    val text = it.text().trim()
                    val id = parser(it.attr("href"))
                    if (text != "" && text != "add some" && id != null) {
                        list.add(PairIdName(name = text, malId = id))
                    }
                }
                return if (list.isEmpty()) null else list
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

    fun getUserVote(doc: org.jsoup.nodes.Document): Long? {
        //Tìm theo cách 1
        val userVote = doc.select("td.borderClass span[itemprop=ratingCount]")?.first()?.text()?.replace(",", "")
        if (userVote != null) {
            return try {
                userVote.toLong()
            } catch (e: Exception) {
                null
            }
        } else {//Tìm theo cách 2
            doc.select("td.borderClass .di-ib:has(span.dark_text:contains(Score:)) > span")?.forEach {
                val _userVote = it.text().trim()
                return try {
                    return _userVote.toLong()
                } catch (e: Exception) {
                    null
                }
            }
        }
        return null
    }

    //Lấy thông tin mô tả của anime này
    fun getDescription(doc: org.jsoup.nodes.Document): String? {
        val description =
                doc.selectFirst("span[itemprop=description]")?.text()?.replace("  ", " ")?.trim() ?: return null
        if (!description.contains("No synopsis has been added for this series yet. Click here to update this information.")) {
            return description
        }
        return null
    }

    fun getListHomePage(): ArrayList<Long> {
        val list = arrayListOf<Long>()
        val response = conn.get("https://myanimelist.net/") ?: return list
        response.doc?.select("a[href*=\"/anime/\"]")?.forEach {
            parserMalId(it, list)
        }
        return list
    }

    fun getTopList(limit: Long = 0L): ArrayList<Long> {
        val list = arrayListOf<Long>()
        val response = conn.get("https://myanimelist.net/topanime.php?limit=$limit") ?: return list
        response.doc?.select(".top-ranking-table tr.ranking-list td.title > a")?.forEach {
            parserMalId(it, list)
        }
        return list
    }

    fun getListJustAdded(limit: Long = 0L): ArrayList<Long> {
        val list = arrayListOf<Long>()
        val response = conn.get("https://myanimelist.net/anime.php?o=9&c%5B0%5D=a&c%5B1%5D=d&cv=2&w=1&show=$limit")
                ?: return list
        response.doc?.select(".js-categories-seasonal table tbody tr .borderClass.bgColor0 a")?.forEach {
            parserMalId(it, list)
        }
        return list
    }

    private fun parserMalId(it: Element, list: ArrayList<Long>) {
        val id: Long = parserMalId(it.attr("href")) ?: return
        if (!list.contains(id)) {
            list.add(id)
        }
    }

    private fun parserRecommendationId(href: String?, currentAnimeId: Long): Long? {
        if (href?.contains("/anime/") == false) return null
        val id = href?.substringAfter("/anime/")?.substringBefore("/") ?: return null
        return if (id.contains("-")) {
            val anime1 = id.split("-")[0].toLong()
            val anime2 = id.split("-")[1].toLong()
            if (anime1 == currentAnimeId) anime2 else anime1
        } else {
            try {
                id.toLong()
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun parserMangaId(href: String?): Long? = parserId(href, "/manga/")
    private fun parserMalId(href: String?): Long? = parserId(href, "/anime/")
    private fun parserGenreId(href: String?): Long? = parserId(href, "/genre/")
    private fun parserProducerId(href: String?): Long? = parserId(href, "/producer/")

    private fun parserId(href: String?, prefix: String): Long? {
        if (href?.contains(prefix) == false) return null
        val id = href?.substringAfter(prefix)?.substringBefore("/") ?: return null
        return try {
            id.toLong()
        } catch (e: Exception) {
            null
        }
    }

    fun updateToDatabase(doc: Document) {
        if (doc.containsKey("mal_id")) {
            val findData = org.bson.Document("mal_id", doc.get("mal_id"))
            if (doc.isEmpty()) return
            try {
                val count = collection.countDocuments(findData)
                if (count == 0L) {
                    collection.insertOne(doc)
                } else {
                    doc.remove("mal_id")
                    collection.updateOne(findData, Document("\$set", doc))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
