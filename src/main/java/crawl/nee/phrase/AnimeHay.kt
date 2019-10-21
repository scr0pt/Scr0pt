package crawl.nee.phrase

import crawl.nee.Anime
import crawl.nee.Episode
import crawl.nee.File
import crawl.nee.sortLabel
import org.apache.commons.lang3.StringUtils
import org.jsoup.Connection
import org.jsoup.nodes.Document
import utils.MyString
import utils.curl.LongConnection

/**
 *
 * @author Long
 * @date created Sep 14, 2018 12:10:28 PM
 * @class name AnimeHay
 */
class AnimeHay(con: LongConnection, id: Long?) : WebPhrase() {
    override val subType: String
        get() = "VietSub"

    init {
        this.con = con
        this.id = id
    }

    override fun getSubteam(doc: Document?) =
        doc?.selectFirst(".ah-bg-bd > ul:has(li > a[href].active)")?.previousElementSibling()?.selectFirst("span")?.text()?.trim()

    override fun getAnimeYear(doc: Document?): Long? {
        val parent =
            doc?.selectFirst(".ah-pif-fdetails > ul > li > strong:containsOwn(Năm phát hành)")?.parent() ?: return null
        var text = parent.text()
        val strong = parent.selectFirst("strong").text()
        text = text.replace(strong, "").trim().replace("\\s+".toRegex(), "").trim()
        return try {
            text.toLong()
        } catch (e: Exception) {
            null
        }
    }

    override fun getAnimeStream(doc: Document?): File? {
        val e = doc?.selectFirst("script[src^='http://animehay.tv/load-episode.php']") ?: return null
        val linkLoadEpisode = e.absUrl("src")
        val body  = con?.get(linkLoadEpisode, headers = hashMapOf("Referer" to doc.baseUri(), "User-Agent" to
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36"))?.body ?: return null
        return if (body.length < 10) null else parse_Jwplayer_Setup(body)
    }

    override fun getNewEpisodes(doc: Document?): ArrayList<Episode> {
        val eplist = arrayListOf<Episode>()
        val selects =
            doc?.select(".ah-pif-body .ah-pif-fdetails.ah-bg-bd ul > li.ah-pif-ne > a[href^='http://animehay.tv/phim/']")
                ?: return eplist
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
            doc?.select(".ah-row-film.ah-clear-both .ah-col-film[data-id] .ah-pad-film > a[href^='http://animehay.tv/phim/']")
                ?: return animelist
        for (select in selects) {
            val anime = Anime()
            anime.web_link = select.absUrl("href")
            anime.name = select.selectFirst("span.name-film")?.text()?.trim() ?: continue

            //get anime year
            select.selectFirst(".name-film > span:last-child")?.let {
                val animeYear = it.text()
                if (StringUtils.isNumeric(animeYear)) anime.year = animeYear.toLong()
            }
            animelist.add(anime)
        }
        return animelist
    }

    companion object {
        fun parse_Jwplayer_Setup(sourceCode: String): File? {
            var str_InfoLoad = MyString.textBetween(sourceCode, "var infoLoad", "};") ?: return null
            str_InfoLoad = MyString.textBetween(str_InfoLoad, "links:", "\"},") ?: return null
            val split = str_InfoLoad.split("\",\"".toRegex())
            val arrVideo = arrayListOf<File>()

            split.filter { it.length > 5 && it.contains("http") && it.contains("//") }
                ?.forEach {
                    val string = it.trim()
                    val split1 = string.split("\":\"".toRegex())
                    var label = split1[0]
                    var link: String? = split1[1]
                    if ((link?.length ?: 0) > 10 && link?.contains("http") == true) {
                        link = MyString.cleanJsonLink(link)
                        if (label.contains("z")) {
                            label = label.substring(label.indexOf("z") + 1)
                        }
                        val v = File()
                        v.sourceLink = (link)
                        v.label = label
                        arrVideo.add(v)
                    }
                }

            return when {
                arrVideo.isEmpty() -> null
                arrVideo.size == 1 -> arrVideo.first()
                else -> {
                    arrVideo.sortLabel()
                    val list: File? = arrVideo.first { it.label == "720" }
                    list ?: arrVideo.first()
                }
            }
        }
    }
}
