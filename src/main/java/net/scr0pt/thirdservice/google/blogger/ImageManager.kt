package net.scr0pt.thirdservice.google.blogger

import net.scr0pt.utils.curl.LongConnection

/**
 * Created by Long
 * Date: 10/13/2019
 * Time: 10:11 PM
 */


fun main() {
    val imageManager = ImageManager()
    imageManager.transload("https://images.alphacoders.com/605/605592.png")
}

class ImageManager {
    val endPoint = "https://www.blogger.com/e/picker/getcu"

    val conn = LongConnection().also {
        it.header("sec-fetch-mode", "cors")
        it.header("x-same-domain", "explorer")
        it.header("origin", "https://www.blogger.com")
        it.header("accept-encoding", "gzip, deflate, br")
        it.header("accept-language", "en,en-GB;q=0.9,vi;q=0.8,fr-FR;q=0.7,fr;q=0.6,en-US;q=0.5,ja;q=0.4")
        it.header("content-control", "no-cache")
        it.header(
            "cookie",
            "SID=pQcvtMVKhsTDpDTnGEY6fIV59ia5DUaDFvdQgPViqiaeAvWHiqxBC0rC5rfdyCAaE5yliQ.; HSID=ArqG3waEYO-sJ2YH7; SSID=AxirEXWcV5EF8Sfkz; APISID=IBEhz1YjOJjKXOJl/Ap4Pq8-ATsUQ6hUsF; SAPISID=bWOIVjqnxHWO2PSI/ASSvwdIleYPbIl_4V; NID=189=PnbuZJVw25-AyM_UNfiniHV0700e-zGgzuGQISLF5SPj7kMk87_sggRjh8Io241fwGHu2Ps1kOhFps8Eo0yiAOmUeDCkLoLWXvmaailDufMiy2kKGuYMpcsPFicMqndUEEA7vlZ2vrLK6XVrczLrQOXHfF5Dym-7xpsuiJ8wB_Q; TL=-"
        )
        it.header("pragma", "no-cache")
        it.header(
            "user-agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36"
        )
        it.header(
            "content-type",
            "application/x-www-form-urlencoded;charset=UTF-8",
            method = LongConnection.REQUEST_METHOD.POST
        )
        it.header("accept", "*/*")
        it.header("cache-control", "no-cache")
        it.header("authority", "www.blogger.com")
        it.header(
            "referer",
            "https://www.blogger.com/e/picker?protocol=gadgets&origin=https%3A%2F%2Fwww.blogger.com&hl=en&relayUrl=https%3A%2F%2Fwww.blogger.com%2Frpc_relay.html&rpcUrl=https%3A%2F%2Fwww-rpcjs-opensocial.googleusercontent.com%2Fgadgets%2Fjs%2Frpc.js%3Fc%3D1%26container%3Dblogger&authuser=0&st=000770F203FCE44126F6C2EB5CB087D84B5F3103E5B1EFA048%3A%3A1570979198383&hostId=blogger&multiselectEnabled=true&selectButtonLabel=Add%20selected&uploadToAlbumId=6747304826709347889&thumbs=1600&title=Add%20Images&pp=%5B%5B%22blogger%22%2C%7B%22albumId%22%3A%226747304826709347889%22%2C%22copyFromPicasa%22%3Atrue%7D%5D%5D&nav=((%22photos%22%2C%22Upload%22%2C%7B%22mode%22%3A%22palette%22%2C%22hideBc%22%3A%22true%22%2C%22upload%22%3A%22true%22%2C%22data%22%3A%7B%22silo_id%22%3A%223%22%7D%2C%22parent%22%3A%226747304826709347889%22%7D)%2C(%22photos%22%2C%22From%20this%20blog%22%2C%7B%22hideBc%22%3A%22true%22%2C%22mode%22%3A%22blogger%22%2C%22parent%22%3A%226747304826709347889%22%7D)%2C(%22photos%22%2C%22From%20Google%20Album%20Archive%22)%2C(%22photos%22%2C%22From%20your%20phone%22%2C%7B%22type%22%3A%22camerasync%22%7D)%2C(%22webcam%22%2C%22From%20your%20webcam%22%2C%7B%22type%22%3A%22standard%22%2C%22data%22%3A%7B%22silo_id%22%3A%223%22%7D%7D)%2C(%22url%22%2C%22From%20a%20URL%22))&rpcService=14yili3ckz9f&rpctoken=u6y9bxdi7gxr"
        )
        it.header("sec-fetch-site", "same-origin")
        it.header("dnt", "1")
    }

    fun transload(url: String) {
        val response = conn.post(
            "https://www.blogger.com/e/picker/getcu",
            hashMapOf(
                "url" to url,
//                "h" to "600",
//                "w" to "800",
                "token" to "Nty26W0BAAA.rjOhFakLotk21XPuXH31ig.rhD9ONqiWPPlLzFlRYeiIw"
            )
        )

        println(response?.body)
    }
}