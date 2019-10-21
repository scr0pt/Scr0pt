package net.scr0pt.crawl.school

import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import org.bson.Document
import org.jsoup.Connection
import org.jsoup.Jsoup


fun main() {
    val mongoClient = MongoClients.create("mongodb+srv://root:mongo@cluster0-qzu3e.mongodb.net/admin")
    val serviceAccountDatabase = mongoClient.getDatabase("edu-school-account")
    val collection: MongoCollection<Document> = serviceAccountDatabase.getCollection("hust-student-infomation")

//    for (name in "qwertyuioplkjhgfdsazxcvbnm") {
//        search("" + name, true)
//    }

    for (year in 2000..2020) {
            Thread{
                for (i in 0..9999) {
                    var indexStr = i.toString()
                    while (indexStr.length < 4) indexStr = "0" + indexStr
                    val MSSV = year.toString() + indexStr

                    search(MSSV, true, collection)
                }
            }.start()
    }



//    while (true){
//        getRandomName()?.let { search(it) }
//    }
}

//fun getRandomName(): String? {
//    val doc = collection.random(Document())
//        return doc?.getString("Tên")
//        return doc?.getString("Ngày cấp bằng")
//    return doc?.getString("Họ đệm")?.split(" ")?.first()
//    return doc?.getString("Đệm")
//        return doc?.getString("Họ")
//        return doc?.getString("Ngày sinh")
//}

fun search(
    searchQuery: String,
    update: Boolean = true,
    collection: MongoCollection<Document>
) {
    println(searchQuery)
    val response = Jsoup.connect("http://sis.hust.edu.vn/ModuleSearch/GroupList.aspx")
        .method(Connection.Method.POST)
        .cookie("ASP.NET_SessionId", "vityql2dqibkkr24wp11tni4")
        .header("Pragma", "no-cache")
        .header("Origin", "http://sis.hust.edu.vn")
        .header("Accept-Encoding", "gzip, deflate")
        .header("Accept-Language", "en,en-GB;q=0.9,vi;q=0.8,fr-FR;q=0.7,fr;q=0.6,en-US;q=0.5,ja;q=0.4")
        .header(
            "User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36"
        )
        .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
        .header("Accept", "*/*")
        .header("Cache-Control", "no-cache")
        .header("Referer", "http://sis.hust.edu.vn/ModuleSearch/GroupList.aspx")
        .header("Connection", "keep-alive")
        .header("DNT", "1")
        .data("\$__EVENTTARGET", "")
        .data("__EVENTARGUMENT", "")
        .data(
            "__VIEWSTATE",
            "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"
        )
        .data("__VIEWSTATEGENERATOR", "6142266D")
        .data("ctl00\$cLogIn1\$tb_cLogIn_User", "")
        .data("ctl00\$cLogIn1\$tb_cLogIn_Pass", "")
        .data("MainContent_cbStudyCourse_VI", "9")
        .data("ctl00\$MainContent\$cbStudyCourse", "9")
        .data("MainContent_cbStudyCourse_DDDWS", "0:0:-1:-10000:-10000:0:-10000:-10000:1:0:0:0")
        .data("MainContent_cbStudyCourse_DDD_LDeletedItems", "")
        .data("MainContent_cbStudyCourse_DDD_LInsertedItems", "")
        .data("MainContent_cbStudyCourse_DDD_LCustomCallback", "")
        .data("ctl00\$MainContent\$cbStudyCourse\$DDD\$L", "9")
        .data("MainContent_cbFaculty_VI", "")
        .data("ctl00\$MainContent\$cbFaculty", "")
        .data("MainContent_cbFaculty_DDDWS", "0:0:-1:-10000:-10000:0:-10000:-10000:1:0:0:0")
        .data("MainContent_cbFaculty_DDD_LDeletedItems", "")
        .data("MainContent_cbFaculty_DDD_LInsertedItems", "")
        .data("MainContent_cbFaculty_DDD_LCustomCallback", "")
        .data("ctl00\$MainContent\$cbFaculty\$DDD\$L", "")
        .data("MainContent_cbGroup_VI", "")
        .data("ctl00\$MainContent\$cbGroup", "")
        .data("MainContent_cbGroup_DDDWS", "0:0:-1:-10000:-10000:0:-10000:-10000:1:0:0:0")
        .data("MainContent_cbGroup_DDD_LDeletedItems", "")
        .data("MainContent_cbGroup_DDD_LInsertedItems", "")
        .data("MainContent_cbGroup_DDD_LCustomCallback", "")
        .data("ctl00\$MainContent\$cbGroup\$DDD\$L", "")
        .data("ctl00\$MainContent\$tbStudentID", searchQuery)
        .data("ctl00\$MainContent\$gvStudents\$DXSelInput", "")
        .data("ctl00\$MainContent\$gvStudents\$DXKVInput", "['$searchQuery']")
        .data(
            "ctl00\$MainContent\$gvStudents\$CallbackState",
            "BwQHAgIERGF0YQa2AQAAAAABAAAAAQAAAAAAAAABAAAAAAgAAAAJU3R1ZGVudElEClN0dWRlbnQgSUQHAAALR3JvdXBTdGF0dXMMR3JvdXAgU3RhdHVzAwAACkZhbWlseU5hbWULRmFtaWx5IE5hbWUHAAAKTWlkZGxlTmFtZQtNaWRkbGUgTmFtZQcAAAlGaXJzdE5hbWUKRmlyc3QgTmFtZQcAAAlCaXJ0aERhdGUKQmlydGggRGF0ZQgAAAlHcm91cE5hbWUKR3JvdXAgTmFtZQcAAAtQcm9ncmFtTmFtZQxQcm9ncmFtIE5hbWUHAAAKAAAAC0dyb3VwTGlzdElEB0dyb3VwSUQNQWNhZFByb2dyYW1JRAlQcm9ncmFtSUQKQWNhZFN0YXR1cwZUZXJtSUQNTWFuYWdlclR5cGVJRA1TdHVkZW50VHlwZUlECUZhY3VsdHlJRAtTdHVkeUNvdXJzZQcABwAHAAcABv//BwIIMjAwNjAwMDMDBkACBwIHxJDhurduZwcCBsSQw6xuaAcCAkFuCAIEAKDrVwdJtAgHAgxDTiBE4buHdCBLNTIHAg7EkEMgxJBIQ1EgMjAwNwIFU3RhdGUHYwcIBwACAQcBAgEHAgIBBwMCAQcEAgEHBQIBBwYCAQcHAgEHAAcABwAHAAIABQAAAIAJAglTdHVkZW50SUQHAQIJU3R1ZGVudElEBwkCAAIAAwcEAgAHAAIBBwEHAAIBBwAHAAIIUGFnZVNpemUDBx4CCVBhZ2VJbmRleAMHBQ=="
        )
        .data(
            "DXScript",
            "1_145,1_81,1_137,1_99,1_106,1_92,1_130,1_135,1_121,1_98,1_78,1_128,1_101,1_91,1_114,1_85,1_93,1_126"
        )
        .data("__CALLBACKID", "ctl00\$MainContent\$gvStudents")
        .data("__CALLBACKPARAM", "c0:KV|12;['$searchQuery'];GB|40;14|CUSTOMCALLBACK20|tbStudentID#"+searchQuery+";")
        .data(
            "__EVENTVALIDATION",
            "/wEdAAVJm7cv7UKQp6WuoCs/cR3drGU9Y0qlcd3kc7V14M4XkBu1L3GlpVEMaDJdIrKpXhV/Y7++7fP8g3U9zrzfZ4kK2rCNZiO3JGCB4A801evvajLS/DdqP7ESFOtJi2jx3VlO81CYOrq3NA2IZFjlWnIm"
        )
        .timeout(120000)
        .execute()
    val doc = response.parse()
    val table = doc.selectFirst("table#MainContent_gvStudents_DXMainTable") ?: return

    val headers = arrayListOf<String>()
    table.select("tr#MainContent_gvStudents_DXHeadersRow0 .dxgvHeader_SisTheme")?.forEach {
        headers.add(it.text().replace("\\t", "").replace("\\n", "").replace("\\r", "").trim())
    }

    table.select("tr.dxgvDataRow_SisTheme")?.forEach {
        val values = arrayListOf<String>()
        it.select("td")?.forEach {
            values.add(it.text())
            println(values.last())
        }

        if (values.isNotEmpty()) {
            val doc = Document()
            for (i in 0 until headers.size) {
                val key = headers[i]
                val value = values[i]
                doc.append(key, value)
            }
            collection.insertOneUnique(doc)
        }
    }

}