package net.scr0pt.thirdservice.openload


/**
 * Created by Long
 * Date: 2/22/2019
 * Time: 11:38 AM
 */
class OPAcc {
    var cookie: String? = null
    private var xCsrfToken: String? = null

    fun getxCsrfToken(): String? {
        return xCsrfToken
    }

    fun setxCsrfToken(xCsrfToken: String) {
        this.xCsrfToken = xCsrfToken
    }
}
