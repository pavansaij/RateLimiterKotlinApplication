package SamplePackage.Models

import java.lang.IllegalArgumentException

class OpeningHour(openingDay: String, openingType: String, openingHour: Int) {

    var day: Days = Days.getCaseInsensitiveDay(openingDay)

    var type: Type = Type.getCaseInsensitiveType(openingType)

    var value: Int = if (openingHour in 0..86399) openingHour else throw IllegalArgumentException("Invalid hour value passed in input")

}