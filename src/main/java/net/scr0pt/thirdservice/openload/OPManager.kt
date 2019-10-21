/*
package thirdservice.openload

import com.google.gson.Gson
import net.minidev.json.JSONArray
import net.minidev.json.JSONObject
import net.minidev.json.JSONValue
import org.apache.commons.lang3.StringUtils
import org.jsoup.Connection
import org.jsoup.Jsoup
import net.scr0pt.Convert
import net.scr0pt.File
import net.scr0pt.OpenloadAcc
import net.scr0pt.RemoteUploadStatus
import net.scr0pt.FileInfo_Response
import net.scr0pt.GetJobs_Response
import net.scr0pt.GetSplash_Response
import net.scr0pt.RemoteUploadStatus_Response
import net.scr0pt.MyString
import net.scr0pt.LongConnection
import java.sql.Timestamp
import java.util.*

*/
/**
 * Created by Long
 * Date: 2/21/2019
 * Time: 8:12 PM
 *//*

class OPManager {
    private var acc: OpenloadAcc? = null
    private val conn = LongConnection().apply {
        acc?.cookie?.let { header("cookie", it, isHard = true) }
        header("origin", "https://openload.co", isHard = true)
        referrer("https://openload.co/account", isHard = true)
        userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.109 Safari/537.36")
        header("content-type","application/x-www-form-urlencoded; charset=UTF-8", method = LongConnection.REQUEST_METHOD.POST)
        acc?.xCsrfToken?.let { header("x-csrf-token", it) }
        header("x-requested-with", "XMLHttpRequest")
    }

    private val token: String? =
        conn.get("https://openload.co/account")?.doc?.selectFirst("meta[name='csrf-token']")?.attr("content")

    val files2: List<File>
        get() {
            val files = ArrayList<File>()
            val body = conn.get("https://openload.co/filemanager/getfiles2")?.body ?: return files
            val split = body.split("\"name\":\"".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            for (s in split) {
                val name = StringUtils.substringBefore(s, ".mp4\"")
                val file = File()
                file.name = name
                file.sha1 = MyString.textBetween(s, "\"sha1\":\"", "\"")
                file.folderid = MyString.textBetween(s, "\"folderid\":\"", "\"")
                file.size = MyString.textBetween(s, "\"size\":\"", "\"")
                file.link = MyString.textBetween(s, "\"link\":\"", "\"")
                file.linkextid = MyString.textBetween(s, "\"linkextid\":\"", "\"")

                if (file.link != null && file.linkextid != null) {
                    files.add(file)
                }
            }
            return files
        }

    private val remoteUploads: List<RemoteUploadStatus>
        get() {
            try {
                val body = conn.get("https://openload.co/filemanager/getremoteuploads")?.body ?: return arrayListOf()
                val bodyObj = validBody(body) as JSONObject? ?: return arrayListOf()
                val remoteUploadStatus_response =
                    gson.fromJson(bodyObj!!.toJSONString(), RemoteUploadStatus_Response::class.java)
                if (remoteUploadStatus_response.success == 1) {
                    val data = remoteUploadStatus_response.data
                    if (data != null) {
                        for (status in data) {
                            println(status.id)
                        }
                        return data
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return arrayListOf()
        }

    private val jobs: List<Convert>
        get() {
            try {
                val body = conn.get("https://openload.co/filemanager/getjobs")?.body ?: return arrayListOf()
                val bodyObj = validBody(body) as JSONObject? ?: return arrayListOf()
                val getJobs_response = gson.fromJson(bodyObj.toJSONString(), GetJobs_Response::class.java)
                if (getJobs_response.success == 1) {
                    val converts = getJobs_response.converts
                    if (converts != null) {
                        for (convert in converts) {
                            println(convert.id)
                            println(convert.link)
                            println(convert.name)
                            println("----------------")
                        }
                        return converts
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return arrayListOf()
        }

    constructor(acc: OpenloadAcc) {
        this.acc = acc
        this.acc!!.setxCsrfToken(token)
    }

    constructor(acc: OpenloadAcc, proxy: Proxy) {
        this.acc = acc
        this.proxy = proxy
    }

    fun updateThumbtoDB() {
        val db = DB.getAnigoo("anigoo_standard")

        val files2 = files2
        for (openloadFile in files2) {
            openloadFile = getFileInfo(openloadFile!!.linkextid)
            if (openloadFile == null) {
                continue
            }

            println(openloadFile.name)
            println(openloadFile.link)


            var opFile = OPFile()
            opFile.setFolderid(java.lang.Long.valueOf(openloadFile.folderid!!))
            opFile.setLink(openloadFile.link)
            opFile.setLinkextid(openloadFile.linkextid)
            opFile.setName(openloadFile.name)
            opFile.setSha1(openloadFile.sha1)
            opFile.setSize(java.lang.Long.valueOf(openloadFile.size!!))
            opFile.setUploadAt(Timestamp(java.lang.Long.parseLong(openloadFile.uploadAt!!) * 1000))
            opFile.setOpenloadAcc(this.acc!!.getId())

            var opFileDao = OPFileDao(opFile)
            if (!db.select("SELECT * FROM anigoo_standard.op_file WHERE name = '" + opFile.getName() + "'") && opFileDao.insert()) {
                opFile = opFileDao.getEntity()

                if (opFile.getThumb() == null) {
                    val splash = getSplash(openloadFile.linkextid)
                    if (splash != null) {
                        var image: Image? = Image()
                        image!!.setServer("Url")
                        image!!.setDocId(splash)
                        image!!.setPriority(3L)
                        val imageDao = ImageDao(image)
                        if (imageDao.insert()) {
                            image = imageDao.getEntity()
                            if (image != null && image!!.getId() != null) {
                                opFile.setThumb(image!!.getId())
                                opFileDao = OPFileDao(opFile)
                                opFileDao.insert()
                            }
                        }
                    }
                }
            }
        }
    }

    fun remoteUploadAllDriveFile() {
        var sleep = 1000
        val db = DB.getAnigoo("anigoo_standard")
        while (db.select("SELECT * FROM anigoo_standard.file WHERE doc_id not in (SELECT name FROM anigoo_standard.op_file) and server like 'Google Drive%' AND copy_of is null ORDER By rand() LIMIT 100")) {
            val result = db.getResult()
            for (record in result) {
                val url = GDriveManager.getLinkDownloadv2(record.getValue("doc_id"))
                println(url)
                if (remoteUpload(url)) {
                    sleep = 1000
                } else {
                    if (sleep > 30000) {
                        return
                    }
                    try {
                        Thread.sleep(sleep.toLong())
                        sleep = sleep * 2
                    } catch (ignored: Exception) {
                    }

                }
            }
        }
    }

    fun getFileInfo(fileId: String?): File? {
        fileId ?: return null
        val body =
            conn.post("https://openload.co/filemanager/getfileinfo", hashMapOf("id" to fileId))?.body
        val fileInfoResponse = gson.fromJson<FileInfo_Response>(body, FileInfo_Response::class.java)
        if (fileInfoResponse != null && fileInfoResponse.success == 1) {
            fileInfoResponse.files?.let { files ->
                for (file in files) {
                    if (StringUtils.equals(file.linkextid, fileId)) {
                        file.name = StringUtils.removeEnd(file.name, ".mp4")
                        println(file.name)
                        println(file.link)
                        println(file.linkextid)
                        return file
                    }
                }
            }
        }

        return null
    }

    private fun getSplash(fileId: String?): String? {
        fileId ?: return null
        val body =
            conn.post("https://openload.co/filemanager/getsplash", hashMapOf("id" to "[\"$fileId\"]"))?.body
                ?: return null
        val bodyObj = validBody(body) as JSONObject? ?: return null
        println(body)
        val splashResponse = gson.fromJson(bodyObj.toJSONString(), GetSplash_Response::class.java)
        if (splashResponse.success == 1) {
            splashResponse.url?.first { it.linkextid == fileId }
        }
        return null
    }


    private fun remoteUpload(url: String): Boolean {
        try {
            val body = conn.post("https://openload.co/submitRemotedl", data = hashMapOf("links" to url))?.body
            println(body)
            return "{\"success\":1}" == body
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    companion object {
        internal var gson = Gson()
        internal var db = DB.getAnigoo("anigoo_standard")

        fun getAcc(): OpenloadAcc? {
            //        if (db.select("SELECT * FROM anigoo_standard.openload_acc WHERE cookie is not null ORDER BY id desc limit 0,1")) {
            if (db.select("SELECT * FROM anigoo_standard.openload_acc WHERE cookie is not null ORDER BY rand() limit 1")) {
                val firstResult = db.getFirstResult()
                return Entity.fromData(firstResult, OpenloadAcc::class.java)
            }
            return null
        }

        @JvmStatic
        fun net.scr0pt.main(args: Array<String>) {
            while (true) {
                run()
            }
        }

        fun run() {
            val opManager = opManager
            opManager.remoteUploadAllDriveFile()
            opManager.updateThumbtoDB()
        }

        private val opManager: OPManager
            get() {
                var opAcc = getAcc()
                var opManager = OPManager(opAcc)
                while (opManager.acc!!.getxCsrfToken() == null) {
                    try {
                        Thread.sleep(1000)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                    opAcc = getAcc()
                    opManager = OPManager(opAcc)
                }
                return opManager
            }

        fun validBody(body: String): Any? {
            var body = body
            body = StringUtils.removeStart(body, "\"")
            body = StringUtils.removeEnd(body, "\"")
            val parse = JSONValue.parse(body)
            return validObject(parse)
        }


        fun validObject(o: Any?): Any? {
            return when (o) {
                is JSONObject -> validJSONObject(o)
                is JSONArray -> validJSONArray(o)
                else -> o
            }
        }


        //valid json data
        fun validJSONObject(jsonObject: JSONObject?): JSONObject? {
            val newJSONObject = JSONObject()
            val entries = jsonObject?.entries ?: return null
            for (entry in entries) {
                var (key, value) = entry
                key = StringUtils.removeStart(key, "\\\"")
                key = StringUtils.removeEnd(key, "\\\"")

                when (value) {
                    is String -> {
                        value = StringUtils.replace(value, "\\\\/", "/")
                        value = StringUtils.removeStart(value, "\\\"")
                        value = StringUtils.removeEnd(value, "\\\"")
                        newJSONObject[key] = value
                    }
                    is JSONObject -> {
                        newJSONObject[key] = validJSONObject(value)
                    }
                    is JSONArray -> {
                        value = validJSONArray(value)
                        newJSONObject[key] = value
                    }
                    else -> newJSONObject[key] = value
                }
            }
            return newJSONObject
        }

        //valid json data
        fun validJSONArray(jsonArray: JSONArray?): JSONArray? {
            if (jsonArray == null || jsonArray.isEmpty()) {
                return jsonArray
            }
            val newJSONArray = JSONArray()
            for (o in jsonArray) {
                when (o) {
                    is JSONObject -> newJSONArray.add(validJSONObject(o))
                    is JSONArray -> newJSONArray.add(validJSONArray(o))
                    else -> newJSONArray.add(o)
                }
            }
            return newJSONArray
        }
    }
}
*/
