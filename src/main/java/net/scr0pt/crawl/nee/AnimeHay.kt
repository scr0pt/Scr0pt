package net.scr0pt.crawl.nee
/**
 *
 * @author Long
 * @date created Jul 13, 2018 7:18:57 PM
 * @class name AnimeHay
 */
class AnimeHay @Throws(Exception::class)
constructor() : Web("animehay") {
    init {
        this.allEpisodesinWatchPage = ".ah-wf-body .ah-wf-le.ah-bg-bd > ul > li > a[href^='http://animehay.tv/phim/']"
        this.animeNameinAnimeInfoPage = ".ah-pif-fname h1 span[itemprop=\"name\"]"
        this.xemAnimeButtoninAnimeInfo =
            ".ah-float-left > span > a.button-one[href^='http://animehay.tv/phim/']:containsOwn(Xem phim)"

        this.conn.cookie("check_vn", "1")
    }

    /**
     * Cài đặt session, biến con
     * @return the boolean
     */
    override fun initCon(): Boolean {
        println("Connecting to session")
        return initFromConfigFile(2) || initFromJsoupCon(false, 4)
    }

    companion object {

        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val ahay = AnimeHay()
            //        ahay.getThisAnime("http://animehay.tv/phim/nogizaka-haruka-no-himitsu-f1140.html");
            ahay.run()
            //        ahay.getAnimeStreamTest("http://animehay.tv/phim/jashin-chan-dropkick-tap-1-e64733.html");
            //        ahay.getNewEpisodesTest("http://animehay.tv/phim/isekai-maou-to-shoukan-shoujo-no-dorei-majutsu-f2653.html");
            //        ahay.getAnimeYearTest("http://animehay.tv/phim/isekai-maou-to-shoukan-shoujo-no-dorei-majutsu-f2653.html");
        }
    }


}
