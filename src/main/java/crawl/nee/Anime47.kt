package crawl.nee


/**
 *
 * @author Long
 * @date created Jul 11, 2018 10:47:39 PM
 * @class name Anime47
 */
class Anime47 @Throws(Exception::class)
constructor() : Web("anime47") {
    init {
        this.allEpisodesinWatchPage = ".server .episodes > ul > li > a[href*='anime47.com/xem-phim-']"
        this.animeNameinAnimeInfoPage = "h1.movie-title > span.title-1[itemprop=\"name\"]"
        this.xemAnimeButtoninAnimeInfo =
            "ul.btn-block > li.item > a#btn-film-watch.btn-red[href]:containsOwn(Xem Anime)"
        //    this.con.cookie("", newPage) khong biet phai them cookie gi de location la viet nam

    }

    /**
     * Cài đặt session, biến con
     * @return the boolean
     */
    override fun initCon(): Boolean {
        println("Connecting to session")
        return if (OSUtils.isWindows()) {
            initFromConfigFile(2) || initFromHtmlUnit(false, 4)

        } else
            initFromConfigFile(2) || initFromHtmlUnit(true, 6)
    }

    companion object {

        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val a47 = Anime47()
            a47.getThisAnime("https://anime47.com/phim/the-idolm-ster-shiny-festa/m4493.html")
            //        a47.run();
            //        a47.getAnimeStreamTest("http://anime47.com/xem-phim-baki-ep-10/155585.html");
            //        a47.getNewEpisodesTest("http://anime47.com/phim/violet-evergarden/m6563.html");
            //        a47.getAnimeYearTest("https://anime47.com/phim/the-idolm-ster-shiny-festa/m4493.html");
        }
    }


}
