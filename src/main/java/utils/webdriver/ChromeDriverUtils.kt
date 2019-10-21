package utils.webdriver

import net.lingala.zip4j.ZipFile
import org.jsoup.Jsoup
import utils.curl.NetworkUtils
import java.io.File

fun main(){
    println(ChromeDriverUtils.getChromeDriver())
}

object ChromeDriverUtils {
    val DOWNLOAD_FOLDER = System.getProperty("user.dir") + File.separator + "DOWNLOAD" + File.separator
    val CHROMEDRIVER_DOWNLOAD_FOLDER = DOWNLOAD_FOLDER + "ChromeDriver" + File.separator
    val CHROMEDRIVER_ZIP_File =
        CHROMEDRIVER_DOWNLOAD_FOLDER + (CHROMEDRIVER_ZIP_File_URL?.split("/")?.last() ?: "ChromeDriver.ZIPFILE")
    val CHROMEDRIVER_EXE_FILE =
        CHROMEDRIVER_DOWNLOAD_FOLDER + (if (OSUtils.isWindows()) "chromedriver.exe" else "chromedriver")

    fun getLatestReleaseVersion(): String? =
        Jsoup.connect("https://chromedriver.storage.googleapis.com/LATEST_RELEASE").execute().body()

    var _CHROMEDRIVER_ZIP_File_URL: String? = null
    val CHROMEDRIVER_ZIP_File_URL: String?
        get() {
            if (_CHROMEDRIVER_ZIP_File_URL == null) {
                val latestReleaseVersion = getLatestReleaseVersion() ?: return null
                _CHROMEDRIVER_ZIP_File_URL = when {
                    OSUtils.isWindows() -> "https://chromedriver.storage.googleapis.com/$latestReleaseVersion/chromedriver_win32.zip"
                    OSUtils.isLinux() -> "https://chromedriver.storage.googleapis.com/$latestReleaseVersion/chromedriver_linux64.zip"
                    OSUtils.isMac() -> "https://chromedriver.storage.googleapis.com/$latestReleaseVersion/chromedriver_mac64.zip"
                    else -> null
                }
            }
            return _CHROMEDRIVER_ZIP_File_URL
        }


    fun getChromeDriver(): Boolean {
        //check if file exist
        if (File(CHROMEDRIVER_EXE_FILE).exists()) {
            return true
        }

        CHROMEDRIVER_ZIP_File_URL ?: return false

        if (!File(CHROMEDRIVER_ZIP_File).exists() && !NetworkUtils.downloadRemoteFile(
                CHROMEDRIVER_ZIP_File,
                CHROMEDRIVER_ZIP_File_URL!!
            )
        ) return false
        ZipFile(CHROMEDRIVER_ZIP_File).extractAll(CHROMEDRIVER_DOWNLOAD_FOLDER)
        return File(CHROMEDRIVER_EXE_FILE).exists()
    }


}