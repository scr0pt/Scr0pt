package utils


class InfinityMail(val originMail: String) {
    data class GenerateInfinityMail(val mail: String, var isScan: Boolean = false)

    val list = arrayListOf(GenerateInfinityMail(originMail, false))

    private fun generateMail(email: String) {
        val existMail = list.firstOrNull { it.mail == email }
        if (existMail?.isScan == true) return
        else if(existMail != null) list.removeIf { it.mail == email }
        list.add(GenerateInfinityMail(email, true))

        for (j in 1 until email.length) {
            if (!(j > 1 && email[j - 1].toString() == ".") && !(email[j].toString() == ".")) {
                val mynewEmail = email.substring(0, j) + "." + email.substring(j)
                if (list.firstOrNull { it.mail == mynewEmail } == null) {
                    list.add(GenerateInfinityMail(mynewEmail))
                }
            }
        }
    }

    fun getNext() {
        list.forEach {
            if (!it.isScan) {
                generateMail(it.mail)
                return@getNext
            }
        }
    }
}