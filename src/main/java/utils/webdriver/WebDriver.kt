package utils.webdriver

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement

/**
 * Created by Long
 * Date: 10/18/2019
 * Time: 9:15 PM
 */


val WebDriver.document: Document?
    get() = Jsoup.parse(pageSource)

val WebDriver.currentUrlTrim: String?
    get() = currentUrl.removeSuffix("/")

fun WebDriver.executeAsyncScript(js: String): Any {
    return (this as JavascriptExecutor).executeAsyncScript(js)
}

fun WebDriver.executeScript(js: String): Any {
    return (this as JavascriptExecutor).executeScript(js)
}

fun WebDriver.findElWait(
    interval: Long,
    maxTime: Long,
    selector: String,
    jsoup: Boolean = true,
    filter: (WebElement) -> Boolean = { _ -> true }
): ArrayList<WebElement> {
    var waitTime = 0L
    while (waitTime < maxTime) {
        val els = this.findEl(selector, jsoup = jsoup, filter = filter)
        if (els.isNotEmpty()) {
            return els
        }
        waitTime += interval
        Thread.sleep(interval)
    }
    return arrayListOf()
}

fun WebDriver.findFirstElWait(
    interval: Long,
    maxTime: Long,
    selector: String,
    jsoup: Boolean = true,
    filter: (WebElement) -> Boolean = { _ -> true }
): WebElement? {
    return this.findElWait(interval, maxTime, selector, jsoup, filter = filter).firstOrNull()
}

fun WebDriver.waitUntilUrlChange(interval: Long, maxTime: Long) {
    val url = this.currentUrl
    var waitTime = 0L
    while (waitTime < maxTime) {
        if (url != this.currentUrl) return
        waitTime += interval
        Thread.sleep(interval)
    }
}

fun WebDriver.waitUntilUrlChangeTo(interval: Long, maxTime: Long, url: String) {
    var waitTime = 0L
    while (waitTime < maxTime) {
        if (url == this.currentUrlTrim) return
        waitTime += interval
        Thread.sleep(interval)
    }
}

fun WebDriver.waitUmtilDismiss(interval: Long, maxTime: Long, selector: String) {
    var waitTime = 0L
    while (waitTime < maxTime) {
        val findEl = this.findEl(selector)
        if (findEl.isEmpty()) {
            return
        }
        waitTime += interval
        Thread.sleep(interval)
    }
}

fun WebDriver.waitUmtil(interval: Long, maxTime: Long, onDone: (driver: WebDriver) -> Boolean) {
    var waitTime = 0L
    while (waitTime < maxTime) {
        if (onDone(this)) {
            return
        }
        waitTime += interval
        Thread.sleep(interval)
    }
}

fun WebDriver.findEl(
    selector: String,
    jsoup: Boolean = true,
    filter: (WebElement) -> Boolean = { _ -> true }
): ArrayList<WebElement> {
    val arr = arrayListOf<WebElement>()
    try {
        if (selector.isNotEmpty()) {
            if (jsoup) {
                Jsoup.parse(pageSource)?.select(selector)?.forEach { el ->
                    this.findElement(By.cssSelector(el.cssSelector()))?.let {
                        if(filter(it)) arr.add(it)
                    }
                }
            } else {
                this.findElements(By.cssSelector(selector))?.forEach {
                    if(filter(it)) arr.add(it)
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return arr
}