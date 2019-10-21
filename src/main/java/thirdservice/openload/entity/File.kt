package thirdservice.openload.entity

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by Long
 * Date: 2/22/2019
 * Time: 12:20 AM
 */
class File {
    @SerializedName("name")
    @Expose
    var name: String? = null
    @SerializedName("cblock")
    @Expose
    var cblock: String? = null
    @SerializedName("sha1")
    @Expose
    var sha1: String? = null
    @SerializedName("folderid")
    @Expose
    var folderid: String? = null
    @SerializedName("upload_at")
    @Expose
    var uploadAt: String? = null
    @SerializedName("status")
    @Expose
    var status: String? = null
    @SerializedName("size")
    @Expose
    var size: String? = null
    @SerializedName("content_type")
    @Expose
    var contentType: String? = null
    @SerializedName("download_count")
    @Expose
    var downloadCount: String? = null
    @SerializedName("cstatus")
    @Expose
    var cstatus: String? = null
    @SerializedName("link")
    @Expose
    var link: String? = null
    @SerializedName("linkextid")
    @Expose
    var linkextid: String? = null
}
