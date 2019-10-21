package thirdservice.mlab

import bot.MlabPageResponse
import bot.Page
import bot.PageManager
import bot.PageResponse
import bot.google.GoogleSearch
import bot.google.LoginEnterEmailPage
import bot.google.LoginEnterPasswordPage
import bot.google.ProtectYourAccount
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import org.jsoup.nodes.Document
import org.openqa.selenium.WebDriver
import utils.FakeProfile
import utils.InfinityMail
import utils.webdriver.Browser
import utils.webdriver.findElWait

/**
 * Created by Long
 * Date: 10/15/2019
 * Time: 11:07 AM
 */

suspend fun main() {
    val mongoClient =
        MongoClients.create("mongodb+srv://root:mongo@cluster0-enhmy.mongodb.net/admin?retryWrites=true&w=majority")
    val serviceAccountDatabase = mongoClient.getDatabase("mlab")
    val collection: MongoCollection<org.bson.Document> = serviceAccountDatabase.getCollection("mlab-account")
    val infinityMail = InfinityMail("tranvananh.200896")
    for (i in 0..100)
        infinityMail.getNext()
    for (iMail in infinityMail.list) {
        val email = iMail.mail + "@gmail.com"
        if (collection.countDocuments(
                org.bson.Document(
                    "email",
                    email
                )
            ) == 0L && email != "t.ranvananh.2.00.896@gmail.com"
        ) {
            val result = FakeProfile.getNewProfile()
            val first = result?.name?.first ?: "Bruce"
            val last = result?.name?.last ?: "Lee"
            println(email)
            println(first)
            println(last)
            registerMlab(
                email = email,
                firstName = first,
                lastName = last,
                driver = Browser.chrome,
                collection = collection
            )
        }
    }
}


suspend fun loginGoogle(email: String, password: String, driver: WebDriver, onLoginSuccess: suspend () -> Unit) {
    val googlePageManager = PageManager(
        arrayListOf<Page>(
            LoginEnterEmailPage(email) {
                println("enter email success")
            },
            LoginEnterPasswordPage(password) {
                println("enter password success")
            },
            ProtectYourAccount(defaultAction = ProtectYourAccount.DEFAULT_ACTION.DONE) {
                println("ProtectYourAccount success")
            },
            GoogleSearch {
                println("GoogleSearch success")
            }
        ),
        driver,
        "https://accounts.google.com/signin/v2/identifier?hl=vi&passive=true&continue=https%3A%2F%2Fwww.google.com%2F&flowName=GlifWebSignIn&flowEntry=ServiceLogin"
    )

    googlePageManager.generalWatingResult = { jsoupDoc, currentUrl ->
        if ((jsoupDoc.selectFirst("img#captchaimg")?.attr("src")?.length ?: 0) > 5) {
            PageResponse.RECAPTCHA()
        } else PageResponse.WAITING_FOR_RESULT()
    }

    googlePageManager.run { pageResponse ->
        if (pageResponse is PageResponse.OK) {
            onLoginSuccess()
        } else {
            driver.close()
        }
    }
}

suspend fun registerMlab(
    email: String,
    firstName: String,
    lastName: String,
    driver: WebDriver,
    collection: MongoCollection<org.bson.Document>
) {
    val password = "XinChaoVietnam"

    val pageManager = PageManager(
        arrayListOf<Page>(
            TryMongoDBAtlasPage(email, password, firstName, lastName) {
                collection.insertOne(
                    org.bson.Document("email", email)
                        .append("password", password).append("firstName", firstName).append("lastName", lastName)
                )
                println("register success")
            },
            BuildClusterPage() {
                println("BuildClusterPage success")
            }
        ),
        driver,
        "https://www.mongodb.com/atlas-signup-from-mlab?utm_source=mlab.com&utm_medium=referral&utm_campaign=mlab%20signup&utm_content=blue%20sign%20up%20button"
    )
    pageManager.run { pageResponse ->
        println(pageResponse)
        Thread.sleep(60000)
        driver.close()
        Thread.sleep(180000)
    }
}

suspend fun loginMlab(driver: WebDriver, collection: MongoCollection<org.bson.Document>) {
    val email = "v.a.n.a.n.ngu.y.en.0.8.3@gmail.com"
    val password = "XinChaoVietnam"
    val firstName = "Bruce"
    val lastName = "Lee"
    val db_username = "root"
    val db_password = "mongo"

    val pageManager = PageManager(
        arrayListOf<Page>(
            TryMongoDBAtlasPage(email, password, firstName, lastName) {
                collection.insertOne(
                    org.bson.Document("email", email)
                        .append("password", password).append("firstName", firstName).append("lastName", lastName)
                )
                println("register success")
            },
            BuildClusterPage() {
                println("BuildClusterPage success")
            },
            CreateClusterTypePage {
                println("BuildClusterPage success")
            },
            ClusterCreatingPage() {
                println("ClusterCreatingPage success")
            },
            CreatingDatabaseUserPage() {
                println("CreatingDatabaseUserPage success")
            },
            AddNewUserPage(db_username, db_password) {
                println("AddNewUserPage success")
            },
            CreatingDatabaseUserDonePage() {
                println("CreatingDatabaseUserDonePage success")
            },
            NetworkAccessPage() {
                println("NetworkAccessPage success")
            },
            NetworkAccessAddWhitelistPage {
                println("NetworkAccessAddWhitelistPage success")
            },
            NetworkAccessAddWhitelistDonePage {
                println("NetworkAccessAddWhitelistDonePage success")
            }
        ),
        driver,
        "https://www.mongodb.com/atlas-signup-from-mlab?utm_source=mlab.com&utm_medium=referral&utm_campaign=mlab%20signup&utm_content=blue%20sign%20up%20button"
    )
    pageManager.run { pageResponse ->
        when (pageResponse) {
            is MlabPageResponse.LOGIN_ERROR -> println(pageResponse.msg)
        }

        println(pageResponse)
    }
}

class TryMongoDBAtlasPage(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    onPageFinish: (() -> Unit)? = null
) : Page(onPageFinish = onPageFinish) {
    override fun isEndPage() = false

    override fun watingResult(doc: Document, currentUrl: String, title: String): PageResponse? {
        val selectFirst = doc.selectFirst("div.form-error")
        if (selectFirst != null && selectFirst.attr("style")?.contains("display: none;") == false) {
            return MlabPageResponse.LOGIN_ERROR(msg = selectFirst.text())
        }
        return null
    }

    override fun _action(driver: WebDriver): PageResponse {
        println(this::class.java.simpleName + ": action")
        val emailInputs = driver.findElWait(100, 5000, "input#email")
        val firstNameInputs = driver.findElWait(100, 5000, "input#first_name")
        val lastNameInputs = driver.findElWait(100, 5000, "input#last_name")
        val passwordInputs = driver.findElWait(100, 5000, "input#password")
        val atlasCheckboxInputs = driver.findElWait(100, 5000, "input#atlasCheckbox")
        val submitBtns = driver.findElWait(100, 5000, "input#atlas-submit-btn")
        return if (emailInputs.isEmpty() || firstNameInputs.isEmpty() || lastNameInputs.isEmpty() || passwordInputs.isEmpty() || atlasCheckboxInputs.isEmpty() || submitBtns.isEmpty()) {
            PageResponse.NOT_FOUND_ELEMENT()
        } else {
            emailInputs.first().sendKeys(email)
            firstNameInputs.first().sendKeys(firstName)
            lastNameInputs.first().sendKeys(lastName)
            passwordInputs.first().sendKeys(password)
            atlasCheckboxInputs.first().click()
            submitBtns.first().click()
            PageResponse.WAITING_FOR_RESULT()
        }
    }

    override fun _detect(doc: Document, currentUrl: String, title: String): Boolean =
        doc.selectFirst("h1.txt-center")?.text() == "Try MongoDB Atlas"
}

class BuildClusterPage(
    onPageFinish: (() -> Unit)? = null
) : Page(onPageFinish = onPageFinish) {
    override fun isEndPage() = true

    override fun _action(driver: WebDriver): PageResponse {
        println(this::class.java.simpleName + ": action")
        val submitBtns =
            driver.findElWait(100, 5000, ".path-selector-door-footer-starter .path-selector-door-submit", jsoup = false)
//            driver.findElWait(100, 5000, ".path-selector-door-hourly-price-free ~ .path-selector-door-submit")
        return if (submitBtns.isEmpty()) {
            PageResponse.NOT_FOUND_ELEMENT()
        } else {
            submitBtns.first().click()
            PageResponse.WAITING_FOR_RESULT()
        }
    }

    override fun _detect(doc: Document, currentUrl: String, title: String): Boolean {
        return currentUrl.startsWith("https://cloud.mongodb.com") &&
                doc.selectFirst("span.path-selector-header-title")?.text() == "MONGODB ATLAS" &&
                doc.selectFirst("span.path-selector-header-main-text")?.text() == "Choose a path. Adjust anytime."
    }
}

class CreateClusterTypePage(
    onPageFinish: (() -> Unit)? = null
) : Page(onPageFinish = onPageFinish) {
    override fun isEndPage() = false

    override fun _action(driver: WebDriver): PageResponse {
        println(this::class.java.simpleName + ": action")
        val submitBtns = driver.findElWait(100, 5000, "button[type=\"button\"]:containsOwn(Create Cluster)")
        return if (submitBtns.isEmpty()) {
            PageResponse.NOT_FOUND_ELEMENT()
        } else {
            submitBtns.first().click()
            PageResponse.WAITING_FOR_RESULT()
        }
    }

    override fun _detect(doc: Document, currentUrl: String, title: String): Boolean {
        return currentUrl.startsWith("https://cloud.mongodb.com") &&
                doc.selectFirst("header.editor-layout-header h1 strong")?.text() == "Create a Starter Cluster" &&
                doc.selectFirst("button[type=\"button\"]:containsOwn(Create Cluster)") != null
    }
}

class ClusterCreatingPage(
    onPageFinish: (() -> Unit)? = null
) : Page(onPageFinish = onPageFinish) {
    override fun isEndPage() = false

    override fun _action(driver: WebDriver): PageResponse {
        println(this::class.java.simpleName + ": action")
        val databaseAccessBtns = driver.findElWait(100, 5000, ".left-nav a:containsOwn(Database Access)")
        return if (databaseAccessBtns.isEmpty()) {
            PageResponse.NOT_FOUND_ELEMENT()
        } else {
            databaseAccessBtns.first().click()
            PageResponse.WAITING_FOR_RESULT()
        }
    }

    override fun _detect(doc: Document, currentUrl: String, title: String): Boolean {
        return currentUrl.startsWith("https://cloud.mongodb.com") &&
                doc.selectFirst(".nds-sparkline-empty-header")?.text() == "Your cluster is being created"
    }
}

class CreatingDatabaseUserPage(
    onPageFinish: (() -> Unit)? = null
) : Page(onPageFinish = onPageFinish) {
    override fun isEndPage() = false

    override fun _action(driver: WebDriver): PageResponse {
        println(this::class.java.simpleName + ": action")
        val databaseAccessBtns = driver.findElWait(100, 5000, ".section-controls-is-end-justified .button-is-primary")
        return if (databaseAccessBtns.isEmpty()) {
            PageResponse.NOT_FOUND_ELEMENT()
        } else {
            databaseAccessBtns.first().click()
            PageResponse.WAITING_FOR_RESULT()
        }
    }

    override fun _detect(doc: Document, currentUrl: String, title: String): Boolean {
        return currentUrl.startsWith("https://cloud.mongodb.com") && currentUrl.endsWith("database/users") &&
                doc.selectFirst(".empty-view-text-is-heading")?.text() == "Create a database user" &&
                doc.selectFirst(".section-controls-is-end-justified .button-is-primary")?.text() == "Add New User" &&
                doc.selectFirst("button[name=\"deleteUser\"]") == null
    }
}

class AddNewUserPage(
    val username: String,
    val password: String,
    onPageFinish: (() -> Unit)? = null
) : Page(onPageFinish = onPageFinish) {
    override fun isEndPage() = false

    override fun _action(driver: WebDriver): PageResponse {
        println(this::class.java.simpleName + ": action")
        val usernameInputs = driver.findElWait(100, 5000, "input[name=\"user\"]")
        val passwordInputs = driver.findElWait(100, 5000, "input[name=\"password\"]")
        val submitBtns = driver.findElWait(100, 5000, "button[type=\"submit\"]:containsOwn(Add User)")
        return if (usernameInputs.isEmpty() || passwordInputs.isEmpty() || submitBtns.isEmpty()) {
            PageResponse.NOT_FOUND_ELEMENT()
        } else {
            usernameInputs.first().sendKeys(username)
            passwordInputs.first().sendKeys(password)
            submitBtns.first().click()
            PageResponse.WAITING_FOR_RESULT()
        }
    }

    override fun _detect(doc: Document, currentUrl: String, title: String): Boolean {
        return currentUrl.startsWith("https://cloud.mongodb.com") && currentUrl.endsWith("database/users") &&
                doc.selectFirst(".nds-edit-modal-footer-checkbox-description")?.text() == "Save as temporary user" &&
                doc.selectFirst("h3.view-modal-header-title")?.text() == "Add New User"
    }
}


class CreatingDatabaseUserDonePage(
    onPageFinish: (() -> Unit)? = null
) : Page(onPageFinish = onPageFinish) {
    override fun isEndPage() = false

    override fun _action(driver: WebDriver): PageResponse {
        println(this::class.java.simpleName + ": action")
        val networkAccessBtns = driver.findElWait(100, 5000, ".left-nav a:containsOwn(Network Access)")
        return if (networkAccessBtns.isEmpty()) {
            PageResponse.NOT_FOUND_ELEMENT()
        } else {
            networkAccessBtns.first().click()
            PageResponse.WAITING_FOR_RESULT()
        }
    }

    override fun _detect(doc: Document, currentUrl: String, title: String): Boolean {
        return currentUrl.startsWith("https://cloud.mongodb.com") && currentUrl.endsWith("database/users") &&
                doc.selectFirst(".empty-view-text-is-heading") == null &&
                doc.selectFirst(".section-controls-is-end-justified .button-is-primary")?.text() == "Add New User" &&
                doc.selectFirst("button[name=\"deleteUser\"]")?.text() == "Delete"
    }
}

class NetworkAccessPage(
    onPageFinish: (() -> Unit)? = null
) : Page(onPageFinish = onPageFinish) {
    override fun isEndPage() = false

    override fun _action(driver: WebDriver): PageResponse {
        println(this::class.java.simpleName + ": action")
        val addIpAddressBtns = driver.findElWait(100, 5000, ".section-controls-is-end-justified .button-is-primary")
        return if (addIpAddressBtns.isEmpty()) {
            PageResponse.NOT_FOUND_ELEMENT()
        } else {
            addIpAddressBtns.first().click()
            PageResponse.WAITING_FOR_RESULT()
        }
    }

    override fun _detect(doc: Document, currentUrl: String, title: String): Boolean {
        return currentUrl.startsWith("https://cloud.mongodb.com") && currentUrl.endsWith("network/whitelist") &&
                doc.selectFirst("h1.section-header-title")?.text() == "Network Access" &&
                doc.selectFirst(".section-controls-is-end-justified .button-is-primary")?.text() == "Add IP Address"
    }
}

class NetworkAccessAddWhitelistPage(
    onPageFinish: (() -> Unit)? = null
) : Page(onPageFinish = onPageFinish) {
    override fun isEndPage() = false

    override fun _action(driver: WebDriver): PageResponse {
        println(this::class.java.simpleName + ": action")
        val allowAccessAnywhereBtns = driver.findElWait(100, 5000, "button[name=\"allowAccessAnywhere\"]")
        val confirmBtns = driver.findElWait(100, 5000, "button[name=\"confirm\"]")
        return if (allowAccessAnywhereBtns.isEmpty() || confirmBtns.isEmpty()) {
            PageResponse.NOT_FOUND_ELEMENT()
        } else {
            allowAccessAnywhereBtns.first().click()
            confirmBtns.first().click()
            PageResponse.WAITING_FOR_RESULT()
        }
    }

    override fun _detect(doc: Document, currentUrl: String, title: String): Boolean {
        return currentUrl.startsWith("https://cloud.mongodb.com") && currentUrl.endsWith("/network/whitelist/addToWhitelist") &&
                doc.selectFirst("header.view-modal-header h3.view-modal-header-title")?.text() == "Add Whitelist Entry"
    }
}

class NetworkAccessAddWhitelistDonePage(
    onPageFinish: (() -> Unit)? = null
) : Page(onPageFinish = onPageFinish) {
    override fun isEndPage() = false

    override fun _action(driver: WebDriver): PageResponse {
        println(this::class.java.simpleName + ": action")
        val clustersBtns = driver.findElWait(100, 5000, ".left-nav a:containsOwn(Clusters)")
        return if (clustersBtns.isEmpty()) {
            PageResponse.NOT_FOUND_ELEMENT()
        } else {
            clustersBtns.first().click()
            PageResponse.WAITING_FOR_RESULT()
        }
    }

    override fun _detect(doc: Document, currentUrl: String, title: String): Boolean {
        return currentUrl.startsWith("https://cloud.mongodb.com") && currentUrl.endsWith("network/whitelist") &&
                doc.selectFirst("td.plain-table-cell")?.text() == "0.0.0.0/0 (includes your current IP address)"
    }
}
