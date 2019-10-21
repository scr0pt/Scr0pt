package crawl.school

import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Updates
import org.bson.Document
import org.bson.conversions.Bson

/**
 * Created by Long
 * Date: 10/13/2019
 * Time: 10:09 AM
 */

fun main() {
    val mongoClient = MongoClients.create("mongodb+srv://root:mongo@cluster0-qzu3e.mongodb.net/admin")
    val serviceAccountDatabase = mongoClient.getDatabase("edu-school-account")
    val collection: MongoCollection<Document> = serviceAccountDatabase.getCollection("oude-student-infomation")
    val collection2: MongoCollection<Document> = serviceAccountDatabase.getCollection("oude-student-infomation-2")
//    collection2.deleteMany(Document())
    collection.find(Document()).forEach { old ->
        if (old.containsKey("MSSV")) {
            val mssv = old.getString("MSSV")
            if (mssv != null) {
                println(mssv)

                if (collection2.countDocuments(Document("MSSV", mssv)) > 0) {

                    val a = arrayListOf<Bson>()
                    old.forEach { t, u ->
                        if (t != "_id" && u != null) {
                            a.add(Updates.set(t, u))
                        }
                    }

                    collection2.updateOne(
                        Document("MSSV", mssv), Updates.combine(a)
                    )
                } else {
                    collection2.insertOne(old)
                }
            }
        }
    }


//    Excel2Mongo("C:\\Users\\Long\\Downloads\\DSSV_DU_DK_NHAN_BANG_TN_DOT_02_2016_06_06_2016.xls", collection).also {
//        it.headersNameCheck.addAll(arrayListOf("HỌ LÓT","TÊN","NƠI SINH"))
//        it.headersRemove.addAll(arrayListOf("STT"))
//        it.headersRename.addAll(
//            arrayListOf(
//                Pair("HỌ LÓT", "HỌ"),
//                Pair("NGÀY\n SINH", "NGÀY SINH"),
//                Pair("NGÀY\n SINH (GÓC)", "NGÀY SINH"),
//                Pair("MÃ SỐ\nSINH VIÊN", "MSSV"),
//                Pair("HÌNH 3x4\n(2 TẤM)", "HÌNH 3x4 (3 TẤM)"),
//                Pair("MÃ SỐ SINH VIÊN", "MSSV"),
//                Pair("MÃ SINH VIÊN", "MSSV")
//            )
//        )
//    }.parse(update = true)
}