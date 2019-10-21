package net.scr0pt.thirdservice.imgur.entity

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by Long
 * Date: 2/27/2019
 * Time: 12:01 AM
 */
class CheckCaptchaResponse {

    @SerializedName("data")
    @Expose
    var data: CheckCaptchaResponseData? = null
    @SerializedName("success")
    @Expose
    var success: Boolean? = null
    @SerializedName("status")
    @Expose
    var status: Int? = null

}
