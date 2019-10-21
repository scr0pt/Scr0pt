package net.scr0pt.crawl.nee.phrase


import net.minidev.json.JSONArray
import net.minidev.json.JSONObject
import net.minidev.json.JSONValue
import net.scr0pt.crawl.nee.Anime
import net.scr0pt.crawl.nee.Episode
import net.scr0pt.crawl.nee.File
import net.scr0pt.crawl.nee.sortLabel
import org.jsoup.nodes.Document
import net.scr0pt.utils.MyString
import net.scr0pt.utils.curl.LongConnection

/**
 *
 * @author Long
 * @date created Sep 14, 2018 12:09:17 PM
 * @class name Anime47
 */
class Anime47(con: LongConnection, id: Long?) : WebPhrase() {
    override val subType: String
        get() = "VietSub"

    init {
        this.con = con
        this.id = id
    }

    override fun getSubteam(doc: Document?) =
        doc?.selectFirst(".name + .episodes:has(ul li a.active)")?.previousElementSibling()?.selectFirst(".name span")?.text()

    override fun getAnimeYear(doc: Document?): Long? {
        val selects = doc?.selectFirst(".movie-dl > .movie-dt:containsOwn(Năm:)") ?: return null
        if (selects.text().trim() != "Năm:") return null
        val year = selects.nextElementSibling()?.text()?.replace("\\s+".toRegex(), "")?.trim() ?: return null
        return try {
            year.toLong()
        } catch (e: Exception) {
            null
        }
    }

    override fun getAnimeStream(doc: Document?) =
        getgetAnimeStream_mpd(doc) ?: getgetAnimeStream_Jwplayer(doc) ?: getgetAnimeStream_Embed(doc)

    /**
     * Hàm chính
     * Lấy link video từ các tham số truyền vào của jwplayer (ở chỗ setup)
     * @param linkEp the value of linkEp
     * @param doc the value of doc
     */

    fun getgetAnimeStream_Jwplayer(doc: Document?): File? {
        doc ?: return null
        try {
            var playerInstanceSetup =
                MyString.textBetween(doc.html(), "playerInstance.setup(", "});") ?: return null
            playerInstanceSetup = playerInstanceSetup + "}"
            val jo = JSONValue.parse(playerInstanceSetup) as JSONObject
            val sources = jo["sources"] as JSONArray? ?: return null
            val files = arrayListOf<File>()
            for (source in sources) {
                val fileObject = source as JSONObject
                val link = fileObject["file"] as String
                if (link.endsWith("anime47.com/player/errorrrrr.mp4")) continue
                val label = fileObject["label"] as String
                val type = fileObject["type"] as String
                val f = File()
                f.sourceLink = (link)
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

    fun getgetAnimeStream_mpd(doc: Document?): File? {
        doc ?: return null
        try {
            val playerInstanceSetup =
                MyString.textBetween(doc.html(), "playerInstance.setup(", "});") ?: return null
            var mpd = MyString.textBetween(playerInstanceSetup, "http://anime47.com/getlink/mpd/mpd/", ".mpd\"")
                ?: return null
            mpd = "http://anime47.com/getlink/mpd/mpd/$mpd.mpd"

            val xmlDoc: Document? = con?.get(mpd)?.doc  ?: return null
            val Representations = xmlDoc?.select("Representation ") ?: return null
            if (Representations.isEmpty()) return null
            val files = arrayListOf<File>()
            for (Representation in Representations) {
                val link = Representation.select("BaseURL").first().text()
                val label = Representation.attr("FBQualityLabel")
                val f = File()
                f.sourceLink = (link)
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
        doc ?: return eplist
        val selects = doc.select(".episodes > ul > li > a[href^='/xem-phim-']") ?: return eplist
        for (select in selects) {
            val ep = Episode()
            ep.web_link = select.absUrl("href")
            ep.name = (select.text().trim())
            ep.subtype = subType
            eplist.add(ep)
        }
        return eplist
    }

    /**
     * Chú ý: Anime47 láy cả các tập mới cập nhật của:
     * ANIME MỚI CẬP NHẬT
     * Tất cả
     * Mùa này
     * Mùa trước
     * Bộ cũ
     * nên phải lọc các anime xem có bị trùng hay không (filterAnimes())
     */
    override fun getNewAnimes(doc: Document?): ArrayList<Anime> {
        val animelist = arrayListOf<Anime>()
        doc ?: return animelist
        val selects = doc.select(".last-film-box-wrapper .content ul#movie-last-movie > li > a[href^='/phim/']") ?: return animelist
        for (select in selects) {
            val anime = Anime()
            anime.web_link =  select?.absUrl("href") ?: continue
            anime.name = select.selectFirst(".movie-meta .movie-title-1")?.text()?.trim() ?: continue
            animelist.add(anime)
        }
        return animelist
    }

    /**
     * Lấy link video từ iframe (thường là link embed openload)
     * @param doc the value of doc
     */
    fun getgetAnimeStream_Embed(doc: Document?): File? {
        doc ?: return null
        //        Elements e1 = doc.select("#player-area iframe[src]");
        //        if (e1 == null || e1.size() < 1 || e1.first() == null) {
        //            return null;
        //        }
        //        String src = e1.first().attr("src");
        //        if(src.startsWith("https://openload.co/embed/")) {
        //
        //        }
        return null
    }
}
