package net.scr0pt.thirdservice.openload.entity.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import net.scr0pt.thirdservice.openload.entity.File

/**
 * Created by Long
 * Date: 2/22/2019
 * Time: 12:19 AM
 */
class FileInfo_Response {

    @Expose
    @SerializedName("success")
    var success: Int? = null

    @Expose
    @SerializedName("files")
    var files: List<File>? = null
}
