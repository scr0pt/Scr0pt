package net.scr0pt.thirdservice.google.utils

import net.scr0pt.utils.MyString
import net.scr0pt.utils.curl.NetworkUtils

/**
 * Created by Long
 * Date: 10/14/2019
 * Time: 10:35 PM
 */
object GoogleParser {
    fun validDriveid(driveid: String?): String? {
        return if (driveid == null || driveid.length < 24)
            null
        else
            driveid
    }

    fun getDriveIdfromURL(link: String?): String? {
        link ?: return null
        try {
            var driveid: String? =
                fromStream(link)
                        ?: fromDownload(link)
                        ?: fromDocEmbed(link)
                        ?: fromDriveEmbed(link)
                        ?: fromApiDrivev2(
                                link
                        )
            if (driveid != null) {
                return driveid
            }

            //xuống bước này
            val finalLink = NetworkUtils.followRedirect(url = link)  ?: return null
            if (link == finalLink) {
                return null
            }

            driveid = fromStream(finalLink) ?: return null
            validDriveid(driveid) ?: return null
            return driveid
        } catch (e: Exception) {
        }

        return null
    }

    fun fromStream(link: String?): String? {
        link ?: return null
        return if (link.contains("driveid=")) {
            MyString.textBetween(link, "driveid=", "&amp;", 25, 40)
        } else {
            null
        }
    }

    /**
     * @param link the value of link
     */
    fun fromDownload(link: String?): String? {
        link ?: return null
        return if (link.contains("docs.googleusercontent.com/docs/securesc/") && link.contains("/*/")) {
            link.substringAfter("/*/").substringBefore("?")
        } else
            null
    }

    fun fromDocEmbed(link: String?): String? {
        link ?: return null
        return if (link.contains("doc.google.com") && link.contains("/d/")) {
            link.substringAfter("/d/").substringBefore("/")
        } else {
            null
        }
    }

    fun fromDriveEmbed(link: String?): String? {
        link ?: return null
        return if (link.contains("drive.google.com") && link.contains("/d/")) {
            link.substringAfter("/d/").substringBefore("/")
        } else {
            null
        }
    }

    fun fromApiDrivev2(link: String?): String? {
        link ?: return null
        if (link.contains("googleapis.com/drive/v") && link.contains("/files/")) {
            return link.substringAfter("/files/").substringBefore("?")
        } else {
            return null
        }
    }
}