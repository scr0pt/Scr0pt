/*
package thirdservice.imgur

import anigoo_standard.dao.ImgurAccDao
import anigoo_standard.entity.Entity
import anigoo_standard.entity.ImgurAcc
import org.openqa.selenium.Cookie
import org.openqa.selenium.WebDriver
import untils.db.DB
import untils.db.model.Record
import untils.db.model.Records
import untils.webdriver.DriverControl
import untils.webdriver.MyDriver

*/
/**
 * Created by Long
 * Date: 2/27/2019
 * Time: 12:54 AM
 *//*

object GetImgurCookie {
    @JvmStatic
    fun main(args: Array<String>) {
        val db = DB.getAnigoo("anigoo_standard")
        while (db.select("SELECT * FROM anigoo_standard.imgur_accounts where cookie is null order by rand() limit 10")) {
            val result = db.getResult()
            if (result != null) {
                for (record in result!!) {
                    val driver = MyDriver.chromeIncognito()
                    val driverControl = DriverControl(driver)
                    driver.get("https://imgur.com/signin")
                    val imgurAcc = Entity.fromData(record, ImgurAcc::class.java)
                    driverControl.sendKey("input#username", imgurAcc.getEmail())
                    driverControl.sendKey("input#password", imgurAcc.getPass())

                    val currentUrl = driver.getCurrentUrl()
                    driverControl.click("button[type=\"submit\"]")
                    driverControl.sleepUntilUrlChange(currentUrl)
                    val cookies = driver.manage().getCookies()
                    val cookiesStr = StringBuilder()
                    for (cookie in cookies) {
                        cookiesStr.append(cookie.getName()).append("=").append(cookie.getValue()).append("; ")
                    }
                    imgurAcc.setCookie(cookiesStr.toString().trim { it <= ' ' })

                    driver.quit()
                    ImgurAccDao(imgurAcc).insert()
                }
            }
        }
    }
}
*/
