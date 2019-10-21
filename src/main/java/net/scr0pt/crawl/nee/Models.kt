package net.scr0pt.crawl.nee

import org.apache.commons.lang3.StringUtils
import net.scr0pt.thirdservice.google.utils.GoogleParser

/**
 * Created by Long
 * Date: 10/14/2019
 * Time: 10:32 PM
 */


class Anime {
    var name: String? = null
    var web_link: String? = null
    var mal_id: Long? = null
    var year: Long? = null
        set(year) {
            if (year == null || year < 1900 || year > 2030) {
                println("Year anime is invalid: " + year!!)
                return
            }
            field = year
        }
    var image: String? = null

    var eps: List<Episode>? = null
}


class Episode {
    var id: Long? = null
    var name: String? = null
        set(name) {
            field = ChuanHoa.chuanHoaEp(name)
        }
    var subteam: String? = null
        set(subteam) {
            field = ChuanHoa.chuanHoaSubTeamName(subteam)
        }
    var subtype: String? = null
    var web_link: String? = null
    var web_id: Long? = null
    var anime: Anime? = null
    var file: File? = null

}


class File {
    var server: String? = null
        private set
    var sourceLink: String? = null
        set(link) {
            field = link
            GoogleParser.getDriveIdfromURL(link)?.let{ driveid ->
                this.server = "Google Drive"
                this.fileId = driveid
            }
        }
    var fileId: String? = null
        private set
    var label: String? = null
    var size: Long? = null

    companion object {
        fun getIntLabel(label: String?): Int {
            val label1 = label?.trim() ?: return 0
            if (StringUtils.isNumeric(label1)) return label1.toInt()
            arrayOf("240", "360", "480", "720", "1080").forEach {
                if (label1.contains(it, ignoreCase = true)) return it.toInt()
            }
            if ("HD".equals(label1, ignoreCase = true)) return 720
            return if ("SD".equals(label1, ignoreCase = true)) 480 else 0
        }
    }
}


fun ArrayList<File>.sortLabel(){
    for (i in 0 until this.size - 1) {
        for (j in i + 1 until this.size) {
            val fi = this[i]
            val fj = this[j]
            if (File.getIntLabel(fi.label) < File.getIntLabel(fj.label)) {
                this[i] = fj
                this[j] = fi
            }
        }
    }
}