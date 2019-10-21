package thirdservice.imgur.entity

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by Long
 * Date: 2/27/2019
 * Time: 12:24 AM
 */
class RemoteUploadResponse {
    @SerializedName("data")
    @Expose
    var data: RemoteUploadResponseData? = null
    @SerializedName("success")
    @Expose
    var success: Boolean? = null
    @SerializedName("status")
    @Expose
    var status: Int? = null
}

