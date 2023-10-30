package ru.netology.nmedia.dto

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class PublishedDateTime {
    companion object {
        fun getTime(publishedDate: String): String {
            val newStr = publishedDate.replace("T", " ").substring(0, 19)
            val formatterFromString = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val dt = LocalDateTime.parse(newStr, formatterFromString)

            val localDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd MMM в HH:mm")
            val formatterDate = DateTimeFormatter.ofPattern("yy dd MM")
            val formatterTime = DateTimeFormatter.ofPattern("HH:mm")

            val minutesFromLocal = (localDateTime.hour * 60 + localDateTime.minute) - (dt.hour * 60 + dt.minute)
            return when (ChronoUnit.MINUTES.between(dt, localDateTime)) {
                in 0..59 -> minutesFromLocal.toString() + getMinuteWord(minutesFromLocal) + " назад"
                in 60..119 -> "час назад"
                in 120..179 -> "два часа назад"
                in 180..239 -> "три часа назад"
                else -> {
                    if (localDateTime.format(formatterDate) == dt.format(formatterDate)) {
                        "сегодня в " + dt.format(formatterTime)
                    } else dt.format(formatter)
                }
            }
        }


        fun getMinuteWord(minutes: Int): String {
            return when (minutes) {
                1 -> " минуту"
                2, 3, 4 -> " минуты"
                else -> " минут"
            }

        }
    }
}