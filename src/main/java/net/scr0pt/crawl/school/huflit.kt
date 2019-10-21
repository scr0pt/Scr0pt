package net.scr0pt.crawl.school

import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import org.apache.commons.lang3.StringUtils
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.pdfbox.text.PDFTextStripperByArea
import org.bson.Document
import net.scr0pt.utils.curl.LongConnection
import java.io.File
import java.lang.reflect.Type


/**
 * Created by Long
 * Date: 10/12/2019
 * Time: 2:47 PM
 */

fun main() {
    val mongoClient = MongoClients.create("mongodb+srv://root:mongo@cluster0-qzu3e.mongodb.net/admin")
    val serviceAccountDatabase = mongoClient.getDatabase("edu-school-account")
    val collection2: MongoCollection<Document> = serviceAccountDatabase.getCollection("huflit-diploma-2")
    val conn = LongConnection().also {
        it.header("Pragma", "no-cache")
        it.header("Origin", "http://huflit.edu.vn")
        it.header("Accept-Encoding", "gzip, deflate")
        it.header("Accept-Language", "en,en-GB;q=0.9,vi;q=0.8,fr-FR;q=0.7,fr;q=0.6,en-US;q=0.5,ja;q=0.4")
        it.header(
            "User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36"
        )
        it.header(
            "Content-Type",
            "application/x-www-form-urlencoded; charset=UTF-8",
            method = LongConnection.REQUEST_METHOD.POST
        )
        it.header("Accept", "*/*")
        it.header("Cache-Control", "no-cache")
        it.header("X-Requested-With", "XMLHttpRequest")
        it.header("Connection", "keep-alive")
        it.header("Referer", "http://huflit.edu.vn/index.php?language=vi&nv=vanbang")
        it.header("DNT", "1")
    }

    conn.get("http://huflit.edu.vn/index.php?language=vi&nv=vanbang")


    collection2.find(Filters.exists("Số văn bằng", false)).forEach {
        if (!it.containsKey("Số văn bằng")) {
            val MSSV = it.getString("MSSV")
            println(MSSV)

            var response = conn.post(
                "http://huflit.edu.vn/index.php?language=vi&nv=vanbang&op=net.scr0pt.main",
                hashMapOf(
                    "post" to "1", "msv" to MSSV
                )
            )

            while (response?.body?.contains("Hệ thống đang từ chối truy cập của bạn.") ?: false) {
                val second = response?.doc?.selectFirst("span#secField")?.text()?.toLong() ?: 60L
                println("Sleep $second second")
                Thread.sleep(second * 1000)
                response = conn.post(
                    "http://huflit.edu.vn/index.php?language=vi&nv=vanbang&op=net.scr0pt.main",
                    hashMapOf(
                        "post" to "1", "msv" to MSSV
                    )
                )
            }

            println(response?.body)

            val typeToken = object : TypeToken<List<Example>>() {}
            val collectionType: Type = typeToken.type
            val list = try {
                Gson().fromJson<ArrayList<Example>>(response?.body ?: "", collectionType)
            } catch (e: Exception) {
                arrayListOf<Example>()
            }

            if (list != null && list.isNotEmpty()) {
                val example = list[0]
                println(example)
                if (example.fMasv == MSSV) {
                    val updateDoc = Document()
                        .append("Hệ đào tạo", example.fHedtvn)
                        .append("Khóa học", example.fKhoahoc)
                        .append("Giới tính", example.fNamnu)
                        .append("Ngày sinh", example.fNgaysinh)
                        .append("Số văn bằng", example.fSovanbg)
                        .append("Ngành đào tạo", example.fTenngvn)
                        .append("Tên", example.fTenvn)
                        .append("Họ", example.fHolotvn)
                        .append("Xếp loại", example.fXeploai)
                        .append("Auto_Id", example.id)

                    collection2.updateOne(
                        Document("MSSV", MSSV), Updates.combine(
                            Updates.set("Hệ đào tạo", example.fHedtvn),
                            Updates.set("Khóa học", example.fKhoahoc),
                            Updates.set("Giới tính", example.fNamnu),
                            Updates.set("Ngày sinh", example.fNgaysinh),
                            Updates.set("Số văn bằng", example.fSovanbg),
                            Updates.set("Ngành đào tạo", example.fTenngvn),
                            Updates.set("Tên", example.fTenvn),
                            Updates.set("Họ", example.fHolotvn),
                            Updates.set("Xếp loại", example.fXeploai),
                            Updates.set("Auto_Id", example.id)
                        )
                    )
                }
            }
        }
    }


}

fun main3() {
    val mongoClient = MongoClients.create("mongodb+srv://root:mongo@cluster0-qzu3e.mongodb.net/admin")
    val serviceAccountDatabase = mongoClient.getDatabase("edu-school-account")
    val collection2: MongoCollection<Document> = serviceAccountDatabase.getCollection("huflit-diploma-2")
    collection2.find(Document("Dân tộc", "")).forEach {
        collection2.updateOne(
            Document("MSSV", it.getString("MSSV")),
            Updates.combine(Updates.set("Khoa", "Quan hệ quốc tế"))
        )
//        collection2.updateOne(
//            Document("MSSV", it.getString("MSSV")),
//            Updates.combine(Updates.set("Ngành đào tạo", "Quan hệ quốc tế"))
//        )
    }


//        if (nganh == "Quản trị kinh doanh Quản trị kinh doanh quốc tế") {

//            collection2.updateOne(
//                Document("MSSV", it.getString("MSSV")),
//                Updates.combine(Updates.set("Ngành đào tạo", "Quản trị kinh doanh quốc tế"))
//            )
//        }
//    }
}

fun main2() {
    val mongoClient = MongoClients.create("mongodb+srv://root:mongo@cluster0-qzu3e.mongodb.net/admin")
    val serviceAccountDatabase = mongoClient.getDatabase("edu-school-account")
    val collection2: MongoCollection<Document> = serviceAccountDatabase.getCollection("huflit-diploma-2")
    PDDocument.load(File("D:\\Google Drive Unlimited Edu\\huflit\\DSSV tot nghiep - du kien - dot 2 - 2018.pdf"))
        .use { document ->
            if (!document.isEncrypted) {

                val stripper = PDFTextStripperByArea()
                stripper.sortByPosition = true

                val tStripper = PDFTextStripper()

                val pdfFileInText = tStripper.getText(document)
                //println("Text:" + st);

                val nganhs = arrayListOf(
                    "Quản trị kinh doanh",
                    "Ngôn ngữ Anh",
                    "Đông Phương học",
                    "Tài chính - Ngân hàng",
                    "Tiếng Anh",
                    "Quản trị khách sạn",
                    "Quản trị Khách sạn",
                    "Quản trị dịch vụ du lịch và lữ hành",
                    "Tiếng Trung Quốc",
                    "Kế toán",
                    "Quan hệ quốc tế",
                    "Công nghệ thông tin",
                    "Ngôn ngữ Trung Quốc",
                    "Tài chính - Ngân hàng",
                    "QT Dịch vụ Du lịch và Lữ hành"
                )

                val danhtocs = arrayListOf("Kinh", "Hàn Quốc", "Hoa", "Nùng", "Hán", "Thái", "Thổ", "Tày")
                val xeploais = arrayListOf("Trung bình khá", "Trung bình", "Khá", "khá", "Giỏi")

                val provinces = arrayListOf(
                    "Thành phố Đà Nẵng",
                    "Thành phố Hải Phòng",
                    "Quảng Nam-Đà Nẵng",
                    "Minh Hải", "Gia Lai", "Ninh Bình", "Bắc Giang", "Phú Thọ", "Yên Bái",
                    "Đăk Nông",
                    "Bạc Liêu",
                    "Sông Bé",
                    "Vĩnh Phúc",
                    "Trà Vinh",
                    "Quảng Bình",
                    "Thanh Hóa",
                    "Hải Dương",
                    "Nam Định",
                    "Thành phố Hồ Chí Minh",
                    "TP. Hồ Chí Minh",
                    "TP.HCM",
                    "Tp HCM",
                    "Cửu Long",
                    "Bình Phước",
                    "Đồng Nai",
                    "Tây Ninh",
                    "Hậu Giang",
                    "Đồng Tháp",
                    "Quảng Ngãi",
                    "Cam Ranh",
                    "Kiên Giang",
                    "Thừa Thiên-Huế",
                    "Bình Dương",
                    "Hà Nam",
                    "Nghĩa Bình",
                    "TP Cần Thơ",
                    "Đà Nẵng",
                    "Bình Định",
                    "Thành phố Hà Nội",
                    "Hà Tĩnh",
                    "Quảng Nam",
                    "Tuyên Quang",
                    "An Giang",
                    "Cà Mau",
                    "Sóc Trăng",
                    "Bình Thuận",
                    "Phú Yên",
                    "Bắc Ninh",
                    "Bến Tre",
                    "Bà Rịa-Vũng Tàu",
                    "Dak Lak",
                    "Ninh Thuận",
                    "Tiền Giang",
                    "Long An",
                    "Thái Bình",
                    "Khánh Hòa",
                    "Ukraina",
                    "Lâm Đồng",
                    "Daklak",
                    "Cần Thơ",
                    "Hải Phòng",
                    "Đak Lak",
                    "Nghệ An",
                    "Hà Nội",
                    "Vĩnh Long",
                    "Nha Trang", "Vũng Tàu", "Hà Tây", "Cộng Hòa Liên Bang Đức"
                )

                val renameProvinces = hashMapOf(
                    "TP. Hồ Chí Minh" to "Thành phố Hồ Chí Minh",
                    "TP.HCM" to "Thành phố Hồ Chí Minh",
                    "Tp HCM" to "Thành phố Hồ Chí Minh",
                    "Dak Lak" to "Đắk Lắk",
                    "Đak Lak" to "Đắk Lắk",
                    "Thành phố Đà Nẵng" to "Đà Nẵng",
                    "Thành phố Hải Phòng" to "Hải Phòng"

                )
                // split by whitespace
                val lines = pdfFileInText.split("\\r?\\n".toRegex())
                line@ for (line in lines) {
                    var lineStr = line

                    if (line.length < 20 || !StringUtils.isNumeric(line.first().toString())) continue@line

                    val doc = Document()
                    lineStr = lineStr.substringAfter(" ")

                    val MSSV = lineStr.substringBefore(" ")
                    doc.append("MSSV", MSSV)
                    lineStr = lineStr.substringAfter(MSSV + " ")

                    var nganh = ""
                    run nganh@{
                        nganhs.forEach {
                            if (lineStr.endsWith(it)) {
                                nganh = it
                                return@nganh
                            }
                        }
                    }

                    if (nganh.isEmpty())
                        continue@line

                    doc.append("Ngành đào tạo", nganh)

                    lineStr = lineStr.removeSuffix(nganh).trim()


                    var dantoc = ""
                    run dantoc@{
                        danhtocs.forEach {
                            if (lineStr.endsWith(it)) {
                                dantoc = it
                                return@dantoc
                            }
                        }
                    }

                    if (dantoc.isEmpty() && !arrayListOf(
                            "13BE710004",
                            "13BE710034",
                            "13BE710207",
                            "13BE710208",
                            "14BE710015",
                            "14BE710150",
                            "14BE710165",
                            "14VA202045",
                            "15VA102001",
                            "15VA102012",
                            "15VA102019",
                            "15VA102025",
                            "15VA102026",
                            "15VA102032",
                            "15VA102048",
                            "15VA102065",
                            "15VA102076",
                            "15VA102096"
                        ).contains(MSSV)
                    )
                        continue@line

                    doc.append("Dân tộc", dantoc)
                    lineStr = lineStr.removeSuffix(dantoc).trim()

                    var que = ""
                    run loop@{
                        provinces.forEach {
                            if (lineStr.endsWith(it)) {
                                que = it
                                return@loop
                            }
                        }
                    }

                    if (que.isEmpty() && !arrayListOf("14BE710150").contains(MSSV))
                        continue@line

                    lineStr = lineStr.removeSuffix(que).trim()
                    renameProvinces.forEach { t, u ->
                        if (t == que) que = u
                    }

                    doc.append("Nơi sinh", que)

                    val lop = lineStr.substringAfterLast(" ")
                    doc.append("Lớp", lop)
                    lineStr = lineStr.substringBeforeLast(" ")

                    val TCTL = lineStr.substringAfterLast(" ")
                    doc.append("Số TCT", TCTL)
                    lineStr = lineStr.substringBeforeLast(" ")

                    val TBTL = lineStr.substringAfterLast(" ")
                    doc.append("Điểm TBT", TBTL)
                    lineStr = lineStr.substringBeforeLast(" ")


                    val birthday = lineStr.substringAfterLast(" ")
                    doc.append("Ngày sinh", birthday)
                    lineStr = lineStr.substringBeforeLast(" ")

                    if (!birthday.contains("/"))
                        continue@line

                    var gender = ""
                    run gender@{
                        lineStr.split(" ").reversed().forEach {
                            if (it == "Nam") {
                                gender = "Nam"
                                return@gender
                            } else if (it == "Nữ") {
                                gender = "Nữ"
                                return@gender
                            }
                        }
                    }
                    if (gender.isEmpty())
                        continue@line

                    doc.append("Giới tính", gender)


                    lineStr = lineStr.substringBeforeLast(gender)

                    val fullName = lineStr.trim()
                    if (fullName.contains(" ")) {
                        val firstName = fullName.substringAfterLast(" ")
                        val lastName = fullName.substringBeforeLast(" ")
                        doc.append("Tên", firstName)
                        doc.append("Họ", lastName)
                    } else {
                        doc.append("Tên", fullName)
                    }

                    println(doc)
                    collection2.insertOneUnique(doc, Document("MSSV", MSSV))
                }
            }
        }

}

class Example {
    @SerializedName("id")
    @Expose
    var id: Int? = null
    @SerializedName("f_sovanbg")
    @Expose
    var fSovanbg: String? = null
    @SerializedName("f_masv")
    @Expose
    var fMasv: String? = null
    @SerializedName("f_holotvn")
    @Expose
    var fHolotvn: String? = null
    @SerializedName("f_tenvn")
    @Expose
    var fTenvn: String? = null
    @SerializedName("f_ngaysinh")
    @Expose
    var fNgaysinh: String? = null
    @SerializedName("f_namnu")
    @Expose
    var fNamnu: String? = null
    @SerializedName("f_khoahoc")
    @Expose
    var fKhoahoc: String? = null
    @SerializedName("f_xeploai")
    @Expose
    var fXeploai: String? = null
    @SerializedName("f_hedtvn")
    @Expose
    var fHedtvn: String? = null
    @SerializedName("f_tenngvn")
    @Expose
    var fTenngvn: String? = null
}