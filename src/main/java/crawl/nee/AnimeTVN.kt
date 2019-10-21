package crawl.nee

import crawl.nee.Web

/**
 *
 * @author Long
 * @date created Jul 13, 2018 6:47:02 PM
 * @class name NewClass
 */
class AnimeTVN @Throws(Exception::class)
constructor() : Web("animetvn") {
    init {
        this.allEpisodesinWatchPage = "#_listep a.tapphim[id^=\"ep_\"][href^='http://animetvn.tv/xem-phim/']"
        this.animeNameinAnimeInfoPage = ".main_page_title2 > h1.h1_main_title2"
        this.xemAnimeButtoninAnimeInfo = "a.btn.play-now[href^='http://animetvn.tv/xem-phim/']:containsOwn(Xem phim)"

        this.conn
            .cookie("animetvn_dh_popup_ads", "1")//add cookie ads
            .cookie("animetvn_dh_qaa", "1")//add cookie 18+
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
            val tvn = AnimeTVN()
            //        tvn.getThisAnime("http://animetvn.tv/thong-tin-phim/f5141-inuyashiki.html");
            tvn.run()
            //        tvn.getAnimeStreamTest("http://animetvn.tv/xem-phim/f146238-otome-wa-boku-ni-koishiteru-futari-no-elder-the-animationtap-02.html");
            //        tvn.getNewEpisodesTest("http://animetvn.tv/thong-tin-phim/f4797-violet-evergarden.html");
            //        tvn.getAnimeYearTest("http://animetvn.tv/thong-tin-phim/f4797-violet-evergarden.html");
        }
    }


}
