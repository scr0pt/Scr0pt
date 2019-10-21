package utils.curl.adapter

import java.net.MalformedURLException
import java.net.URL

/**
 * Created by Long
 * Date: 10/15/2019
 * Time: 12:40 AM
 */

object ParseURL {
    fun isValidUrl(url: String?): Boolean {
        url ?: return false
        /* Try creating a valid URL */
        return try {
            URL(url).toURI()
            true
        } catch (e: Exception) {
            //            println("Not a valid url : " + url);
            false
        }
        // If there was an Exception
        // while creating URL object
    }

    fun absUrl(url: String?, base: String): String? {
        if (url == null || url.length < 2) {
            return null
        }
        return if (url.contains("http://") || url.contains("https://")) {
            url
        } else {
            base.replace("/+$".toRegex(), "") + "/" + url.replace("^/+".toRegex(), "")
        }
    }

    fun getBase(link: String): String? {
        val protocal = getProtocal(link) ?: return null
        val host = getHost(link) ?: return null
        return protocal + "://" + host
    }

    fun getProtocal(link: String): String? {
        try {
            val aURL = URL(link)

            return aURL.protocol
        } catch (ex: MalformedURLException) {
            System.err.println("Đã có lỗi xảy ra getPath :$link")
        }

        return null
    }

    fun getHost(link: String): String? {
        try {
            val aURL = URL(link)

            return aURL.host
        } catch (ex: MalformedURLException) {
            System.err.println("Đã có lỗi xảy ra getHost :$link")
        }

        return null
    }
}