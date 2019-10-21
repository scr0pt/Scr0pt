package net.scr0pt

import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Updates
import net.scr0pt.bot.Page
import net.scr0pt.bot.PageManager
import net.scr0pt.bot.PageResponse
import org.jsoup.nodes.Document
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import net.scr0pt.utils.FakeProfile
import net.scr0pt.utils.InfinityMail
import net.scr0pt.utils.tempmail.Gmail
import net.scr0pt.utils.tempmail.event.MailReceiveEvent
import net.scr0pt.utils.tempmail.models.Mail
import net.scr0pt.utils.webdriver.Browser
import net.scr0pt.utils.webdriver.document
import net.scr0pt.utils.webdriver.findElWait
import net.scr0pt.utils.webdriver.waitUmtil
import java.io.File


suspend fun main() {
    val infinityMail = InfinityMail("vanlethi74")
    for (i in 0..200)
        infinityMail.getNext()

    val mongoClient =
        MongoClients.create("mongodb+srv://root:mongo@cluster0-enhmy.mongodb.net/admin?retryWrites=true&w=majority")
    val serviceAccountDatabase = mongoClient.getDatabase("mega")
    val collection: MongoCollection<org.bson.Document> = serviceAccountDatabase.getCollection("mega-account")


    val gmailUsername = "vanlethi74@gmail.com"
    val gmailPassword = "XinChaoVietNam@@2000"

    infinityMail.list.forEach { iMail ->
        val email = iMail.mail + "@gmail.com"

        if (collection.countDocuments(org.bson.Document("User_Name", email)) > 0L) return@forEach

        val result = FakeProfile.getNewProfile()
        val firstName = result?.name?.first ?: "Bruce"
        val lastName = result?.name?.last ?: "Lee"
        val password = "XinChaoVietNam@2024"
        val registerTime = System.currentTimeMillis()
        val megaRegisterPage = PageManager(
                arrayListOf(
                        MegaRegisterPage(firstName, lastName, email, password) {
                            println("net.scr0pt.MegaRegisterPage finish")
                            collection.insertOne(
                                    org.bson.Document("User_Name", email)
                                            .append("Password", password).append("firstName", firstName)
                                            .append("lastName", lastName).append("verify_email", false)
                            )
                        },
                        MegaRegisterConfirmEmailPage(gmailUsername, gmailPassword, registerTime) {
                            println("net.scr0pt.MegaRegisterConfirmEmailPage finish")
                            collection.updateOne(org.bson.Document("User_Name", email), Updates.set("verify_email", true))
                        },
                        MegaRegisterEnterPasswordAfterEnterConfirmLinkPage(
                                gmailUsername,
                                gmailPassword,
                                password,
                                registerTime
                        ) {
                            println("net.scr0pt.MegaRegisterEnterPasswordAfterEnterConfirmLinkPage finish")
                        },
                        MegaChooseAccTypePage() {
                            println("net.scr0pt.MegaChooseAccTypePage finish")
                        },
                        MegaDownloadAppPage() {
                            println("net.scr0pt.MegaDownloadAppPage finish")
                        },
                        MegaGetRecoverKeyPage() {
                            println("net.scr0pt.MegaGetRecoverKeyPage finish")
                            Thread.sleep(10000)
                            getRecoverKey()?.let { recoverKey ->
                                //update recover key
                                println("recoverKey: $recoverKey")

                            }
                        },
                        MegaRecoverKeyDownloadedPage() {
                            println("net.scr0pt.MegaRecoverKeyDownloadedPage finish")
                        },
                        MegaCloudDrivePage() {
                            println("net.scr0pt.MegaCloudDrivePage finish")
                        }
                ),
                Browser.firefox,
                "https://mega.nz/register"
        )

        megaRegisterPage.run { response ->
            println(response)
            Thread.sleep(20000)
            megaRegisterPage.driver.close()
            Thread.sleep(600000)
        }
    }
}

class MegaRegisterPage(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    onPageFinish: (() -> Unit)? = null
) : Page(onPageFinish = onPageFinish) {
    override fun isEndPage() = false

    override fun _action(driver: WebDriver): PageResponse {
        println(this::class.java.simpleName + ": action")
        val firstNameInputs = driver.findElWait(100, 5000, "input#register-firstname-registerpage2", jsoup = false)
        val lastNameInputs = driver.findElWait(100, 5000, "input#register-lastname-registerpage2", jsoup = false)
        val emailInputs = driver.findElWait(100, 5000, "input#register-email-registerpage2", jsoup = false)
        val passwordInputs = driver.findElWait(100, 5000, "input#register-password-registerpage2", jsoup = false)
        val repasswordInputs = driver.findElWait(100, 5000, "input#register-password-registerpage3", jsoup = false)

        //I understand that if I lose my password, I may lose my data. Read more about MEGA’s end-to-end encryption.
        val checkCheckBoxs =
            driver.findElWait(100, 5000, ".checkbox-block.pw-remind .understand-check input.checkboxOff", jsoup = false)
        //I agree with the MEGA Terms of Service
        val registerCheckCheckBoxs = driver.findElWait(100, 5000, "input#register-check-registerpage2", jsoup = false)
        val registerBtns = driver.findElWait(100, 5000, "form#register_form .register-button.active", jsoup = false)
        return if (firstNameInputs.isEmpty() || lastNameInputs.isEmpty() || emailInputs.isEmpty() || passwordInputs.isEmpty() || checkCheckBoxs.isEmpty() || registerCheckCheckBoxs.isEmpty()) {
            PageResponse.NOT_FOUND_ELEMENT()
        } else {
            firstNameInputs.first().sendKeys(firstName)
            lastNameInputs.first().sendKeys(lastName)
            emailInputs.first().sendKeys(email)
            passwordInputs.first().sendKeys(password)
            repasswordInputs.first().sendKeys(password)
            checkCheckBoxs.first().click()
            registerCheckCheckBoxs.first().click()
            Thread.sleep(500)
            registerBtns.first().click()
            PageResponse.WAITING_FOR_RESULT()
        }
    }

    override fun _detect(doc: Document, currentUrl: String, title: String): Boolean =
        currentUrl.startsWith("https://mega.nz/register") &&
                doc.selectFirst("form#register_form .account.top-header.wide")?.text() == "Create your free account" &&
                MegaRegisterConfirmEmailPage("", "", 0L)._detect(doc, currentUrl, title) == false
}

class MegaRegisterConfirmEmailPage(
    private val gmailUsername: String,
    private val gmailPassword: String,
    private val registerTime: Long,
    onPageFinish: (() -> Unit)? = null
) : Page(onPageFinish = onPageFinish) {
    override fun isEndPage() = false

    override fun _action(driver: WebDriver): PageResponse {
        println(this::class.java.simpleName + ": action")

        Gmail(gmailUsername, gmailPassword)?.apply {
            onEvent(
                    MailReceiveEvent(
                            key = "ona1sender",
                            validator = { mail ->
                                Mail.CompareType.EQUAL_IGNORECASE.compare(mail.from, "welcome@mega.nz")
                            },
                            callback = { mails ->
                                val mail = mails.firstOrNull {
                                    (it.id
                                            ?: 0) > registerTime && it.contentDocumented?.selectFirst("a[href^='https://mega.nz/#confirm']#bottom-button") != null
                                }
                                val timestamp = mail?.id
                                println(timestamp)
                                mail?.contentDocumented?.selectFirst("a[href^='https://mega.nz/#confirm']#bottom-button")
                                        ?.attr("href")?.let { confirmLink ->
                                            driver.get(confirmLink)
                                            this.logout()
                                        }
                            },
                            once = false,
                            new = true,
                            fetchContent = true
                    )
            )
        }

        return PageResponse.WAITING_FOR_RESULT()
    }

    override fun _detect(doc: Document, currentUrl: String, title: String): Boolean =
        currentUrl.startsWith("https://mega.nz/register") &&
                doc.selectFirst(".registration-page-success.special .reg-success-special .reg-success-txt")?.text()?.trim() == "Please check your email and click the link to confirm your account." &&
                doc.selectFirst(".reg-resend-button-bl .resend-email-button")?.text()?.trim() == "Resend"
}

class MegaRegisterEnterPasswordAfterEnterConfirmLinkPage(
    val gmailUsername: String,
    val gmailPassword: String,
    val password: String,
    val registerTime: Long,
    onPageFinish: (() -> Unit)? = null
) : Page(onPageFinish = onPageFinish) {
    override fun isEndPage() = false

    override fun _action(driver: WebDriver): PageResponse {
        println(this::class.java.simpleName + ": action")
        val doc = driver.document

        val notiText = doc?.selectFirst(".net.scr0pt.main-top-info-block .net.scr0pt.main-top-info-text")?.text()
        if (notiText == "Please enter your password to confirm your account.") {
            if (doc?.selectFirst("#login_form .account.top-header.login")?.text()?.trim() == "Confirm your account" &&
                doc?.selectFirst("#login_form .big-red-button.login-button.button")?.text()?.trim() == "Confirm your account"
            ) {
                val passwordInputs = driver.findElWait(100, 5000, "input#login-password2", jsoup = false)
                val confirmBtns =
                    driver.findElWait(100, 5000, "#login_form .big-red-button.login-button.button", jsoup = false)
                return if (passwordInputs.isEmpty() || confirmBtns.isEmpty()) {
                    PageResponse.NOT_FOUND_ELEMENT()
                } else {
                    passwordInputs.first()?.sendKeys(password)
                    confirmBtns.first()?.click()
                    return PageResponse.WAITING_FOR_RESULT()
                }
            }
        } else if (notiText == "Your confirmation link is no longer valid. Your account may already be activated or you may have cancelled your registration.") {
            Gmail(gmailUsername, gmailPassword)?.apply {
                onEvent(
                        MailReceiveEvent(
                                key = "ona1sender",
                                validator = { mail ->
                                    Mail.CompareType.EQUAL_IGNORECASE.compare(mail.from, "welcome@mega.nz")
                                },
                                callback = { mails ->
                                    val mail = mails.firstOrNull {
                                        (it.id
                                                ?: 0) > registerTime && it.contentDocumented?.selectFirst("a[href^='https://mega.nz/#confirm']#bottom-button") != null
                                    }
                                    val timestamp = mail?.id
                                    println(timestamp)
                                    mail?.contentDocumented?.selectFirst("a[href^='https://mega.nz/#confirm']#bottom-button")
                                            ?.attr("href")?.let { confirmLink ->
                                                driver.get(confirmLink)
                                                this.logout()
                                            }
                                },
                                once = false,
                                new = true,
                                fetchContent = true
                        )
                )
            }

            return PageResponse.WAITING_FOR_RESULT()
        }
        return PageResponse.WAITING_FOR_RESULT()
    }

    override fun _detect(doc: Document, currentUrl: String, title: String): Boolean =
        currentUrl.startsWith("https://mega.nz/confirm") && doc.selectFirst(".net.scr0pt.main-top-info-block .net.scr0pt.main-top-info-text")?.text()?.isNotEmpty() == true
                && doc.selectFirst(".fm-dialog.warning-dialog-a .fm-notification-info h1") == null
}

class MegaChooseAccTypePage(
    onPageFinish: (() -> Unit)? = null
) : Page(onPageFinish = onPageFinish) {
    override fun isEndPage() = false

    override fun _action(driver: WebDriver): PageResponse {
        println(this::class.java.simpleName + ": action")
        val freeMemberships =
            driver.findElWait(100, 5000, ".key .plans .reg-st3-membership-bl.free .membership-pad-bl", jsoup = false)
        if (freeMemberships.isEmpty()) {
            return PageResponse.NOT_FOUND_ELEMENT()
        } else {
            driver.waitUmtil(1000, 30000, { driver ->
                driver.findElement(By.className("loading-info")).isDisplayed == false
            })
            freeMemberships.first().click()
            return PageResponse.WAITING_FOR_RESULT()
        }
    }

    override fun _detect(doc: Document, currentUrl: String, title: String): Boolean =
        currentUrl.startsWith("https://mega.nz/pro") &&
                doc.selectFirst(".bottom-page.top-info .big-header")?.text()?.trim() == "Choose your account type"
}

class MegaGetRecoverKeyPage(
    onPageFinish: (() -> Unit)? = null
) : Page(onPageFinish = onPageFinish) {
    override fun isEndPage() = false

    override fun _action(driver: WebDriver): PageResponse {
        println(this::class.java.simpleName + ": action")
        val downloadKeyBtns =
            driver.findElWait(
                100,
                5000,
                ".improved-recovery-steps .recover-paste-block .right-section > div:not(.hidden)"
            )
        if (downloadKeyBtns.isEmpty()) {
            return PageResponse.NOT_FOUND_ELEMENT()
        } else {
            downloadKeyBtns.first().click()
            return PageResponse.WAITING_FOR_RESULT()
        }
    }

    override fun _detect(doc: Document, currentUrl: String, title: String): Boolean =
        currentUrl.startsWith("https://mega.nz/fm/") &&
                doc.selectFirst(".post-register .step-net.scr0pt.main-question.post-register")?.text()?.trim() == "Here is your Recovery Key!" &&
                doc.selectFirst(".improved-recovery-steps .recover-paste-block .right-section > div:not(.hidden)")?.text()?.trim() == "Download key" &&
                !doc.selectFirst(".fm-dialog.recovery-key-dialog.backup-recover.improved-recovery-steps.post-register").hasClass(
                    "hidden"
                )
}

class MegaRecoverKeyDownloadedPage(
    onPageFinish: (() -> Unit)? = null
) : Page(onPageFinish = onPageFinish) {
    override fun isEndPage() = false

    override fun _action(driver: WebDriver): PageResponse {
        println(this::class.java.simpleName + ": action")
        val closeDialogBtns =
            driver.findElWait(100, 5000, ".content-wrapper .default-green-button.close-dialog")
        if (closeDialogBtns.isEmpty()) {
            return PageResponse.NOT_FOUND_ELEMENT()
        } else {
            closeDialogBtns.first().click()
            return PageResponse.WAITING_FOR_RESULT()
        }
    }

    override fun _detect(doc: Document, currentUrl: String, title: String): Boolean =
        currentUrl.startsWith("https://mega.nz/fm/") &&
                doc.selectFirst(".fm-dialog-header .fm-dialog-title.top-pad")?.text()?.trim() == "Account Recovery" &&
                doc.selectFirst(".content-wrapper .default-green-button.close-dialog")?.text()?.trim() == "OK" &&
                !doc.selectFirst(".fm-dialog.recovery-key-info.improved-recovery-steps").hasClass("hidden")
}

class MegaDownloadAppPage(
    onPageFinish: (() -> Unit)? = null
) : Page(onPageFinish = onPageFinish) {
    override fun isEndPage() = false

    override fun _action(driver: WebDriver): PageResponse {
        println(this::class.java.simpleName + ": action")
        val skipThisStepBtns =
            driver.findElWait(100, 5000, ".button-wrappers .redirect-clouddrive-link")
        if (skipThisStepBtns.isEmpty()) {
            return PageResponse.NOT_FOUND_ELEMENT()
        } else {
            skipThisStepBtns.first().click()
            return PageResponse.WAITING_FOR_RESULT()
        }
    }

    override fun _detect(doc: Document, currentUrl: String, title: String): Boolean =
        currentUrl.startsWith("https://mega.nz/downloadapp") &&
                doc.selectFirst(".download-app")?.text()?.trim() == "Download the MEGA App" &&
                doc.selectFirst(".button-wrappers .redirect-clouddrive-link")?.text() == "Skip this step"
}

class MegaCloudDrivePage(
    onPageFinish: (() -> Unit)? = null
) : Page(onPageFinish = onPageFinish) {
    override fun isEndPage() = true

    override fun _action(driver: WebDriver): PageResponse {
        println(this::class.java.simpleName + ": action")
        return PageResponse.OK()
    }

    override fun _detect(doc: Document, currentUrl: String, title: String): Boolean =
        currentUrl.startsWith("https://mega.nz/fm/") &&
                doc.selectFirst(".cloud-drive .nw-fm-tree-header.cloud-drive input")?.attr("placeholder")?.trim() == "Cloud Drive" &&
                doc.selectFirst("#how-to-upload .dropdown.hint-info-block .dropdown.hint-header")?.text()?.trim() == "How to upload"
}


fun getRecoverKey(): String? {
    val fileName = "C:\\Users\\Administrator\\Downloads\\MEGA-RECOVERYKEY.txt"
    val myFile = File(fileName)
    if (myFile.exists()) {
        val recoverKey = myFile.readLines()?.first()
        myFile.delete()
        return recoverKey
    }
    return null
}