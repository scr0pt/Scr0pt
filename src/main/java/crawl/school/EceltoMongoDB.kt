package crawl.school

import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Updates
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.bson.Document
import java.io.FileInputStream
import java.io.FileOutputStream


fun main() {
    val mongoClient = MongoClients.create("mongodb+srv://root:mongo@cluster0-qzu3e.mongodb.net/admin")
    val serviceAccountDatabase = mongoClient.getDatabase("edu-school-account")
//    val collection: MongoCollection<Document> = serviceAccountDatabase.getCollection("hcmus-student-infomation")
    val collection2: MongoCollection<Document> = serviceAccountDatabase.getCollection("hcmus-scholarships")

//    collection2.find().forEach {
//        val mssv = it.getString("MSSV")
//        var gender = it.getString("GENDER")
//        println(mssv + ": " + gender)
//        gender = when (gender) {
//            "0.0" -> "Nam"
//            "1.0" -> "Nữ"
//            else -> gender
//        }
//        collection2.updateOne(Document("MSSV", mssv), Updates.combine(Updates.set("GENDER", gender)))
//    }

    Excel2Mongo("D:\\College Edu\\hcmus\\done\\p_tai_vu.xls", collection2).also {
        it.headersNameCheck.addAll(arrayListOf("MSSV", "Họ tên"))
//        it.headersNameCheck.addAll(arrayListOf("Mã số", "NTNS"))
        it.headersRemove.addAll(arrayListOf("STT", "Ghi chú"))
        it.headersRename.addAll(
            arrayListOf(
                Pair("Họ tên", "HỌ TÊN"),
                Pair("Mã số", "MSSV"),
                Pair("NTNS", "Ngày Sinh")
            )
        )
    }.parse(update = true)
}

class Excel2Mongo(val filePath: String, val collection: MongoCollection<Document>? = null) {
    val headersNameCheck = arrayListOf<String>()
    val headersRename = arrayListOf<Pair<String, String>>()
    val headersName = arrayListOf<String>()
    val headersRemove = arrayListOf<String>()

    fun getHeaderAfterRename(headerValue: String): String {
        try {
            return headersRename.first { it.first == headerValue }.second
        } catch (e: Exception) {
            return headerValue
        }
    }

    fun isThisRowHeader(row: Row): Boolean {
        val headerCheck = mutableListOf<String>().apply { addAll(headersNameCheck) }
        for (i in 0 until row.lastCellNum) {
            headerCheck.removeIf { it.equals(row.getCell(i)?.toString(), true) }
        }
        return headerCheck.isEmpty()
    }

    fun Cell.getStringValue() = when (cellTypeEnum) {
        CellType.NUMERIC -> numericCellValue.toString()
        CellType.STRING -> stringCellValue
        else -> toString()
    }


    fun parse(update: Boolean = true) {
        val inputStream = FileInputStream(filePath)
        val xlWb = WorkbookFactory.create(inputStream)
        for (numberOfSheets in 0 until xlWb.numberOfSheets) {
            val xlWs = xlWb.getSheetAt(numberOfSheets)
            xlWs.forEach { row ->

                if (row != null) {
                    if (isThisRowHeader(row)) {
                        if (headersName.isEmpty()) {
                            row.forEach { cell ->
                                var element = getHeaderAfterRename(cell.getStringValue())
                                element = element.replace("\n"," ").replace("  ", "").trim()
                                println(element)
                                headersName.add(element)
                            }
                        }
                    } else {
                        val values = arrayListOf<String>()
                        row.forEach { cell ->
                            val element = cell.getStringValue()
                            values.add(element)
                        }
                        if (values.isNotEmpty() && headersName.isNotEmpty() && values.size == headersName.size) {
                            val doc = Document()
                            for (i in 0 until headersName.size) {
                                val header = headersName[i]
                                val value = values.get(i)

                                if (!headersRemove.contains(header)) {
                                    doc.append(header, value)
                                }
                            }
                            println(doc)
                            if (update)
                                collection?.insertOneUnique(doc)
                        }
                    }
                }
            }
        }
    }


}
