package thirdservice.mal
import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import net.minidev.json.JSONObject
import net.minidev.json.JSONValue
import org.bson.Document

/**
 * Created by Long
 * Date: 10/14/2019
 * Time: 12:23 AM
 */

class AnimeModel {

    @Expose
    var premiered: String? = null

    @Expose
    var name: String? = null

    @Expose
    @SerializedName("en_name")
    var enName: String? = null
    @Expose
    @SerializedName("ja_name")
    var jaName: String? = null
    @Expose
    @SerializedName("sy_name")
    var syName: String? = null

    @Expose
    var image: String? = null
    @Expose
    @SerializedName("image_90x")
    var image90x: String? = null
    @Expose
    @SerializedName("image_50x")
    var image50x: String? = null

    @Expose
    @SerializedName("description")
    var descriptionEn: String? = null
    @Expose
    var type: String? = null
    @Expose
    @SerializedName("num_ep")
    var numEp: Long? = null
    @Expose
    var status: String? = null
    @Expose
    var aired: String? = null
    @Expose
    var season: String? = null
    @Expose
    var year: Long? = null
    @Expose
    @SerializedName("broadcast")
    var broadcast: String? = null

    @Expose
    var genres: ArrayList<PairIdName>? = null
    @Expose
    var studios: ArrayList<PairIdName>? = null
    @Expose
    var producers: ArrayList<PairIdName>? = null

    @Expose
    var source: String? = null
    @Expose
    var duration: String? = null
    @Expose
    var rating: String? = null
    @Expose
    var score: Double? = null
    @SerializedName("user_vote")
    @Expose
    var uservote: Long? = null
    @Expose
    var members: Long? = null
    @Expose
    var fav: Long? = null
    @Expose
    var rank: Long? = null
    @Expose
    @SerializedName("mal_id")
    var malId: Long? = null
    @Expose
    @SerializedName("ann_link")
    var ann: String? = null
    @Expose
    @SerializedName("anidb_link")
    var anidb: String? = null
    @Expose
    @SerializedName("crunchyroll_link")
    var crunchyroll: String? = null
    @Expose
    var wiki: String? = null
    @Expose
    @SerializedName("official_page")
    var officialPage: String? = null


    @Expose
    @SerializedName("recommendations")
    var recommendations: List<Recommendation>? = null
    @Expose
    @SerializedName("related")
    var related: List<RelatedAnime>? = null
}

data class Recommendation(@Expose val users: String, @Expose @SerializedName("mal_id") val malId: Long, @Expose  val title: String)
data class PairIdName(@Expose val name: String, @Expose @SerializedName("mal_id") val malId: Long)
data class RelatedAnime(@Expose val relationship: String, @Expose val name: String, @Expose @SerializedName("mal_id") val malId: Long, val type: String )//type: Anime or Manga

fun AnimeModel.toDocument(): Document {
    val doc = Document()
    (JSONValue.parse(Gson().toJson(this)) as JSONObject)?.forEach { t, u ->
        doc.append(t, u)
    }
    return doc
}