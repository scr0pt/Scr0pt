package net.scr0pt.test

import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Updates
import net.minidev.json.JSONArray
import net.minidev.json.JSONObject
import net.minidev.json.JSONValue
import net.scr0pt.utils.curl.LongConnection
import net.scr0pt.utils.curl.NetworkUtils
import org.bson.Document
import org.bson.types.ObjectId

/**
 * Created by Long
 * Date: 10/4/2019
 * Time: 9:31 PM
 */

fun main44() {
    val mongoClient = MongoClients.create("mongodb+srv://root:mongo@cluster0-yuuro.mongodb.net/test")
    val serviceAccountDatabase = mongoClient.getDatabase("mal")
    val collection = serviceAccountDatabase.getCollection("anime")
    val malId = arrayListOf<Int>()
    collection.find(Document("producers", "add some")).forEach {
        val id = it.getInteger("mal_id")
        if (id != null)
            collection.updateOne(Document("mal_id", id), Updates.unset("producers"))
    }


}

fun main2() {
    val mongoClient = MongoClients.create("mongodb+srv://root:mongo@cluster0-yuuro.mongodb.net/test")
    val serviceAccountDatabase = mongoClient.getDatabase("mal")
    val collection = serviceAccountDatabase.getCollection("anime")
    val malId = arrayListOf<Int>()
    val deleteRecord = arrayListOf<ObjectId>()
    collection.find().forEach {
        val id = it.getInteger("mal_id")
        if (malId.contains(id)) {
            deleteRecord.add(it.getObjectId("_id"))
        } else {
            malId.add(id)
        }
    }

    deleteRecord.forEach { _id ->
        collection.deleteOne(Document("_id", _id))
    }

}

class CTUSession() {
    private val collectionQlvbDiploma: MongoCollection<Document>
    val conn = LongConnection().also {
        NetworkUtils.getUnsafeOkHttpClient()
    }
    var browserDetailsUrl: String = ""
    var serviceUrl: String = ""
    var csrfToken: String = ""
    var syncId: Int = 0

    init {
        val mongoClient = MongoClients.create("mongodb+srv://root:mongo@cluster0-qzu3e.mongodb.net/admin")
        val serviceAccountDatabase = mongoClient.getDatabase("edu-school-account")
        collectionQlvbDiploma = serviceAccountDatabase.getCollection("qlvb-diploma")
        initCsrfToken()
    }

    fun run() {
        val alphaArra = getAlphaBetVietnamese()
        for (selectedType in 1..33) {
            selectDiplomaType(selectedType) //Chứng chỉ tiếng Anh trình độ A
            for (char in alphaArra) {
                println(char)
                findName(char)
            }
        }
    }

    fun initCsrfToken() {
        val doc = conn.get("https://qlvb.ctu.edu.vn/")?.doc
        val script = doc?.select("script")
            ?.filter { it.data().contains("or press ESC to continue") && it.data().contains("click here") }
            ?.first()

        val layout = (doc?.selectFirst(".timkiemvanbang")?.id() ?: "")
//        endPointUrl = """https://qlvb.ctu.edu.vn/tra-cuu?p_p_id=timkiemvanbang_WAR_ctueduvnquanlyvanbangportlet&p_p_lifecycle=2&p_p_state=normal&p_p_mode=view&p_p_resource_id=APP&p_p_cacheability=cacheLevelPage&p_p_col_id=column-1&p_p_col_pos=1&p_p_col_count=2&_timkiemvanbang_WAR_ctueduvnquanlyvanbangportlet_v-resourcePath=%2FUIDL%2F&v-wsver=7.5.2&v-uiId=0"""
        browserDetailsUrl = script.toString().substringAfter("browserDetailsUrl:\"").substringBefore("\"")
        serviceUrl = script.toString().substringAfter("serviceUrl:\"").substringBefore("\"")
        val postResponse = conn.post(
            browserDetailsUrl,
            hashMapOf(
                "v-browserDetails" to "1",
                "theme" to "valo",
                "v-appId" to layout,
                "v-sh" to "768",
                "v-sw" to "1366",
                "v-cw" to "625",
                "v-curdate" to System.currentTimeMillis().toString(),
                "v-tzo" to "-420",
                "v-dstd" to "0",
                "v-rtzo" to "-420",
                "v-dston" to "false",
                "v-vw" to "1179",
                "v-vh" to "0",
                "v-loc" to "https://qlvb.ctu.edu.vn/",
                "v-wn" to layout,
                "v-td" to "1"
            )
        )

        val body = postResponse?.body ?: return
        val parse1 = JSONValue.parse(body.removePrefix("for(;;);")) as JSONObject
        val uidl = parse1.get("uidl") as String
        val jsonObject = JSONValue.parse(uidl) as JSONObject
        csrfToken = jsonObject.getAsString("Vaadin-Security-Key")
        csrfToken = "48d9a4b3-c06c-420b-8be8-1461a3f82e36"
        syncId = jsonObject.getAsNumber("syncId").toInt()
    }

    fun selectDiplomaType(selectedIndex: Int) {
        val postResponse = conn.post(
            serviceUrl + "&_timkiemvanbang_WAR_ctueduvnquanlyvanbangportlet_v-resourcePath=%2FUIDL%2F&v-uiId=0",
//            data = """{"csrfToken":"$csrfToken","rpc":[["2021","v","v",["selected",["S",["$selectedIndex"]]]]],"syncId":${syncId}"""
            data = hashMapOf("""{"csrfToken":"$csrfToken","rpc":[["0","com.vaadin.shared.ui.ui.UIServerRpc","resize",[236,1179,1349,625]],["3","v","v",["filter",["s",""]]],["3","v","v",["page",["i",-1]]]],"syncId":${syncId}}""" to "")
        )

        val body = postResponse?.body ?: return
        if (body.contains("Vaadin-Security-Key")) {
            println("Vaadin-Security-Key")
            return
        }
        println(body)
        val parse1 = JSONValue.parse(body.removePrefix("for(;;);")) as JSONArray
        syncId = (parse1[0] as JSONObject).getAsNumber("syncId").toInt()
    }


    fun findName(
        searchQuery: String
    ) {

        var postData = if (syncId % 2 == 0) {
            """
                {"csrfToken":"$csrfToken","rpc":[["4","v","v",["text",["s","$searchQuery"]]],["9","com.vaadin.shared.ui.button.ButtonServerRpc","click",[{"altKey":false,"button":"LEFT","clientX":558,"clientY":363,"ctrlKey":false,"metaKey":false,"relativeX":61,"relativeY":5,"shiftKey":false,"type":1}]]],"syncId":$syncId}
            """.trimIndent()
        } else {
            """
                {"csrfToken":"$csrfToken","rpc":[["4","v","v",["text",["s","$searchQuery"]]],["4","v","v",["c",["i",3]]],["0","v","v",["actiontarget",["c","4"]]],["0","v","v",["action",["s","1"]]]],"syncId":$syncId}
            """.trimIndent()
        }

        postData = """
            {"csrfToken":"48d9a4b3-c06c-420b-8be8-1461a3f82e36","rpc":[["0","com.vaadin.shared.ui.ui.UIServerRpc","resize",[476,1179,1349,625]],["183","v","v",["filter",["s","$searchQuery"]]],["183","v","v",["page",["i",-1]]]],"syncId":$syncId}
        """.trimIndent()

        val response = conn.post(
            "https://qlvb.ctu.edu.vn/tra-cuu?p_p_id=timkiemvanbang_WAR_ctueduvnquanlyvanbangportlet&p_p_lifecycle=2&p_p_state=normal&p_p_mode=view&p_p_resource_id=APP&p_p_cacheability=cacheLevelPage&p_p_col_id=column-1&p_p_col_pos=1&p_p_col_count=2&_timkiemvanbang_WAR_ctueduvnquanlyvanbangportlet_v-resourcePath=%2FUIDL%2F&v-uiId=0",
//            serviceUrl + "&_timkiemvanbang_WAR_ctueduvnquanlyvanbangportlet_v-resourcePath=%2FUIDL%2F&v-uiId=0",
            data = hashMapOf(postData to "")
        )

        val body = response?.body ?: return
        println(body)

        syncId = body.substringAfter("syncId\": ").substringBefore(",").toInt()

        val parse1 = JSONValue.parse(body.removePrefix("for(;;);")) as JSONArray
        val data = getChildValid(parse1, "rows") ?: return
        val headersJSONArray = getChildValid(parse1, "visiblecolumns") ?: return
        var headers = arrayListOf<String>()
        for (tr in headersJSONArray) {
            if (tr is JSONArray) {
                (tr[1] as JSONObject).getAsString("caption")?.takeIf { it != "Chi tiết" }
                    ?.let { headers.add(it) }
            }
        }
        println(headers)


        for (tr in data) {
            if (tr is JSONArray) {
                var row = arrayListOf<String?>()
                for (td in tr) {
                    println("------------ $td")
                    if (td is String && td != "tr") {
                        row.add(td as String)
                    }
                }

                val doc = Document()
                for (i in 0 until headers.size) {
                    val key = headers[i]
                    val value = row[i]
                    doc.append(key, value)
                }

                updateDocumenttoDatabase(doc)

                println(row)
            }
        }
    }


    fun updateDocumenttoDatabase(doc: Document) {
        try {
            this.collectionQlvbDiploma.insertOne(doc)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}


fun getAlphaBetVietnamese(): ArrayList<String> {
    val alphabet =
        "a b c d e f g h i j k l m n o p q r s t u v w x y z".replace(" ", "") +
                "á|à|ả|ã|ạ|ă|ắ|ằ|ẳ|ẵ|ặ|â|ấ|ầ|ẩ|ẫ|ậ".replace("|", "") +
                "đ".replace("|", "") +
                "é|è|ẻ|ẽ|ẹ|ê|ế|ề|ể|ễ|ệ".replace("|", "") +
                "í|ì|ỉ|ĩ|ị".replace("|", "") +
                "ó|ò|ỏ|õ|ọ|ô|ố|ồ|ổ|ỗ|ộ|ơ|ớ|ờ|ở|ỡ|ợ".replace("|", "") +
                "ú|ù|ủ|ũ|ụ|ư|ứ|ừ|ử|ữ|ự".replace("|", "") +
                "ý|ỳ|ỷ|ỹ|ỵ".replace("|", "")
    val alphaArra = arrayListOf<String>()
    for (char in alphabet) {
        if (!alphaArra.contains(char.toString())) {
            alphaArra.add(char.toString())
        }
    }
    return alphaArra
}


fun getChildValid(any: Any, key: String): JSONArray? {
    return when (any) {
        is JSONObject -> {
            for (entry in any) {
                val _varr = getChildValid(entry.value, key)
                if (_varr != null) {
                    return@getChildValid _varr
                }
            }
            return@getChildValid null
        }
        is JSONArray -> {
            if (any.isEmpty()) {
                return@getChildValid null
            } else if (any[0] is String && any[0] == key) {
                var varr = JSONArray()
                for (value in any) {
                    if (value is JSONArray && value.isNotEmpty()) {
                        varr.add(value)
                    }
                }
                return@getChildValid if (varr.isEmpty()) null else varr
            } else for (value in any) {
                val _varr = getChildValid(value, key)
                if (_varr != null) {
                    return@getChildValid _varr
                }
            }
            return@getChildValid null
        }
        is String -> null
        else -> null
    }
}