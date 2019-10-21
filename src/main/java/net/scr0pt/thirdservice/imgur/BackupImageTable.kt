/*
package thirdservice.imgur

import anigoo_standard.dao.ImageDao
import anigoo_standard.entity.Entity
import anigoo_standard.entity.Image
import anigoo_standard.entity.ImgurAcc
import net.scr0pt.RemoteUploadResponse
import net.scr0pt.RemoteUploadResponseData
import untils.MyNumber
import untils.db.DB
import untils.db.model.Record
import untils.db.model.Records
import untils.net.proxy.GatherProxy
import untils.net.proxy.Proxy

*/
/**
 * Created by Long
 * Date: 2/27/2019
 * Time: 12:35 AM
 *//*

object BackupImageTable {
    @JvmStatic
    fun main1(args: Array<String>) {
        val db = DB.getAnigoo("anigoo_standard")
        var sleep = 1000
        val vn = GatherProxy.getProxysfrom_GatherProxy("vn")
        while (db.select("SELECT * FROm anigoo_standard.image WHERE server = 'Url' AND copy_of is null AND crop_of is null AND id not in (SELECT copy_of FROm anigoo_standard.image where server = 'Imgur' AND copy_of is not null) ORDER BY RAND() LIMIT 100")) {
            val result = db.getResult()
            if (result != null) {
                if (db.select("SELECT * FROM anigoo_standard.imgur_accounts where cookie is not null order by rand() limit 1")) {
                    val imgurAcc = Entity.fromData(db.getFirstResult(), ImgurAcc::class.java)
                    val manager = ImgurManager(imgurAcc)
                    for (record in result!!) {
                        val image = Entity.fromData(record, Image::class.java)
                        val url = image.getDocId()
                        val remoteUploadResponse = manager.remoteUpload(url)
                        if (remoteUploadResponse != null && remoteUploadResponse!!.success!!) {
                            sleep = 1000
                            val imageCopy = Image()
                            val data = remoteUploadResponse!!.data
                            imageCopy.setDocId(data!!.hash)
                            println(data!!.hash)
                            imageCopy.setServer("Imgur")
                            imageCopy.setCopyOf(image.getId())
                            imageCopy.setWidth(data!!.width)
                            imageCopy.setHeight(data!!.height)
                            imageCopy.setDirectLink("https://i.imgur.com/" + data!!.hash + data!!.ext)
                            ImageDao(imageCopy).insert()
                        } else {
                            manager.setProxy(vn.get(MyNumber.ran(0, vn.size - 1)))
                            try {
                                Thread.sleep((sleep *= 2).toLong())
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            }

                        }
                    }
                }
            }
        }
    }
}
*/
