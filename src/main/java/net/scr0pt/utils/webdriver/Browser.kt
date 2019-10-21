package net.scr0pt.utils.webdriver

import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions


object Browser {
    val firefox: FirefoxDriver
        get() {
            if (GeckoUtils.getGeckoDriver()) {
                System.setProperty("webdriver.gecko.driver", GeckoUtils.GECKODRIVER_EXE_FILE);
            } else {
                println("Cant get getko driver")
            }
            return FirefoxDriver()
        }

    val chrome: ChromeDriver
        get() {
            //file config phai dat o desktop
            val options = ChromeOptions()
            options.addArguments("--start-maximized")
            options.addArguments("--ignore-certificate-errors")
            options.addArguments("--disable-popup-blocking")
            options.addArguments("disable-infobars") //disable chrome is being controlled by automated test software
//        options.addArguments("user-data-dir=" + Config.getInstance().get("chrome_profile"))
            if (ChromeDriverUtils.getChromeDriver()) {
                System.setProperty("webdriver.chrome.driver", ChromeDriverUtils.CHROMEDRIVER_EXE_FILE)
            }
            return ChromeDriver(options)
        }


}
