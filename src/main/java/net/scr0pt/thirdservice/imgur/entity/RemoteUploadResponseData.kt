package net.scr0pt.thirdservice.imgur.entity

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by Long
 * Date: 2/27/2019
 * Time: 12:26 AM
 */
class RemoteUploadResponseData {
    @SerializedName("id")
    @Expose
    var id: String? = null

    @SerializedName("title")
    @Expose
    var title: Object? = null

    @SerializedName("description")
    @Expose
    var description: Object? = null

    @SerializedName("datetime")
    @Expose
    var datetime: Int? = null

    @SerializedName("type")
    @Expose
    var type: String? = null

    @SerializedName("animated")
    @Expose
    var animated: Boolean? = null

    @SerializedName("width")
    @Expose
    var width: Long? = null

    @SerializedName("height")
    @Expose
    var height: Long? = null

    @SerializedName("size")
    @Expose
    var size: Long? = null

    @SerializedName("views")
    @Expose
    var views: Int? = null

    @SerializedName("bandwidth")
    @Expose
    var bandwidth: Int? = null

    @SerializedName("vote")
    @Expose
    var vote: Int? = null

    @SerializedName("favorite")
    @Expose
    var favorite: Boolean? = null

    @SerializedName("nsfw")
    @Expose
    var nsfw: Object? = null

    @SerializedName("section")
    @Expose
    var section: Object? = null

    @SerializedName("account_url")
    @Expose
    var accountUrl: String? = null

    @SerializedName("account_id")
    @Expose
    var accountId: Int? = null

    @SerializedName("is_ad")
    @Expose
    var isAd: Boolean? = null

    @SerializedName("in_most_viral")
    @Expose
    var inMostViral: Boolean? = null

    @SerializedName("has_sound")
    @Expose
    var hasSound: Boolean? = null

    @SerializedName("tags")
    @Expose
    var tags: List<Object>? = null

    @SerializedName("ad_type")
    @Expose
    var adType: Int? = null

    @SerializedName("ad_url")
    @Expose
    var adUrl: String? = null

    @SerializedName("edited")
    @Expose
    var edited: String? = null

    @SerializedName("in_gallery")
    @Expose
    var inGallery: Boolean? = null

    @SerializedName("deletehash")
    @Expose
    var deletehash: String? = null

    @SerializedName("name")
    @Expose
    var name: String? = null

    @SerializedName("link")
    @Expose
    var link: String? = null
}