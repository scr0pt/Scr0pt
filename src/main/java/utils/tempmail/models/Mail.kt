package utils.tempmail.models

import org.jsoup.nodes.Element

class Mail(var from: String?, var to: String?, var subject: String? = null, var content: String? = null) {
    var id: Long? = null
    var contentDocumented: Element? = null
        set(value) {
            value?.let {
                field = it
                content = it.text()
            }
        }

    override fun toString(): String {
        return "id: $id\nfrom: $from\nto: $to\nsubject: $subject\ncontent: $content"
    }

    enum class CompareType {
        PREFIX {
            override fun compare(str1: String?, str2: String?): Boolean {
                if (nullCheck(str1, str2)) {
                    return true
                }
                str2 ?: return false
                return str1?.startsWith(str2) ?: false
            }
        },
        SUFFIX {
            override fun compare(str1: String?, str2: String?): Boolean {
                if (nullCheck(str1, str2)) {
                    return true
                }
                str2 ?: return false
                return str1?.endsWith(str2) ?: false
            }
        },
        EQUAL {
            override fun compare(str1: String?, str2: String?): Boolean {
                if (nullCheck(str1, str2)) {
                    return true
                }
                return str1?.equals(str2) ?: false
            }
        },
        PREFIX_IGNORECASE {
            override fun compare(str1: String?, str2: String?): Boolean =
                PREFIX.compare(str1?.toLowerCase(), str2?.toLowerCase())
        },
        SUFFIX_IGNORECASE {
            override fun compare(str1: String?, str2: String?): Boolean =
                SUFFIX.compare(str1?.toLowerCase(), str2?.toLowerCase())
        },
        EQUAL_IGNORECASE {
            override fun compare(str1: String?, str2: String?): Boolean =
                EQUAL.compare(str1?.toLowerCase(), str2?.toLowerCase())
        };

        abstract fun compare(str1: String?, str2: String?): Boolean
        fun nullCheck(str1: String?, str2: String?): Boolean = (str1 == null && str2 == null)
    }

    enum class PropertyType {
        FROM {
            override fun getPropertyName(): String? {
                return "from"
            }

            override fun getValue(mail: Mail?): String? {
                return mail?.from
            }
        },
        TO {
            override fun getPropertyName(): String? {
                return "to"
            }

            override fun getValue(mail: Mail?): String? {
                return mail?.to
            }
        },
        SUBJECT {
            override fun getPropertyName(): String? {
                return "subject"
            }

            override fun getValue(mail: Mail?): String? {
                return mail?.subject
            }
        },
        CONTENT {
            override fun getPropertyName(): String? {
                return "content"
            }

            override fun getValue(mail: Mail?): String? {
                return mail?.content
            }
        };

        abstract fun getValue(mail: Mail?): String?
        abstract fun getPropertyName(): String?
    }
}




