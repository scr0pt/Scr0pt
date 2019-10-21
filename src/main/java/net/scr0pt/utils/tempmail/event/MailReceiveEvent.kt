package net.scr0pt.utils.tempmail.event

import net.scr0pt.utils.tempmail.models.Mail

/**
 * Created by Long
 * Date: 10/3/2019
 * Time: 10:58 PM
 */
class MailReceiveEvent(
        val key: String,
        val validator: (Mail) -> Boolean,//filter inboxs
        private val callback: (List<Mail>) -> Unit,
        val once: Boolean = true,//only run callback once
        val fetchContent: Boolean = false,
        val new: Boolean = true//only get new emails
) {
    var done: Boolean = false//is this MailReceiveEvent called
    private val mails = arrayListOf<Mail>()//list mails that go throught validator
    val runCallback = { mails: List<Mail> ->
        done = true

        if (mails.isNotEmpty()) {
            val list = mails.filter { m -> this.mails.none { it.id == m.id } }//list of mails that not in this.mails
            this.mails.addAll(list)

            if (new) {
                if (list.isNotEmpty()) callback(list)
            } else {
                callback(mails)
            }
        }
    }
}