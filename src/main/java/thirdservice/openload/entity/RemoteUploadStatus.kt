package thirdservice.openload.entity

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by Long
 * Date: 2/21/2019
 * Time: 8:42 PM
 */
class RemoteUploadStatus {

    @SerializedName("id")
    @Expose
    var id: String? = null
    @SerializedName("status")
    @Expose
    var status: String? = null
    @SerializedName("url")
    @Expose
    var url: String? = null
    @SerializedName("headers")
    @Expose
    var headers: String? = null
    @SerializedName("bytes_loaded")
    @Expose
    var bytesLoaded: String? = null
    @SerializedName("bytes_total")
    @Expose
    var bytesTotal: String? = null
    @SerializedName("added")
    @Expose
    var added: String? = null
    @SerializedName("last_update")
    @Expose
    var lastUpdate: String? = null
    @SerializedName("retries")
    @Expose
    var retries: String? = null
    @SerializedName("folderid")
    @Expose
    var folderid: String? = null

}
