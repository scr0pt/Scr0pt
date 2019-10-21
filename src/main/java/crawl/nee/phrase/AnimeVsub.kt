package crawl.nee.phrase


import crawl.nee.Anime
import crawl.nee.Episode
import crawl.nee.File
import crawl.nee.sortLabel
import net.minidev.json.JSONArray
import net.minidev.json.JSONObject
import net.minidev.json.JSONValue
import org.jsoup.nodes.Document
import utils.MyString
import utils.curl.LongConnection

/**
 *
 * @author Long
 * @date created Sep 14, 2018 12:10:34 PM
 * @class name AnimeVsub
 */
class AnimeVsub(con: LongConnection, id: Long?) : WebPhrase() {

    override val subType: String
        get() = "VietSub"

    init {
        this.con = con
        this.id = id
    }

    override fun getSubteam(doc: Document?) =
        doc?.selectFirst("#list-server .server:has(.list-episode .playing) h3.server-name")?.text()?.trim()
            ?: doc?.selectFirst("ul.server-list > li.backup-server > ul.list-episode:has(li.episode.playing > a)")?.previousElementSibling()?.selectFirst(
                "h3.server-title"
            )?.text()

    override fun getAnimeYear(doc: Document?): Long? {
        val year = doc?.selectFirst("p.Info > span.Date")?.text()?.replace("\\s+".toRegex(), "")?.trim() ?: return null
        return try {
            year.toLong()
        } catch (e: Exception) {
            null
        }
    }

    override fun getAnimeStream(doc: Document?): File? {
        doc ?: return null
        val so = doc.html()
        val level = MyString.textBetween(so, "AnimeVsub('", "',") ?: return null
        val filmInfofilmID = MyString.textBetween(so, "filmInfo.filmID", "');") ?: return null
        val filmID = MyString.getFirstNuminString(filmInfofilmID) ?: return null
        return PhimLe(level, filmID, doc.baseUri())
    }

    //AnimeVsuv.tv
    fun PhimLe(level: String, filmID: String, linkEp: String): File? {
        try {
            val body = con?.post(
                "http://animevsub.tv/ajax/player",
                data = hashMapOf("link" to level, "id" to filmID),
                headers = hashMapOf("Referer" to linkEp)
            )?.body ?: return null
            val jobject = JSONValue.parse(body) as JSONObject
            val link = jobject["link"] as JSONArray
            if (link.isEmpty()) {
                return null
            }
            val files = arrayListOf<File>()
            for (`object` in link) {
                val label = (`object` as JSONObject).getAsString("label").removeSuffix("p")
                val stream_link = `object`.getAsString("file")
                val f = File()
                f.sourceLink = (stream_link)
                f.label = label
                files.add(f)
            }
            return when {
                files.isEmpty() -> null
                files.size == 1 -> files.first()
                else -> {
                    files.sortLabel()
                    files.first()
                }
            }
        } catch (e: Exception) {
            return null
        }
    }

    override fun getNewEpisodes(doc: Document?): ArrayList<Episode> {
        val eplist = arrayListOf<Episode>()
        val selects =
            doc?.select(".MovieInfo.TPost li.latest_eps > a[href^='http://animevsub.tv/phim/']") ?: return eplist
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
            doc?.select("#single-home > ul.MovieList.Rows > li.TPostMv > article[id^='post-'].post.type-post > a[href^='http://animevsub.tv/phim/']")
                ?: return animelist

        for (select in selects) {
            val anime = Anime()
            anime.web_link = select.absUrl("href")
            anime.name = select.selectFirst("h2.Title").text()?.trim()
            animelist.add(anime)
        }
        return animelist
    }
}
