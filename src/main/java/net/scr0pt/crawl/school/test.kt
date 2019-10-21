package net.scr0pt.crawl.school

import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import org.apache.commons.lang3.RandomUtils
import org.bson.Document


fun main() {
    for(i in 0..100) println(RandomUtils.nextInt(0,3))
}

fun main22() {
    val mongoClient = MongoClients.create("mongodb+srv://root:mongo@cluster0-qzu3e.mongodb.net/admin")
    val serviceAccountDatabase = mongoClient.getDatabase("service-account")
    val schoolAccountDatabase = mongoClient.getDatabase("edu-school-account")
    val collection: MongoCollection<Document> = serviceAccountDatabase.getCollection("google")
    val collectionVimaru: MongoCollection<Document> = schoolAccountDatabase.getCollection("vinhuni-email-info")

    collection.find(Document("school", "vinhuni")).forEach {
        it.remove("give_way")
        val CMND = it.getString("cmt")
        it.remove("cmt")
        if (CMND != null) {
            it.append("cmnd", CMND)
        }

        val gender = it.getString("gender")
        when (gender) {
            "FeMale" -> it.replace("gender", "female")
            "Male" -> it.replace("gender", "male")
            "None" -> it.remove("gender")
        }

        it.remove("school")
        val hacked = it.getString("hacked")
        var login_status: String? = null
        var email_status: String? = null//ACC_DISABLE
        when (hacked) {
            "Not Yet" -> {

            }
            "No" -> {
                login_status = "PASSWORD_INCORRECT"
            }

            "Not Exist" -> {
                email_status = "NOT_EXIST"
            }
            "Yes" -> {
                login_status = "PASSWORD_CORRECT"
                email_status = "HACKED"
            }
        }

        it.append("login_status", login_status)
        it.append("email_status", email_status)
        it.remove("hacked")

        collectionVimaru.insertOne(it)
    }


}
