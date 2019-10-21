package utils.javamail

import org.apache.commons.mail.DefaultAuthenticator
import org.apache.commons.mail.SimpleEmail
import java.util.Properties
import javax.mail.Folder
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.Session
import javax.mail.Store
import javax.mail.URLName

fun main() {
//    SendMailSSL.sendMail()

    val mailService = MailService()
    mailService.login(
        "imap.gmail.com", MailConfig.APP_EMAIL,
        MailConfig.APP_PASSWORD
    )
    var messageCount = mailService.messageCount
    if (messageCount > 5)
        messageCount = 5
    val messages = mailService.messages
    for (i in 0 until messageCount) {
        var subject = ""
        if (messages[i].getSubject() != null)
            subject = messages[i].getSubject()
        val fromAddress = messages[i].getFrom()

        println(subject)
        println(fromAddress)
    }

}


object MailConfig {
    val HOST_NAME = "smtp.gmail.com"
    val SSL_PORT = 465 // Port for SSL
    val TSL_PORT = 587 // Port for TLS/STARTTLS
    val IMAP_PORT = 993 // Port for IMAP
    val APP_EMAIL = "vinhnguyen4h4@gmail.com" // your email
    val APP_PASSWORD = "eHK;HyL.e=2k1704FgqN" // your password
    val RECEIVE_EMAIL = "animebentojp@gmail.com"
}

object SendMailSSL {
    fun sendMail() {
        val email = SimpleEmail()
        email.hostName = MailConfig.HOST_NAME
        email.setSmtpPort(MailConfig.SSL_PORT)
        email.setAuthenticator(DefaultAuthenticator(MailConfig.APP_EMAIL, MailConfig.APP_PASSWORD))
        email.isSSLOnConnect = true
        email.setFrom(MailConfig.APP_EMAIL)
        email.addTo(MailConfig.RECEIVE_EMAIL)
        email.subject = "Testing Subject"
        email.setMsg("Hello World")
        email.send()
        println("Message sent successfully")
    }
}


class MailService {
    private var session: Session? = null
    private var store: Store? = null
    private var folder: Folder? = null

    // hardcoding protocol and the folder
    // it can be parameterized and enhanced as required
    private val protocol = "imaps"
    private val file = "INBOX"

    val isLoggedIn: Boolean
        get() = store!!.isConnected

    val messageCount: Int
        get() {
            var messageCount = 0
            try {
                messageCount = folder!!.messageCount
            } catch (me: MessagingException) {
                me.printStackTrace()
            }

            return messageCount
        }

    val messages: Array<Message>
        @Throws(MessagingException::class)
        get() = folder!!.messages

    /**
     * to login to the mail host server
     */
    @Throws(Exception::class)
    fun login(host: String, username: String, password: String) {
        val url = URLName(protocol, host, MailConfig.IMAP_PORT, file, username, password)

        if (session == null) {
            var props: Properties? = null
            try {
                props = System.getProperties()
            } catch (sex: SecurityException) {
                props = Properties()
            }

            session = Session.getInstance(props!!, null)
        }
        store = session!!.getStore(url)
        store!!.connect()
        folder = store!!.getFolder(url)

        folder!!.open(Folder.READ_WRITE)
    }

    /**
     * to logout from the mail host server
     */
    @Throws(MessagingException::class)
    fun logout() {
        folder!!.close(false)
        store!!.close()
        store = null
        session = null
    }

}