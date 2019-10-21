package net.scr0pt.utils.curl

import net.minidev.json.JSONArray
import net.minidev.json.JSONObject
import net.minidev.json.JSONValue
import org.jsoup.nodes.Document

class LongResponse {
    var url: String? = null
    var mCookies = arrayListOf<LongConnection.Data>()
    var mHeaders = arrayListOf<LongConnection.Data>()
    var body: String? = null
    var statusCode: Int? = null
    var statusMessage: String? = null
    var doc: Document? = null
    var jsonObj: JSONObject? = null
        get() = JSONValue.parse(body) as JSONObject?

    var jsonArr: JSONArray? = null
        get() = JSONValue.parse(body) as JSONArray?
}