package thirdservice.imgur.entity

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

import java.sql.Timestamp

/**
 * Created by Long
 * Date: 2/27/2019
 * Time: 12:56 AM
 */
class ImgurAcc {
    @Expose
    var id: Long? = null
    @Expose
    var user: String? = null
    @Expose
    var pass: String? = null
    @Expose
    var email: String? = null
    @Expose
    var cookie: String? = null
    @Expose
    @SerializedName("clientid")
    var clientId: String? = null
    @Expose
    var secret: String? = null
    @Expose
    @SerializedName("date_created")
    var dateCreated: Timestamp? = null
    @Expose
    @SerializedName("last_update")
    var lastUpdate: Timestamp? = null
    @Expose
    @SerializedName("temp_banned")
    var tempNanned: Timestamp? = null
}
