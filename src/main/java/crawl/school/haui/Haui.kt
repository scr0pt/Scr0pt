package crawl.school.haui

import com.google.gson.Gson
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import org.bson.Document
import utils.curl.LongConnection

fun main() {
    val haui = Haui()

    val years = 3..16 //2..16
    val viens = 1..99 //1..70


    for (year in years) {
        Thread {
            var fail = 0
            val yearStr = (if (year < 10) "0" else "") + year.toString()
            for (vien in viens) {
                val vienStr = (if (vien < 10) "0" else "") + vien.toString()
                var mssv = 0L
                var nextStep = 1
                while (mssv < 999999) {
                    val mssvStr = yearStr + vienStr + Haui.getMssv(mssv)
                    if (mssvStr.toLong() >= 1102099079) {
                        println(mssvStr)
                        val frees = haui.getfees(mssvStr, true)
                        if ((frees != null && frees.isNotEmpty())) {
                            fail = 0
                        } else {
                            fail++
                        }


                        if (fail < 10) {
                            val chedules = haui.getchedules(mssvStr, true)
                            if ((chedules != null && chedules.isNotEmpty())) {
                                fail = 0
                            } else {
                                fail++
                            }

                            val examschedules = haui.getexamschedules(mssvStr, true)
                            if ((examschedules != null && examschedules.isNotEmpty())) {
                                fail = 0
                            } else {
                                fail++
                            }

                            val examresults = haui.getexamresults(mssvStr, true)
                            if ((examresults != null && examresults.isNotEmpty())) {
                                fail = 0
                            } else {
                                fail++
                            }

                            val examresultsext = haui.getexamresultsext(mssvStr, true)
                            if ((examresultsext != null && examresultsext.isNotEmpty())) {
                                fail = 0
                            } else {
                                fail++
                            }

                            val certificate = haui.getcertificate(mssvStr, true)
                            if ((certificate != null && certificate.isNotEmpty())) {
                                fail = 0
                            } else {
                                fail++
                            }
                        }

                        if (fail < 50) {
                            nextStep = 1
                        } else if (fail < 100) {
                            nextStep = 2
                        } else if (fail < 500) {
                            nextStep = 10
                        } else if (fail < 1000) {
                            nextStep = 100
                        }
//                    else {
//                        nextStep = nextStep * 2
//                    }

                        if (nextStep > 1000) nextStep = nextStep / 2
                    }
                    mssv = mssv + nextStep
                }
            }
        }.start()
    }
}

class Haui {
    val conn = LongConnection().also {
        it.header("Sec-Fetch-Mode", "cors", isHard = true)
        it.header("Origin", "https://itc.haui.edu.vn", isHard = true)
        it.header("Accept-Encoding", "deflate, br", isHard = true)
        it.header("Accept-Language", "en,en-GB;q=0.9,vi;q=0.8,fr-FR;q=0.7,fr;q=0.6,en-US;q=0.5,ja;q=0.4", isHard = true)
        it.header("X-Requested-With", "XMLHttpRequest", isHard = true)
        it.header("Connection", "keep-alive", isHard = true)
        it.header("Pragma", "no-cache", isHard = true)
        it.header(
            "User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36",
            isHard = true
        )
        it.header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8", isHard = true)
        it.header("Accept", "application/json, text/javascript, */*; q=0.01", isHard = true)
        it.header("Cache-Control", "no-cache", isHard = true)
        it.header("Referer", "https://itc.haui.edu.vn/vn/lookup", isHard = true)
        it.header("Sec-Fetch-Site", "same-origin", isHard = true)
        it.header("DNT", "1", isHard = true)
    }
    var token = ""

    val mongoClient = MongoClients.create("mongodb+srv://root:mongo@cluster0-qzu3e.mongodb.net/admin")
    val serviceAccountDatabase = mongoClient.getDatabase("edu-school-account")
    private val feesCollection: MongoCollection<Document> = serviceAccountDatabase.getCollection("haui-frees")
    private val schedulesCollection: MongoCollection<Document> = serviceAccountDatabase.getCollection("haui-schedules")
    private val examschedulesCollection: MongoCollection<Document> =
        serviceAccountDatabase.getCollection("haui-examschedules")
    private val examresultsCollection: MongoCollection<Document> =
        serviceAccountDatabase.getCollection("haui-examresults")
    private val examresultsSextCollection: MongoCollection<Document> =
        serviceAccountDatabase.getCollection("haui-examresultsext")
    private val certificateCollection: MongoCollection<Document> =
        serviceAccountDatabase.getCollection("haui-certificate")

    init {
//        serviceAccountDatabase.createCollection("haui-frees")
//        serviceAccountDatabase.createCollection("haui-schedules")
//        serviceAccountDatabase.createCollection("haui-examschedules")
//        serviceAccountDatabase.createCollection("haui-examresults")
//        serviceAccountDatabase.createCollection("haui-examresultsext")
//        serviceAccountDatabase.createCollection("haui-certificate")
        val response = conn.get("https://itc.haui.edu.vn/vn/lookup")
        token = response?.doc?.selectFirst("input[name='__RequestVerificationToken']")?.`val`() ?: ""
    }

    fun getfees(mssv: String, update: Boolean = false): ArrayList<HashMap<String, String?>?>? {
        return getApi(mssv, APIEndpoint.FEES, update)
    }

    fun getchedules(mssv: String, update: Boolean = false): ArrayList<HashMap<String, String?>?>? {
        return getApi(mssv, APIEndpoint.SCHEDULES, update)
    }

    fun getexamschedules(mssv: String, update: Boolean = false): ArrayList<HashMap<String, String?>?>? {
        return getApi(mssv, APIEndpoint.EXAMSCHEDULES, update)
    }

    fun getexamresults(mssv: String, update: Boolean = false): ArrayList<HashMap<String, String?>?>? {
        return getApi(mssv, APIEndpoint.EXAMRESULTS, update)
    }

    fun getexamresultsext(mssv: String, update: Boolean = false): ArrayList<HashMap<String, String?>?>? {
        return getApi(mssv, APIEndpoint.EXAMRESULTSEXT, update)
    }

    fun getcertificate(mssv: String, update: Boolean = false): ArrayList<HashMap<String, String?>?>? {
        return getApi(mssv, APIEndpoint.CERTIFICATE, update)
    }


    fun getApi(mssv: String, type: APIEndpoint, update: Boolean = false): ArrayList<HashMap<String, String?>?>? {
        val endPoint = when (type) {
            APIEndpoint.FEES -> "getfees"
            APIEndpoint.SCHEDULES -> "getschedules"
            APIEndpoint.EXAMSCHEDULES -> "getexamschedules"
            APIEndpoint.EXAMRESULTS -> "getexamresults"
            APIEndpoint.EXAMRESULTSEXT -> "getexamresultsext"
            APIEndpoint.CERTIFICATE -> "getcertificate"
        }

        val response = conn.post(
            "https://itc.haui.edu.vn/vn/m/api/$endPoint", hashMapOf(
                "code" to mssv,
                "__RequestVerificationToken" to token
            )
        ) ?: return null

        println(response.body)

        if (response.body?.contains("The anti-forgery token could not be decrypted") ?: false) return null

        val apiResponse = Gson().fromJson<HauiApiResponse>(response.body, HauiApiResponse::class.java) ?: return null
        if (apiResponse.err == 0) {

            val data = apiResponse.data
            if (update) {
                updateToDatabase(data, type)
            }

            return data
        } else {
            println(apiResponse.msg)
            return null
        }
    }

    companion object {
        fun getMssv(mssv: Long): String {
            var mssvStr = mssv.toString()
            while (mssvStr.length < 6) {
                mssvStr = "0" + mssvStr
            }
            return mssvStr
        }
    }

    enum class APIEndpoint {
        FEES, SCHEDULES, EXAMSCHEDULES, EXAMRESULTS, EXAMRESULTSEXT, CERTIFICATE

    }

    data class HauiApiResponse(
        val current: Long,
        val data: ArrayList<HashMap<String, String?>?>,
        val err: Int,
        val msg: String
    )

    fun updateToDatabase(data: ArrayList<HashMap<String, String?>?>?, type: APIEndpoint) {
        data ?: return

        val collection = when (type) {
            APIEndpoint.FEES -> feesCollection
            APIEndpoint.SCHEDULES -> schedulesCollection
            APIEndpoint.EXAMSCHEDULES -> examschedulesCollection
            APIEndpoint.EXAMRESULTS -> examresultsCollection
            APIEndpoint.EXAMRESULTSEXT -> examresultsSextCollection
            APIEndpoint.CERTIFICATE -> certificateCollection
        }

        val docs = arrayListOf<Document>()
        for (hashMap in data) {
            hashMap?.let {
                val doc = Document()
                it.forEach { key, value -> doc.append(key, value) }
                docs.add(doc)
            }
        }

        if (docs.isEmpty()) {
            return
        }

        try {
            collection.insertMany(docs)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}