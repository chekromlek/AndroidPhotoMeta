package com.example.photometa.extension

import android.content.res.Resources

fun Int.dip(res: Resources): Int  {
    return (res.displayMetrics.density * this).toInt()
}

fun Int.dp(res: Resources): Int  {
    return (res.displayMetrics.density * this).toInt()
}

fun Float.dip(res: Resources): Int  {
    return (res.displayMetrics.density * this).toInt()
}

fun Float.dp(res: Resources): Int  {
    return (res.displayMetrics.density * this).toInt()
}

fun Int.HumanReadableFileSzie(): String {
    val kb = this/1024
    if (kb < 1024) return "${kb}KB"
    else return "${kb/1024}MB"
}

fun Long.HumanReadableFileSzie(): String {
    val kb = this/1024
    if (kb < 1024) return "${kb}KB"
    else return "${kb/1024}MB"
}