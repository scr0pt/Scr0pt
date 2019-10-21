package net.scr0pt.thirdservice.google.photo

/**
 * Created by Long
 * Date: 1/23/2019
 * Time: 11:13 PM
 */
class MediaItem {
    //All full Url
    var id: String? = null
    var title: String? = null
    var baseUrl: String? = null
    var width: Long? = null
    var height: Long? = null
    var creationTime: String? = null
    var realDocId: String? = null
//        shareUrl = StringUtils.removeStart(shareUrl, "https://photos.app.goo.gl/");
    var shareUrl: String? = null
    var shareAlbum: String? = null

    var status: Long? = null

    val isFileNotFound: Boolean
        get() = FILE_NOT_FOUND == status

    val productUrl: String?
        get() = if ((id?.length ?: 0) > 4 ) {
            "https://photos.google.com/lr/photo/$id"
        } else null

    val fullShareUrl: String?
        get() = if ((shareUrl?.length ?: 0)  > 10) {
            "https://photos.app.goo.gl/$shareUrl"
        } else null

    companion object {
        val FILE_NOT_FOUND: Long? = -1L
    }
}
