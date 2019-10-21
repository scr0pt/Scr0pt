package net.scr0pt.thirdservice.openload.entity.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import net.scr0pt.thirdservice.openload.entity.Url

/**
 * Created by Long
 * Date: 2/21/2019
 * Time: 9:26 PM
 */
class GetSplash_Response {
    @SerializedName("success")
    @Expose
    var success: Int? = null
    @SerializedName("url")
    @Expose
    var url: List<Url>? = null

}

