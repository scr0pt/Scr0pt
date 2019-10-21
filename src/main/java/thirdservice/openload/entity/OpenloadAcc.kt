package thirdservice.openload.entity

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

import java.sql.Timestamp


/**
 * Created by Long
 * Date: 2/22/2019
 * Time: 10:23 PM
 */
class OpenloadAcc {
    @Expose
    var id: Long? = null

    @Expose
    var email: String? = null


    @Expose
    @SerializedName("pass")
    var password: String? = null

    @Expose
    var cookie: String? = null

    @Expose
    @SerializedName("x_csrf_token")
    var xCsrfToken: String? = null

    @Expose
    @SerializedName("api_login")
    var apiLogin: String? = null

    @Expose
    @SerializedName("api_key")
    var apiKey: String? = null

    @Expose
    @SerializedName("temp_ban")
    var tempBan: Timestamp? = null

}
