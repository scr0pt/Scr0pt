package net.scr0pt.thirdservice.fembed

import net.scr0pt.bot.Page
import net.scr0pt.bot.PageManager
import net.scr0pt.bot.PageResponse
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import org.bson.Document
import org.openqa.selenium.WebDriver
import net.scr0pt.utils.FakeProfile
import net.scr0pt.utils.InfinityMail
import net.scr0pt.utils.tempmail.Gmail
import net.scr0pt.utils.tempmail.event.MailReceiveEvent
import net.scr0pt.utils.tempmail.models.Mail
import net.scr0pt.utils.webdriver.Browser
import net.scr0pt.utils.webdriver.findElWait
import java.util.*

/**
 * Created by Long
 * Date: 10/19/2019
 * Time: 9:36 PM
 */

suspend fun main() {
    val gmailUsername = "vanlethi74@gmail.com"
    val gmailPassword = "XinChaoVietNam@@2000"

    val mongoClient =
        MongoClients.create("mongodb+srv://root:mongo@cluster0-enhmy.mongodb.net/admin?retryWrites=true&w=majority")
    val serviceAccountDatabase = mongoClient.getDatabase("fembed")
    val collection: MongoCollection<Document> = serviceAccountDatabase.getCollection("fembed-account")

    val infinityMail = InfinityMail(gmailUsername.removeSuffix("@gmail.com"))
    for (i in 0..100)
        infinityMail.getNext()
    for (iMail in infinityMail.list) {
        val email = iMail.mail + "@gmail.com"
        if (arrayListOf(
                "vinhnguyen4h4@gmail.com", "v.inhnguyen4h4@gmail.com", "vi.nhnguyen4h4@gmail.com",
                "vinh.nguyen4h4@gmail.com", "vinhng.uyen4h4@gmail.com", "vinhnguy.en4h4@gmail.com"
            ).contains(
                email
            )
        ) {
            continue
        }

        if (collection.countDocuments(Document("email", email)) > 0L) continue

        val result = FakeProfile.getNewProfile()
        val first = result?.name?.first ?: "Bruce"
        val last = result?.name?.last ?: "Lee"

        registerFembed(
                "${first} $last", email,
                "IAmLegend2002",
                gmailUsername,
                gmailPassword,
                collection,
                Browser.firefox
        )
    }
}

class FembedRegisterPage(
    val name: String,
    val email: String,
    onPageFinish: (() -> Unit)? = null
) : Page(onPageFinish = onPageFinish) {
    override fun isEndPage() = false

    override fun _action(driver: WebDriver): PageResponse {
        println(this::class.java.simpleName + ": action")
        driver.findElWait(1000, 60000, "input#display_name", jsoup = false).firstOrNull()?.sendKeys(name)
            ?: return PageResponse.NOT_FOUND_ELEMENT()
        driver.findElWait(1000, 60000, "input#email_register", jsoup = false).firstOrNull()?.sendKeys(email)
            ?: return PageResponse.NOT_FOUND_ELEMENT()
        driver.findElWait(1000, 60000, "button#register", jsoup = false).firstOrNull()?.click()
            ?: return PageResponse.NOT_FOUND_ELEMENT()
        return PageResponse.WAITING_FOR_RESULT()
    }

    override fun _detect(doc: org.jsoup.nodes.Document, currentUrl: String, title: String): Boolean =
        currentUrl.startsWith("https://dash.fembed.com/auth/register") &&
                doc.selectFirst("#register_form .title")?.text() == "Free Register!"
}

class FembedThankYouForJoiningPage(
    onPageFinish: (() -> Unit)? = null
) : Page(onPageFinish = onPageFinish) {
    override fun isEndPage() = false

    override fun _action(driver: WebDriver): PageResponse {
        println(this::class.java.simpleName + ": action")
        return PageResponse.WAITING_FOR_RESULT()
    }

    override fun _detect(doc: org.jsoup.nodes.Document, currentUrl: String, title: String): Boolean =
        currentUrl.startsWith("https://dash.fembed.com/auth/register") &&
                doc.selectFirst("#register_done .title")?.text() == "Thank You for joining."
}

class FembedActivatingSetPasswordPage(
    val password: String,
    onPageFinish: (() -> Unit)? = null
) : Page(onPageFinish = onPageFinish) {
    override fun isEndPage() = false

    override fun _action(driver: WebDriver): PageResponse {
        println(this::class.java.simpleName + ": action")
        driver.findElWait(1000, 60000, "input#password", jsoup = false).firstOrNull()?.sendKeys(password)
            ?: return PageResponse.NOT_FOUND_ELEMENT()
        driver.findElWait(1000, 60000, "input#password2", jsoup = false).firstOrNull()?.sendKeys(password)
            ?: return PageResponse.NOT_FOUND_ELEMENT()
        driver.findElWait(1000, 60000, "input#is_subscribed", jsoup = false).firstOrNull()?.click()
        driver.findElWait(1000, 60000, "button#verify", jsoup = false).firstOrNull()?.click()
            ?: return PageResponse.NOT_FOUND_ELEMENT()
        return PageResponse.WAITING_FOR_RESULT()
    }

    override fun _detect(doc: org.jsoup.nodes.Document, currentUrl: String, title: String): Boolean =
        currentUrl.startsWith("https://dash.fembed.com/auth/register") &&
                doc.selectFirst(".container h3")?.text() == "Activating" &&
                doc.selectFirst(".container h4")?.text() == "Please set your password."
}

class FembedDashboardPage(
    onPageFinish: (() -> Unit)? = null
) : Page(onPageFinish = onPageFinish) {
    override fun isEndPage() = true

    override fun _action(driver: WebDriver): PageResponse {
        println(this::class.java.simpleName + ": action")
        return PageResponse.OK()
    }

    override fun _detect(doc: org.jsoup.nodes.Document, currentUrl: String, title: String): Boolean =
        title == "Dashboard - Fembed" &&
                currentUrl.startsWith("https://dash.fembed.com") &&
                doc.selectFirst(".container h1.title")?.text() == "Dashboard"
}

//    val driver = object : HtmlUnitDriver(BrowserVersion.FIREFOX_60, true) {
//        override fun modifyWebClient(client: WebClient): WebClient {
//            val webClient = super.modifyWebClient(client)
//            // you might customize the client here
//            webClient.options.isCssEnabled = false
//            return webClient
//        }
//    }

suspend fun registerFembed(
    name: String,
    email: String,
    password: String,
    gmailUsername: String,
    gmailPassword: String,
    collection: MongoCollection<Document>,
    driver: WebDriver
) {
    println(email)
    val registerTime = System.currentTimeMillis()

    val pageManager = PageManager(
            arrayListOf(
                    FembedRegisterPage(name, email) {
                        println("FembedRegisterPage success")
                    },
                    FembedThankYouForJoiningPage {
                        println("FembedThankYouForJoiningPage success")
                        getVerifyEmail(registerTime, gmailUsername, gmailPassword) { confirmLink ->
                            driver.get(confirmLink)
                        }
                    },
                    FembedActivatingSetPasswordPage(password) {
                        println("FembedActivatingSetPasswordPage success")
                        collection.insertOne(
                                Document("email", email).append("password", password).append(
                                        "name",
                                        name
                                ).append("created_at", Date()).append("updated_at", Date())
                        )
                    },
                    FembedDashboardPage {
                        println("FembedDashboardPage success")
                    }
            ), driver,
            "https://dash.fembed.com/auth/register"
    )

    pageManager.run { pageResponse ->
        println(pageResponse)
    }
}

fun getVerifyEmail(registerTime: Long, gmailUsername: String, gmailPassword: String, onSuccess: (String) -> Unit) {
    Gmail(gmailUsername, gmailPassword).apply {
        onEvent(
                MailReceiveEvent(
                        key = "ona1sender",
                        validator = { mail ->
                            (mail.id ?: 0) > registerTime &&
                                    Mail.CompareType.EQUAL_IGNORECASE.compare(mail.from, "noreply@notify.fembed.com")
                            Mail.CompareType.EQUAL_IGNORECASE.compare(
                                    mail.subject,
                                    "Thank for registering an account with us"
                            )
                        },
                        callback = { mails ->
                            mails.firstOrNull()
                                    ?.contentDocumented?.selectFirst("a[href*='https://dash.fembed.com/auth/verify?token']")
                                    ?.attr("href")?.let { confirmLink ->
                                        println(confirmLink)
                                        this.logout()
                                        onSuccess(confirmLink)
                                    }
                        },
                        once = false,
                        new = true,
                        fetchContent = true
                )
        )
    }


}

