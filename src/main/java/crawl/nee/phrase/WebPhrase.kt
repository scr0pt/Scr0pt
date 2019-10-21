package crawl.nee.phrase


import crawl.nee.Anime
import crawl.nee.Episode
import crawl.nee.File
import org.jsoup.Connection
import org.jsoup.nodes.Document
import utils.curl.LongConnection


/**
 *
 * @author Long
 * @date created Sep 14, 2018 12:09:37 PM
 * @class name Web
 */
abstract class WebPhrase {
    var con: LongConnection? = null
    var id: Long? = null//id cua web trong bang anime_source_website

    abstract val subType: String
    /**
     * get subteam of this episode from watch anime page
     */
    abstract fun getSubteam(doc: Document?): String?

    /**
     * Lấy thông tin năm sản xuất anime từ trang anime info
     * @param doc the value of doc
     */
    abstract fun getAnimeYear(doc: Document?): Long?

    /**
     * Lấy thông tin link stream của tập này từ document của trang xem anime
     * @param doc the value of doc
     * @return File
     */
    abstract fun getAnimeStream(doc: Document?): File?

    /**
     * Hàm này lấy danh sách các tập của anime từ trang anime info
     * @param doc the value of doc
     */
    abstract fun getNewEpisodes(doc: Document?): ArrayList<Episode>

    /**
     */
    abstract fun getNewAnimes(doc: Document?): ArrayList<Anime>

    companion object {
        fun getInstance(webName: String?, con: LongConnection, id: Long?): WebPhrase? {
            return when (webName?.toLowerCase()) {
                //Cùng tên class với các class trong folder Nee
                "anime47" -> Anime47(con, id)
                "animetvn" -> AnimeTVN(con, id)
                "animevsub" -> AnimeVsub(con, id)
                "animehay" -> AnimeHay(con, id)
                "gogoanime" -> GoGoAnime(con, id)
                else -> throw AssertionError()
            }
        }
    }
}
