package utils.curl

import org.apache.commons.io.FileUtils
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.io.File
import java.io.IOException
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object NetworkUtils {
    fun getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(p0: Array<out java.security.cert.X509Certificate>?, p1: String?) {
                }

                override fun checkServerTrusted(p0: Array<out java.security.cert.X509Certificate>?, p1: String?) {
                }

                override fun getAcceptedIssuers(): Array<out java.security.cert.X509Certificate>? {
                    return arrayOf()
                }

            })

            // Install the all-trusting trust manager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            // Create an ssl socket factory with our all-trusting manager
            val sslSocketFactory = sslContext.getSocketFactory()

            HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory)
            HttpsURLConnection.setDefaultHostnameVerifier { hostname, session -> true }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    fun followRedirect(con: Connection? = null, url: String?): String? {
        url ?: return null
        var _con = con
        if (_con == null) {
            _con = Jsoup.connect(url).ignoreContentType(true).ignoreHttpErrors(true).followRedirects(true)
        }
        return try {
            _con?.execute()?.url()?.toString()
        } catch (ex: IOException) {
            null
        }
    }

    fun getFileSize(url: String): Long {
        val connection = Jsoup.connect(url).method(Connection.Method.HEAD).ignoreContentType(true).followRedirects(true)
            .ignoreContentType(true)
        val response: Connection.Response?
        try {
            response = connection.execute()
        } catch (ex: Exception) {
            return -1
        }

        response?.header("Content-Length")?.let {
            return try {
                it.toLong()
            } catch (e: Exception) {
                -1
            }
        }
        return -1

    }

    fun downloadRemoteFile(filePath: String, fileUrl: String): Boolean {
        println("Downloading remote file $filePath")
        try {
            val desFile = File(filePath)
            FileUtils.copyURLToFile(
                URL(fileUrl),
                desFile
            )
            return true
        } catch (e: Exception) {
            return false
        }
    }
}