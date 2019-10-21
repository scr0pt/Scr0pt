package thirdservice.imgur.entity

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by Long
 * Date: 2/27/2019
 * Time: 12:00 AM
 */
class CheckCaptchaResponseData {

    @SerializedName("overLimits")
    @Expose
    var overLimits: Int? = null
    @SerializedName("upload_count")
    @Expose
    var uploadCount: String? = null
    @SerializedName("new_album_id")
    @Expose
    var newAlbumId: String? = null
    @SerializedName("deletehash")
    @Expose
    var deletehash: String? = null

}

