package thirdservice.google.photo

/**
 * Created by Long
 * Date: 10/7/2019
 * Time: 8:45 PM
 */
class PhotoAcc {
    val cookie: String? = null
    val pageId: String? = null

    val baseUrl: String
        get() {
            var requestUrl = "https://photos.google.com"
            pageId?.let { requestUrl += "/b/$pageId" }
            return requestUrl
        }
}