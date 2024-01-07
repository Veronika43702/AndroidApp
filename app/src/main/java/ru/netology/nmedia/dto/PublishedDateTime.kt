package ru.netology.nmedia.dto

import java.text.SimpleDateFormat
import java.time.OffsetDateTime

class PublishedDateTime {
    companion object {
        fun getTime(publishedSeconds: Long): String {
            val timeNowSeconds = OffsetDateTime.now().toEpochSecond()

            val fullDateTime =
                SimpleDateFormat("dd MMM yy в HH:mm").format(publishedSeconds * 1000L)
            val dateTime = SimpleDateFormat("dd MMM в HH:mm").format(publishedSeconds * 1000L)
            val formatYear = SimpleDateFormat("yy")
            val formatDay = SimpleDateFormat("dd")
            val time = SimpleDateFormat("HH:mm").format(publishedSeconds * 1000L)

            val publishedDay = formatDay.format(publishedSeconds * 1000L)
            val publishedYear = formatYear.format(publishedSeconds * 1000L)
            val timeNowDay = formatDay.format(timeNowSeconds * 1000L)
            val timeNowYear = formatYear.format(timeNowSeconds * 1000L)

            val minutesFromNow = ((timeNowSeconds - publishedSeconds) / 60).toInt()

            return when (timeNowDay.toInt() - publishedDay.toInt()) {
                0 -> when (minutesFromNow) {
                    in 0..59 -> minutesFromNow.toString() + getMinuteWord(minutesFromNow)
                    in 60..119 -> "час назад"
                    in 120..179 -> "два часа назад"
                    in 180..239 -> "три часа назад"
                    else -> "сегодня в $time"
                }

                1 -> if (timeNowYear == publishedYear) {
                    "вчера в $time"
                } else fullDateTime

                else -> if (timeNowYear == publishedYear) {
                    dateTime
                } else fullDateTime
            }
        }


        fun getMinuteWord(minutes: Int): String {
            return if (minutes % 100 in 11..19) {
                " минут"
            } else when (minutes % 10) {
                1 -> " минуту"
                2, 3, 4 -> " минуты"
                else -> " минут"
            } + " назад"
        }
    }
}