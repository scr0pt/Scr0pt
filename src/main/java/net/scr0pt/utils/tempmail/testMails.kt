package net.scr0pt.utils.tempmail

import net.scr0pt.utils.tempmail.event.MailReceiveEvent
import net.scr0pt.utils.tempmail.models.Mail
import net.scr0pt.utils.tempmail.models.getMail

fun main() {
    testOnMailReceive()
}

fun testOnMailReceive() {
    val tempmailaddress = Tempmailaddress(onInnitSuccess = { tempmailaddress ->
        println(tempmailaddress.emailAddress)
    })

    val onAnimebentojpSender = MailReceiveEvent(
            key = "ona1sender",
            validator = { mail -> Mail.CompareType.SUFFIX_IGNORECASE.compare(mail.from, "gmail.com") },
            callback = { it.forEach(::println) },
            once = false,
            new = true,
            fetchContent = true
    )

    tempmailaddress.onEvent(onAnimebentojpSender)
}

fun testFilterMail() {
    val mails = arrayListOf<Mail>()
    mails.add(Mail(from = "a1@gmail.com", to = "a2@gmail.com", content = "hello world"))
    mails.add(Mail(from = "a1@gmail.com", to = "a3@gmail.com", content = "hello world 2"))
    mails.add(Mail(from = "a2@gmail.com", to = "a3@gmail.com", content = "good morning"))
    mails.add(Mail(from = "a1@gmail.com", to = "a4@gmail.com", content = "i don't care"))

    mails.getMail(query = "A1", propertyType = Mail.PropertyType.FROM, compareType = Mail.CompareType.PREFIX_IGNORECASE)
        .forEach {
            println(it)
        }
}