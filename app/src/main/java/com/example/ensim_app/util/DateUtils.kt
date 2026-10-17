package com.example.ensim_app.util

import java.util.Calendar
import java.util.Date
import java.util.TimeZone

private val TZ_PARIS = TimeZone.getTimeZone("Europe/Paris")

fun mondayOfLocal(date: Date): Date {
    val cal = Calendar.getInstance(TZ_PARIS).apply { time = date }
    val daysFromMonday = (cal.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7
    cal.add(Calendar.DAY_OF_MONTH, -daysFromMonday)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.time
}

fun weeksBetween(fromMonday: Date, toMonday: Date): Int {
    val cal = Calendar.getInstance(TZ_PARIS).apply { time = fromMonday }
    val targetMs = toMonday.time
    var weeks = 0
    while (cal.timeInMillis < targetMs) {
        cal.add(Calendar.DAY_OF_MONTH, 7)
        weeks++
    }
    while (cal.timeInMillis > targetMs) {
        cal.add(Calendar.DAY_OF_MONTH, -7)
        weeks--
    }
    return weeks
}

fun startOfDayMs(date: Date): Long {
    val cal = Calendar.getInstance(TZ_PARIS).apply {
        time = date
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}

fun mondayToFridayOfWeek(now: Date, weekOffset: Int): List<Date> {
    val cal = Calendar.getInstance(TZ_PARIS)
    cal.time = now
    val daysFromMonday = (cal.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7
    cal.add(Calendar.DAY_OF_MONTH, -daysFromMonday + weekOffset * 7)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return (0 until 5).map {
        val d = cal.time
        cal.add(Calendar.DAY_OF_MONTH, 1)
        d
    }
}

fun minutesOfDay(date: Date): Int {
    val cal = Calendar.getInstance(TZ_PARIS).apply { time = date }
    return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
}

fun isSameDay(a: Date, b: Date): Boolean {
    val ca = Calendar.getInstance(TZ_PARIS).apply { time = a }
    val cb = Calendar.getInstance(TZ_PARIS).apply { time = b }
    return ca.get(Calendar.YEAR) == cb.get(Calendar.YEAR) &&
        ca.get(Calendar.DAY_OF_YEAR) == cb.get(Calendar.DAY_OF_YEAR)
}
