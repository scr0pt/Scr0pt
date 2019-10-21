package net.scr0pt.crawl.school

import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import net.minidev.json.JSONArray
import net.minidev.json.JSONObject
import net.minidev.json.JSONValue
import net.scr0pt.utils.webdriver.*
import okhttp3.*
import org.bson.Document
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import java.io.IOException


fun main() {
    val mongoClient = MongoClients.create("mongodb+srv://root:mongo@cluster0-qzu3e.mongodb.net/admin")
    val serviceAccountDatabase = mongoClient.getDatabase("edu-school-account")
    val collectionQlvbDiploma = serviceAccountDatabase.getCollection("qlvb-diploma")


    val firefox = Browser.firefox
    val jse = firefox as JavascriptExecutor

    firefox.get("https://qlvb.ctu.edu.vn/")

    var type = 9
    while (true) {
        type++
        while (!clickFilterButton(firefox)) {
        }
        val typs = firefox.findElWait(500, 20000, ".v-filterselect-suggestmenu .gwt-MenuItem")
        if (type >= 10) {
            for (i in 1..(type / 10)) {
                firefox.findElWait(500, 20000, ".v-filterselect-nextpage")?.get(0)?.click()
                Thread.sleep(2000)
                jse.executeScript("document.querySelectorAll('.v-filterselect-suggestmenu .gwt-MenuItem')[${type - 10}].click()")
            }
        } else typs?.get(type)?.click()


        for (char in "qưertyuioplkjhgfdsazxcvbnmwôơ") {


            val input =
                firefox.findElWait(500, 20000, "tr.v-formlayout-row:has(#gwt-uid-4) td.v-formlayout-contentcell input")
                    ?.first()
            input?.clear()
            input?.sendKeys("$char")
            firefox.findElWait(500, 20000, ".v-button:has(span.v-button-caption:contains(Tìm kiếm))")?.first()?.click()
            firefox.waitUmtil(500, 20000) {
                it.document?.select("table.v-table-table tbody > tr")?.size ?: 0 > 0
            }

            if (firefox.document?.select("table.v-table-table tbody > tr")?.size == 0) {

            } else {

                while (!clickNotificationSuccessButton(firefox)) {
                }
                while (!waitNotificationSuccessDissmiss(firefox)) {
                }

                firefox.findElWait(500, 20000, ".pagedtable-itemsperpagecombobox .v-filterselect-button")?.first()
                    ?.click()
                firefox.findElWait(500, 20000, ".v-filterselect-suggestmenu table tbody td.gwt-MenuItem")?.last()
                    ?.click()

                firefox.waitUmtil(500, 20000) {
                    it.document?.select("table.v-table-table tbody > tr")?.size ?: 0 > 10
                }


                while (true) {
                    firefox.document?.let { parser(it, collectionQlvbDiploma) }
                    Thread.sleep(1000)
                    firefox.waitUmtilDismiss(500, 20000, ".v-Notification-success")
                    val nextPage = firefox.findElWait(500, 20000, ".pagedtable-next")?.first()
                    println(nextPage.getAttribute("aria-disabled"))
                    if (nextPage.getAttribute("aria-disabled") == "true") {
                        break
                    } else {
                        val firstRow =
                            firefox.document?.selectFirst("table.v-table-table .v-table-row:first-child")
                                ?.text()
                        println(firstRow)
                        nextPage.click()
                        firefox.waitUmtil(500, 60000) { driver: WebDriver ->
                            val firstRow2 =
                                firefox.document?.selectFirst("table.v-table-table .v-table-row:first-child")
                                    ?.text()
                            println(firstRow2)
                            firstRow2 != firstRow
                        }
                    }
                }
            }
        }
        type++
    }


}

fun waitNotificationSuccessDissmiss(firefox: WebDriver): Boolean {
    return try {
        firefox.waitUmtilDismiss(500, 20000, ".v-Notification-success")
        true
    } catch (e: Exception) {
        false
    }
}

fun clickNotificationSuccessButton(firefox: WebDriver): Boolean {
    return try {
        firefox.findElWait(500, 20000, ".v-Notification-success")?.first()?.click()
        true
    } catch (e: Exception) {
        false
    }
}

fun clickFilterButton(firefox: WebDriver): Boolean {
    return try {
        firefox.findElWait(500, 20000, ".v-formlayout-contentcell .v-filterselect-button")?.first()?.click()
        true
    } catch (e: Exception) {
        false
    }
}


fun parser(doc: org.jsoup.nodes.Document, collectionQlvbDiploma: MongoCollection<Document>) {
    val headers = arrayListOf<String>()
    doc.select(".v-table-header-cell")?.forEach { tr ->
        val h = tr.text()
        if (h != "Chi tiết") {
            headers.add(h)
        }
    }
    println(headers)
    doc.select("table.v-table-table tbody tr")?.forEach { tr ->
        val values = arrayListOf<String>()
        tr?.select(".v-table-cell-content:not(.v-table-cell-content-rowheader)")?.forEach {
            values.add(it.text())
        }
        println(values)

        val bson = Document()
        for (i in 0 until headers.size) {
            bson.append(headers[i], values[i])
        }
        collectionQlvbDiploma.insertOneUnique(bson)
    }
}

class CTUSession() {
    private val collectionQlvbDiploma: MongoCollection<Document>
    var csrfToken: String = ""
    var syncId: Int = 0
    var headers: ArrayList<Pair<String, String>> = arrayListOf(
        Pair(
            "User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36"
        ),
        Pair("Referer", "https://qlvb.ctu.edu.vn/tra-cuu"),
        Pair("Origin", "https://qlvb.ctu.edu.vn"),
        Pair("Content-type", "application/x-www-form-urlencoded"),
        Pair("Sec-Fetch-Mode", "cors")
    )
    val client = OkHttpClient.Builder().build()
//    val client = OkHttpClient.Builder().getUnsafeOkHttpClient().build()

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
        val builder = MultipartBody.Builder()
        builder.addFormDataPart("v-browserDetails", "1")
        builder.addFormDataPart("theme", "valo")
        builder.addFormDataPart("v-appId", "v-timkiemvanbang_WAR_ctueduvnquanlyvanbangportlet_LAYOUT_2450949")
        builder.addFormDataPart("v-sh", "1080")
        builder.addFormDataPart("v-sw", "1920")
        builder.addFormDataPart("v-cw", "996")
        builder.addFormDataPart("v-ch", "937")
        builder.addFormDataPart("v-curdate", System.currentTimeMillis().toString())
        builder.addFormDataPart("v-tzo", "-420")
        builder.addFormDataPart("v-dstd", "0")
        builder.addFormDataPart("v-rtzo", "-420")
        builder.addFormDataPart("v-dston", "false")
        builder.addFormDataPart("v-vw", "826")
        builder.addFormDataPart("v-vh", "0")
        builder.addFormDataPart("v-loc", "https://qlvb.ctu.edu.vn/")
        builder.addFormDataPart(
            "v-wn",
            "v-timkiemvanbang_WAR_ctueduvnquanlyvanbangportlet_LAYOUT_2450949-0.7472867800865768"
        )
        builder.addFormDataPart("v-td", "1")


        val resquestBulider = Request.Builder()
            .url("https://qlvb.ctu.edu.vn/tra-cuu?p_p_id=timkiemvanbang_WAR_ctueduvnquanlyvanbangportlet&p_p_lifecycle=2&p_p_state=normal&p_p_mode=view&p_p_resource_id=v-browserDetails&p_p_cacheability=cacheLevelPage&p_p_col_id=column-1&p_p_col_pos=1&p_p_col_count=2&v-1570612818271")
        this.headers.forEach { resquestBulider.header(it.first, it.second) }

        client.newCall(
            resquestBulider.post(builder.build()).build()
        ).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            val headers = response.headers()
            val names = headers.names()
            for (nam in names) {
                if (nam == "Set-Cookie") {
                    this.headers.add(Pair("Cookie", headers[nam]!!))
                } else {
                    this.headers.add(Pair(nam, headers[nam]!!))
                }
            }
            val body = response.body()!!.string()
            val parse1 = JSONValue.parse(body.removePrefix("for(;;);")) as JSONObject
            val uidl = parse1.get("uidl") as String
            val jsonObject = JSONValue.parse(uidl) as JSONObject
            csrfToken = jsonObject.getAsString("Vaadin-Security-Key")
            syncId = jsonObject.getAsNumber("syncId").toInt()
        }
    }

    fun selectDiplomaType(selectedIndex: Int): Int {
        val builder = Request.Builder().url(
            """
            https://qlvb.ctu.edu.vn/tra-cuu?p_p_id=timkiemvanbang_WAR_ctueduvnquanlyvanbangportlet&p_p_lifecycle=2&p_p_state=normal&p_p_mode=view&p_p_resource_id=APP&p_p_cacheability=cacheLevelPage&p_p_col_id=column-1&p_p_col_pos=1&p_p_col_count=2&_timkiemvanbang_WAR_ctueduvnquanlyvanbangportlet_v-resourcePath=%2FUIDL%2F&v-uiId=0
        """.trimIndent()
        )

        this.headers.forEach { builder.header(it.first, it.second) }

        val request = builder.post(
            RequestBody.create(
                MediaType.get("application/json; charset=utf-8"),
                """{"csrfToken":"$csrfToken","rpc":[["2021","v","v",["selected",["S",["$selectedIndex"]]]]],"syncId":${syncId}"""
            )
        )
        client.newCall(request.build()).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            val body = response.body()!!.string()
            println(body)
            val parse1 = JSONValue.parse(body.removePrefix("for(;;);")) as JSONArray
            return (parse1[0] as JSONObject).getAsNumber("syncId").toInt()
        }
    }


    fun findName(
        searchQuery: String
    ) {
        val builder = Request.Builder().url(
            """
            https://qlvb.ctu.edu.vn/tra-cuu?p_p_id=timkiemvanbang_WAR_ctueduvnquanlyvanbangportlet&p_p_lifecycle=2&p_p_state=normal&p_p_mode=view&p_p_resource_id=APP&p_p_cacheability=cacheLevelPage&p_p_col_id=column-1&p_p_col_pos=1&p_p_col_count=2&_timkiemvanbang_WAR_ctueduvnquanlyvanbangportlet_v-resourcePath=%2FUIDL%2F&v-uiId=0
        """.trimIndent()
        )

        this.headers.forEach { builder.header(it.first, it.second) }

        val request = builder.post(
            RequestBody.create(
                MediaType.get("application/json; charset=utf-8"),
                """{"csrfToken":"$csrfToken","rpc":[["0","com.vaadin.shared.ui.ui.UIServerRpc","resize",[236,1733,1903,418]],["4","v","v",["text",["s","$searchQuery"]]],["4","v","v",["c",["i",3]]],["0","v","v",["actiontarget",["c","4"]]],["0","v","v",["action",["s","1"]]]],"syncId":$syncId}"""
            )
        )
        try {
            client.newCall(request.build()).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")
                val body = response.body()!!.string()
                println(body)

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
        } catch (e: Exception) {
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