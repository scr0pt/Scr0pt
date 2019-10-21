package net.scr0pt.utils.tempmail

import net.scr0pt.utils.curl.LongConnection
import net.scr0pt.utils.curl.LongConnectionLocalStorage
import net.scr0pt.utils.curl.LongResponse


abstract class TempMail(val url: String, onInnitSuccess: ((GenericMail) -> Unit)?, onInitFail: (() -> Unit)?) :
    GenericMail(onInnitSuccess, onInitFail) {
    val localStorage = LongConnectionLocalStorage(fileName = "TempMalConnection")

    var curl = LongConnection()

    override fun init(): Boolean {
        val emailAddress = initMailAdress()
        return if (emailAddress != null) {
            this.emailAddress = emailAddress
            true
        } else {
            false
        }
    }

    //get email adress initial time
    abstract fun initMailAdress(): String?

    fun curl(url: String, referer: String? = null): LongResponse? {
        referer?.let {
            curl.referrer(referer)
        }
        val response = curl.execute(url)
        println(this.javaClass.simpleName + " curl: " + response?.body)
        localStorage.save(curl)
        return response
    }

}