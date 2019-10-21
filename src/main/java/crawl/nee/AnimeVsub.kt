package crawl.nee

import crawl.nee.Web

/**
 *
 * @author Long
 * @date created Jul 13, 2018 4:03:07 PM
 * @class name NewClass
 */
class AnimeVsub @Throws(Exception::class)
constructor() : Web("animevsub") {
    init {
        this.allEpisodesinWatchPage = "li.episode > a.episode-link[data-id][href^='http://animevsub.tv/phim/']"
        this.animeNameinAnimeInfoPage = ".TPost.Single > header > h1.Title"
        this.xemAnimeButtoninAnimeInfo = "a.watch_button_more[href^='http://animevsub.tv/phim/']:containsOwn(Xem phim)"

        this.conn.cookie("PLTV__geoip_confirm", "1")
    }

    /**
     * Cài đặt session, biến con
     * @return the boolean
     */
    override fun initCon(): Boolean {
        println("Connecting to session")
        return if (OSUtils.isWindows()) {
            initFromConfigFile(2) || initFromJsoupCon(false, 4)

        } else
            initFromConfigFile(2) || initFromJsoupCon(true, 6)
    }

    companion object {

        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val avsub = AnimeVsub()
            avsub.run()
            //        avsub.getAnimeStreamTest("http://animevsub.tv/phim/violet-evergarden-2882/tap-13-57230.html");
            //        avsub.getNewEpisodesTest("http://animevsub.tv/phim/violet-evergarden-2882/");
            //        avsub.getAnimeYearTest("http://animevsub.tv/phim/violet-evergarden-2882/");
        }
    }


}
