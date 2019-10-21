package crawl.nee.phrase

import crawl.nee.Anime
import crawl.nee.Episode
import crawl.nee.File
import crawl.nee.sortLabel
import net.minidev.json.JSONArray
import net.minidev.json.JSONObject
import net.minidev.json.JSONValue
import org.jsoup.Connection
import org.jsoup.nodes.Document
import utils.MyString
import utils.curl.LongConnection

/**
 *
 * @author Long
 * @date created Sep 14, 2018 12:10:20 PM
 * @class name AnimeTVN
 */
class AnimeTVN(con: LongConnection, id: Long?) : WebPhrase() {
    override val subType: String
        get() = "VietSub"

    init {
        this.con = con
        this.id = id
    }

    override fun getSubteam(doc: Document?) = doc?.selectFirst(".svep:has(.playing) > span.svname")?.text()?.trim()

    override fun getAnimeYear(doc: Document?) =
        extractAnimeYear(doc?.selectFirst("li.has-color > span:containsOwn(Năm phát sóng:)")?.parent()?.text())

    override fun getAnimeStream(doc: Document?): File? = getAnimeStream1(doc) ?: getAnimeStream2(doc)

    fun getAnimeStream1(doc: Document?): File? {
        doc ?: return null
        var playerInstanceSetup = MyString.textBetween(doc.html(), "var source =", "\"}];") ?: return null
        playerInstanceSetup = playerInstanceSetup.trim() + "\"}]"
        val joAtt = JSONValue.parse(playerInstanceSetup) as JSONArray
        val jo = joAtt[0] as JSONObject
        if (!jo.containsKey("file")) {
            return null
        }
        val link = jo.getAsString("file") as String
        var label = jo.getAsString("label") as String
        if (label.endsWith("p")) {
            label = label.substring(0, label.length - 1)
        }
        val f = File()
        f.sourceLink = (link)
        f.label = label
        return f
    }

    fun getAnimeStream2(doc: Document?): File? {
        doc ?: return null
        var playerInstanceSetup = MyString.textBetween(doc.html(), "jwplayer(\"myElement\").setup(", "});")
            ?: return null
        playerInstanceSetup += "}"
        val jo = JSONValue.parse(playerInstanceSetup) as JSONObject
        if (!jo.containsKey("sources")) {
            return null
        }
        val sourcesObject = jo["sources"]
        if (sourcesObject !is JSONArray) {
            return null
        }
        val sources = jo["sources"] as JSONArray
        val arrFile = arrayListOf<File>()
        for (source in sources) {
            val file = source as JSONObject
            val link = file["file"] as String
            val label = file["label"] as String
            val f = File()
            f.sourceLink = (link)
            f.label = label
            arrFile.add(f)
        }
        return when {
            arrFile.isEmpty() -> null
            arrFile.size == 1 -> arrFile.first()
            else -> {
                arrFile.sortLabel()
                arrFile.first()
            }
        }
    }

    override fun getNewEpisodes(doc: Document?): ArrayList<Episode> {
        val eplist = arrayListOf<Episode>()
        val selects = doc?.select(".latest_eps > a[href^='http://animetvn.tv/xem-phim/']") ?: return eplist
        for (select in selects) {
            val ep = Episode()
            ep.web_link = select.absUrl("href")
            ep.name = select.text().trim()
            ep.subtype = subType
            eplist.add(ep)
        }
        return eplist
    }

    override fun getNewAnimes(doc: Document?): ArrayList<Anime> {
        val animelist = arrayListOf<Anime>()
        val selects =
            doc?.select(".home-film-list > .anime > .film-list > .item.film_item > .film_item_inner > .data > h3.title > a[href^='http://animetvn.tv/thong-tin-phim/']") ?: return animelist
        for (select in selects) {
            val anime = Anime()
            //get anime link
            anime.web_link = select.absUrl("href")
            anime.name = select.text().trim()
            animelist.add(anime)
        }
        return animelist
    }

    companion object {
        /**
         * Tach nam ra khoi text kieu nhu: Q3 2018, Năm phát sóng: Q2 2018
         * @param text the value of text
         */
        fun extractAnimeYear(text: String?): Long? {
            var text = text ?: return null
            text = text.replace("Năm phát sóng:", "").replace("\\s+".toRegex(), "").trim()

            for (i in 1..4) {
                val quy = "Q$i"
                if (text.contains(quy)) {
                    text = text.substring(text.indexOf(quy) + quy.length)
                    break
                }
            }

            return try {
                text.toLong()
            } catch (e: Exception) {
                null
            }
        }
    }
}
