package utils.tempmail

import org.jsoup.nodes.Element
import utils.tempmail.event.MailReceiveEvent
import utils.tempmail.event.onReceiveMails
import utils.tempmail.models.Mail
import java.util.*

abstract class GenericMail(val onInnitSuccess: ((GenericMail) -> Unit)?, val onInitFail: (() -> Unit)?) {
    val inbox = arrayListOf<Mail>()
    var timeUpdate = 5000L//milisecond
    var schedule: Timer? = null
    private var events = arrayListOf<MailReceiveEvent>()
    fun onEvent(event: MailReceiveEvent) {
        events.removeIf { it.key == event.key }
        events.add(event)
    }
    var emailAddress: String? = null

    init {
        Thread(Runnable {
            if (init()) {
                onInnitSuccess?.let { it(this) }
                startSchedule()
            } else {
                onInitFail?.let { it() }
            }
        }).start()
    }

    fun startSchedule(){
        schedule = Timer().also { timer ->
            timer.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    onUpdateInbox()
                }
            }, 0, timeUpdate)
        }
    }

    abstract fun init(): Boolean

    abstract fun updateInbox(): List<Mail>?

    fun onUpdateInbox() {
        println("onUpdateInbox")
        updateInbox()?.let { newInboxs ->
            val filter =
                newInboxs.filter { newInbox -> inbox.none { it.id != null && newInbox.id != null && it.id == newInbox.id } }//new inbox mails that not in old inbox mails
            if (filter.isNotEmpty()) {
                inbox.addAll(filter)
                events.onReceiveMails(filter) { mail ->
                    this.getMailContent(mail)
                }
            }
        }
    }

    abstract fun getMailContent(mail: Mail): Element?
}