package net.scr0pt.thirdservice.google.photo

import org.jsoup.nodes.Document
import net.scr0pt.utils.MyString
import net.scr0pt.utils.curl.LongConnection

/**
 * Created by Long
 * Date: 10/7/2019
 * Time: 8:41 PM
 */
class PhotoManager(val photoAcc: PhotoAcc) {
    val conn = LongConnection().also {
        it.header("accept", "*/*")
        it.header("accept-encoding", "gzip, deflate, br")
        it.header("dnt", "1")
        photoAcc.cookie?.let { it1 -> it.header("cookie", it1) }
        it.header("Upgrade-Insecure-Requests", "1")
        it.header(
            "content-type",
            "application/x-www-form-urlencoded;charset=UTF-8",
            method = LongConnection.REQUEST_METHOD.POST
        )
        it.header("origin", "https://photos.google.com")
        it.header("pragma", "no-cache")
        it.referrer("https://photos.google.com/")
        it.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36")
    }


    //using curl
    fun getRealDocId(productUrl: String): String? {
        conn.get(productUrl)?.let { response ->
            if (response.statusCode == 404 && response.statusMessage == "Not Found") {
                return null
            } else {
                response.url
                    ?.takeIf { it.contains("photos.google.com/photo/") }
                    ?.let {
                        return it.substringAfter("photos.google.com/photo/").removeSuffix("/")
                    }
            }
        }
        return null

    }

    private fun getRealPhotoId(doc: Document): String? {
        var href = doc.selectFirst("link[rel=\"canonical\"][href^='https://photos.google.com/photo/']")?.attr("href")
            ?: return null
        if (href.contains("https://photos.google.com/photo/")) {
            href = href.removePrefix("https://photos.google.com/photo/")
            href = href.removeSuffix("/")
        }
        return href
    }

    //Get AF_with_timestamp
    fun getAF_with_timestamp(doc: Document): String? {
        doc.select("script")?.forEach { script ->
            script.data()?.takeIf {
                it.startsWith("window.IJ_values") && it.contains("get.google.com")
                        && it.contains("photos.google.com") && it.contains("plus.google.com")
                        && it.contains(":") && it.contains("'")
            }?.let { data ->
                data.split(",".toRegex())
                    .filter { it.startsWith("'") && it.endsWith("'") && it.contains(":") }
                    .forEach { s1 ->
                        val s2 = s1.substring(1, s1.length - 1)
                        s2.split(":".toRegex())?.get(1)?.toLongOrNull()?.let {
                            return s2
                        }
                    }
            }
        }
        return null
    }

    fun getSid(doc: Document): String? {
        doc.select("script")?.forEach { script ->
            script.data()
                ?.takeIf { it.startsWith("window.WIZ_global_data") && it.contains("FdrFJe") }
                ?.let { data ->
                    data.split(",".toRegex())
                        .filter { it.startsWith("\"FdrFJe\":\"") && it.length > 20 && it.endsWith("\"") }
                        .forEach {
                            return it.substring(10, it.length - 1)
                        }
                }
        }
        return null
    }

    private fun getSharePhotoId(doc: Document): String? {
        doc.select("script")?.forEach { script ->
            script.data()
                ?.takeIf {
                    it.startsWith("AF_initDataCallback") &&
                            it.contains("video-downloads.googleusercontent.com") &&
                            it.contains("ds:") &&
                            it.contains("key:") &&
                            it.contains("hash")
                }
                ?.let { data ->
                    data.split(",".toRegex())
                        .filter { it.startsWith("[[\"") && it.length > 20 && it.endsWith("\"") }
                        .forEach {
                            return it.substring(3, it.length - 1)
                        }
                }
        }
        return null
    }

    fun makeCopy(mediaItem: MediaItem?): String? {
        if (mediaItem?.fullShareUrl == null) {
            return null
        }
        conn.get(mediaItem.fullShareUrl)?.let { response ->
            println(response.body)
            val finalShareUrl = response.url ?: return null
            val doc = response.doc ?: return null
            val shareAlbum = MyString.textBetween(finalShareUrl, "photos.google.com/share/", "?key=") ?: return null
            val key = finalShareUrl.substringAfterLast("?key=")
            val AF_with_timestamp = getAF_with_timestamp(doc) ?: return null
            val sharePhotoId = getSharePhotoId(doc) ?: return null
            val sid = getSid(doc) ?: return null

            val requestUrl =
                """${photoAcc.baseUrl}/_/PhotosUi/data/batchexecute?rpcids=V8RKJ&f.sid=$sid&bl=boq_photosuiserver_20190121.06_p0&hl=vi&soc-app=165&soc-platform=1&soc-device=1&_reqid=1084990&rt=c"""

            conn.post(
                requestUrl, hashMapOf(
                    "f.req" to """[[["V8RKJ","[[\"$sharePhotoId\"],\"$key\",\"$shareAlbum\"]",null,"generic"]]]""",
                    "at" to AF_with_timestamp
                )
            )?.let { response2 ->
                response2.body?.split("\"".toRegex())
                    ?.filter {
                        it.length > 20 && !it.startsWith("-")
                                && it[0].toString().toLongOrNull() == null && it.endsWith("\\")
                    }
                    ?.forEach {
                        return it.removeSuffix("\\")
                    }
            }
        }
        return null
    }

    //Valid for current ip
    fun getFinalShareUrl(shareUrl: String): String? {
        val response = conn.get(shareUrl) ?: return null
        val body = response.body ?: return null
        val doc = response.doc ?: return null
        var finalShareUrl = response.url ?: return null
        if (body.contains("video-downloads.googleusercontent.com")
            && finalShareUrl.contains("share")
        ) {
            val finalSharePhotoId = getSharePhotoId(doc) ?: return null
            finalShareUrl = (finalShareUrl.substringBefore("?key=")
                    + "/photo/" + finalSharePhotoId
                    + "?key=" + finalShareUrl.substringAfter("?key="))

            if (conn.get(finalShareUrl)?.statusCode == 200) {
                return finalShareUrl
            }
        }
        return null
    }

    private fun getPhotoUrl(mediaItem: MediaItem?): String? {
        return when {
            mediaItem == null -> null
            mediaItem.realDocId != null -> photoAcc.baseUrl + "/photo/" + mediaItem.realDocId
            mediaItem.id != null -> photoAcc.baseUrl
            else -> null
        }
    }

    fun getShareUrl(mediaItem: MediaItem): String? {
        val photoUrl = getPhotoUrl(mediaItem) ?: return null
        val response = conn.get(photoUrl)
        val doc = response?.doc ?: return null

        if (response.statusCode == 404 && response.statusMessage == "Not Found") {
            return null
        }

        if ("Đăng nhập - Tài khoản Google".equals(doc.title(), ignoreCase = true)) {
            return null
        }

        val sid = getSid(doc) ?: return null
        val af_with_timestamp = getAF_with_timestamp(doc) ?: return null

        if (mediaItem.realDocId == null) {
            val realPhotoId = getRealPhotoId(doc) ?: return null
            mediaItem.realDocId = realPhotoId
        }

        val requestUrl =
            photoAcc.baseUrl + "/_/PhotosUi/data/batchexecute?rpcids=SFKp8c&f.sid=" + sid + "&bl=boq_photosuiserver_20190121.06_p0&hl=vi&soc-app=165&soc-platform=1&soc-device=1&_reqid=2338785&rt=c"
        conn.post(
            requestUrl, hashMapOf(
                "f.req" to """[[["SFKp8c","[null,null,[null,false,null,null,true,null,[[[1,1],false],[[1,2],false],[[2,1],true],[[2,2],true]]],[2,null,[[[\"${mediaItem.realDocId}\"]]],null,null,[],[1],false,null,null,[]]]",null,"generic"]]]""",
                "at" to af_with_timestamp
            )
        )?.body?.takeIf { it.contains("\\\"https://photos.app.goo.gl/") }
            ?.let {
                return MyString.textBetween(it, "\\\"https://photos.app.goo.gl/", "\\\"")
            }
        return null
    }
}