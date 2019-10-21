package utils.tempmail

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.jsoup.nodes.Element
import utils.tempmail.models.Mail
import java.lang.reflect.Type;

class Tempmailaddress(onInnitSuccess: ((Tempmailaddress) -> Unit)? = null, onInitFail: (() -> Unit)? = null) : TempMail(
    url = "https://www.tempmailaddress.com",
    onInnitSuccess = onInnitSuccess as ((GenericMail) -> Unit)?,
    onInitFail = onInitFail
) {
    init {
        with(curl) {
            header("Accept", "application/json, text/javascript, */*; q=0.01")
            header("Sec-Fetch-Mode", "cors")
            header("X-Requested-With", "XMLHttpRequest")
            userAgent(
                "Mozilla/5.0 (iPad; CPU OS 11_0 like Mac OS X) AppleWebKit/604.1.34 (KHTML, like Gecko) Version/11.0 Mobile/15A5341f Safari/604.1",
                isHard = true
            )
            referrer("https://www.tempmailaddress.com/", isHard = true)
        }
    }

    override fun updateInbox(): List<Mail>? {
        val body = curl("https://www.tempmailaddress.com/index/refresh")?.body ?: return null
        val typeToken = object : TypeToken<List<UpdateInboxResponse>>() {}
        val collectionType: Type = typeToken.type
        val list = try {
            Gson().fromJson<List<UpdateInboxResponse>>(body, collectionType)
        } catch (e: Exception) {
            arrayListOf<UpdateInboxResponse>()
        }
        list ?: return null
        val mails = arrayListOf<Mail>()
        for (inboxResponse in list) {
            val from = inboxResponse.od.split("<")[1].split(">")[0]
            mails.add(Mail(from = from, to = this.emailAddress, subject = inboxResponse.predmet).also {
                it.id = inboxResponse.id
            })
        }
        return mails
    }

    override fun getMailContent(mail: Mail): Element? {
        val body = curl(
            "https://www.tempmailaddress.com/email/id/${mail.id}",
            referer = "https://www.tempmailaddress.com/window/id/${mail.id}"
        ) ?: return null
        return body.doc?.selectFirst("body > div[dir='ltr']")
    }

    override fun initMailAdress(): String? {
        val body = curl("https://www.tempmailaddress.com/index/index")?.body ?: return null
        val parse = try {
            Gson().fromJson(body, InitMailAdressResponse::class.java)
        } catch (e: Exception) {
            null
        }
        return parse?.email
    }

    data class InitMailAdressResponse(val email: String, val heslo: String)
    data class UpdateInboxResponse(
        val akce: String,
        val id: Long,
        val kdy: String,
        val od: String,
        val precteno: String,
        val predmet: String,
        val predmetZkraceny: String
    )
}

