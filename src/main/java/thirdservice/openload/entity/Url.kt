package thirdservice.openload.entity

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by Long
 * Date: 2/21/2019
 * Time: 9:26 PM
 */
class Url {

    @SerializedName("linkextid")
    @Expose
    var linkextid: String? = null
    @SerializedName("splash")
    @Expose
    var splash: String? = null

}