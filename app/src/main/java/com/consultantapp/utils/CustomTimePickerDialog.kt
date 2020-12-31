package com.consultantapp.utils

import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.widget.NumberPicker
import android.widget.TimePicker
import com.consultantapp.appClientDetails
import java.util.*

class CustomTimePickerDialog(context: Context, private val mTimeSetListener: OnTimeSetListener?,
    hourOfDay: Int, minute: Int, is24HourView: Boolean) :
    TimePickerDialog(
        context, THEME_HOLO_LIGHT, mTimeSetListener, hourOfDay,
        minute / TIME_PICKER_INTERVAL, is24HourView
    ) {
    private var mTimePicker: TimePicker? = null

    var savedHour = 0
    var savedMinute = 0

    init {
        savedHour = hourOfDay
        savedMinute = minute / TIME_PICKER_INTERVAL
    }

    override fun updateTime(hourOfDay: Int, minuteOfHour: Int) {
        mTimePicker?.currentHour = hourOfDay
        mTimePicker?.currentMinute = minuteOfHour / TIME_PICKER_INTERVAL
    }

    override fun onTimeChanged(view: TimePicker?, hourOfDay: Int, minute: Int) {

        if (savedHour != 0) {
            if (savedMinute != minute && savedHour != hourOfDay) {
                mTimePicker?.currentHour = savedHour
            }else{
                savedMinute = minute
                savedHour = hourOfDay
            }
        }else{
            savedHour = hourOfDay
            savedMinute = minute
        }
        super.onTimeChanged(view, savedHour, savedMinute)
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> mTimeSetListener?.onTimeSet(
                mTimePicker, (mTimePicker?.currentHour ?: 0),
                (mTimePicker?.currentMinute ?: 0) * TIME_PICKER_INTERVAL
            )
            DialogInterface.BUTTON_NEGATIVE -> cancel()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        try {
            val classForid = Class.forName("com.android.internal.R\$id")
            val timePickerField = classForid.getField("timePicker")
            mTimePicker = findViewById<View>(timePickerField.getInt(null)) as TimePicker?
            val field = classForid.getField("minute")

            val minuteSpinner = mTimePicker?.findViewById<View>(field.getInt(null)) as NumberPicker
            minuteSpinner.minValue = 0
            minuteSpinner.maxValue = 60 / TIME_PICKER_INTERVAL - 1
            val displayedValues = ArrayList<String>()
            var i = 0
            while (i < 60) {
                displayedValues.add(String.format("%02d", i))
                i += TIME_PICKER_INTERVAL
            }
            minuteSpinner.displayedValues = displayedValues
                .toTypedArray()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    companion object {

        private val TIME_PICKER_INTERVAL = appClientDetails.slot_duration?.toInt() ?: 30
    }
}