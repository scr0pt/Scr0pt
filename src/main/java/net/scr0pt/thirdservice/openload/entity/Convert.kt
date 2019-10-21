package net.scr0pt.thirdservice.openload.entity

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by Long
 * Date: 2/21/2019
 * Time: 8:47 PM
 */
class Convert {

    @SerializedName("name")
    @Expose
    var name: String? = null
    @SerializedName("id")
    @Expose
    var id: String? = null
    @SerializedName("folderid")
    @Expose
    var folderid: String? = null
    @SerializedName("status")
    @Expose
    var status: String? = null
    @SerializedName("last_update")
    @Expose
    var lastUpdate: Any? = null
    @SerializedName("progress")
    @Expose
    var progress: Any? = null
    @SerializedName("retries")
    @Expose
    var retries: String? = null
    @SerializedName("link")
    @Expose
    var link: String? = null
    @SerializedName("linkextid")
    @Expose
    var linkextid: String? = null

}



