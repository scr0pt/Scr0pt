package thirdservice.openload.entity.response

/**
 * Created by Long
 * Date: 2/21/2019
 * Time: 8:41 PM
 */

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import thirdservice.openload.entity.RemoteUploadStatus

class RemoteUploadStatus_Response {

    @SerializedName("success")
    @Expose
    var success: Int? = null
    @SerializedName("data")
    @Expose
    var data: List<RemoteUploadStatus>? = null

}