package thirdservice.openload.entity.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import thirdservice.openload.entity.Convert

/**
 * Created by Long
 * Date: 2/21/2019
 * Time: 8:48 PM
 */

class GetJobs_Response {

    @SerializedName("success")
    @Expose
    var success: Int? = null
    @SerializedName("remote")
    @Expose
    var remote: List<Any>? = null
    @SerializedName("converts")
    @Expose
    var converts: List<Convert>? = null
}
