package com.consultantapp.utils

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.text.format.DateUtils
import com.consultantapp.ui.dashboard.home.bookservice.datetime.OnTimeSelected
import java.text.SimpleDateFormat
import java.util.*


object DateUtils {

    val utcFormat = SimpleDateFormat(DateFormat.UTC_FORMAT_NORMAL, Locale.getDefault())

    fun openDatePicker(activity: Activity, listener: OnDateSelected, max: Boolean, min: Boolean) {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(activity,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    var selectedDate = "$dayOfMonth/${monthOfYear.plus(1)}/$year"

                    selectedDate =
                            dateFormatChange(DateFormat.DATE_FORMAT_SLASH_YEAR, DateFormat.DATE_FORMAT_SLASH, selectedDate)
                    listener.onDateSelected(selectedDate)

                }, year, month, day
        )

        if (max)
            dpd.datePicker.maxDate = System.currentTimeMillis() - 36000
        if (min)
            dpd.datePicker.minDate = System.currentTimeMillis() - 36000

        dpd.show()
    }


    fun getTime(context: Context, startTime: String, endTime: String?,
                isStart: Boolean, listener: OnTimeSelected) {
        var compareDate = true
        if (endTime == null)
            compareDate = false
        else if (isStart && endTime.isEmpty()) {
            compareDate = false
        } else if (startTime.isEmpty()) {
            compareDate = false
        }

        val sdf = SimpleDateFormat(DateFormat.TIME_FORMAT, Locale.ENGLISH)

        val endTime = if (endTime != null && endTime.isNotEmpty())
            sdf.parse(endTime)
        else Date()

        val startTime = if (startTime.isNotEmpty())
            sdf.parse(startTime)
        else Date()

        val cal = Calendar.getInstance()
        var selectedTime = ""
        var isError = false

        val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)

            val newTime = sdf.parse(sdf.format(cal.time))
            val time = sdf.format(cal.time)

            /* val different = if (isStart) newTime.time - endTime.time
             else
                 newTime.time - startTime.time*/

            if (isStart) {
                if (!compareDate || newTime.before(endTime)) {
                    selectedTime = time
                } else {
                    isError = true
                }

            } else {
                if (!compareDate || startTime.before(newTime)) {
                    selectedTime = sdf.format(cal.time)
                } else {
                    isError = true
                }
            }

            listener.onTimeSelected(Triple(selectedTime, isStart, isError))
        }

        CustomTimePickerDialog(
                context, timeSetListener,
                cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),
                false
        ).show()
    }

    fun dateFormatFromMillis(format: String, timeInMillis: Long?): String {
        val fmt = SimpleDateFormat(format, Locale.ENGLISH)
        return if (timeInMillis == null || timeInMillis == 0L)
            ""
        else
            fmt.format(timeInMillis)
    }


    fun dateFormatChange(formatFrom: String, formatTo: String, value: String): String {
        val originalFormat = SimpleDateFormat(formatFrom, Locale.ENGLISH)
        val targetFormat = SimpleDateFormat(formatTo, Locale.ENGLISH)
        val date = originalFormat.parse(value)
        return targetFormat.format(date)
    }


    fun getTimeAgo(createdAt: String?): String {
        val agoString: String

        if (createdAt == null) {
            return ""
        }

        utcFormat.timeZone = TimeZone.getTimeZone("Etc/UTC")
        val time = utcFormat.parse(createdAt).time
        val now = System.currentTimeMillis()

        val ago = DateUtils.getRelativeTimeSpanString(
                time, now, DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE
        )


        return ago.toString()
    }

    fun getTimeAgoForMillis(millis: Long): String {

        val now = System.currentTimeMillis()

        return DateUtils.getRelativeTimeSpanString(
                millis, now, DateUtils.SECOND_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE
        ).toString()
    }

    fun getLocalTimeAgo(timeString: Long?, removeAgo: String): String {
        var agoString = ""

        timeString?.let {
            val now = System.currentTimeMillis()

            val ago = DateUtils.getRelativeTimeSpanString(
                    timeString,
                    now,
                    DateUtils.SECOND_IN_MILLIS,
                    DateUtils.FORMAT_SHOW_TIME
            )

            agoString = ago.toString()
        }

        return agoString
    }

    fun dateTimeFormatFromUTC(format: String, createdDate: String?): String {
        return if (createdDate == null || createdDate.isEmpty())
            ""
        else {
            utcFormat.timeZone = TimeZone.getTimeZone("Etc/UTC")

            val fmt = SimpleDateFormat(format, Locale.getDefault())
            fmt.format(utcFormat.parse(createdDate))
        }
    }
}

/*On Date selected listener*/
interface OnDateSelected {
    fun onDateSelected(date: String)
}

fun isYesterday(calendar: Calendar): Boolean {
    val tempCal = Calendar.getInstance()
    tempCal.add(Calendar.DAY_OF_MONTH, -1)
    return calendar.get(Calendar.DAY_OF_MONTH) == tempCal.get(Calendar.DAY_OF_MONTH)
}

object DateFormat {
    const val DATE_FORMAT = "yyyy-MM-dd"
    const val DATE_TIME_FORMAT = "dd MMM yyyy · hh:mm a"
    const val DAY_DATE_FORMAT = "EEE · dd MMM yyyy"
    const val TIME_FORMAT = "hh:mm a"
    const val TIME_FORMAT_24 = "HH:mm"
    const val MON_YEAR_FORMAT = "dd MMM yyyy"
    const val MON_DAY_YEAR = "MMMM dd, yyyy"
    const val MON_DATE_YEAR = "MMM dd, yy"
    const val DATE_MON_YEAR = "dd MMM, yy"
    const val MON_DATE = "MMM dd"
    const val DATE_ONLY = "dd"
    const val MONTH_YEAR = "MMMM yyyy"
    const val DATE_FORMAT_SLASH = "MM/dd/yy"
    const val DATE_FORMAT_SLASH_YEAR = "dd/MM/yyyy"
    const val UTC_FORMAT_NORMAL = "yyyy-MM-dd hh:mm:ss"
    const val UTC_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    const val MONTH_FORMAT = "MMM"
}
