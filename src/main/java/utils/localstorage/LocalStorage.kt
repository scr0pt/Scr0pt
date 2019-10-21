package utils.localstorage

import org.apache.commons.io.FileUtils
import java.io.File
import java.util.*


class LocalStorage(val fileName: String) {
    val file: File
    val prop: Properties

    init {
        file = File(System.getProperty("user.dir") + File.separator + "LOCALSTORAGE" + File.separator + fileName)
        FileUtils.forceMkdirParent(file)
        if (!file.exists()) {
            file.createNewFile()
        }
        prop = Properties()
    }

    fun getItem(key: String): String? {
        prop.load(file.inputStream())
        return prop.getProperty(key)
    }

    fun setItem(key: String, value: String): Boolean {
        try {
            val storeValue = getItem(key)
            if (value.equals(storeValue)) {
                return true
            }
            prop.setProperty(key, value)
            prop.store(file.outputStream(), null)
            return true
        } catch (e: Exception) {
            return false
        }
    }
}