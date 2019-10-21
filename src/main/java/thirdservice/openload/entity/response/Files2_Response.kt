package thirdservice.openload.entity.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import thirdservice.openload.entity.File
import thirdservice.openload.entity.Folder

/**
 * Created by Long
 * Date: 2/22/2019
 * Time: 12:28 AM
 */
class Files2_Response {
    @SerializedName("folders")
    @Expose
    var folders: List<Folder>? = null
    @SerializedName("files")
    @Expose
    var files: List<File>? = null
    @SerializedName("uploadlink")
    @Expose
    var uploadlink: String? = null

}
