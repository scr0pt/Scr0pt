package net.scr0pt.utils

import org.apache.commons.lang3.StringUtils

object MyString {
    fun textBetween(
        _word: String?,
        startWord: String,
        endWord: String,
        minlen: Int? = null,
        maxlen: Int? = null
    ): String? {
        var word = _word ?: return null
        val startWordlength = startWord.length
        while (true) {
            var start: Int = word.indexOf(startWord)
            if (start == -1) {
                return null
            } else {
                start += startWordlength
            }
            val end = word.indexOf(endWord, start)
            if (end == -1) {
                return null
            }

            val endsubstart = end - start
            if ((maxlen == null || endsubstart <= maxlen) && (minlen == null || endsubstart >= minlen)) {
                return word.substring(start, end)
            }
            word = word.substring(start)
        }
    }

    fun cleanJsonLink(jsonLink: String?): String? = jsonLink?.replace("\\/", "/")?.replace("&amp;", "&")
    //Lấy số đầu tiên xuất hiện trong dãy string
    fun getFirstNuminString(textField: String): String? {
        var chuaqua = true
        var temp = ""
        for (text in textField) {
            val textStr = text.toString()
            if (StringUtils.isNumeric(textStr)) {
                chuaqua = false
                temp += textStr
            } else if (!chuaqua) break
        }
        return if (StringUtils.isNumeric(temp))
            temp
        else
            null
    }
}