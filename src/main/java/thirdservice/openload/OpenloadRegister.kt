package thirdservice.openload

import bot.Page
import bot.PageManager
import bot.PageResponse
import bot.google.GoogleSearch
import bot.google.LoginEnterEmailPage
import bot.google.LoginEnterPasswordPage
import com.gargoylesoftware.htmlunit.BrowserVersion
import com.gargoylesoftware.htmlunit.WebClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Updates
import org.bson.Document
import org.openqa.selenium.WebDriver
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import thirdservice.mlab.loginGoogle
import utils.InfinityMail
import utils.tempmail.Gmail
import utils.tempmail.event.MailReceiveEvent
import utils.tempmail.models.Mail
import utils.webdriver.Browser
import utils.webdriver.findElWait
import utils.webdriver.waitUntilUrlChange
import java.util.*


suspend fun main() {
    register()
}

suspend fun register() {
    val gmailUsername = "vanlethi74@gmail.com"
    val gmailPassword = "XinChaoVietNam@@2000"


    val infinityMail = InfinityMail("vanlethi74")
    for (i in 0..100)
        infinityMail.getNext()
    val mongoClient =
        MongoClients.create("mongodb+srv://root:mongo@cluster0-enhmy.mongodb.net/admin?retryWrites=true&w=majority")
    val serviceAccountDatabase = mongoClient.getDatabase("openload")
    val collection: MongoCollection<Document> = serviceAccountDatabase.getCollection("openload-account")
    for (iMail in infinityMail.list) {
        val email = iMail.mail + "@gmail.com"
        if (email.contains("vanlethi74")) continue
        if (collection.countDocuments(org.bson.Document("email", email)) > 0L) continue
        val password = "XinChaoVietNam@1990"
        val driver = Browser.firefox
        loginGoogle(email = gmailUsername, password = gmailPassword, driver = driver) {
            driver.get("https://openload.co/register")

            val emailInputs =
                driver.findElWait(1000, 60000, "form#register-form input#registerform-email", jsoup = false)
            emailInputs.first().sendKeys(email)

            val passwordInputs =
                driver.findElWait(1000, 60000, "form#register-form input#registerform-password", jsoup = false)
            passwordInputs.first().sendKeys(password)
            val repasswordInputs =
                driver.findElWait(1000, 60000, "form#register-form input#registerform-passwordconfirm", jsoup = false)
            repasswordInputs.first().sendKeys(password)
            val iagreeInputs =
                driver.findElWait(1000, 60000, "form#register-form input#registerform-iagree", jsoup = false)
            iagreeInputs.first().click()

            val recaptcha = driver.findElWait(
                1000,
                60000,
                "form#register-form iframe[src*='https://www.google.com/recaptcha']",
                jsoup = false
            )

            recaptcha.first().click()

            driver.waitUntilUrlChange(1000, 300000)
            println(driver.currentUrl)

            var cookies = ""
            driver.manage().cookies.forEach {
                cookies += "${it.name}=${it.value};"
            }
            cookies.removeSuffix(";")

            collection.insertOne(
                Document("email", email).append("password", password).append(
                    "cookies",
                    cookies
                ).append("temp_ban", null).append("created_at", Date()).append("updated_at", Date())
            )
            driver.close()

        }
    }
}

fun login(
    openloadEmail: String,
    openloadPassword: String,
    gmailUsername: String,
    gmailPassword: String,
    collection: MongoCollection<Document>
) {//update cookie
//    val driver = Browser.firefox

    val driver = object : HtmlUnitDriver(BrowserVersion.FIREFOX_60, true) {
        override fun modifyWebClient(client: WebClient): WebClient {
            val webClient = super.modifyWebClient(client)
            // you might customize the client here
            webClient.options.isCssEnabled = false
            return webClient
        }
    }

    driver.get("https://openload.co/login")
    try {
        driver.executeAsyncScript(
            """
                document.querySelector(".sign-in-button").click();
                setTimeout(function(){ 
                    document.querySelector("#loginform-email").value = "${openloadEmail}"
                    document.querySelector("#loginform-password").value = "${openloadPassword}";
                    document.querySelector("#login-form .submitcontainer button").click();
                }, 3000);
        """.trimIndent()
        )
    } catch (e: Exception) {
    }

    val gmail = Gmail(gmailUsername, gmailPassword).apply {
        onEvent(
            MailReceiveEvent(
                key = "ona1sender",
                validator = { mail ->
                    Mail.CompareType.EQUAL_IGNORECASE.compare(mail.from, "admin@openload.co")
                },
                callback = { mails ->
                    val mail =
                        mails.firstOrNull { it.content?.contains("Please log in using your login code below:") == true }
                    val code = mail?.content?.substringAfter("Please log in using your login code below:")?.trim()
                        ?.substringBefore(" ")?.trim()
                    if (code != null) {
                        try {
                            driver.executeAsyncScript(
                                """
                                    document.getElementById("loginform-loginkey").value = "${code}"
                                    setTimeout(function(){ 
                                        document.querySelector("#main #login-form .submitcontainer button").click();
                                    }, 3000);
                            """.trimIndent()
                            )
                        } catch (e: Exception) {
                        }
                    }
                },
                once = false,
                new = true,
                fetchContent = true
            )
        )
    }

    driver.waitUntilUrlChange(1000, 180000)
    println(driver.currentUrl)
    gmail.logout()

    var cookies = ""
    driver.manage().cookies.forEach {
        cookies += "${it.name}=${it.value};"
    }
    cookies.removeSuffix(";")

    collection.updateOne(
        Document("email", openloadEmail), Updates.combine(
            Updates.set("cookies", cookies),
            Updates.set("updated_at", Date())
        )
    )
}