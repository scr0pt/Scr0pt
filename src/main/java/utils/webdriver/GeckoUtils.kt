package utils.webdriver

import net.lingala.zip4j.ZipFile
import org.apache.commons.io.FileUtils
import org.jsoup.Jsoup
import utils.curl.NetworkUtils
import java.io.File
import java.net.URL

fun main() {
    print(GeckoUtils.getGeckoDriver())
}

object GeckoUtils {
    val DOWNLOAD_FOLDER = System.getProperty("user.dir") + File.separator + "DOWNLOAD" + File.separator
    val GECKODRIVER_DOWNLOAD_FOLDER = DOWNLOAD_FOLDER + "GeckoDriver" + File.separator
    var _GECKODRIVER_ZIP_File_URL: String? = null
    val GECKODRIVER_ZIP_File_URL: String?
        get() {
            if (_GECKODRIVER_ZIP_File_URL == null) {
                val latestReleaseVersion = getLatestReleaseVersion() ?: return null
                _GECKODRIVER_ZIP_File_URL = when {
                    OSUtils.isWindows() && OSUtils.OSModel == "32" -> "https://github.com/mozilla/geckodriver/releases/download/$latestReleaseVersion/geckodriver-$latestReleaseVersion-win32.zip"
                    OSUtils.isWindows() && OSUtils.OSModel == "64" -> "https://github.com/mozilla/geckodriver/releases/download/$latestReleaseVersion/geckodriver-$latestReleaseVersion-win64.zip"
                    OSUtils.isLinux() && OSUtils.OSModel == "32" -> "https://github.com/mozilla/geckodriver/releases/download/$latestReleaseVersion/geckodriver-$latestReleaseVersion-linux32.tar.gz"
                    OSUtils.isLinux() && OSUtils.OSModel == "64" -> "https://github.com/mozilla/geckodriver/releases/download/$latestReleaseVersion/geckodriver-$latestReleaseVersion-linux64.tar.gz"
                    OSUtils.isMac() -> "https://github.com/mozilla/geckodriver/releases/download/$latestReleaseVersion/geckodriver-$latestReleaseVersion-macos.tar.gz"
                    else -> null
                }
            }
            return _GECKODRIVER_ZIP_File_URL
        }

    val GECKODRIVER_ZIP_File =
        GECKODRIVER_DOWNLOAD_FOLDER + (GECKODRIVER_ZIP_File_URL?.split("/")?.last() ?: "GeckoDriver.ZIPFILE")
    val GECKODRIVER_EXE_FILE =
        GECKODRIVER_DOWNLOAD_FOLDER + (if (OSUtils.isWindows()) "geckodriver.exe" else "geckodriver")


    fun getLatestReleaseVersion(): String? =
        Jsoup.connect("https://github.com/mozilla/geckodriver/releases/latest").execute().url()?.path?.split("/")?.last()


    /*
    * Download Gecko Driver (Version: v0.24.0) for curent OS environment
    * @return null if fail or absolute path String of the file location
    * */
    fun getGeckoDriver(): Boolean {
        //check if file exist
        if (File(GECKODRIVER_EXE_FILE).exists()) {
            return true
        }

        GECKODRIVER_ZIP_File_URL ?: return false

        if (!File(GECKODRIVER_ZIP_File).exists() && !NetworkUtils.downloadRemoteFile(
                GECKODRIVER_ZIP_File,
                GECKODRIVER_ZIP_File_URL!!
            )
        ) return false
        ZipFile(GECKODRIVER_ZIP_File).extractAll(GECKODRIVER_DOWNLOAD_FOLDER)
        return File(GECKODRIVER_EXE_FILE).exists()
    }
}