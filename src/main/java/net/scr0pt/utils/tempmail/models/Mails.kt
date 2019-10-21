package net.scr0pt.utils.tempmail.models



fun ArrayList<Mail>.getMail(
        query: String?,
        compareType: Mail.CompareType = Mail.CompareType.EQUAL_IGNORECASE,
        propertyType: Mail.PropertyType = Mail.PropertyType.CONTENT
): List<Mail> = this.filter {
    compareType.compare(propertyType.getValue(it), query)
}

