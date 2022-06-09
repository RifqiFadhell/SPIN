package id.qasir.webviewaddon.utils

import java.util.regex.Matcher
import java.util.regex.Pattern

object PhoneNumberUtils {

  fun isValidFormat(phoneNumber : String) : Boolean {

    // example valid format
    // first type = 0 , 62 , +62, ()
    // minimal input 7
    // example 08212651625 +62182613751 atc
    val regex = "(\\()?(\\+62|62|0)(\\d{2,3})?\\)?[ .-]?\\d{2,4}[ .-]?\\d{2,4}[ .-]?\\d{2,4}"

    val pattern: Pattern = Pattern.compile(regex)
    val matcher: Matcher = pattern.matcher(phoneNumber)

    return matcher.matches()
  }


}
