package net.scr0pt.utils.tempmail.event

import org.jsoup.nodes.Element
import net.scr0pt.utils.tempmail.models.Mail

/**
 * Created by Long
 * Date: 10/3/2019
 * Time: 11:00 PM
 */

fun ArrayList<MailReceiveEvent>.onReceiveMails(newMails: List<Mail>, getMailContent: (mail: Mail) -> Element?) {
    val removeEvent = { registrationMail: MailReceiveEvent -> registrationMail.done && registrationMail.once }

    this.forEach { registrationMail ->
        val filter = newMails.filter { !removeEvent(registrationMail) && registrationMail.validator(it) }
        if (filter.isNotEmpty()) {
            if (registrationMail.fetchContent) {
                filter.forEach { mail ->
                    if (mail.contentDocumented == null) {
                        getMailContent(mail)?.let { mail.contentDocumented = it }
                    }
                }
            }
            registrationMail.runCallback(filter)
        }
    }

    this.removeIf { removeEvent(it) }
}
