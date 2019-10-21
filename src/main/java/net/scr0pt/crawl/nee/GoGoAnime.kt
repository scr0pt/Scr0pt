package net.scr0pt.crawl.nee

import net.scr0pt.OSUtils

/**
 *
 * @author Long
 * @date created Jul 14, 2018 10:48:58 AM
 * @class name GoGoAnime
 */
class GoGoAnime @Throws(Exception::class)
constructor() : Web("gogoanime") {
    init {
        this.allEpisodesinWatchPage = null//suing ajax
        this.animeNameinAnimeInfoPage = ".anime_info_body > .anime_info_body_bg > h1"

        //    this.con.cookie("", newPage) khong biet phai them cookie gi de location la viet nam

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
            val gogo = GoGoAnime()
            gogo.run()
            //        gogo.getAnimeStreamTest("https://www4.gogoanime.se/future-card-buddyfight-ace-episode-7");
        }
    }


}
