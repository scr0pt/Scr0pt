package net.scr0pt.crawl.nee

/**
 * Created by Long
 * Date: 10/14/2019
 * Time: 10:54 PM
 */

object ChuanHoa {

    //Chuẩn hóa subteam Name
    fun chuanHoaSubTeamName(name: String?): String? {
        var subteamName = name?.trim() ?: return null
        if (subteamName.isEmpty()) return null

        if (subteamName.length < 2) return subteamName
        subteamName.removeSuffix(":")
        subteamName = subteamName.replace("\\s+".toRegex(), " ").trim()
        subteamName = subteamName.replace("(?i)(server)(:|\\s:|\\s:\\s|\\s)".toRegex(), "")
        return subteamName.replace("\\s+".toRegex(), " ").trim()
    }

    //Chuan hoa ep
    fun chuanHoaEp(episode: String?): String? {
        var ep = episode ?: return null
        if (ep.isEmpty()) return null
        ep = ep.replace("\\s+".toRegex(), " ").trim()
        ep = ep.replace("(?i)(episode\\s)".toRegex(), "")
        ep = ep.replace("(?i)(tập\\s)".toRegex(), "")
        ep = ep.replace("(?i)(tap\\s)".toRegex(), "")
        ep = ep.replace("(?i)(ep\\s)".toRegex(), "")
        //        ep =  ep.replaceAll("(?i)(movie)$", "");
        ep = ep.replace("\\s+".toRegex(), " ").trim()
        return ep
    }
}