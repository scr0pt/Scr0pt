package net.scr0pt.bot.google

import net.scr0pt.bot.Page
import net.scr0pt.bot.PageResponse
import org.jsoup.nodes.Document
import org.openqa.selenium.WebDriver
import net.scr0pt.utils.webdriver.findElWait


class LoginEnterEmailPage(val email: String, onPageFinish: (() -> Unit)? = null) : Page(onPageFinish = onPageFinish) {
    override fun isEndPage() = false
    override fun watingResult(doc: Document, currentUrl: String, title: String): PageResponse? {
        if (doc.selectFirst(".LXRPh .GQ8Pzc")?.text() == "Không thể tìm thấy Tài khoản Google của bạn") {
            return PageResponse.NOT_FOUND_EMAIL()
        }
        return null
    }

    override fun _action(driver: WebDriver): PageResponse {
        println(this::class.java.simpleName + ": action")
        val emailInputs = driver.findElWait(100, 5000, "input#identifierId[type=\"email\"]")
        if (emailInputs.isEmpty()) {
            return PageResponse.NOT_FOUND_ELEMENT()
        } else {
            emailInputs.first().sendKeys(email)
            val nextBtns = driver.findElWait(100, 5000, "div#identifierNext[role=\"button\"]")
            if (nextBtns.isEmpty()) {
                return PageResponse.NOT_FOUND_ELEMENT()
            } else {
                nextBtns.first().click()
                return PageResponse.WAITING_FOR_RESULT()
            }
        }
    }


    override fun _detect(doc: Document, currentUrl: String, title: String): Boolean {
        val headingText = doc.selectFirst("#headingText")?.text() ?: return false
        val headingSubtext = doc.selectFirst("#headingSubtext")?.text() ?: return false
        val emailInput = doc.selectFirst("input#identifierId[type=\"email\"]") ?: return false

        when (headingText) {
            "Chào mừng" -> {
                return headingSubtext == ""
            }
            "Đăng nhập" -> {
                return headingSubtext == "Sử dụng Tài khoản Google của bạn"
            }
            else -> return false
        }
    }
}

class LoginEnterPasswordPage(val password: String, onPageFinish: (() -> Unit)? = null) :
    Page(onPageFinish = onPageFinish) {
    override fun _detect(doc: Document, currentUrl: String, title: String): Boolean {
        val headingText = doc.selectFirst("#headingText")?.text() ?: return false
        val forgotPassword = doc.selectFirst("#forgotPassword")?.text() ?: return false
//        val headingSubtext = doc.selectFirst("#headingSubtext")?.text() ?: return false
//        val passwordInput = doc.selectFirst("input[type=\"password\"]") ?: return false
//        if (passwordInput.attr("name") == "hiddenPassword") {
//            return false
//        }

        if (forgotPassword != "Bạn quên mật khẩu?") {
            return false
        } else if (headingText == "Chào mừng" || headingText == "Đăng nhập") {
            return true
        } else return false
    }

    override fun watingResult(doc: Document, currentUrl: String, title: String): PageResponse? {
        if (doc.selectFirst("[jsname=\"B34EJ\"]")?.text()?.startsWith("Mật khẩu không chính xác.") ?: false) {
            return PageResponse.INCORECT_PASSWORD()//incorect password
        } else if (doc.selectFirst(".Xk3mYe.Jj6Lae .xgOPLd")?.text()?.startsWith("Mật khẩu của bạn đã thay đổi")
                ?: false
        ) {
            return PageResponse.PASSWORD_CHANGED()//incorect password
        }
        return null
    }

    override fun isEndPage() = false

    override fun _action(driver: WebDriver): PageResponse {
        println(this::class.java.simpleName + ": action")
        val passwordInputs = driver.findElWait(100, 5000, "input[name=\"password\"]")
        if (passwordInputs.isEmpty()) {
            return PageResponse.NOT_FOUND_ELEMENT()
        } else {
            passwordInputs.first().clear()
            passwordInputs.first().sendKeys(password)
            val nextBtns = driver.findElWait(100, 5000, "div[role=\"button\"]#passwordNext")
            if (nextBtns.isEmpty()) {
                return PageResponse.NOT_FOUND_ELEMENT()
            } else {
                nextBtns.first().click()
                return PageResponse.WAITING_FOR_RESULT()
            }
        }
    }

}

class WellcomeToNewAccount(onPageFinish: (() -> Unit)? = null) : Page(onPageFinish = onPageFinish) {
    override fun _detect(doc: Document, currentUrl: String, title: String): Boolean {
        val wellcomeTitle = doc.selectFirst(".glT6eb h1")?.text() ?: return false
        return wellcomeTitle == "Chào mừng bạn đến với tài khoản mới"
    }

    override fun isEndPage() = false

    override fun _action(driver: WebDriver): PageResponse {
        println(this::class.java.simpleName + ": action")
        val acceptBtns = driver.findElWait(100, 5000, "input#accept[name=\"accept\"]")
        if (acceptBtns.isEmpty()) {
            return PageResponse.NOT_FOUND_ELEMENT()
        } else {
            acceptBtns.first().click()
            return PageResponse.WAITING_FOR_RESULT()
        }
    }
}

class ChangePasswordFirstTime(val newPassword: String, onPageFinish: (() -> Unit)? = null) :
    Page(onPageFinish = onPageFinish) {
    override fun _detect(doc: Document, currentUrl: String, title: String): Boolean {
        val wellcomeTitle = doc.selectFirst(".glT6eb h1")?.text() ?: return false
        return wellcomeTitle.startsWith("Thay đổi mật khẩu cho ")
    }

    override fun isEndPage() = false

    override fun _action(driver: WebDriver): PageResponse {
        println(this::class.java.simpleName + ": action")
        val passwordBtns = driver.findElWait(100, 5000, "input#Password")
        if (passwordBtns.isEmpty()) {
            return PageResponse.NOT_FOUND_ELEMENT()
        } else {
            passwordBtns.first().clear()
            passwordBtns.first().sendKeys(newPassword)
            val confirmPasswordBtns = driver.findElWait(100, 5000, "input#ConfirmPassword")
            if (confirmPasswordBtns.isEmpty()) {
                return PageResponse.NOT_FOUND_ELEMENT()
            } else {
                confirmPasswordBtns.first().clear()
                confirmPasswordBtns.first().sendKeys(newPassword)
                val submitBtns = driver.findElWait(100, 5000, "input#submit")
                if (submitBtns.isEmpty()) {
                    return PageResponse.NOT_FOUND_ELEMENT()
                } else {
                    submitBtns.first().click()
                    return PageResponse.WAITING_FOR_RESULT()
                }
            }
        }
    }
}

class EnterPasswordFirstTimeChanged(val newPassword: String, onPageFinish: (() -> Unit)? = null) :
    Page(onPageFinish = onPageFinish) {
    override fun _detect(doc: Document, currentUrl: String, title: String): Boolean {
        val wellcomeTitle = doc.selectFirst(".dMArKd .ck6P8")?.text() ?: return false
        return wellcomeTitle.startsWith("Để tiếp tục, trước tiên, hãy xác minh danh tính của bạn")
    }

    override fun isEndPage() = false

    override fun _action(driver: WebDriver): PageResponse {
        println(this::class.java.simpleName + ": action")
        val passwordBtns = driver.findElWait(100, 5000, "input[name=\"password\"]")
        if (passwordBtns.isEmpty()) {
            return PageResponse.NOT_FOUND_ELEMENT()
        } else {
            passwordBtns.first().clear()
            passwordBtns.first().sendKeys(newPassword)
            val submitBtns = driver.findElWait(100, 5000, "div#passwordNext")
            if (submitBtns.isEmpty()) {
                return PageResponse.NOT_FOUND_ELEMENT()
            } else {
                submitBtns.first().click()
                return PageResponse.WAITING_FOR_RESULT()
            }
        }
    }

}

class ProtectYourAccount(
        val defaultAction: DEFAULT_ACTION = DEFAULT_ACTION.UPDATE,
        onPageFinish: (() -> Unit)? = null
) : Page(onPageFinish = onPageFinish) {
    enum class DEFAULT_ACTION {
        UPDATE, DONE
    }

    override fun _detect(doc: Document, currentUrl: String, title: String): Boolean {
        return doc.selectFirst("#headingText") == null &&
                doc.selectFirst("#headingSubtext") == null &&
                doc.selectFirst(".mkCr7e .N4lOwd")?.text() == "Bảo vệ tài khoản của bạn"
    }

    override fun isEndPage() = false

    override fun _action(driver: WebDriver): PageResponse {
        println(this::class.java.simpleName + ": action ~ $defaultAction")
        return when (defaultAction) {
            DEFAULT_ACTION.UPDATE -> actionUpdate(driver)
            DEFAULT_ACTION.DONE -> actionDone(driver)
        }
    }

    fun actionUpdate(driver: WebDriver): PageResponse {
        val updateBtns = driver.findElWait(100, 5000, ".n6Gm2e div[role=\"button\"].U26fgb")
        if (updateBtns.isEmpty()) {
            return PageResponse.NOT_FOUND_ELEMENT()
        } else {
            updateBtns.first().click()
            return PageResponse.WAITING_FOR_RESULT()
        }
    }

    fun actionDone(driver: WebDriver): PageResponse {
        val doneBtns = driver.findElWait(100, 5000, ".yKBrKe div[role=\"button\"].U26fgb")
        if (doneBtns.isEmpty()) {
            return PageResponse.NOT_FOUND_ELEMENT()
        } else {
            doneBtns.first().click()
            return PageResponse.WAITING_FOR_RESULT()
        }
    }
}

class ProtectYourAccountUpdatePhone(onPageFinish: (() -> Unit)? = null) : Page(onPageFinish = onPageFinish) {
    override fun _detect(doc: Document, currentUrl: String, title: String): Boolean {
        return doc.selectFirst("#headingText") == null &&
                doc.selectFirst("#headingSubtext") == null &&
                doc.selectFirst(".mkCr7e .N4lOwd")?.text() == "Xác minh số điện thoại của bạn"
    }

    override fun isEndPage() = false

    override fun _action(driver: WebDriver): PageResponse {
        println(this::class.java.simpleName + ": action")
        val nextBtns = driver.findElWait(100, 5000, ".n6Gm2e > a:nth-child(1)")
        if (nextBtns.isEmpty()) {
            return PageResponse.NOT_FOUND_ELEMENT()
        } else {
            nextBtns.first().click()
            return PageResponse.WAITING_FOR_RESULT()
        }
    }

}

class ProtectYourAccountUpdateRecoverEmail(val recoverEmail: String, onPageFinish: (() -> Unit)? = null) :
    Page(onPageFinish = onPageFinish) {
    override fun _detect(doc: Document, currentUrl: String, title: String): Boolean {
        return doc.selectFirst("#headingText") == null &&
                doc.selectFirst("#headingSubtext") == null &&
                doc.selectFirst(".mkCr7e .N4lOwd")?.text() == "Thêm email khôi phục"
    }

    override fun isEndPage() = false

    override fun _action(driver: WebDriver): PageResponse {
        println(this::class.java.simpleName + ": action")
        val emailInputs = driver.findElWait(100, 5000, "input.whsOnd")
        if (emailInputs.isEmpty()) {
            return PageResponse.NOT_FOUND_ELEMENT()
        } else {
            emailInputs.first().sendKeys(recoverEmail)
            val nextBtns = driver.findElWait(100, 5000, ".yKBrKe .U26fgb")
            if (nextBtns.isEmpty()) {
                return PageResponse.NOT_FOUND_ELEMENT()
            } else {
                nextBtns.first().click()
                return PageResponse.WAITING_FOR_RESULT()
            }
        }
    }

}

class ProtectYourAccountUpdateRecoverEmailSuccess(onPageFinish: (() -> Unit)? = null) :
    Page(onPageFinish = onPageFinish) {
    override fun _detect(doc: Document, currentUrl: String, title: String): Boolean {
        return doc.selectFirst("#headingText") == null &&
                doc.selectFirst("#headingSubtext") == null &&
                doc.selectFirst(".mkCr7e .N4lOwd")?.text() == "Thành công!"
    }

    override fun isEndPage() = false

    override fun _action(driver: WebDriver): PageResponse {
        println(this::class.java.simpleName + ": action")
        val nextBtns = driver.findElWait(100, 5000, ".yKBrKe div[role=\"button\"].U26fgb")
        if (nextBtns.isEmpty()) {
            return PageResponse.NOT_FOUND_ELEMENT()
        } else {
            nextBtns.first().click()
            return PageResponse.WAITING_FOR_RESULT()
        }
    }

}

class GoogleSearch(onPageFinish: (() -> Unit)? = null) : Page(onPageFinish = onPageFinish) {
    override fun _detect(doc: Document, currentUrl: String, title: String): Boolean {
        return doc.selectFirst("#net.scr0pt.main input[type=\"search\"][name=\"q\"]") != null ||
                doc.selectFirst("#searchform input[autocomplete=\"off\"][name=\"q\"]") != null
    }

    override fun isEndPage() = true

    override fun _action(driver: WebDriver): PageResponse {
        println(this::class.java.simpleName + ": action")
        return PageResponse.WAITING_FOR_RESULT()
    }
}

class VerifyItsYou(onPageFinish: (() -> Unit)? = null) : Page(onPageFinish = onPageFinish) {
    override fun _detect(doc: Document, currentUrl: String, title: String): Boolean {
        val headingText = doc.selectFirst("#headingText")?.text() ?: return false
        return "Xác minh đó là bạn" == headingText
    }

    override fun isEndPage() = true

    override fun _action(driver: WebDriver): PageResponse {
        println(this::class.java.simpleName + ": action")
        return PageResponse.WAITING_FOR_RESULT()
    }
}

class CanotLoginForYou(onPageFinish: (() -> Unit)? = null) : Page(onPageFinish = onPageFinish) {
    override fun _detect(doc: Document, currentUrl: String, title: String): Boolean {
        val headingText = doc.selectFirst("#headingText")?.text() ?: return false
        return "Không thể đăng nhập cho bạn" == headingText
    }

    override fun isEndPage() = true

    override fun _action(driver: WebDriver): PageResponse {
        println(this::class.java.simpleName + ": action")
        return PageResponse.WAITING_FOR_RESULT()
    }
}


class AccountDisable(onPageFinish: (() -> Unit)? = null) : Page(onPageFinish = onPageFinish) {
    override fun _detect(doc: Document, currentUrl: String, title: String): Boolean {
        val headingText = doc.selectFirst("#headingText")?.text() ?: return false
        return "Đã vô hiệu hóa tài khoản" == headingText
    }

    override fun isEndPage() = true

    override fun _action(driver: WebDriver): PageResponse {
        //RveJvd snByac Cố gắng khôi phục
        println(this::class.java.simpleName + ": action")
        return PageResponse.WAITING_FOR_RESULT()
    }
}
