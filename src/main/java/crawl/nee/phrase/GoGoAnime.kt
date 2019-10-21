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
 * @date created Sep 14, 2018 12:10:39 PM
 * @class name GoGoAnime
 */
class GoGoAnime(con: LongConnection, id: Long?) : WebPhrase() {
    override val subType: String
        get() = "EngSub"

    init {
        this.con = con
        this.id = id
    }

    override fun getSubteam(doc: Document?): String? = null

    override fun getAnimeYear(doc: Document?): Long? = null

    override fun getAnimeStream(doc: Document?): File? {
        if (doc == null) return null
        val src = "http://vidstreaming.io/streaming.php?id=" + MyString.textBetween(
            doc.html(),
            "vidstreaming.io/streaming.php?id=",
            "\"",
            4,
            100
        ) ?: return null
        if (!src.contains("http")) return null
        val docVidstreamingIO = con?.get(src)?.doc ?: return null
        val f1 = getgetAnimeStream__VidstreamingIO(docVidstreamingIO)
        return f1
    }

    override fun getNewEpisodes(doc: Document?): ArrayList<Episode> = arrayListOf()

    override fun getNewAnimes(doc: Document?): ArrayList<Anime> {
        val animelist = arrayListOf<Anime>()
        val selects = doc?.select("#load_recent_release > .last_episodes.loaddub > ul.items > li") ?: return animelist
        for (select in selects) {
            val anime = Anime()

            val animeLinkElements = select.selectFirst("p.name > a[href]") ?: continue
            //get anime name
            val animeTitle = animeLinkElements.text()
            anime.name = animeTitle.trim { it <= ' ' }

            val eplist = arrayListOf<Episode>()
            val ep = Episode()

            //get ep subtype
            ep.subtype = subType
            ep.web_id = this.id
            //get ep link
            ep.web_link = animeLinkElements.absUrl("href")
            anime.web_link = ep.web_link
            ep.name = select.selectFirst("p.episode")?.text()?.replace("Episode", "")?.trim()
            eplist.add(ep)
            anime.eps = eplist
            animelist.add(anime)
        }
        return animelist
    }

    companion object {

        fun getgetAnimeStream__VidstreamingIO(doc: Document): File? {
            var playerInstanceSetup =
                MyString.textBetween(doc.html(), "playerInstance.setup(", "});") ?: return null
            playerInstanceSetup = playerInstanceSetup + "}"
            val jo = JSONValue.parse(playerInstanceSetup) as JSONObject
            val sources = jo["sources"] as JSONArray
            val arrVideo = arrayListOf<File>()
            for (source in sources) {
                val file = source as JSONObject
                val link = file["file"] as String
                val label = file["label"] as String
                val type = file["type"] as String
                val v = File()
                v.sourceLink = (link)
                v.label = label
                arrVideo.add(v)
            }
            return when {
                arrVideo.isEmpty() -> null
                arrVideo.size == 1 -> arrVideo.first()
                else -> {
                    arrVideo.sortLabel()
                    arrVideo.first()
                }
            }
        }
    }
}
