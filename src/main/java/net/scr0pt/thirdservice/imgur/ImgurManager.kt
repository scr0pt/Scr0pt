package net.scr0pt.thirdservice.imgur

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.jsoup.Jsoup
import net.scr0pt.thirdservice.imgur.entity.CheckCaptchaResponse
import net.scr0pt.thirdservice.imgur.entity.ImgurAcc
import net.scr0pt.thirdservice.imgur.entity.RemoteUploadResponse
import net.scr0pt.utils.curl.LongConnection

fun main2() {
    val imgurAcc = ImgurAcc()
    val imgurManager = ImgurManager(imgurAcc)

    val arr = arrayListOf<String>()
    for (j in 1..5) {
        arr.addAll(getImage(j))
    }

    var i = 0

        val longConnection = LongConnection()
    while (true) {
        Thread.sleep(2000)
        i++
        println(i)
        val url = arr[i]

        val response = longConnection.post(
            "https://postimages.org/json/rr",
            hashMapOf(
                "token" to "61aa06d6116f7331ad7b2ba9c7fb707ec9b182e8",
                "upload_session" to "XpmWNtKoit1PeLrbBSAsCiIftrMzqe6B",
                "url" to url,
                "numfiles" to "1",
                "gallery" to "",
                "token" to "61aa06d6116f7331ad7b2ba9c7fb707ec9b182e8"
                )
        )

        println("url: $url ~ " + response?.jsonObj?.get("url"))
    }
}

fun main() {
    val imgurAcc = ImgurAcc()
    val imgurManager = ImgurManager(imgurAcc)

    val arr = arrayListOf<String>()
    for (j in 1..5) {
        arr.addAll(getImage(j))
    }

    var i = 0

    while (true) {
        Thread.sleep(2000)
        i++
        println(i)
        val url = arr[i]
        println("url: $url")
        val remoteUpload = imgurManager.remoteUpload(url)
        println(Gson().toJson(remoteUpload))
        remoteUpload?.data?.let {
            if (it.id == null) {
                return
            }

        }
    }
}

fun getImage(i: Int): ArrayList<String> {
    val arr = arrayListOf<String>()
    Jsoup.connect("https://wall.alphacoders.com/by_category.php?id=3&name=Anime+Wallpapers&page=$i").get()
        ?.body()
        ?.select(".thumb-container-big .boxgrid img")
        ?.forEach {
            it?.attr("data-src")?.let {
                arr.add(it)
            }
        }
    return arr
}

/**
 * Created by Long
 * Date: 2/26/2019
 * Time: 11:46 PM
 */
class ImgurManager(var imgurAcc: ImgurAcc) {
    val conn = LongConnection().also {
        it.header("x-requested-with", "XMLHttpRequest")
        it.header("accept", "*/*")
        it.header("accept-encoding", "gzip, deflate, br")
        it.header("accept-language", "en,en-GB;q=0.9,vi;q=0.8,fr-FR;q=0.7,fr;q=0.6,en-US;q=0.5,ja;q=0.4")
        it.header("cache-control", "no-cache")
        it.header("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
        it.header("dnt", "1")
        it.header("origin", "https://imgur.com")
        it.header("pragma", "no-cache")
        it.referrer("https://imgur.com/upload")
        it.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.119 Safari/537.36")
    }

    fun checkCaptcha(): CheckCaptchaResponse? {
        val body = conn.post(
            "https://imgur.com/upload/checkcaptcha",
            hashMapOf("total_uploads" to "1", "create_album" to "true")
        )?.body
        //        {"data":{"overLimits":0,"upload_count":0,"new_album_id":"vP6yRUn","deletehash":"O1xIdRqfN0yPIn4"},"success":true,"status":200}
        return gson.fromJson<CheckCaptchaResponse>(body, CheckCaptchaResponse::class.java)
    }

    fun remoteUpload(url: String, newAlbumId: String? = null): RemoteUploadResponse? {
        val data = hashMapOf("image" to url, "type" to "URL", "name" to "")
        if (newAlbumId != null) {
            data.put("new_album_id", newAlbumId)
        }
        val body = conn.post("https://api.imgur.com/3/image?client_id=546c25a59c58ad7", data)?.body
        return gson.fromJson<RemoteUploadResponse>(body, RemoteUploadResponse::class.java)
    }

    companion object {
        var gson = GsonBuilder().setPrettyPrinting().create()
    }
}
