package ru.netology.nmedia.dto

class Number {
    companion object {
        fun setNumberView(number: Int): String {
            return when (number) {
                in -100..-1 -> "-0"
                in 0..999 -> number.toString()
                in 1000..9999 -> {
                    val firstDigit = number.toString()[0]
                    val secondDigit = number.toString()[1]

                    if (secondDigit == '0') {
                        firstDigit.toString() + "K"
                    } else {
                        firstDigit.toString() + "." + firstDigit + "K"
                    }
                }

                in 10_000..99_000_000 -> {
                    (number / 1000).toString() + "K"
                }

                else -> {
                    val firstDigit = number / 1_000_000
                    val secondDigit = (number / 100_000).toString().last()

                    if (secondDigit == '0') {
                        firstDigit.toString() + "K"
                    } else {
                        firstDigit.toString() + "." + secondDigit + "K"
                    }
                }
            }
        }
    }
}