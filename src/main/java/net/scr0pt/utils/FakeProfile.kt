package net.scr0pt.utils

import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.jsoup.Jsoup

/**
 * Created by Long
 * Date: 10/15/2019
 * Time: 10:23 PM
 */


fun main() {
    FakeProfile.getNewProfile()
}

object FakeProfile {
    fun getNewProfile(): Result? {
        val response =
            Jsoup.connect("https://randomuser.me/api/").ignoreHttpErrors(true).ignoreContentType(true).execute()
        println(response.body())
        val randomUserResponse =
            Gson().fromJson<RandomUserResponse>(response.body(), RandomUserResponse::class.java) ?: return null
        return randomUserResponse.results?.first()
    }
}


class RandomUserResponse {
    @SerializedName("results")
    @Expose
    var results: ArrayList<Result>? = null
}


class Name {
    @SerializedName("title")
    @Expose
    var title: String? = null
    @SerializedName("first")
    @Expose
    var first: String? = null
    @SerializedName("last")
    @Expose
    var last: String? = null
}


class Result {
    @SerializedName("gender")
    @Expose
    var gender: String? = null
    @SerializedName("name")
    @Expose
    var name: Name? = null
    @Expose
    var email: String? = null

}
