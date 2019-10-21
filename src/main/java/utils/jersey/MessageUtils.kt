package utils.jersey

import net.minidev.json.JSONObject
import net.minidev.json.JSONStyle
import javax.ws.rs.core.Response

/**
 * Created by Long
 * Date: 10/14/2019
 * Time: 12:29 AM
 */

object MessageUtils {
    fun echoErrorMessage(message: String = "Error", result: Any? = null): Response {
        return echoMessage(message, result, Response.Status.INTERNAL_SERVER_ERROR)
    }
    fun echoSuccessMessage(message: String = "OK", result: Any? = null): Response {
        return echoMessage(message, result, Response.Status.OK)
    }

    fun echoMessage(message: String = "OK", result: Any? = null, httpCode: Response.Status = Response.Status.OK): Response {
        val responseBuilder = Response.status(httpCode)

        val data = JSONObject()
            .appendField("status", httpCode.statusCode)
            .appendField("message", message)
            .appendField("result", result).toJSONString(JSONStyle(JSONStyle.FLAG_IGNORE_NULL))
        return responseBuilder.entity(data).build()
    }

}